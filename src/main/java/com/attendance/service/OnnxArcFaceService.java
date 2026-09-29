package com.attendance.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OnnxArcFaceService {
    private static final Logger log = LoggerFactory.getLogger(OnnxArcFaceService.class);
    private final OrtEnvironment environment = OrtEnvironment.getEnvironment();
    private final OrtSession session;

    public OnnxArcFaceService() {
        try (InputStream model = new ClassPathResource("models/w600k_mbf.onnx").getInputStream()) {
            session = environment.createSession(model.readAllBytes(), new OrtSession.SessionOptions());
            log.info("ArcFace ONNX model loaded: model=models/w600k_mbf.onnx, inputs={}, outputs={}",
                    session.getInputNames(), session.getOutputNames());
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load ArcFace ONNX model models/w600k_mbf.onnx", ex);
        }
    }

    public String embeddingFromBase64(String imageBase64) {
        try {
            String encoded = imageBase64.substring(imageBase64.indexOf(',') + 1);
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(encoded)));
            if (source == null) throw new IllegalArgumentException("Invalid face image");
            BufferedImage image = new BufferedImage(112, 112, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.drawImage(source, 0, 0, 112, 112, null);
            graphics.dispose();
            float[] input = new float[1 * 3 * 112 * 112];
            for (int y = 0; y < 112; y++) for (int x = 0; x < 112; x++) {
                int rgb = image.getRGB(x, y);
                int offset = y * 112 + x;
                input[offset] = ((rgb >> 16 & 255) - 127.5f) / 127.5f;
                input[112 * 112 + offset] = ((rgb >> 8 & 255) - 127.5f) / 127.5f;
                input[2 * 112 * 112 + offset] = ((rgb & 255) - 127.5f) / 127.5f;
            }
            try (OnnxTensor tensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(input), new long[]{1, 3, 112, 112});
                 OrtSession.Result result = session.run(java.util.Map.of(session.getInputNames().iterator().next(), tensor))) {
                Object value = result.get(0).getValue();
                float[] vector = value instanceof float[][] matrix ? matrix[0] : (float[]) value;
                if (vector.length != 512) throw new IllegalArgumentException("ArcFace model returned " + vector.length + " dimensions; expected 512");
                double norm = 0;
                for (float item : vector) norm += item * item;
                norm = Math.sqrt(norm);
                if (!Double.isFinite(norm) || norm <= 1e-12) throw new IllegalArgumentException("ArcFace model returned a zero/invalid vector");
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < vector.length; i++) {
                    if (i > 0) json.append(',');
                    json.append(vector[i] / norm);
                }
                String embedding = json.append(']').toString();
                log.debug("ArcFace embedding generated: dimensions={}", vector.length);
                return embedding;
            }
        } catch (IOException | RuntimeException | ai.onnxruntime.OrtException ex) {
            throw new IllegalArgumentException("Unable to create ArcFace embedding", ex);
        }
    }
}
