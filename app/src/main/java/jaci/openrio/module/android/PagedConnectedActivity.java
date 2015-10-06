package jaci.openrio.module.android;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import com.melnykov.fab.FloatingActionButton;
import jaci.openrio.delegate.DelegateClient;
import jaci.openrio.module.android.fragments.PagerFragment;
import jaci.openrio.module.android.fragments.ProfilerPageFragment;
import jaci.openrio.module.android.fragments.TileFragment;
import jaci.openrio.module.android.fragments.TilePageFragment;
import jaci.openrio.module.android.net.PacketManager;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;

public class PagedConnectedActivity extends ActionBarActivity implements TileFragment.TouchListener {

    public String uid;
    public PacketManager.RobotID id;
    public SectionPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        if (b != null && b.containsKey("uid")) {
            uid = b.getString("uid");
            id = PacketManager.robotIDs.get(uid);
        }

        setContentView(R.layout.activity_paged_connected);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(Integer.toString(id.getTeam()));
        setSupportActionBar(toolbar);

        TabLayout tabber = (TabLayout) findViewById(R.id.tabber);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);

        adapter = new SectionPagerAdapter(getSupportFragmentManager(), this);
        pager.setAdapter(adapter);
        tabber.setupWithViewPager(pager);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.fab);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoggerActivity.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_out);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_paged_connected, menu);
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

    public class SectionPagerAdapter extends FragmentPagerAdapter {

        public PagerFragment[] fragments;

        PagedConnectedActivity act;

        public SectionPagerAdapter(FragmentManager fm, PagedConnectedActivity act) {
            super(fm);
            this.act = act;

            fragments = new PagerFragment[] {
                    PagerFragment.newInstance("Tiles", TilePageFragment.class, act),
                    PagerFragment.newInstance("Profiler", ProfilerPageFragment.class, act)
            };
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragments[position].tab_name;
        }
    }

    //Net
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
                            case 0x04:
                                final JSONObject obj = PacketManager.profiler(dis);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            ((ProfilerPageFragment)adapter.fragments[1]).chartUpdate(obj);
                                        } catch (Exception e) {}
                                    }
                                });
                                break;
                        }
                    }
                } catch (Exception e) {
                    run = false;
                    setBarColor(Color.RED);
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
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((GridLayout)adapter.fragments[0].getView().findViewById(R.id.tile_container)).removeAllViews();
                }
            });
        } catch (Exception e) {}
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
            fragment.attachListener(this);
            tiles.put(id, fragment);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    FragmentManager manager = adapter.fragments[0].getFragmentManager();     //TODO Fix Orientation
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
                    FragmentManager manager = adapter.fragments[0].getFragmentManager();     //Frag
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

    public void updateProfiler() {
        if (run && outStream != null) {
            try {
                outStream.writeByte(0x11);
                outStream.flush();
            } catch (IOException e) {}
        }
    }
}
