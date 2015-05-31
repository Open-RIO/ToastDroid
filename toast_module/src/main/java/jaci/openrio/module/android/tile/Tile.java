package jaci.openrio.module.android.tile;

import jaci.openrio.module.android.net.MainDelegate;
import jaci.openrio.module.android.net.PacketManager;

import java.awt.*;

/**
 * The Base Class for a Tile. Extend this or invoke it yourself and register in the {@link TileRegistry}
 *
 * @author Jaci
 */
public class Tile {

    String title;
    String id;
    String[] subtitles;
    Color color;

    /**
     * Create a new Tile. Does not register the tile.
     * @param id The unique ID for your tile. This should be unique to your program. TOAST_* ids are reserved and should not be used
     * @param title The title for your Tile. This is gathered by {@link #getTitle()}. The title is shown at the top of your tile
     * @param subtitles The subtitles for your card. This is the smaller text below the Title. This is given by {@link #getSubtitles()}
     */
    public Tile(String id, String title, String... subtitles) {
        this.id = id;
        this.title = title;
        this.subtitles = subtitles;
        color = new Color(33,33,33);
    }

    /**
     * Retrieve the Unique ID for the Tile. This is final as the ID should not and will not change
     */
    public final String getID() {
        return id;
    }

    /**
     * Retrieve the Title for the Tile. This is what is displayed on the top of your tile
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get an Array of subtitles. This is the smaller text below the Title. If there are no subtitles, this should return
     * a new String array of size 0, NOT a null.
     */
    public String[] getSubtitles() {
        return subtitles;
    }

    /**
     * Called when the Card is touched on the Device. This should be overridden if you wish to invoke actions based on Touch.
     * Touch is checked every 100ms.
     */
    public void onTouch() { }

    /**
     * Get the color of the card. By default, this is rgb(33, 33, 33). This will only be updated on the device if the color
     * is detected as changed.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Set the color of the card. By default, this is rgb(33, 33, 33)
     */
    public void setColor(Color col) {
        color = col;
    }

    /**
     * Update the card on the device. This will send the entire card's metadata to all android devices connected. If registered
     * on the TileTicker, this is done for you, however, in other cases, you must call this yourself if you wish to change any
     * values.
     */
    public void update() {
        try {
            byte[] encoded = PacketManager.encodeTile(this);
            MainDelegate.broadcast(encoded);
        } catch (Exception e) {}
    }

    /**
     * Called when the Tile is ticked. Override this to change behaviour per-tick and call it as a super method to update
     * the card
     */
    public void tick_update() {
        update();
    }

}
