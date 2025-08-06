package Main;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class EmotionClassifier {
    // Emotion profiles with 7 basic emotions
    private static final Map<String, double[]> EMOTION_PROFILES = Map.of(
        "Angry",    new double[]{0.9, 0.8, 0.2, 0.1, 0.9, 0.1, 0.3, 0.8, 0.7, 0.6},
        "Disgust",   new double[]{0.7, 0.6, 0.5, 0.3, 0.4, 0.5, 0.6, 0.4, 0.3, 0.5},
        "Fear",     new double[]{0.3, 0.7, 0.8, 0.6, 0.2, 0.7, 0.8, 0.5, 0.4, 0.3},
        "Happy",    new double[]{0.8, 0.4, 0.3, 0.7, 0.6, 0.2, 0.1, 0.3, 0.9, 0.8},
        "Neutral",  new double[]{0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5},
        "Sad",      new double[]{0.2, 0.3, 0.9, 0.8, 0.1, 0.8, 0.7, 0.6, 0.2, 0.3},
        "Surprise", new double[]{0.6, 0.9, 0.4, 0.8, 0.7, 0.3, 0.5, 0.7, 0.6, 0.9}
    );

    // Feature indices explanation
    private static final int TEMPO = 0;
    private static final int SPECTRAL_CENTROID = 1;
    private static final int ENERGY = 2;
    private static final int DANCEABILITY = 3;
    private static final int LOUDNESS = 4;
    private static final int VALENCE = 5;
    private static final int SPEECHINESS = 6;
    private static final int MFCC1 = 7;
    private static final int MFCC2 = 8;
    private static final int CHROMA = 9;

    public static String classifySong(File songFile) {
        double[] features = extractAudioFeatures(songFile);
        
        String closestEmotion = "Neutral"; // Default
        double minDistance = Double.MAX_VALUE;
        
        for (Map.Entry<String, double[]> entry : EMOTION_PROFILES.entrySet()) {
            double distance = cosineDistance(features, entry.getValue());
            if (distance < minDistance) {
                minDistance = distance;
                closestEmotion = entry.getKey();
            }
        }
        
        return closestEmotion;
    }
    
    private static double[] extractAudioFeatures(File songFile) {
        // In a real implementation, you would use a library like Essentia or Librosa
        // Here's a mock implementation that simulates feature extraction
        
        // These values would normally come from audio analysis
        return new double[]{
            getMockFeature(120, 200, TEMPO),             // Tempo (BPM)
            getMockFeature(0.1, 1.0, SPECTRAL_CENTROID), // Spectral centroid
            getMockFeature(0.1, 1.0, ENERGY),           // Energy/RMS
            getMockFeature(0.1, 1.0, DANCEABILITY),      // Danceability
            getMockFeature(0.1, 1.0, LOUDNESS),         // Loudness
            getMockFeature(0.1, 1.0, VALENCE),          // Valence (positivity)
            getMockFeature(0.1, 1.0, SPEECHINESS),      // Speechiness
            getMockFeature(-50, 50, MFCC1),             // MFCC 1st coefficient
            getMockFeature(-50, 50, MFCC2),             // MFCC 2nd coefficient
            getMockFeature(0.1, 1.0, CHROMA)            // Chroma feature
        };
    }
    
    private static double getMockFeature(double min, double max, int featureIndex) {
        // Normalize to 0-1 range based on expected min/max values
        double range = max - min;
        double normalized = (Math.random() * range + min - min) / range;
        
        // Add some emotion-specific bias to make the mock data somewhat meaningful
        switch(featureIndex) {
            case TEMPO:
                return 0.2 + (normalized * 0.8); // Tempo usually 0.2-1.0
            case SPECTRAL_CENTROID:
                return 0.3 + (normalized * 0.5); // Typically 0.3-0.8
            default:
                return normalized;
        }
    }
    
    private static double cosineDistance(double[] v1, double[] v2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            norm1 += Math.pow(v1[i], 2);
            norm2 += Math.pow(v2[i], 2);
        }
        
        return 1 - (dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2)));
    }
    
   

    // Helper method to get emotion by index
    public static String getEmotionById(int index) {
        String[] emotions = {"Angry", "Disgust", "Fear", "Happy", "Neutral", "Sad", "Surprise"};
        return emotions[index];
    }
}
