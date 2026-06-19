package Main;

import javax.swing.*;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

public class EmotionDetectionPanel extends JPanel {

    public interface DetectionListener {
        void onDetectionCycleComplete(String detectedEmotion);
    }

    private JLabel videoLabel, emotionLabel;
    private JButton nextBtn;

    private volatile boolean cameraActive = false, nextClicked = false;
    private String detectedEmotion = null;

    private Runnable onDetectedCallback;
    private DetectionListener detectionListener;

    private Timer scheduler;

    private static final long DETECTION_DURATION_MS = 5 * 60 * 1000; // 5 minutes
    private static final long LOOP_INTERVAL_MS = 60 * 60 * 1000; // 1 hour

    private static final String OPENCV_DLL = "C:\\Users\\athil\\eclipse-workspace\\Emotion_2\\src\\main\\resources\\opencv_java451.dll";
    private static final String CASCADE_PATH = "C:\\Users\\athil\\eclipse-workspace\\Emotion_2\\src\\main\\resources\\haarcascade_frontalface_alt2.xml";
    private static final String MODEL_PATH = "C:\\Users\\athil\\eclipse-workspace\\Emotion_2\\src\\main\\resources\\final_emotion_detection.onnx";

    public EmotionDetectionPanel(JFrame parent) {
        setLayout(new GridBagLayout());
        JPanel innerPanel = new JPanel(new BorderLayout());

        videoLabel = new JLabel();
        videoLabel.setPreferredSize(new Dimension(640, 480));
        videoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        videoLabel.setVerticalAlignment(SwingConstants.CENTER);
        innerPanel.add(videoLabel, BorderLayout.CENTER);

        emotionLabel = new JLabel("Detecting emotion...", SwingConstants.CENTER);
        emotionLabel.setFont(new Font("Arial", Font.BOLD, 22));
        innerPanel.add(emotionLabel, BorderLayout.SOUTH);

        nextBtn = new JButton("Recommmend Song");
        JPanel btnPanel = new JPanel();
        btnPanel.add(nextBtn);
        innerPanel.add(btnPanel, BorderLayout.NORTH);

        nextBtn.addActionListener(e -> {
            cameraActive = false;
            nextClicked = true;
      });

        add(innerPanel);

        System.load(OPENCV_DLL);

        startAutoDetectionLoop();
    }

    public void setDetectionListener(DetectionListener listener) {
        this.detectionListener = listener;
    }

    /** Register a runnable callback for detection complete, optional */
    public void setOnDetected(Runnable cb) {
        this.onDetectedCallback = cb;
    }

    public String getDetectedEmotion() {
        return detectedEmotion;
    }

    public void startAutoDetectionLoop() {
        if (scheduler != null) {
            scheduler.cancel();
        }
        scheduler = new Timer(true);

        scheduler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runDetectionCycle();
            }
        }, 0, LOOP_INTERVAL_MS);
    }

    public void stopAutoDetectionLoop() {
        if (scheduler != null) {
            scheduler.cancel();
            scheduler = null;
        }
        cameraActive = false;
        nextClicked = true;
    }

    private void runDetectionCycle() {
        cameraActive = true;
        nextClicked = false;
        detectedEmotion = null;

      SwingUtilities.invokeLater(() -> emotionLabel.setText("Detecting emotion..."));

        try {
            FaceDetector faceDetection = new FaceDetector(CASCADE_PATH);
            ModelManager modelManager = new ModelManager(MODEL_PATH);
            EmotionDetector emotionDetector = new EmotionDetector(modelManager);

            VideoCapture camera = faceDetection.getCamera();
            Mat frame = new Mat();

            long startTime = System.currentTimeMillis();

            while (cameraActive && (System.currentTimeMillis() - startTime < DETECTION_DURATION_MS)) {
                if (nextClicked) break;

                if (camera.read(frame)) {
                    Rect[] faces = FaceDetector.detectFaces(frame, faceDetection.faceCascade);
                    String currentEmotion = null;

                    for (Rect face : faces) {
                        Mat faceRegion = frame.submat(face);
                        try {
                            currentEmotion = emotionDetector.predictEmotion(faceRegion);
                        } catch (Exception e) {
                            currentEmotion = "?";
                        }

                        Imgproc.rectangle(frame, face.tl(), face.br(), new Scalar(0, 255, 0), 2);
                        Imgproc.putText(frame, currentEmotion, new Point(face.x, face.y - 10),
                                Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(255, 0, 0), 2);
                        break; // Only first face processed per frame
                    }

                    if (currentEmotion != null && !"?".equals(currentEmotion)) {
                        detectedEmotion = currentEmotion;
                        SwingUtilities.invokeLater(() ->
                            emotionLabel.setText("Detected: " + detectedEmotion + ". Please click Recommend Song.")
                        );
                    } else {
                        SwingUtilities.invokeLater(() ->
                            emotionLabel.setText("Detecting emotion...")
                        );
                    }

                    BufferedImage image = FaceDetector.matToBufferedImage(frame);
                    SwingUtilities.invokeLater(() -> {
                        videoLabel.setIcon(new ImageIcon(image));
                        videoLabel.repaint();
                    });
                }
                Thread.sleep(75);
            }

            cameraActive = false;
            faceDetection.releaseCamera();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Notify listeners that detection cycle finished
        if (detectionListener != null) {
            SwingUtilities.invokeLater(() -> detectionListener.onDetectionCycleComplete(detectedEmotion));
        }
        if (onDetectedCallback != null) {
            SwingUtilities.invokeLater(onDetectedCallback);
        }
    }

    /** Manual restart of detection loop */
    public void restartDetection() {
        stopAutoDetectionLoop();
        startAutoDetectionLoop();
    }
}
