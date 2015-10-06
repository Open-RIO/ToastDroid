package jaci.openrio.module.android.tile;

import jaci.openrio.module.android.net.MainDelegate;
import jaci.openrio.module.android.net.PacketManager;
import jaci.openrio.module.android.tile.stock.TilePDP;
import jaci.openrio.toast.core.command.AbstractCommand;
import jaci.openrio.toast.core.command.CommandBus;
import jaci.openrio.toast.core.loader.module.ModuleContainer;
import jaci.openrio.toast.core.loader.module.ModuleManager;

import java.io.IOException;
import java.util.*;

import static jaci.openrio.module.android.ToastDroid.*;
import static jaci.openrio.toast.core.Environment.*;

/**
 * The Tile registrar. This handles all the Tiles to be used with ToastDroid and their appropriate actions. Register/Remove
 * your tiles here.
 *
 * @author Jaci
 */
public class TileRegistry {

    private static LinkedHashMap<String, Tile> tiles = new LinkedHashMap<>();

    /**
     * Register a Tile with the Registrar. This will add the Tile to any existing clients as well as
     * add it to any new ones. Although you can call this whenever, it's prefered to do it in Robot
     * Prestart/Start
     */
    public static void register(Tile tile) {
        tiles.put(tile.getID(), tile);
        try {
            tile.update();
            MainDelegate.broadcast(PacketManager.encodeTile(tile));
        } catch (IOException e) {
        }
    }

    /**
     * Alias for {@link #register(Tile)}
     */
    public static void add(Tile tile) {
        register(tile);
    }

    /**
     * Remove a tile from the Registrar. This removes the Tile on any currently connected devices as well as
     * not show it on any new ones.
     */
    public static void destroy(Tile tile) {
        tiles.remove(tile.getID());
    }

    /**
     * Alias for {@link #destroy(Tile)}
     */
    public static void remove(Tile tile) {
        destroy(tile);
    }

    /**
     * Processes the onTouch method in Tiles. This is done internally, don't call this yourself.
     */
    public static void process_touch(String id) {
        if (tiles.containsKey(id)) {
            getTileByID(id).onTouch();
        }
    }

    /**
     * Internal Registration done in PreInit, do not call.
     */
    public static void registerDefaults() {
        register(new Tile("TOAST_ident", "About", String.format("Name: %s, Team: %s, Toast: %s, %s", currentID.getName(), currentID.getTeam(), currentID.toastVersion(), currentID.getDescription()).split(",\\s?")));
        register(new Tile("TOAST_env", "Environment", String.format("Type: %s, %sOS: %s %s (%s), Java: %s (%s)", getEnvironmentalType(), isCompetition() ? "FMS Attached, " : "", getOS_Name(), getOS_Version(), getOS_Architecture(), getJava_version(), getJava_vendor()).split(",\\s?")));
        register(new Tile("TOAST_mod", "Modules") {
            public String[] getSubtitles() {
                List<ModuleContainer> containers = ModuleManager.getContainers();
                String[] s = new String[containers.size()];
                for (int i = 0; i < s.length; i++)
                    s[i] = containers.get(i).getDetails();
                return s;
            }
        });
       TileTicker.register(new TilePDP());
    }

    /**
     * Get a tile with the given ID.
     */
    public static Tile getTileByID(String id) {
        return tiles.get(id);
    }

    public static Collection<Tile> getAllTiles() {
        return tiles.values();
    }

}
