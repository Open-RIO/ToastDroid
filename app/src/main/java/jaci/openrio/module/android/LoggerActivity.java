package jaci.openrio.module.android;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import jaci.openrio.delegate.DelegateClient;
import jaci.openrio.module.android.net.PacketManager;

import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoggerActivity extends ActionBarActivity {

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
            setContentView(R.layout.activity_logger);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle(Integer.toString(id.getTeam()) + " Console");
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            ImageButton button = (ImageButton) findViewById(R.id.send_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText text = (EditText) findViewById(R.id.command_input);
                    String textContent = text.getText().toString();
                    send(textContent);
                    text.setText("");
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    DelegateClient client;
    DelegateClient write_client;
    Socket socket;
    Socket write_socket;
    Thread delegateThread;
    boolean run;

    public void send(final String s) {
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                if (write_socket != null && write_socket.isConnected()) {
                    try  {
                        write_socket.getOutputStream().write((s + "\n").getBytes());
                    } catch (Exception e) {}
                }
                return null;
            }
        }.execute(s);
    }

    public void delegate() {
        delegateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    run = true;
                    setBarColor(Color.rgb(33, 33, 33));
                    client = new DelegateClient(id.getClient().getHostAddress(), 5805, "TOAST_logger");
                    client.connect();
                    socket = client.getSocket();
                    write_client = new DelegateClient(id.getClient().getHostAddress(), 5805, "TOAST_command");
                    write_client.connect();
                    write_socket = write_client.getSocket();
                    BufferedReader dis = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while (run) {
                        String data = dis.readLine();
                        append(data);
                    }
                } catch (Exception e) {
                    run = false;
                    setBarColor(Color.RED);
                }
            }
        });
        delegateThread.start();
    }

    public void append(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView v = (TextView) findViewById(R.id.logger_output);
                if (v != null) {
                    String string = analyse(s);
                    v.append(Html.fromHtml(string));
                    v.append("\n");
                    final ScrollView scroll = (ScrollView) findViewById(R.id.scrollView);
                    scroll.post(new Runnable() {
                        @Override
                        public void run() {
                            scroll.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                }
            }
        });
    }

    boolean backlog = false;

    public String analyse(String s) {
        if (s.contains("BEGIN BACKLOG")) {
            backlog = true;
        }

        if (s.contains("END BACKLOG")) {
            backlog = false;
        }

        s = s.replaceAll("\033\\[(\\d+)m([^\033]*)\033\\[(\\d+)m", "<font ansi=$1-$3>$2</font>");
        s = processFont(s);

        return s;
    }

    static Pattern fontMatcher = Pattern.compile("<font ansi=(\\d+)-(\\d+)>");
    String processFont(String s) {
        Matcher m = fontMatcher.matcher(s);
        StringBuffer buffer = new StringBuffer(s.length());
        while (m.find()) {
            String col = Formatting.Colors.match(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
            String text = "";
            if (col != null) {
                text = "<font color=" + col + ">";
            } else {
                text = "<font>";
            }
            m.appendReplacement(buffer, Matcher.quoteReplacement(text));
        }
        m.appendTail(buffer);
        return buffer.toString();
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
        try {
            if (write_socket != null)
                write_socket.close();
        } catch (IOException e) { }
    }

    @Override
    public void onStart() {
        super.onStart();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.logger_output)).setText("");
            }
        });
        delegate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logger, menu);
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
}
