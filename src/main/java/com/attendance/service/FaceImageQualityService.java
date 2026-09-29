package com.attendance.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FaceImageQualityService {
    private static final Logger log = LoggerFactory.getLogger(FaceImageQualityService.class);
    private static final int MAX_BASE64_LENGTH = 5_000_000;

    public void validateOrThrow(String userCode, String imageBase64) {
        try {
            if (imageBase64 == null || imageBase64.isBlank() || imageBase64.length() > MAX_BASE64_LENGTH)
                throw new IllegalArgumentException("Face image is empty or too large.");
            int comma = imageBase64.indexOf(',');
            String encoded = comma >= 0 ? imageBase64.substring(comma + 1) : imageBase64;
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(encoded)));
            if (image == null || image.getWidth() < 96 || image.getHeight() < 96)
                throw new IllegalArgumentException("Face image resolution is too small.");
            double aspect = (double) image.getWidth() / image.getHeight();
            if (aspect < 0.85 || aspect > 1.15)
                throw new IllegalArgumentException("Face crop aspect ratio is invalid.");

            int width = image.getWidth(), height = image.getHeight();
            double[] gray = new double[width * height];
            double brightness = 0;
            for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                double value = 0.299 * (rgb >> 16 & 255) + 0.587 * (rgb >> 8 & 255) + 0.114 * (rgb & 255);
                gray[y * width + x] = value;
                brightness += value;
            }
            brightness /= width * height;
            if (brightness < 40) throw new IllegalArgumentException("Face image is too dark.");
            if (brightness > 230) throw new IllegalArgumentException("Face image is too bright.");

            double sum = 0, squareSum = 0;
            int count = 0;
            for (int y = 1; y < height - 1; y++) for (int x = 1; x < width - 1; x++) {
                int index = y * width + x;
                double laplacian = gray[index - width] + gray[index + width] + gray[index - 1]
                        + gray[index + 1] - 4 * gray[index];
                sum += laplacian;
                squareSum += laplacian * laplacian;
                count++;
            }
            double mean = sum / count;
            double sharpness = squareSum / count - mean * mean;
            log.info("Backend face image quality: userCode={}, width={}, height={}, brightness={}, sharpness={}",
                    userCode, width, height, brightness, sharpness);
            if (sharpness < 35)
                throw new IllegalArgumentException("Face image is blurred. Hold camera steady and try again.");
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to validate face image quality.", ex);
        }
    }
}
