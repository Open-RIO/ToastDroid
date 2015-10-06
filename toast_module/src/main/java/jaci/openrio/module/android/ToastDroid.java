package jaci.openrio.module.android;

import jaci.openrio.delegate.BoundDelegate;
import jaci.openrio.module.android.net.MainDelegate;
import jaci.openrio.module.android.net.MulticastThread;
import jaci.openrio.module.android.tile.Tile;
import jaci.openrio.module.android.tile.TileRegistry;
import jaci.openrio.module.android.tile.TileTicker;
import jaci.openrio.toast.core.Toast;
import jaci.openrio.toast.core.ToastConfiguration;
import jaci.openrio.toast.core.command.AbstractCommand;
import jaci.openrio.toast.core.command.CommandBus;
import jaci.openrio.toast.core.loader.annotation.Priority;
import jaci.openrio.toast.core.network.SocketManager;
import jaci.openrio.toast.core.thread.Heartbeat;
import jaci.openrio.toast.lib.log.Logger;
import jaci.openrio.toast.lib.module.ModuleConfig;
import jaci.openrio.toast.lib.module.ToastModule;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;

/**
 * The main Toast Module for Droid's activities. This module is welcomed with @Branch and @Tree tags. Initiations should be done anytime after the
 * HIGHEST priority Prestart.
 *
 * @author Jaci
 */
public class ToastDroid extends ToastModule {

    protected static ModuleConfig preferences;
    public static RobotIdentifier currentID;
    public static Logger log;
    protected static Thread multicastThread;

    @Override
    public String getModuleName() {
        return "ToastDroid";
    }

    @Override
    public String getModuleVersion() {
        return "0.1.0";
    }

    @Priority(level = Priority.Level.HIGHEST)
    public void prestart() {
        preferences = new ModuleConfig("toast_droid");
        log = new Logger("ToastDroid", Logger.ATTR_DEFAULT);
        BoundDelegate mainDelegate = SocketManager.register("TOAST_DroidMain");
        mainDelegate.callback(new MainDelegate());
        currentID = RobotIdentifier.createInstance();
        TileRegistry.registerDefaults();
    }

    @Priority(level = Priority.Level.HIGHEST)
    public void start() {
        multicastThread = new MulticastThread();
        multicastThread.start();
        Heartbeat.add(new TileTicker());
    }

    public static String getFriendlyName() {
        return ToastConfiguration.Property.ROBOT_NAME.asString();
    }

    public static int getTeamNumber() {
        return ToastConfiguration.Property.ROBOT_TEAM.asInt();
    }

    public static String getDescription() {
        return ToastConfiguration.Property.ROBOT_DESC.asString();
    }

    public static String getInterfaceHostname() {
        return preferences.getString("protocol.hostname", "");
    }

    public static boolean getPreferIPv4() {
        return preferences.getBoolean("protocol.preferIPv4", true);
    }


}
