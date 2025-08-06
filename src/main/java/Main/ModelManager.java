package Main;

import ai.onnxruntime.*;
import java.io.File;

public class ModelManager {
    private final OrtEnvironment env;
    private final OrtSession session;
    public ModelManager(String modelPath) throws Exception {
        if (!modelPath.endsWith(".onnx")) throw new IllegalArgumentException("Invalid model path: Expected an ONNX model file.");
        this.env = OrtEnvironment.getEnvironment();
        this.session = env.createSession(new File(modelPath).getAbsolutePath(), new OrtSession.SessionOptions());
    }
    public OrtEnvironment getEnv() { return env; }
    public OrtSession getSession() { return session; }
}
