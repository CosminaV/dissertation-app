package ro.ase.ism.dissertation.service.digitalwatermarking;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ro.ase.ism.dissertation.exception.WatermarkingFailedException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class PdfWatermarkEmbedder {

    public ByteArrayInputStream embedWatermark(MultipartFile file, BufferedImage watermarkImage) {
        try {
            InputStream inputStream = file.getInputStream();
            byte[] pdfBytes = inputStream.readAllBytes();

            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                PDImageXObject pdImage = LosslessFactory.createFromImage(document, watermarkImage);

                for (PDPage page : document.getPages()) {
                    try (PDPageContentStream contentStream = new PDPageContentStream(
                            document,
                            page,
                            PDPageContentStream.AppendMode.APPEND,
                            true,
                            true
                    )) {
                        float pageWidth = page.getMediaBox().getWidth();
                        float pageHeight = page.getMediaBox().getHeight();

                        float imageWidth = pdImage.getWidth();
                        float imageHeight = pdImage.getHeight();

                        float x = (pageWidth - imageWidth) / 2;
                        float y = (pageHeight - imageHeight) / 2;

                        contentStream.drawImage(pdImage, x, y, imageWidth, imageHeight);
                    }
                }

                // Step 3: Save to output
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                document.save(outputStream);

                return new ByteArrayInputStream(outputStream.toByteArray());
            }
        } catch (IOException e) {
            throw new WatermarkingFailedException("Failed to embed invisible watermark into PDF");
        }
    }

    public BufferedImage extractWatermarkImageFromPdf(InputStream inputStream) {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            for (PDPage page : document.getPages()) {
                Iterable<COSName> objectNames = page.getResources().getXObjectNames();
                for (COSName name : objectNames) {
                    PDXObject xObject = page.getResources().getXObject(name);
                    if (xObject instanceof PDImageXObject imageXObject) {
                        // Found an image, return it
                        return imageXObject.getImage();
                    }
                }
            }
            throw new RuntimeException("No watermark image found in PDF");
        } catch (IOException e) {
            throw new WatermarkingFailedException("Failed to extract watermark image");
        }
    }

    public ByteArrayInputStream embedVisibleWatermark(MultipartFile file, String watermarkText) {
        try {
            InputStream inputStream = file.getInputStream();
            byte[] pdfBytes = inputStream.readAllBytes();

            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                for (PDPage page : document.getPages()) {
                    try (PDPageContentStream contentStream = new PDPageContentStream(
                            document,
                            page,
                            PDPageContentStream.AppendMode.APPEND,
                            true,
                            true
                    )) {
                        // Set transparency
                        PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
                        graphicsState.setNonStrokingAlphaConstant(0.1f); // 20% opaque
                        contentStream.setGraphicsStateParameters(graphicsState);

                        // Set font and size
                        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                        float fontSize = 20f;

                        contentStream.beginText();
                        contentStream.setFont(font, fontSize);

                        // Positioning
                        float pageWidth = page.getMediaBox().getWidth();
                        float pageHeight = page.getMediaBox().getHeight();
                        contentStream.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(45), pageWidth/2, pageHeight/2));

                        contentStream.showText(watermarkText);
                        contentStream.endText();
                    }
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                document.save(outputStream);
                return new ByteArrayInputStream(outputStream.toByteArray());
            }
        } catch (IOException e) {
            throw new WatermarkingFailedException("Failed to embed visible watermark into PDF");
        }
    }
}
