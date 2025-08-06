package Main;

public class SpotifyTrack {
    public final String name, artist, url;
    public SpotifyTrack(String name, String artist, String url) {
        this.name = name; this.artist = artist; this.url = url;
    }
    public String toString() { return name + " - " + artist; }
}

