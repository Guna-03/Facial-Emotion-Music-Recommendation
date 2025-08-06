package Main;

import javax.swing.*;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import java.awt.*;

public class WebMusicPanel extends JPanel {
    private JFXPanel webPanel;
    private String platform = "YouTube";
    private String detectedEmotion = null;
    private String selectedLanguage = "All";

    public WebMusicPanel() {
        setLayout(new BorderLayout());
        webPanel = new JFXPanel();
        add(webPanel, BorderLayout.CENTER);
    }

    public void setPlatform(String platform) { this.platform = platform; }
    public void setDetectedEmotion(String emotion, String lang) {
        this.detectedEmotion = emotion;
        this.selectedLanguage = lang;
        loadWebContent();
    }

    private void loadWebContent() {
        StringBuilder q = new StringBuilder();

        // Build query with emotion and language for better recommendations
        if (detectedEmotion != null) q.append("best ").append(detectedEmotion).append(" mood ");
        if (selectedLanguage != null && !"All".equals(selectedLanguage)) q.append(selectedLanguage).append(" ");
        q.append("songs");

        String query = q.toString().trim();
        String url = "";

        if ("YouTube".equals(platform)) {
            // Use YouTube Music directly
            url = "https://music.youtube.com/search?q=" + encodeURIComponent(query);
        } else if ("Spotify".equals(platform)) {
            url = "https://open.spotify.com/search/" + encodeURIComponent(query);
        }

        final String finalUrl = url;
        Platform.runLater(() -> {
            WebView view = new WebView();
            view.getEngine().load(finalUrl);
            webPanel.setScene(new Scene(view, 900, 440));
        });
    }

    private String encodeURIComponent(String s) {
        try { return java.net.URLEncoder.encode(s, "UTF-8").replace("+", "%20"); }
        catch(Exception e){ return s;}
    }
    
    public void stopSong() {
    	Platform.runLater(() -> {
            WebView view = new WebView();
            view.getEngine().loadContent(""); // Blank page
            webPanel.setScene(new Scene(view, 900, 440));
        });
    }
}
