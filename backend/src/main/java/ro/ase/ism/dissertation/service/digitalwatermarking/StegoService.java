package ro.ase.ism.dissertation.service.digitalwatermarking;

import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;

@Service
public class StegoService {

    /**
     * Embeds a UTF-8 message into the blue-channel LSBs of the given image.
     * Modifies the image in-place and also returns it.
     *
     * @param image   the cover image
     * @param message the text to hide
     * @return the same BufferedImage with the message embedded
     * @throws IllegalArgumentException if the image is too small for the message
     */

    public BufferedImage embed(BufferedImage image, String message) {
        byte[] msgBytes;
        try {
            msgBytes = message.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // UTF-8 should always be supported
            throw new RuntimeException(e);
        }

        int msgLength = msgBytes.length;                    // number of bytes to store
        int width     = image.getWidth();
        int height    = image.getHeight();
        int capacity  = width * height;                     // one bit per pixel

        // need 32 bits for length + 8*msgLength bits for message
        if (capacity < 32 + msgLength * 8) {
            throw new IllegalArgumentException("Message is too long to embed in this image.");
        }

        // build a bit-array of [length header][message bits]
        int totalBits = 32 + msgLength * 8;
        int[] bits    = new int[totalBits];

        // 1) length header (big-endian 32-bit)
        for (int i = 0; i < 32; i++) {
            bits[i] = (msgLength >> (31 - i)) & 1;
        }
        // 2) message bytes
        for (int i = 0; i < msgLength; i++) {
            for (int b = 0; b < 8; b++) {
                bits[32 + i * 8 + b] = (msgBytes[i] >> (7 - b)) & 1;
            }
        }

        // embed into image blue-LSBs
        int bitIndex = 0;
        outer:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitIndex >= totalBits) {
                    break outer;
                }

                int rgb   = image.getRGB(x, y);
                int alpha = (rgb >>> 24) & 0xFF;
                int red   = (rgb >>> 16) & 0xFF;
                int green = (rgb >>>  8) & 0xFF;
                int blue  =  rgb & 0xFF;

                // set LSB of blue to our next bit
                blue = (blue & 0xFE) | bits[bitIndex++];

                int newRgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, newRgb);
            }
        }

        return image;
    }

    /**
     * Extracts a UTF-8 message previously embedded by embedMessage().
     *
     * @param image the stego-image containing an embedded message
     * @return the hidden text
     * @throws IllegalArgumentException if no valid message header or image too small
     */
    public String extract(BufferedImage image) {
        int width    = image.getWidth();
        int height   = image.getHeight();
        int capacity = width * height;

        if (capacity < 32) {
            throw new IllegalArgumentException("Image too small to contain message header.");
        }

        // 1) read 32-bit length header
        int msgLength = 0;
        for (int i = 0; i < 32; i++) {
            int p   = i;             // pixel index
            int x   = p % width;
            int y   = p / width;
            int blue = image.getRGB(x, y) & 0xFF;
            int bit  = blue & 1;
            msgLength = (msgLength << 1) | bit;
        }

        int totalMsgBits = msgLength * 8;
        if (capacity < 32 + totalMsgBits) {
            throw new IllegalArgumentException("Image does not contain full message.");
        }

        // 2) read message bytes
        byte[] msgBytes = new byte[msgLength];
        for (int i = 0; i < totalMsgBits; i++) {
            int p = 32 + i;
            int x = p % width;
            int y = p / width;
            int blue = image.getRGB(x, y) & 0xFF;
            int bit  = blue & 1;
            // shift into corresponding byte
            msgBytes[i / 8] = (byte) ((msgBytes[i / 8] << 1) | bit);
        }

        try {
            return new String(msgBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

//    public BufferedImage embed(BufferedImage image, String message) {
//        byte[] messageBytes = message.getBytes();
//        int messageLength = messageBytes.length;
//
//        if (messageLength > Integer.MAX_VALUE / 8)
//            throw new IllegalArgumentException("Message too long to embed");
//
//        int width = image.getWidth();
//        int height = image.getHeight();
//        int capacity = width * height * 3; // 3 bits per pixel
//
//        int totalBitsToEmbed = (4 + messageLength) * 8;
//        if (totalBitsToEmbed > capacity) {
//            throw new IllegalArgumentException("Message too large to embed in this image");
//        }
//
//        // First 4 bytes = message length
//        byte[] data = new byte[4 + messageLength];
//        data[0] = (byte) (messageLength >> 24);
//        data[1] = (byte) (messageLength >> 16);
//        data[2] = (byte) (messageLength >> 8);
//        data[3] = (byte) (messageLength);
//        System.arraycopy(messageBytes, 0, data, 4, messageLength);
//
//        int dataBitIndex = 0;
//
//        outer:
//        for (int y = 0; y < height; y++) {
//            for (int x = 0; x < width; x++) {
//                if (dataBitIndex >= data.length * 8) {
//                    break outer;
//                }
//
//                int rgb = image.getRGB(x, y);
//                int r = (rgb >> 16) & 0xFF;
//                int g = (rgb >> 8) & 0xFF;
//                int b = rgb & 0xFF;
//
//                r = setLSB(r, getBit(data, dataBitIndex++));
//                if (dataBitIndex < data.length * 8)
//                    g = setLSB(g, getBit(data, dataBitIndex++));
//                if (dataBitIndex < data.length * 8)
//                    b = setLSB(b, getBit(data, dataBitIndex++));
//
//                int newRGB = (r << 16) | (g << 8) | b;
//                image.setRGB(x, y, (0xFF << 24) | newRGB); // keep alpha
//            }
//        }
//
//        return image;
//    }
//
//    public String extract(BufferedImage image) {
//        int width = image.getWidth();
//        int height = image.getHeight();
//
//        byte[] lengthBytes = new byte[4];
//        int bitIndex = 0;
//
//        // First extract 32 bits = message length
//        for (int i = 0; i < 32; i++) {
//            int x = (bitIndex / 3) % width;
//            int y = (bitIndex / 3) / width;
//            int rgb = image.getRGB(x, y);
//
//            int color = switch (bitIndex % 3) {
//                case 0 -> (rgb >> 16) & 0xFF;
//                case 1 -> (rgb >> 8) & 0xFF;
//                default -> rgb & 0xFF;
//            };
//
//            setBit(lengthBytes, i, getLSB(color));
//            bitIndex++;
//        }
//
//        int messageLength = ((lengthBytes[0] & 0xFF) << 24)
//                | ((lengthBytes[1] & 0xFF) << 16)
//                | ((lengthBytes[2] & 0xFF) << 8)
//                | (lengthBytes[3] & 0xFF);
//
//        byte[] messageBytes = new byte[messageLength];
//        for (int i = 0; i < messageLength * 8; i++) {
//            int x = (bitIndex / 3) % width;
//            int y = (bitIndex / 3) / width;
//            int rgb = image.getRGB(x, y);
//
//            int color = switch (bitIndex % 3) {
//                case 0 -> (rgb >> 16) & 0xFF;
//                case 1 -> (rgb >> 8) & 0xFF;
//                default -> rgb & 0xFF;
//            };
//
//            setBit(messageBytes, i, getLSB(color));
//            bitIndex++;
//        }
//
//        return new String(messageBytes);
//    }

    private int getBit(byte[] data, int bitIndex) {
        int byteIndex = bitIndex / 8;
        int bitPos = 7 - (bitIndex % 8);
        return (data[byteIndex] >> bitPos) & 1;
    }

    private void setBit(byte[] data, int bitIndex, int value) {
        int byteIndex = bitIndex / 8;
        int bitPos = 7 - (bitIndex % 8);
        if (value == 1) {
            data[byteIndex] |= (byte) (1 << bitPos);
        } else {
            data[byteIndex] &= (byte) ~(1 << bitPos);
        }
    }

    private int setLSB(int value, int bit) {
        return (value & 0xFE) | bit;
    }

    private int getLSB(int value) {
        return value & 1;
    }
}
