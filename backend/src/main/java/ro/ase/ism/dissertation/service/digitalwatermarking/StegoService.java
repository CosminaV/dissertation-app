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

        // 1) encode length header (big-endian 32-bit): MSB first
        for (int i = 0; i < 32; i++) {
            bits[i] = (msgLength >> (31 - i)) & 1;
        }
        // 2) for each byte of your UTF-8 message, extract its eight bits from most significant (bit 7) to least (bit 0),
        // and store them in bits[32]…, bits[33]… and so on.
        for (int i = 0; i < msgLength; i++) {
            for (int b = 0; b < 8; b++) {
                bits[32 + i * 8 + b] = (msgBytes[i] >> (7 - b)) & 1;
            }
        }

        // 3) embed bits into the blue-channel LSB of each pixel:  for each bit write it into the least significant bit of the blue channel of one pixel
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

                // clear LSB ofb blue, then set LSB of blue to our next bit
                blue = (blue & 0xFE) | bits[bitIndex++];

                // reconstruct pixel and write back
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

        // at least 32b for length
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

        // 2) read payload bits and reassemble bytes
        byte[] msgBytes = new byte[msgLength];
        for (int i = 0; i < totalMsgBits; i++) {
            int p = 32 + i;
            int x = p % width;
            int y = p / width;
            int blue = image.getRGB(x, y) & 0xFF;
            int bit  = blue & 1;
            // shift into corresponding byte (MSG first)
            msgBytes[i / 8] = (byte) ((msgBytes[i / 8] << 1) | bit);
        }

        // convert back to string
        try {
            return new String(msgBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
