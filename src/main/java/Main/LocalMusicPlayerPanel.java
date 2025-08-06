package Main;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

import javazoom.jl.player.Player;

public class LocalMusicPlayerPanel extends JPanel {
    private final Map<File, String> songEmotionMap = new HashMap<>();
    private Player currentPlayer;
    private Thread playbackThread;
    private File currentSongFile;
    private boolean isPlaying = false;

    // UI Components
    private DefaultListModel<String> listModel;
    private JList<String> songList;
    private JLabel nowPlayingLabel;
    private JLabel emotionLabel;
    private JLabel statusLabel;
    private JButton playButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JButton nextButton;
    private JButton prevButton;
    private JSlider progressSlider;
    private Timer progressTimer;

    // User's default music directory
    private File musicDirectory;

    // Current detected emotion and language filter set externally
    private String detectedEmotion = "All";
    private String selectedLanguage = "All";

    public LocalMusicPlayerPanel() {
        musicDirectory = getDefaultMusicDirectory();
        initializeUI();
        loadMusicLibrary();  // Load and classify music files
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Top panel with now playing, emotion, and status
        JPanel topPanel = new JPanel(new BorderLayout());
        nowPlayingLabel = new JLabel("No song selected", SwingConstants.CENTER);
        nowPlayingLabel.setFont(new Font("Arial", Font.BOLD, 16));
        emotionLabel = new JLabel("Emotion: -", SwingConstants.CENTER);
        statusLabel = new JLabel("Loading songs...", SwingConstants.CENTER);
        topPanel.add(nowPlayingLabel, BorderLayout.NORTH);
        topPanel.add(emotionLabel, BorderLayout.CENTER);
        topPanel.add(statusLabel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Center panel: song list
        listModel = new DefaultListModel<>();
        songList = new JList<>(listModel);
        songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        songList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && songList.getSelectedIndex() != -1) {
                updateNowPlaying();
            }
        });
        add(new JScrollPane(songList), BorderLayout.CENTER);

        // Control panel: progress slider + playback buttons
        JPanel controlPanel = new JPanel(new BorderLayout());

        progressSlider = new JSlider(0, 100, 0);
        progressSlider.setPreferredSize(new Dimension(400, 25));
        progressSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentSongFile != null) {
                    seekToPosition(progressSlider.getValue() / 100.0);
                }
            }
        });
        controlPanel.add(progressSlider, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel();
        playButton = new JButton("Play");
        pauseButton = new JButton("Pause");
        stopButton = new JButton("Stop");
        nextButton = new JButton("Next");
        prevButton = new JButton("Previous");

        playButton.addActionListener(e -> playCurrentSong());
        pauseButton.addActionListener(e -> pauseSong());
        stopButton.addActionListener(e -> stopSong());
        nextButton.addActionListener(e -> playNextSong());
        prevButton.addActionListener(e -> playPreviousSong());

        buttonsPanel.add(prevButton);
        buttonsPanel.add(playButton);
        buttonsPanel.add(pauseButton);
        buttonsPanel.add(stopButton);
        buttonsPanel.add(nextButton);
        controlPanel.add(buttonsPanel, BorderLayout.CENTER);

        add(controlPanel, BorderLayout.SOUTH);

        playButton.setEnabled(false);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);

        progressTimer = new Timer(1000, e -> updateProgress());
    }

    private File getDefaultMusicDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        File musicDir;
        if (os.contains("win")) {
            musicDir = new File(System.getProperty("user.home"), "Music");
        } else if (os.contains("mac")) {
            musicDir = new File(System.getProperty("user.home"), "Music");
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            musicDir = new File(System.getProperty("user.home"), "Music");
        } else {
            musicDir = new File(System.getProperty("user.home"));
        }
        if (!musicDir.exists() || !musicDir.isDirectory()) {
            musicDir = new File(System.getProperty("user.home"));
        }
        return musicDir;
    }

    private void loadMusicLibrary() {
        statusLabel.setText("Scanning music directory: " + musicDirectory.getAbsolutePath());
        List<File> songFiles = new ArrayList<>();
        try {
            Files.walk(musicDirectory.toPath())
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String n = path.toString().toLowerCase();
                        return n.endsWith(".mp3") || n.endsWith(".wav");
                    })
                    .forEach(path -> songFiles.add(path.toFile()));
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading music files.");
            return;
        }

        if (songFiles.isEmpty()) {
            statusLabel.setText("No music files found in " + musicDirectory.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "No music files found in:\n" + musicDirectory.getAbsolutePath(),
                    "No Music Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        analyzeSongs(songFiles.toArray(new File[0]));
    }

    private void analyzeSongs(File[] songFiles) {
        songEmotionMap.clear();
        listModel.clear();
        statusLabel.setText("Analyzing " + songFiles.length + " songs...");

        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() {
                int count = 0;
                for (File file : songFiles) {
                    String emotion = EmotionClassifier.classifySong(file);
                    songEmotionMap.put(file, emotion);
                    publish(++count);
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int processed = chunks.get(chunks.size() - 1);
                statusLabel.setText("Analyzed " + processed + "/" + songFiles.length + " songs");
                filterSongs();
            }

            @Override
            protected void done() {
                statusLabel.setText("Analysis complete! " + songFiles.length + " songs found.");
                filterSongs();
            }
        };

        worker.execute();
    }


    /**
     * Filter songs by the externally set detected emotion and selected language.
     * Called after classifying songs or updating detected emotion or language.
     */
    public void filterSongs() {
        listModel.clear();

        final String filterEmotion = detectedEmotion == null ? "All" : detectedEmotion;
        final String filterLanguage = selectedLanguage == null ? "All" : selectedLanguage;

        // Filter songs by emotion only; language filtering can be added if you have metadata
        songEmotionMap.entrySet().stream()
                .filter(entry -> filterEmotion.equalsIgnoreCase("All") || entry.getValue().equalsIgnoreCase(filterEmotion))
                .sorted(Comparator.comparing(entry -> entry.getKey().getName()))
                .forEach(entry -> listModel.addElement(entry.getKey().getName() + " (" + entry.getValue() + ")"));

        if (!listModel.isEmpty()) {
            songList.setSelectedIndex(0);
            playButton.setEnabled(true);
        } else {
            playButton.setEnabled(false);
        }
    }

    /** Called externally by container to update filters */
    public void setDetectedEmotion(String emotion, String language) {
        this.detectedEmotion = (emotion == null || emotion.isEmpty()) ? "All" : emotion;
        this.selectedLanguage = (language == null || language.isEmpty()) ? "All" : language;
        filterSongs();
    }

    private void playCurrentSong() {
        if (songList.getSelectedIndex() == -1 && listModel.size() > 0) {
            songList.setSelectedIndex(0);
        }
        if (songList.getSelectedIndex() != -1) {
            File selectedFile = getSelectedSongFile();
            if (selectedFile != null && !selectedFile.equals(currentSongFile)) {
                stopSong();
                currentSongFile = selectedFile;
            }
            playSong(currentSongFile);
        }
    }

    private void playSong(File file) {
        stopSong();

        playbackThread = new Thread(() -> {
            try (FileInputStream fis = new FileInputStream(file)) {
                currentPlayer = new Player(fis);
                isPlaying = true;
                SwingUtilities.invokeLater(() -> {
                    nowPlayingLabel.setText("Now Playing: " + file.getName());
                    emotionLabel.setText("Emotion: " + songEmotionMap.get(file));
                    playButton.setEnabled(false);
                    pauseButton.setEnabled(true);
                    stopButton.setEnabled(true);
                });
                progressSlider.setValue(0);
                progressTimer.start();

                currentPlayer.play();

                if (currentPlayer.isComplete()) {
                    playNextSong();
                }
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Error playing song: " + file.getName(),
                                "Playback Error", JOptionPane.ERROR_MESSAGE));
            } finally {
                isPlaying = false;
                SwingUtilities.invokeLater(() -> {
                    playButton.setEnabled(true);
                    pauseButton.setEnabled(false);
                    stopButton.setEnabled(false);
                    progressTimer.stop();
                    progressSlider.setValue(0);
                });
            }
        });
        playbackThread.start();
    }

    private void pauseSong() {
        if (isPlaying && currentPlayer != null) {
            currentPlayer.close();
            isPlaying = false;
            progressTimer.stop();
            SwingUtilities.invokeLater(() -> {
                playButton.setEnabled(true);
                pauseButton.setEnabled(false);
            });
        }
    }

    public void stopSong() {
        if (currentPlayer != null) {
            currentPlayer.close();
            currentPlayer = null;
        }
        if (playbackThread != null) {
            playbackThread.interrupt();
            playbackThread = null;
        }
        isPlaying = false;
        progressTimer.stop();
        SwingUtilities.invokeLater(() -> {
            playButton.setEnabled(true);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
            progressSlider.setValue(0);
            nowPlayingLabel.setText("No song selected");
            emotionLabel.setText("Emotion: -");
        });
    }

    private void playNextSong() {
        int nextIdx = songList.getSelectedIndex() + 1;
        if (nextIdx < listModel.size()) {
            songList.setSelectedIndex(nextIdx);
            playCurrentSong();
        }
    }

    private void playPreviousSong() {
        int prevIdx = songList.getSelectedIndex() - 1;
        if (prevIdx >= 0) {
            songList.setSelectedIndex(prevIdx);
            playCurrentSong();
        }
    }

    private void seekToPosition(double ratio) {
        if (currentSongFile != null) {
            // Seeking not supported by JLayer - restart song playback
            stopSong();
            playSong(currentSongFile);
        }
    }

    private void updateProgress() {
        if (currentPlayer != null) {
            int progress = progressSlider.getValue() + 5;
            if (progress > 100) progress = 0;
            progressSlider.setValue(progress);
        }
    }

    private void updateNowPlaying() {
        File file = getSelectedSongFile();
        if (file != null) {
            nowPlayingLabel.setText("Selected: " + file.getName());
            emotionLabel.setText("Emotion: " + songEmotionMap.get(file));
        }
    }

    private File getSelectedSongFile() {
        int idx = songList.getSelectedIndex();
        if (idx < 0) return null;

        String display = listModel.get(idx);
        int e = display.lastIndexOf(" (");
        if (e == -1) return null;

        String fileName = display.substring(0, e);
        return songEmotionMap.keySet().stream()
                .filter(f -> f.getName().equals(fileName))
                .findFirst()
                .orElse(null);
    }
}
