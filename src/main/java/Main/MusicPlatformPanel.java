package Main;

import javax.swing.*;
import java.awt.*;

public class MusicPlatformPanel extends JPanel {
    private JComboBox<String> platformSelector, languageSelector;
    private JPanel centerPanel;
    private LocalMusicPlayerPanel localMusicPanel;
    private WebMusicPanel webPanel;
    private SpotifyPanel spotifyPanel;
    private static final String[] INDIAN_LANGUAGES = { "All", "Tamil", "Telugu", "Kannada", "Malayalam", "Marathi", "Bengali", "Gujarati", "Punjabi", "Urdu", "Odia", "Assamese", "Hindi" };
    private String detectedEmotion;
    private String selectedLanguage = INDIAN_LANGUAGES[0];
    private GUI_Music_App mainFrame;

    public MusicPlatformPanel(GUI_Music_App mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        platformSelector = new JComboBox<>(new String[] { "Local Songs", "YouTube", "Spotify" });
        languageSelector = new JComboBox<>(INDIAN_LANGUAGES);
        JButton backBtn = new JButton("Back");

        topPanel.add(new JLabel("Platform:"));
        topPanel.add(platformSelector);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(new JLabel("Language:"));
        topPanel.add(languageSelector);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(backBtn);

        add(topPanel, BorderLayout.NORTH);

        centerPanel = new JPanel(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);

        localMusicPanel = new LocalMusicPlayerPanel();
        webPanel = new WebMusicPanel();
        spotifyPanel = new SpotifyPanel();

        backBtn.addActionListener(e -> {
            localMusicPanel.stopSong();
            mainFrame.backToEmotion();
        });

        platformSelector.addActionListener(e -> switchPlatform());
        languageSelector.addActionListener(e -> {
            selectedLanguage = (String) languageSelector.getSelectedItem();
            switchPlatform();
        });
        switchPlatform();
    }

    public void setDetectedEmotion(String emotion) {
        this.detectedEmotion = emotion;
        localMusicPanel.setDetectedEmotion(emotion, selectedLanguage);
        spotifyPanel.setDetectedEmotion(emotion, selectedLanguage);
        webPanel.setDetectedEmotion(emotion, selectedLanguage);
    }

    private void switchPlatform() {
        // ALWAYS stop songs on all platforms before switching
        localMusicPanel.stopSong();
        spotifyPanel.stopSong();
        webPanel.stopSong();

        String selected = (String) platformSelector.getSelectedItem();
        centerPanel.removeAll();

        if ("Local Songs".equals(selected)) {
            localMusicPanel.setDetectedEmotion(detectedEmotion, selectedLanguage);
            centerPanel.add(localMusicPanel, BorderLayout.CENTER);
        } else if ("YouTube".equals(selected)) {
            webPanel.setPlatform("YouTube");
            webPanel.setDetectedEmotion(detectedEmotion, selectedLanguage);
            centerPanel.add(webPanel, BorderLayout.CENTER);
        } else if ("Spotify".equals(selected)) {
            spotifyPanel.setDetectedEmotion(detectedEmotion, selectedLanguage);
            centerPanel.add(spotifyPanel, BorderLayout.CENTER);
        }
        centerPanel.revalidate();
        centerPanel.repaint();
    }


    public LocalMusicPlayerPanel getLocalMusicPanel() {
        return localMusicPanel;
    }

    public SpotifyPanel getSpotifyPanel() {
        return spotifyPanel;
    }

    public WebMusicPanel getWebMusicPanel() {
        return webPanel;
    }
}
