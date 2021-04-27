package com.manager.aws;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

/**
 * Resize the uploaded user profile image.
 * 
 */
@Service
public class FileResizeService {

    public File resizeImage(File file, int imageWidth, int imageHeight) throws IOException {
        BufferedImage originalImage = ImageIO.read(file);
        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
        BufferedImage drawImage = drawImage(originalImage, type, imageWidth, imageHeight);
        String fileName = file.getName();
        String fileFormat = fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length())
                .toLowerCase();
        ImageIO.write(drawImage, fileFormat, file);
        return file;
    }

    private BufferedImage drawImage(BufferedImage originalImage, int type, int imageWidth, int imageHeight) {
        BufferedImage resizedImage = new BufferedImage(imageWidth, imageHeight, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, imageWidth, imageHeight, null);
        g.dispose();
        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return resizedImage;
    }
}
