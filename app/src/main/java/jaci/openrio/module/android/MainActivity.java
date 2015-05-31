package jaci.openrio.module.android;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.ImageButton;
import jaci.openrio.module.android.fragments.AvailableDevice;
import jaci.openrio.module.android.net.MulticastDiscover;
import jaci.openrio.module.android.net.PacketManager;

import java.util.HashMap;

public class MainActivity extends ActionBarActivity {

    static WifiManager manager;
    public static WifiManager.MulticastLock lock;
    public static Context appContext;
    public static MainActivity INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        INSTANCE = this;
        try {
            setContentView(R.layout.activity_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle(R.string.app_name);
            setSupportActionBar(toolbar);
            manager = (WifiManager) getSystemService(WIFI_SERVICE);
            lock = manager.createMulticastLock("toast_droid_discover");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        appContext = getApplicationContext();
        MulticastDiscover.listen();
    }

    @Override
    public void onStart() {
        super.onStart();
        ImageButton button = (ImageButton) findViewById(R.id.devices_refresh);
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        v.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_refresh));
                        refresh_devices();
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
    }

    void refresh_devices() {
        ((GridLayout) findViewById(R.id.available_devices)).removeAllViews();
        MulticastDiscover.broadcast();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh_devices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    public void addDiscoveredDevice(final PacketManager.RobotID id, final PacketManager.RobotID oldID) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.setCustomAnimations(R.anim.device_found, R.anim.device_found);
                if (oldID != null)
                    transaction.remove(oldID.getFragment());
                transaction.add(R.id.available_devices, id.getFragment());
                transaction.commit();
            }
        });
    }

}
