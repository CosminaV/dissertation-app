package ro.ase.ism.dissertation.service.digitalwatermarking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
@Service
public class ImageService {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 100;

    public BufferedImage generateTransparentImage() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = image.createGraphics();
        Color semiTransparentWhite = new Color(255, 255, 255, 30); // ~12% transparency
        graphics.setColor(semiTransparentWhite);
        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        graphics.dispose();

        log.info("Generated transparent image of size {}x{}", WIDTH, HEIGHT);
        return image;
    }
}
