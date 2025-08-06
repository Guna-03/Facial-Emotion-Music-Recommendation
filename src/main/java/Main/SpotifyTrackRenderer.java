package Main;

import javax.swing.*;
import java.awt.*;

public class SpotifyTrackRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof SpotifyTrack) {
            SpotifyTrack track = (SpotifyTrack)value;
            setText(track.name + " - " + track.artist);
        }
        return this;
    }
}
