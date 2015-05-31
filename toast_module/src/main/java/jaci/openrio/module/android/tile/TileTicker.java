package jaci.openrio.module.android.tile;

import jaci.openrio.module.android.net.MainDelegate;
import jaci.openrio.toast.core.thread.HeartbeatListener;

import java.util.LinkedList;

/**
 * Similar to the TileRegistry, but will have the added bonus of Ticking each card every 2 heartbeats (200ms)
 * Also checks for Touch messages every 100ms
 *
 * @author Jaci
 */
public class TileTicker implements HeartbeatListener {

    static LinkedList<Tile> tiles = new LinkedList<>();

    /**
     * Register your tile to tick. This also registers it on the TileRegistry.
     */
    public static void register(Tile tile) {
        tiles.add(tile);
        TileRegistry.register(tile);
    }

    int heartbeat;

    @Override
    public void onHeartbeat(int skipped) {
        if (MainDelegate.clients() > 0) {
            heartbeat += skipped;
            heartbeat++;

            if (heartbeat >= 2) {
                heartbeat = 0;
                for (Tile tile : tiles)
                    tile.tick_update();
            }
            MainDelegate.listenTick();
        }
    }
}
