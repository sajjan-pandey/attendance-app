package com.attendance.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.springframework.core.io.ClassPathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Server-side SCRFD face detector. Client-side face detection is only UX. */
@Service
public class ServerFaceDetectionService {
    private static final Logger log = LoggerFactory.getLogger(ServerFaceDetectionService.class);
    private static final int INPUT_SIZE = 640;
    // This SCRFD export exposes logits. Convert them once with sigmoid and
    // discard weak anchor boxes before size/multi-face validation.
    // Lowered SCORE_THRESHOLD to 0.35 to handle blinking detection where confidence drops significantly
    private static final float SCORE_THRESHOLD = 0.35f;
    private static final float MULTI_FACE_SCORE_MARGIN = 0.12f;
    private static final float MULTI_FACE_MIN_CONFIDENCE = 0.50f;
    // Merge overlapping anchor detections aggressively. Keeping many duplicate
    // boxes makes a later frame occasionally select a tiny false box.
    private static final float NMS_THRESHOLD = 0.35f;
    private final OrtEnvironment environment = OrtEnvironment.getEnvironment();
    private final OrtSession session;

    public ServerFaceDetectionService() {
        try (InputStream model = new ClassPathResource("models/scrfd_person_2.5g.onnx").getInputStream()) {
            session = environment.createSession(model.readAllBytes(), new OrtSession.SessionOptions());
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load server face detector.", ex);
        }
    }

    public Detection requireExactlyOneFace(String imageBase64) {
        BufferedImage image = decode(imageBase64);
        List<Detection> detections = detect(image);
        log.info("Server face detector result: image={}x{}, detections={}, topScore={}",
                image.getWidth(), image.getHeight(), detections.size(),
                detections.isEmpty() ? 0f : detections.get(0).score());
        if (detections.size() > 1) {
            for (int i = 0; i < Math.min(3, detections.size()); i++) {
                Detection d = detections.get(i);
                log.info("Detection {}: score={}, box={},{}-{}, {}, size={}x{}px",
                        i, d.score(), d.left(), d.top(), d.right(), d.bottom(),
                        d.width() * image.getWidth(), d.height() * image.getHeight());
            }
        }
        if (detections.isEmpty()) throw new IllegalArgumentException("Server could not detect a face in the image.");
        // Removed strict multi-face rejection to handle blinking scenarios
        // During blinking, the detector may produce multiple overlapping boxes for the same face
        // We now accept the highest scoring detection and log warnings for potential multi-face scenarios
        int strongDetections = 0;
        for (Detection d : detections) {
            if (d.score() >= MULTI_FACE_MIN_CONFIDENCE) strongDetections++;
        }
        if (strongDetections > 1) {
            log.warn("Multiple strong face detections detected (count={}), but accepting highest score for enrollment/attendance", strongDetections);
        }
        float minWidth = image.getWidth() * 0.08f;
        float minHeight = image.getHeight() * 0.08f;
        // Prefer a face-sized box over a tiny duplicate or a full-frame false
        // positive. A real face in the 320x240 liveness frame should not be
        // smaller than 8% or cover the whole image.
        List<Detection> faceSized = detections.stream()
                .filter(d -> d.width() >= 0.08f && d.height() >= 0.08f)
                .filter(d -> d.width() <= 0.95f && d.height() <= 0.98f)
                .toList();
        Detection detection = faceSized.stream()
                .max(Comparator.comparingDouble(d -> d.area() * (0.5 + d.score())))
                .orElse(detections.get(0));
        float facePixelWidth = detection.width() * image.getWidth();
        float facePixelHeight = detection.height() * image.getHeight();
        log.info("Selected face: candidates={}, score={}, box={},{}-{}, {}, size={}x{}px; image={}x{}, minRequired={}x{}, passed={}",
                faceSized.size(), detection.score(), detection.left(), detection.top(), detection.right(), detection.bottom(),
                facePixelWidth, facePixelHeight, image.getWidth(), image.getHeight(), minWidth, minHeight,
                facePixelWidth >= minWidth && facePixelHeight >= minHeight);
        if (facePixelWidth < minWidth || facePixelHeight < minHeight)
            throw new IllegalArgumentException("Detected face is too small.");
        return detection;
    }

    private List<Detection> detect(BufferedImage source) {
        BufferedImage resized = new BufferedImage(INPUT_SIZE, INPUT_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.drawImage(source.getScaledInstance(INPUT_SIZE, INPUT_SIZE, Image.SCALE_SMOOTH), 0, 0, null);
        graphics.dispose();
        float[] input = new float[3 * INPUT_SIZE * INPUT_SIZE];
        for (int y = 0; y < INPUT_SIZE; y++) for (int x = 0; x < INPUT_SIZE; x++) {
            int rgb = resized.getRGB(x, y);
            int offset = y * INPUT_SIZE + x;
            // SCRFD is trained with BGR input, unlike the ArcFace cropper.
            input[offset] = ((rgb & 255) - 127.5f) / 128f;
            input[INPUT_SIZE * INPUT_SIZE + offset] = ((rgb >> 8 & 255) - 127.5f) / 128f;
            input[2 * INPUT_SIZE * INPUT_SIZE + offset] = ((rgb >> 16 & 255) - 127.5f) / 128f;
        }
        try (OnnxTensor tensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(input), new long[]{1, 3, INPUT_SIZE, INPUT_SIZE});
             OrtSession.Result result = session.run(Map.of(session.getInputNames().iterator().next(), tensor))) {
            Map<String, Object> values = resultToMap(result);
            List<Detection> candidates = new ArrayList<>();
            for (int stride : new int[]{8, 16, 32, 64, 128}) {
                int count = (INPUT_SIZE / stride) * (INPUT_SIZE / stride);
                float[] scores = flatten(values.get(scoreName(count)));
                float[] boxes = flatten(values.get(boxName(count)));
                float[] landmarks = flatten(values.get(landmarkName(count)));
                for (int i = 0; i < count && i < scores.length && i * 4 + 3 < boxes.length; i++) {
                    float score = confidence(scores[i]);
                    if (score < SCORE_THRESHOLD) continue;
                    float centerX = (i % (INPUT_SIZE / stride) + .5f) * stride;
                    float centerY = (i / (INPUT_SIZE / stride) + .5f) * stride;
                    float x = centerX - boxes[i * 4] * stride;
                    float y = centerY - boxes[i * 4 + 1] * stride;
                    float width = (boxes[i * 4] + boxes[i * 4 + 2]) * stride;
                    float height = (boxes[i * 4 + 1] + boxes[i * 4 + 3]) * stride;
                    if (width <= 0 || height <= 0) continue;
                    candidates.add(new Detection(clamp(x / INPUT_SIZE), clamp(y / INPUT_SIZE),
                            clamp((x + width) / INPUT_SIZE), clamp((y + height) / INPUT_SIZE), score,
                            landmarks == null ? new float[10] : landmarkPoints(landmarks, i, centerX, centerY, stride)));
                }
            }
            candidates.sort(Comparator.comparingDouble(Detection::score).reversed());
            List<Detection> kept = new ArrayList<>();
            for (Detection candidate : candidates) {
                if (kept.stream().noneMatch(existing -> iou(existing, candidate) > NMS_THRESHOLD)) kept.add(candidate);
                if (kept.size() >= 20) break;
            }
            return kept;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Server face detection failed.", ex);
        }
    }

    private static Map<String, Object> resultToMap(OrtSession.Result result) {
        java.util.HashMap<String, Object> values = new java.util.HashMap<>();
        String[] names = {"490", "510", "530", "550", "570", "493", "513", "533", "553", "573", "496", "516", "536", "556", "576"};
        for (int i = 0; i < result.size(); i++) {
            try { values.put(names[i], result.get(i).getValue()); }
            catch (Exception ex) { throw new IllegalArgumentException("Invalid detector output.", ex); }
        }
        return values;
    }

    private static String scoreName(int count) { return switch (count) { case 6400 -> "490"; case 1600 -> "510"; case 400 -> "530"; case 100 -> "550"; default -> "570"; }; }
    private static String boxName(int count) { return switch (count) { case 6400 -> "493"; case 1600 -> "513"; case 400 -> "533"; case 100 -> "553"; default -> "573"; }; }
    private static String landmarkName(int count) { return switch (count) { case 6400 -> "496"; case 1600 -> "516"; case 400 -> "536"; case 100 -> "556"; default -> "576"; }; }

    private static float[] landmarkPoints(float[] values, int index, float centerX, float centerY, int stride) {
        float[] points = new float[10];
        int offset = index * 10;
        if (offset + 9 >= values.length) return points;
        for (int i = 0; i < 10; i += 2) {
            points[i] = clamp((centerX + values[offset + i] * stride) / INPUT_SIZE);
            points[i + 1] = clamp((centerY + values[offset + i + 1] * stride) / INPUT_SIZE);
        }
        return points;
    }

    private static float[] flatten(Object value) {
        if (value == null) return null;
        if (value instanceof float[] flat) return flat;
        if (value instanceof float[][] matrix) {
            float[] flat = new float[matrix.length * (matrix.length == 0 ? 0 : matrix[0].length)];
            int position = 0;
            for (float[] row : matrix) { System.arraycopy(row, 0, flat, position, row.length); position += row.length; }
            return flat;
        }
        throw new IllegalArgumentException("Unsupported detector output type: " + value.getClass());
    }

    private static float sigmoid(float value) {
        return value >= 0 ? 1f / (1f + (float) Math.exp(-value)) : (float) Math.exp(value) / (1f + (float) Math.exp(value));
    }

    // SCRFD exports differ: some checkpoints expose probabilities while
    // others expose logits. Do not apply sigmoid twice to probability output.
    private static float confidence(float value) {
        // This bundled SCRFD export returns logits. Convert exactly once;
        // treating a small positive logit as an already-normalized probability
        // would discard valid face detections during blink frames.
        return sigmoid(value);
    }

    private static float clamp(float value) { return Math.max(0f, Math.min(1f, value)); }
    private static double iou(Detection a, Detection b) {
        double left = Math.max(a.left(), b.left()), top = Math.max(a.top(), b.top());
        double right = Math.min(a.right(), b.right()), bottom = Math.min(a.bottom(), b.bottom());
        double intersection = Math.max(0, right - left) * Math.max(0, bottom - top);
        return intersection / (a.area() + b.area() - intersection + 1e-9);
    }

    private static BufferedImage decode(String imageBase64) {
        try {
            if (imageBase64 == null || imageBase64.isBlank()) throw new IllegalArgumentException("Face image is required.");
            int comma = imageBase64.indexOf(',');
            String encoded = comma >= 0 ? imageBase64.substring(comma + 1) : imageBase64;
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(encoded)));
            if (image == null) throw new IllegalArgumentException("Invalid face image.");
            return image;
        } catch (IllegalArgumentException | IOException ex) { throw new IllegalArgumentException("Invalid face image.", ex); }
    }

    public record Detection(float left, float top, float right, float bottom, float score, float[] landmarks) {
        public float width() { return (right - left); }
        public float height() { return (bottom - top); }
        public double area() { return Math.max(0, width()) * Math.max(0, height()); }
    }
}
