package Main;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import ai.onnxruntime.*;
import java.nio.FloatBuffer;
import java.util.Map;

public class EmotionDetector {
    private final ModelManager modelManager;
    private static final Map<Integer, String> EMOTIONS = Map.of(
            0, "Angry", 1, "Disgust", 2, "Fear", 3, "Happy", 4, "Neutral", 5, "Sad", 6, "Surprise"
    );
    public EmotionDetector(ModelManager modelManager) {
        this.modelManager = modelManager;
    }
    public String predictEmotion(Mat face) throws Exception {
        Imgproc.resize(face, face, new org.opencv.core.Size(48, 48));
        face.convertTo(face, org.opencv.core.CvType.CV_32F, 1.0 / 255.0);
        FloatBuffer buffer = FloatBuffer.allocate(48 * 48);
        for (int i = 0; i < 48; i++)
            for (int j = 0; j < 48; j++)
                buffer.put((float) face.get(i, j)[0]);
        buffer.rewind();
        OnnxTensor inputTensor = OnnxTensor.createTensor(modelManager.getEnv(), buffer, new long[]{1, 48, 48, 1});
        try (OrtSession.Result result = modelManager.getSession().run(Map.of("input", inputTensor))) {
            float[][] output = (float[][]) result.get(0).getValue();
            int emotionIndex = getMaxIndex(output[0]);
            return EMOTIONS.getOrDefault(emotionIndex, "Unknown");
        }
    }
    private int getMaxIndex(float[] array) {
        int maxIdx = 0;
        for (int i = 1; i < array.length; i++)
            if (array[i] > array[maxIdx]) maxIdx = i;
        return maxIdx;
    }
}
