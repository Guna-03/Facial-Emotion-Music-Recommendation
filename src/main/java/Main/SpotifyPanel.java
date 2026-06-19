package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.json.*;
import java.util.List;
import java.util.ArrayList;


public class SpotifyPanel extends JPanel {
    private static final String SPOTIFY_CLIENT_ID = "1197372c35384e4aa09704173f2bc03a";
    private static final String SPOTIFY_CLIENT_SECRET = "520a394cfef54bcc8a15045b53fca152";
    private JList<SpotifyTrack> spotifySongList;
    private DefaultListModel<SpotifyTrack> spotifyModel = new DefaultListModel<>();
    private String detectedEmotion;
    private String selectedLanguage;

    public SpotifyPanel() {
        setLayout(new BorderLayout());
        spotifySongList = new JList<>(spotifyModel);
        spotifySongList.setCellRenderer(new SpotifyTrackRenderer());
        JScrollPane scrollPane = new JScrollPane(spotifySongList);
        add(new JLabel("Spotify Search Results:"), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        spotifySongList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    SpotifyTrack track = spotifySongList.getSelectedValue();
                    if (track != null) {
                        try {
                            Desktop.getDesktop().browse(new URI(track.url));
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(SpotifyPanel.this, "Cannot open browser.");
                        }
                    }
                }
            }
        });
    }

    public void setDetectedEmotion(String emotion, String language) {
        this.detectedEmotion = emotion;
        this.selectedLanguage = language;
        updateSpotifyResults();
    }

    private void updateSpotifyResults() {
        new Thread(() -> {
            try {
                String accessToken = getSpotifyAccessToken();
                String query = getSpotifyQuery();
                List<SpotifyTrack> tracks = searchSpotifyTracks(accessToken, query);
                SwingUtilities.invokeLater(() -> {
                    spotifyModel.clear();
                    for (SpotifyTrack t : tracks) spotifyModel.addElement(t);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(SpotifyPanel.this, "Spotify Search Error: " + ex.getMessage()));
            }
        }).start();
    }

    private String getSpotifyAccessToken() throws Exception {
        String auth = SPOTIFY_CLIENT_ID + ":" + SPOTIFY_CLIENT_SECRET;
        String encoded = Base64.getEncoder().encodeToString(auth.getBytes());

        URL url = new URL("https://accounts.spotify.com/api/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Basic " + encoded);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        String params = "grant_type=client_credentials";
        OutputStream os = conn.getOutputStream();
        os.write(params.getBytes());
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            InputStream errorStream = conn.getErrorStream();
            if (errorStream != null) {
                String error = new String(errorStream.readAllBytes());
                System.out.println("Spotify Error: " + error);
            }
            throw new IOException("Spotify Auth Failed (HTTP " + responseCode + ")");
        }

        InputStream is = conn.getInputStream();
        JsonReader reader = Json.createReader(is);
        JsonObject obj = reader.readObject();
        reader.close();
        is.close();
        return obj.getString("access_token");
    }

    private String getSpotifyQuery() {
        StringBuilder q = new StringBuilder();
        if (detectedEmotion != null) q.append(detectedEmotion).append(" ");
        if (selectedLanguage != null && !"All".equals(selectedLanguage)) q.append(selectedLanguage).append(" ");
        q.append("songs");
        return q.toString().trim();
    }

    private List<SpotifyTrack> searchSpotifyTracks(String accessToken, String query) throws Exception {
        String api = "https://open.spotify.com/search" + URLEncoder.encode(query, "UTF-8") + "&type=track&limit=10";
        URL url = new URL(api);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        if (responseCode != 200)
            throw new IOException("Spotify Search Failed (HTTP " + responseCode + ")");

        InputStream is = conn.getInputStream();
        JsonReader reader = Json.createReader(is);
        JsonObject obj = reader.readObject();
        reader.close();
        is.close();

        List<SpotifyTrack> tracks = new ArrayList<>();
        JsonArray items = obj.getJsonObject("tracks").getJsonArray("items");
        for (JsonValue v : items) {
            JsonObject t = v.asJsonObject();
            String name = t.getString("name");
            String urlStr = t.getJsonObject("external_urls").getString("spotify");
            String artist = t.getJsonArray("artists").getJsonObject(0).getString("name");
            tracks.add(new SpotifyTrack(name, artist, urlStr));
        }
        return tracks;
        
        
    }
   

	public void stopSong() {
		 // You can stop any playback logic or just a placeholder print
		System.out.println("Stopping Spotify playback (implement logic).");
	}
}

