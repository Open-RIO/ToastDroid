package jaci.openrio.module.android.net;

import android.graphics.Color;
import android.util.JsonReader;
import jaci.openrio.module.android.MainActivity;
import jaci.openrio.module.android.fragments.AvailableDevice;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

public class PacketManager {

    public static HashMap<String, RobotID> robotIDs = new HashMap<String, RobotID>();

    public static void processRobotID(DatagramPacket packet) {
        byte[] data = packet.getData();
        String uid = new String(extract(data, 1, 8));
        int team = data[9] * 1000 + data[10] * 100 + data[11] * 10 + data[12];
        String toast_version = data[13] + "." + data[14] + "." + data[15];
        if (data[16] != -1 && data[17] != -1)
            toast_version += "-" + data[16] + "" + (char)data[17];
        String environment = data[18] == 's' ? "Simulation" : data[18] == 'e' ? "Embedded" : data[18] == 'v' ? "Verification" : "Unknown Environment";
        String name = new String(extract(data, 19, 256 - 19)).trim();

        RobotID id = new RobotID(uid, toast_version, environment, name, packet.getAddress(), team);
        RobotID oldID = robotIDs.get(uid);
        robotIDs.put(uid, id);
        MainActivity.INSTANCE.addDiscoveredDevice(id, oldID);
    }

    public static Tile processTile(DataInputStream stream) {
        try {
            byte[] id_a = new byte[stream.readByte()];
            stream.read(id_a);
            byte[] title_a = new byte[stream.readByte()];
            stream.read(title_a);
            byte subs = stream.readByte();
            String[] subTitles = new String[subs];
            for (int i = 0; i < subs; i++) {
                byte[] data = new byte[stream.readByte()];
                stream.read(data);
                subTitles[i] = new String(data, "UTF-8");
            }
            int color = 0;
            boolean colSet = false;
            if (stream.available() > 0) {
                //Color Enabled
                int r = stream.readByte() + 127;
                int g = stream.readByte() + 127;
                int b = stream.readByte() + 127;
                color = Color.rgb(r, g, b);
                colSet = true;
            }
            String id = new String(id_a);
            String title = new String(title_a, "UTF-8");
            Tile tile = new Tile();
            tile.id = id; tile.title = title; tile.subtitles = subTitles;
            if (colSet)
                tile.color = color;
            return tile;
        } catch (IOException e) {
        }
        return null;
    }

    public static String destroyTile(DataInputStream stream) {
        try {
            byte[] data = new byte[stream.readByte()];
            stream.read(data);
            return new String(data);
        } catch (IOException e) {
        }
        return null;
    }

    public static JSONObject profiler(DataInputStream stream) {
        try {
            byte[] data = new byte[decodeint(stream)];
            stream.read(data);
            String jsonstring = new String(data);
            return new JSONObject(jsonstring);
        } catch (Exception e) {}
        return null;
    }

    static byte[] extract(byte[] from, int index, int len) {
        byte[] data = new byte[len];
        System.arraycopy(from, index, data, 0, len);
        return data;
    }

    static int decodeint(DataInputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    public static class Tile {
        public String id, title;
        public String[] subtitles;
        public int color = -1;
    }

    public static class RobotID {

        String uid, toast, env, name;
        InetAddress client;
        int team;

        AvailableDevice fragment;

        public RobotID(String uid, String toast, String env, String name, InetAddress client, int team) {
            this.uid = uid; this.toast = toast; this.env = env; this.name = name; this.client = client; this.team = team;
            fragment = AvailableDevice.newInstance(uid, Integer.toString(team), toast, env, name);
        }

        public String getUID() {
            return uid;
        }

        public String getToastVersion() {
            return toast;
        }

        public String getEnvironment() {
            return env;
        }

        public String getName() {
            return name;
        }

        public InetAddress getClient() {
            return client;
        }

        public int getTeam() {
            return team;
        }

        public AvailableDevice getFragment() {
            return fragment;
        }

    }

}
