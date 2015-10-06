package jaci.openrio.module.android.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageButton;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.PercentFormatter;
import jaci.openrio.module.android.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class ProfilerPageFragment extends PagerFragment implements OnChartValueSelectedListener {

    private PieChart mChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profiler_page, container, false);
        mChart = (PieChart) v.findViewById(R.id.piechart);
        mChart.setDescription("");

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColorTransparent(true);

        mChart.setRotationAngle(0);
        mChart.setRotationEnabled(false);

        mChart.setOnChartValueSelectedListener(this);
        mChart.getLegend().setEnabled(false);
        mChart.highlightValues(null);

        Button b = (Button) v.findViewById(R.id.refresh);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });

        ImageButton back = (ImageButton) v.findViewById(R.id.up_one);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    tree.removeLast();
                    chartUpdate(object);
                } catch (Exception e) { }
            }
        });

        return v;
    }

    LinkedList<String> tree = new LinkedList<String>();
    ArrayList<String> dataset_index;
    JSONObject object;

    public void chartUpdate(JSONObject object) throws JSONException {
        this.object = object;
        ArrayList<Entry> values = new ArrayList<Entry>();
        dataset_index = new ArrayList<String>();

        JSONObject object_tree = object;

        LinkedList<String> nt = new LinkedList<String>();
        for (String name : tree) {
            try {
                object_tree = object_tree.getJSONObject(name);
                nt.addLast(name);
            } catch (JSONException e) {}        //Not a JSON object
        }
        tree = nt;

        Iterator<String> keys = object_tree.keys();

        int ind = 0;
        while (keys.hasNext()) {
            String key = keys.next();
            dataset_index.add(key);

            Object obj = object_tree.get(key);
            if (obj instanceof JSONObject) {
                values.add(new Entry((float)Math.floor(count((JSONObject) obj) / 1000000), ind));
            } else {
                values.add(new Entry((float)Math.floor(object_tree.getLong(key) / 1000000), ind));
            }
            ind++;
        }

        PieDataSet ds = new PieDataSet(values, "Profiler");
        ds.setColors(ColorTemplate.COLORFUL_COLORS);
        ds.setSelectionShift(0);

        PieData data = new PieData(dataset_index, ds);
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        mChart.setData(data);
        mChart.invalidate();
    }

    private long count(JSONObject object) throws JSONException {
        long cnt = 0;
        Iterator<String> keys = object.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            Object obj = object.get(key);
            if (obj instanceof JSONObject) {
                cnt += count((JSONObject) obj);
            } else {
                cnt += object.getLong(key);
            }
        }
        return cnt;
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        try {
            String name = dataset_index.get(e.getXIndex());
            tree.addLast(name);
            chartUpdate(object);
        } catch (JSONException e1) { }
    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public void update() {
        activity.updateProfiler();
    }
}
