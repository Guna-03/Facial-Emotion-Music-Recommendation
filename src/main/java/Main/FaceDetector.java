package Main;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import java.awt.image.*;
import java.awt.image.DataBufferByte;

public class FaceDetector {
    public final CascadeClassifier faceCascade;
    private final VideoCapture camera;

    public FaceDetector(String cascadePath) {
        this.faceCascade = new CascadeClassifier(cascadePath);
        if (faceCascade.empty()) throw new IllegalStateException("Failed to load Haar Cascade file.");
        this.camera = new VideoCapture(0);
        if (!camera.isOpened()) throw new IllegalStateException("Failed to open the camera.");
    }
    public static BufferedImage matToBufferedImage(Mat mat) {
        int type = (mat.channels() > 1) ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage image = new BufferedImage(mat.width(), mat.height(), type);
        mat.get(0, 0, ((DataBufferByte) image.getRaster().getDataBuffer()).getData());
        return image;
    }
    public static Rect[] detectFaces(Mat frame, CascadeClassifier faceCascade) {
        Mat gray = new Mat();
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(gray, gray);
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(gray, faces);
        return faces.toArray();
    }
    public VideoCapture getCamera() { return this.camera; }
    public void releaseCamera() { this.camera.release(); }
}
