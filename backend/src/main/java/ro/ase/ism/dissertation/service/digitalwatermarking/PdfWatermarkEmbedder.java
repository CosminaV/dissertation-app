package ro.ase.ism.dissertation.service.digitalwatermarking;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class PdfWatermarkEmbedder {

    public ByteArrayInputStream embedWatermark(InputStream inputStream, BufferedImage watermarkImage) {
        try {
            // Step 1: Convert InputStream to byte[]
            byte[] pdfBytes = inputStream.readAllBytes();

            // Step 2: Load document from bytes
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
            throw new RuntimeException("Failed to embed watermark into PDF", e);
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
            throw new RuntimeException("Failed to extract watermark image", e);
        }
    }
}
