package jaci.openrio.module.android.net;

import com.grack.nanojson.JsonWriter;
import jaci.openrio.module.android.RobotIdentifier;
import jaci.openrio.module.android.tile.Tile;
import jaci.openrio.toast.lib.Version;
import jaci.openrio.toast.lib.profiler.Profiler;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles packets going to and from the RoboRIO, encoding them properly.
 *
 * @author Jaci
 */
public class PacketManager {

    static Pattern versionPattern = Pattern.compile("(\\d*).(\\d*).(\\d*)(-(\\d*)([a-z]))?");

    public static byte[] encodeRobotID(RobotIdentifier id) {
        byte[] data = new byte[256];
        data[0] = 0x01;
        encode(id.getUID().getBytes(), data, 1);
        data[9] = (byte) (id.getTeam() / 1000 % 10);
        data[10] = (byte) (id.getTeam() / 100 % 10);
        data[11] = (byte) (id.getTeam() / 10 % 10);
        data[12] = (byte) (id.getTeam() % 10);
        String version = id.toastVersion();
        Matcher matcher = versionPattern.matcher(version);
        matcher.matches();

        data[13] = Byte.parseByte(matcher.group(1));
        data[14] = Byte.parseByte(matcher.group(2));
        data[15] = Byte.parseByte(matcher.group(3));

        if (matcher.group(6) != null) {
            data[16] = Byte.parseByte(matcher.group(5));
            data[17] = (byte) matcher.group(6).charAt(0);
        } else {
            data[16] = -1;
            data[17] = -1;
        }

        switch (id.environmentType()) {
            case "Simulation":
                data[18] = 's';
                break;
            case "Verification":
                data[18] = 'v';
                break;
            case "Embedded":
                data[18] = 'e';
                break;
            default:
                data[18] = 'u';
                break;
        }

        encode(id.getName().getBytes(), data, 19);

        return data;
    }

    public static byte[] encodeTile(Tile tile) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        String id = tile.getID();
        String title = tile.getTitle();
        String[] subs = tile.getSubtitles();
        stream.write(0x02);
        stream.write(id.length());
        stream.write(id.getBytes());
        stream.write(title.getBytes("UTF-8").length);
        stream.write(title.getBytes("UTF-8"));
        stream.write(subs.length);
        for (String sub : subs) {
            stream.write(sub.getBytes("UTF-8").length);
            stream.write(sub.getBytes("UTF-8"));
        }
        Color color = tile.getColor();
        stream.write(color.getRed() - 127);
        stream.write(color.getGreen() - 127);
        stream.write(color.getBlue() - 127);
        return stream.toByteArray();
    }

    public static byte[] encodeProfiler() {
        byte[] b = new byte[2048];
        b[0] = 0x04;
        byte[] json = JsonWriter.indent("\t").string().value(Profiler.INSTANCE.toJSON()).done().getBytes();
        encode(encodeint(json.length), b, 1);
        encode(json, b, 5);

        return b;
    }

    public static byte[] encodeDestroy(Tile tile) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(0x03);
        stream.write(tile.getID().length());
        stream.write(tile.getID().getBytes());
        return stream.toByteArray();
    }

    public static String decodeTouch(DataInputStream dis) throws IOException {
        byte[] data = new byte[dis.readByte()];
        dis.read(data);
        return new String(data);
    }

    static byte[] encodeint(int v) {
        byte[] data = new byte[4];
        data[0] = (byte) ((v >>> 24) & 0xFF);
        data[1] = (byte) ((v >>> 16) & 0xFF);
        data[2] = (byte) ((v >>>  8) & 0xFF);
        data[3] = (byte) ((v >>>  0) & 0xFF);
        return data;
    }

    static void encode(byte[] src, byte[] dest, int index) {
        System.arraycopy(src, 0, dest, index, src.length);
    }

}
