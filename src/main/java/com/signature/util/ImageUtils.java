package com.signature.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

import org.springframework.web.multipart.MultipartFile;

public class ImageUtils {

    public static byte[] removeBackground(MultipartFile file) throws IOException {
        BufferedImage src = ImageIO.read(file.getInputStream());
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
    
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int rgb = src.getRGB(x, y);
                Color c = new Color(rgb);
    
                // Détection des pixels presque blancs (tolérance)
                if (c.getRed() > 240 && c.getGreen() > 240 && c.getBlue() > 240) {
                    // Pixel transparent (alpha = 0)
                    dest.setRGB(x, y, 0x00000000);
                } else {
                    // Pixel opaque (alpha = 255)
                    dest.setRGB(x, y, (0xFF << 24) | (rgb & 0x00FFFFFF));
                }
            }
        }
    
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(dest, "PNG", baos); // PNG supporte la transparence
        return baos.toByteArray();
    }
}
