package jaci.openrio.module.android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import jaci.openrio.delegate.DelegateClient;
import jaci.openrio.module.android.fragments.TileFragment;
import jaci.openrio.module.android.net.PacketManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class ConnectedActivity extends ActionBarActivity implements TileFragment.TouchListener {

    String uid;
    PacketManager.RobotID id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        if (b != null && b.containsKey("uid")) {
            uid = b.getString("uid");
            id = PacketManager.robotIDs.get(uid);
        }

        try {
            setContentView(R.layout.activity_connected);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle(Integer.toString(id.getTeam()));
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    Thread delegateThread;
    DelegateClient client;
    Socket socket;
    DataOutputStream outStream;
    boolean run;

    @Override
    public void onResume() {
        super.onResume();
    }

    LinkedHashMap<String, TileFragment> tiles = new LinkedHashMap<String, TileFragment>();

    public void delegate() {
        delegateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    run = true;
                    setBarColor(Color.rgb(33, 33, 33));
                    client = new DelegateClient(id.getClient().getHostAddress(), 5805, "TOAST_DroidMain");
                    client.connect();
                    socket = client.getSocket();
                    outStream = new DataOutputStream(socket.getOutputStream());
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    while (run) {
                        byte packet_code = dis.readByte();
                        switch (packet_code) {
                            case 0x02:
                                PacketManager.Tile tile = PacketManager.processTile(dis);
                                updateTile(tile);
                                break;
                            case 0x03:
                                String id = PacketManager.destroyTile(dis);
                                break;
                        }
                    }
                } catch (Exception e) {
                    run = false;
                    setBarColor(Color.RED);
                    e.printStackTrace();
                }
            }
        });
        delegateThread.start();
    }

    void setBarColor(final int color) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ActionBar bar = getSupportActionBar();
                    if (bar != null) {
                        bar.setBackgroundDrawable(new ColorDrawable(color));
                        bar.setDisplayShowTitleEnabled(false);
                        bar.setDisplayShowTitleEnabled(true);
                    }
                }
            });
        } catch (Exception e) {     //View not init
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) { }
        tiles.clear();
    }

    @Override
    public void onStart() {
        super.onStart();
        tiles.clear();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((GridLayout) findViewById(R.id.tile_container)).removeAllViews();
            }
        });
        delegate();
    }

    public void updateTile(final PacketManager.Tile tile) {
        final String id = tile.id;
        if (tiles.containsKey(id)) {
            final TileFragment fragment = tiles.get(id);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fragment.update(tile.title, tile.subtitles, tile.color);
                }
            });
        } else {
            final TileFragment fragment = TileFragment.newInstance(id, tile.title, tile.subtitles, tile.color);
            tiles.put(id, fragment);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.device_found, R.anim.device_found);
                    transaction.add(R.id.tile_container, fragment);
                    transaction.commit();
                }
            });
        }
    }

    public void removeTile(final String id) {
        if (id != null && tiles.containsKey(id)) {
            runOnUiThread(new Runnable() {
                TileFragment tile = tiles.get(id);

                @Override
                public void run() {
                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.device_found, R.anim.device_found);
                    transaction.remove(tile);
                    transaction.commit();
                }
            });
            tiles.remove(id);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_connected, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                finish();
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_out);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_out);
    }

    @Override
    public void onTileTouched(TileFragment fragment) {
        if (run && outStream != null) {
            try {
                outStream.writeByte(0x10);
                outStream.writeByte(fragment.getTileID().length());
                outStream.write(fragment.getTileID().getBytes());
                outStream.flush();
            } catch (IOException e) {
            }
        }
    }
}
