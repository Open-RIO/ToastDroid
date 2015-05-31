package jaci.openrio.module.android;

import jaci.openrio.delegate.BoundDelegate;
import jaci.openrio.module.android.net.MainDelegate;
import jaci.openrio.module.android.net.MulticastThread;
import jaci.openrio.module.android.tile.Tile;
import jaci.openrio.module.android.tile.TileRegistry;
import jaci.openrio.module.android.tile.TileTicker;
import jaci.openrio.toast.core.Toast;
import jaci.openrio.toast.core.command.AbstractCommand;
import jaci.openrio.toast.core.command.CommandBus;
import jaci.openrio.toast.core.loader.annotation.Priority;
import jaci.openrio.toast.core.loader.groovy.GroovyPreferences;
import jaci.openrio.toast.core.network.SocketManager;
import jaci.openrio.toast.core.thread.Heartbeat;
import jaci.openrio.toast.lib.log.Logger;
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

    protected static GroovyPreferences preferences;
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
        preferences = new GroovyPreferences("toast_droid");
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
        return preferences.getString("id.name", "My Robot", "The 'Friendly Name' for your RoboRIO. This is what is displayed on the Android Device when your device is discovered");
    }

    public static int getTeamNumber() {
        return preferences.getInt("id.team", 1234, "Your Team Number. This is shown on the Android Device to show that the robot does indeed belong to you");
    }

    public static String getDescription() {
        return preferences.getString("id.desc", "My Toasted RoboRIO", "A description to present to any clients that wish to connect to your RoboRIO. Use this to talk about your team or just as an identifier if you have multiple RoboRIOs");
    }

    public static String getInterfaceHostname() {
        return preferences.getString("protocol.hostname", "", "The hostname (IP Address) of the environment. Change this to your Local IP address if you are on a device with multiple network interfaces. This setting is often turned on for Virtual Box users or people with multiple network cards");
    }

    public static boolean getPreferIPv4() {
        return preferences.getBoolean("protocol.preferIPv4", true, "Prefers the IPv4 stack for devices with IPv6 compatibility. For almost all cases this should be true, as the Android app is tuned for IPv4. If you want to break everything, you can set it to false. ");
    }


}
