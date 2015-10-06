package jaci.openrio.module.android.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.TextView;
import jaci.openrio.module.android.MainActivity;
import jaci.openrio.module.android.PagedConnectedActivity;
import jaci.openrio.module.android.R;

public class AvailableDevice extends Fragment {
    private static final String TEAM_NUMBER = "team_number";
    private static final String TOAST_VERS = "toast_version";
    private static final String ENVIRONMENT = "environment";
    private static final String FRIENDLY_NAME = "friendly_name";
    private static final String UID = "unique_id";

    private String mTeam;
    private String mToast;
    private String mEnv;
    private String mName;
    private String uid;

    public static AvailableDevice newInstance(String uid, String team, String toast_version, String environment, String friendly_name) {
        AvailableDevice fragment = new AvailableDevice();
        Bundle args = new Bundle();
        args.putString(TEAM_NUMBER, team);
        args.putString(TOAST_VERS, toast_version);
        args.putString(FRIENDLY_NAME, friendly_name);
        args.putString(ENVIRONMENT, environment);
        args.putString(UID, uid);
        fragment.setArguments(args);
        return fragment;
    }

    public AvailableDevice() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTeam = getArguments().getString(TEAM_NUMBER);
            mToast = getArguments().getString(TOAST_VERS);
            mName = getArguments().getString(FRIENDLY_NAME);
            mEnv = getArguments().getString(ENVIRONMENT);
            uid = getArguments().getString(UID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_available_device, container, false);
        TextView title = (TextView) v.findViewById(R.id.title);
        title.setText(mTeam);
        TextView name = (TextView) v.findViewById(R.id.friendly_name);
        name.setText(mName);
        TextView env = (TextView) v.findViewById(R.id.environment);
        env.setText(mEnv);
        TextView toast = (TextView) v.findViewById(R.id.toast_version);
        toast.setText("Toast " + mToast);

        ImageButton button = (ImageButton) v.findViewById(R.id.expand_button);
        button.setRotation(180);

        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Intent intent = new Intent(MainActivity.INSTANCE.getApplicationContext(), PagedConnectedActivity.class);
                    intent.putExtra("uid", uid);
                    MainActivity.INSTANCE.startActivity(intent);
                    MainActivity.INSTANCE.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_out);
                }
                return false;
            }
        });
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
