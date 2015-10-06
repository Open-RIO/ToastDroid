package jaci.openrio.module.android;

public class Formatting {

    public static enum Colors {
        NORMAL          (0,0, "#ffffff"),
        BLACK           (30,0, "#000000"),
        RED             (31,0, "#ff0000"),
        GREEN           (32,0, "#00ff00"),
        BROWN           (33,0, "#ffff00"),
        BLUE            (34,0, "#0000ff"),
        MAGENTA         (35,0, "#ff00ff"),
        CYAN            (36,0, "#00ffff"),
        GRAY            (37,0, "#808080"),

        BG_BLACK        (40,0, "#000000"),
        BG_RED          (41,0, "#ff0000"),
        BG_GREEN        (42,0, "#00ff00"),
        BG_BROWN        (43,0, "#ffff00"),
        BG_BLUE         (44,0, "#0000ff"),
        BG_MAGENTA      (45,0, "#ff00ff"),
        BG_CYAN         (46,0, "#00ffff"),
        BG_GRAY         (47,0, "#808080"),

        BOLD            (1, 22, null),
        REVERSE         (7, 27, null),
        ;

        int code, multi;
        String hex;

        Colors(int code, int multiplier, String hex) {
            this.code = code; this.multi = multiplier;
            this.hex = hex;
        }

        public static Colors get(String name) {
            for (Colors col : values()) {
                if (col.name().toLowerCase().equals(name.toLowerCase()))
                    return col;
            }
            return Colors.GRAY;
        }

        public static String match(int code, int multi) {
            for (Colors col : values()) {
                if (col.code == code && col.multi == multi)
                    return col.hex;
            }
            return "#ffffff";
        }
    }

}
