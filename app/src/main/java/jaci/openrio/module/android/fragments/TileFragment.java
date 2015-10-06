package jaci.openrio.module.android.fragments;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import jaci.openrio.module.android.R;

public class TileFragment extends Fragment {
    private static final String FRAGMENT_TITLE = "title";
    private static final String FRAGMENT_ID = "id";
    private static final String SUBTITLE = "subtitle";
    private static final String COLOR = "color";

    String[] subtitle;
    String title;
    String id;
    int last_sub_length;
    int color;
    int last_color;

    TouchListener listener;

    public static TileFragment newInstance(String id, String title, String[] subtitle, int color) {
        TileFragment fragment = new TileFragment();
        Bundle args = new Bundle();
        args.putString(FRAGMENT_TITLE, title);
        args.putString(FRAGMENT_ID, id);
        args.putStringArray(SUBTITLE, subtitle);
        args.putInt(COLOR, color);
        fragment.setArguments(args);
        return fragment;
    }

    public TileFragment() {
    }

    public String getTileID() {
        return id;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(FRAGMENT_TITLE);
            subtitle = getArguments().getStringArray(SUBTITLE);
            id = getArguments().getString(FRAGMENT_ID);
            color = getArguments().getInt(COLOR);
        }
    }

    public void update(String title, String[] subtitle, int color) {
        this.title = title;
        this.subtitle = subtitle;
        this.color = color;
        refresh_view(getView());
    }

    public void refresh_view(View v) {
        if (v != null) {
            TextView title_view = (TextView) v.findViewById(R.id.title);
            title_view.setText(title);
            TextView subtitle_view = (TextView) v.findViewById(R.id.subtitles);
            if (subtitle.length == 0)
                subtitle_view.setVisibility(View.GONE);
            else {
                subtitle_view.setText(TextUtils.join("\n", subtitle));
                last_sub_length = subtitle.length;
            }

            if (last_color != color) {                                  //Avoid updating this all the time
                CardView card = (CardView) v.findViewById(R.id.card);
                card.setCardBackgroundColor(color);
                last_color = color;
            }
        }
    }

    @Override
    public void onAttach(Activity parent) {
        super.onAttach(parent);
        if (parent instanceof TouchListener) {
            listener = (TouchListener) parent;
        }
    }

    public void attachListener(TouchListener parent) {
        listener = parent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tile, container, false);
        refresh_view(v);
        try {
            final TileFragment t = this;
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onTileTouched(t);
                    }
                }
            });

        } catch (Exception e) {}
        return v;
    }

    public static interface TouchListener {
        public void onTileTouched(TileFragment fragment);
    }

}
