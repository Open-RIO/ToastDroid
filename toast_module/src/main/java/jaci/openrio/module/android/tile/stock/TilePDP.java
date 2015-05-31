package jaci.openrio.module.android.tile.stock;

import edu.wpi.first.wpilibj.ControllerPower;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.hal.PowerJNI;
import jaci.openrio.module.android.tile.Tile;
import jaci.openrio.toast.core.Toast;
import jaci.openrio.toast.lib.math.MathHelper;

import java.awt.*;

/**
 * A Tile for the Power Distribution Panel. Shows PDP data, tap to expand.
 * Turns red if the Robot is browned out.
 *
 * @author Jaci
 */
public class TilePDP extends Tile {

    PowerDistributionPanel panel;

    int update;
    boolean expanded;

    public TilePDP() {
        super("TOAST_pdp", "Power");
    }

    @Override
    public void tick_update() {
        update++;
        if (update >= 2) {
            if (!ControllerPower.getEnabled6V() || !ControllerPower.getEnabled3V3() || !ControllerPower.getEnabled5V())
                setColor(new Color(100, 33, 33));
            else
                setColor(new Color(33, 33, 33));
            update();
        }
    }

    @Override
    public String[] getSubtitles() {
        panel = new PowerDistributionPanel();
        if (expanded) {
            String[] str = new String[16];
            for (int i = 0; i < str.length; i++) {
                str[i] = "PWM " + i + ": " + panel.getCurrent(i) + "A";
            }
            return str;
        } else {
            String[] str = new String[3];
            str[0] = "Battery: " + MathHelper.round(panel.getVoltage(), 2) + "V";
            str[1] = "RoboRIO: " + MathHelper.round(ControllerPower.getInputVoltage(), 2) + "V";
            str[2] = "Temperature: " + MathHelper.round(panel.getTemperature(), 2) + (char) 0x00B0 + "C";
            return str;
        }
    }

    @Override
    public void onTouch() {
        expanded = !expanded;
        update();
    }

}
