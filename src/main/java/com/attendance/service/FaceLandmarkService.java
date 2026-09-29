package com.attendance.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Base64;
import java.util.Map;
import javax.imageio.ImageIO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/** 106-point landmark inference used by the server-side liveness state machine. */
@Service
public class FaceLandmarkService {
    private static final int INPUT_SIZE = 192;
    private final OrtEnvironment environment = OrtEnvironment.getEnvironment();
    private final OrtSession session;

    public FaceLandmarkService() {
        try (InputStream model = new ClassPathResource("models/2d106det.onnx").getInputStream()) {
            session = environment.createSession(model.readAllBytes(), new OrtSession.SessionOptions());
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load face landmark model.", ex);
        }
    }

    public float[][] detect(String imageBase64) {
        BufferedImage source = decode(imageBase64);
        BufferedImage resized = new BufferedImage(INPUT_SIZE, INPUT_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.drawImage(source.getScaledInstance(INPUT_SIZE, INPUT_SIZE, Image.SCALE_SMOOTH), 0, 0, null);
        graphics.dispose();
        float[] input = new float[3 * INPUT_SIZE * INPUT_SIZE];
        for (int y = 0; y < INPUT_SIZE; y++) for (int x = 0; x < INPUT_SIZE; x++) {
            int rgb = resized.getRGB(x, y), offset = y * INPUT_SIZE + x;
            input[offset] = ((rgb >> 16 & 255) - 127.5f) / 128f;
            input[INPUT_SIZE * INPUT_SIZE + offset] = ((rgb >> 8 & 255) - 127.5f) / 128f;
            input[2 * INPUT_SIZE * INPUT_SIZE + offset] = ((rgb & 255) - 127.5f) / 128f;
        }
        try (OnnxTensor tensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(input), new long[]{1, 3, INPUT_SIZE, INPUT_SIZE});
             OrtSession.Result result = session.run(Map.of(session.getInputNames().iterator().next(), tensor))) {
            Object value = result.get(0).getValue();
            float[] raw = value instanceof float[][] matrix ? matrix[0] : (float[]) value;
            if (raw.length < 212) throw new IllegalArgumentException("Landmark model returned an invalid shape.");
            float[][] points = new float[106][2];
            for (int i = 0; i < 106; i++) {
                points[i][0] = clamp(raw[i * 2] / INPUT_SIZE);
                points[i][1] = clamp(raw[i * 2 + 1] / INPUT_SIZE);
            }
            return points;
        } catch (Exception ex) { throw new IllegalArgumentException("Server face landmark inference failed.", ex); }
    }

    // The 106-point model follows the common InsightFace 106 layout.
    public double eyeAspectRatio(float[][] points) {
        // Use the more-closed eye as the blink signal. Averaging both eyes can
        // hide a short blink when one landmark set is noisy or partially
        // occluded by glasses.
        return Math.min(eyeRatio(points, new int[]{35, 36, 37, 38, 39, 40}),
                eyeRatio(points, new int[]{89, 90, 91, 92, 93, 94}));
    }

    private static double eyeRatio(float[][] points, int[] indexes) {
        float[] a = points[indexes[1]], b = points[indexes[2]], c = points[indexes[4]], d = points[indexes[5]];
        float[] left = points[indexes[0]], right = points[indexes[3]];
        return (distance(a, c) + distance(b, d)) / (2d * Math.max(1e-6, distance(left, right)));
    }

    private static double distance(float[] a, float[] b) { return Math.hypot(a[0] - b[0], a[1] - b[1]); }
    private static float clamp(float value) { return Math.max(0f, Math.min(1f, value)); }
    private static BufferedImage decode(String base64) {
        try {
            int comma = base64.indexOf(',');
            String encoded = comma >= 0 ? base64.substring(comma + 1) : base64;
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(encoded)));
            if (image == null) throw new IllegalArgumentException("Invalid frame image.");
            return image;
        } catch (Exception ex) { throw new IllegalArgumentException("Invalid frame image.", ex); }
    }
}
