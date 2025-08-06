package Main;

import javax.swing.*;
import java.awt.*;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

// Inherit from JFrame and integrate core panels
public class GUI_Music_App extends JFrame {
    protected CardLayout cards;
    protected JPanel mainPanel;
    protected EmotionDetectionPanel emotionPanel;
    protected MusicPlatformPanel musicPanel;
    

    public GUI_Music_App() {
        super("MoodSync");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        cards = new CardLayout();
        mainPanel = new JPanel(cards);

        emotionPanel = new EmotionDetectionPanel(this);
        musicPanel = new MusicPlatformPanel(this);

        mainPanel.add(emotionPanel, "emotion");
        mainPanel.add(musicPanel, "music");

        add(mainPanel);

        emotionPanel.setOnDetected(() -> {
            // When emotion detected, move to music panel
            musicPanel.setDetectedEmotion(emotionPanel.getDetectedEmotion());
            cards.show(mainPanel, "music");
        });

        cards.show(mainPanel, "emotion");
    }

    public void backToEmotion() {
        cards.show(mainPanel, "emotion");
        emotionPanel.restartDetection();
    }

    public static void main(String[] args) {
        Platform.setImplicitExit(false);
        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // For JavaFX Bootstrap
            GUI_Music_App app = new GUI_Music_App();
            app.setVisible(true);
        });
    }
}
