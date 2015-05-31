package jaci.openrio.module.android;

import jaci.openrio.toast.core.Environment;
import jaci.openrio.toast.core.Toast;
import jaci.openrio.toast.lib.Version;

import java.util.Random;

public class RobotIdentifier {

    static String sys = getSysString();

    protected static RobotIdentifier createInstance() {
        return new RobotIdentifier(sys, ToastDroid.getFriendlyName(), ToastDroid.getTeamNumber(), ToastDroid.getDescription());
    }

    static String getSysString() {
        String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUV";
        Random rng = new Random();
        char[] text = new char[8];
        for (int i = 0; i < 8; i++) {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }

    String name, desc, uid;
    int team;

    private RobotIdentifier(String uid, String name, int team, String description) {
        this.name = name;
        this.team = team;
        this.desc = description;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return desc;
    }

    public int getTeam() {
        return team;
    }

    public String getUID() {
        return uid;
    }

    public String toastVersion() {
        return Version.version().get();
    }

    public Version toastVersionR() {
        return Version.version();
    }

    public String environmentType() {
        return Environment.getEnvironmentalType();
    }


}
