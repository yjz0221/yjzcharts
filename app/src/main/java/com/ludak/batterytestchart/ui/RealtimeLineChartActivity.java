
package com.ludak.batterytestchart.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.github.yjz.charting.components.Legend;
import com.github.yjz.charting.components.XAxis;
import com.github.yjz.charting.components.YAxis;
import com.github.yjz.charting.data.Entry;
import com.github.yjz.charting.data.LineData;
import com.github.yjz.charting.data.LineDataSet;
import com.github.yjz.charting.highlight.Highlight;
import com.github.yjz.charting.interfaces.datasets.ILineDataSet;
import com.github.yjz.charting.listener.OnChartValueSelectedListener;
import com.github.yjz.charting.multi.MultiLineChart;
import com.github.yjz.charting.utils.ColorTemplate;
import com.github.yjz.charting.utils.Utils;
import com.ludak.batterytestchart.BuildConfig;
import com.ludak.batterytestchart.R;
import com.ludak.batterytestchart.ui.base.DemoBase;


public class RealtimeLineChartActivity extends DemoBase implements
        OnChartValueSelectedListener {

    private MultiLineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_realtime_linechart);

        setTitle("RealtimeLineChartActivity");

        chart = findViewById(R.id.chart1);
        chart.setLogEnabled(BuildConfig.DEBUG);
        chart.setOnChartValueSelectedListener(this);


        // enable description text
        chart.getDescription().setEnabled(true);

        // enable touch gestures
        chart.setTouchEnabled(true);

//        chart.setExtraLeftOffset(100);


        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

        // set an alternative background color
        chart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        chart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(tfLight);
        l.setTextColor(Color.WHITE);

        XAxis xl = chart.getXAxis();
        xl.setDrawScale(true);
        xl.setTypeface(tfLight);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);

        setYAxis(
                chart.getAxisLeft(),
                "单体电压",
                "(V)",
                ContextCompat.getColor(this, R.color.colorYAxis1),
                ContextCompat.getColor(this, R.color.colorYAxis1),
                ContextCompat.getColor(this, R.color.colorBgYAxis1),
                10.00f,
                0.00f
        );


        YAxis yAxis2 = createYAxis(
                YAxis.AxisDependency.LEFT1,
                "单体温度",
                "(℃)",
                ContextCompat.getColor(this, R.color.colorYAxis2),
                ContextCompat.getColor(this, R.color.colorYAxis2),
                ContextCompat.getColor(this, R.color.colorBgYAxis2),
                100f,
                0f
        );


        YAxis yAxis3 = createYAxis(
                YAxis.AxisDependency.LEFT2,
                "剩余电量",
                "(%)",
                ContextCompat.getColor(this, R.color.colorYAxis3),
                ContextCompat.getColor(this, R.color.colorYAxis3),
                ContextCompat.getColor(this, R.color.colorBgYAxis3),
                100f,
                0f
        );


        YAxis yAxis4 = createYAxis(
                YAxis.AxisDependency.LEFT3,
                "总电流",
                "(A)",
                ContextCompat.getColor(this, R.color.colorYAxis4),
                ContextCompat.getColor(this, R.color.colorYAxis4),
                ContextCompat.getColor(this, R.color.colorBgYAxis4),
                500f,
                0f
        );


        YAxis yAxis5 = createYAxis(
                YAxis.AxisDependency.LEFT4,
                "总电压",
                "(V)",
                ContextCompat.getColor(this, R.color.colorYAxis5),
                ContextCompat.getColor(this, R.color.colorYAxis5),
                ContextCompat.getColor(this, R.color.colorBgYAxis5),
                1000f,
                0f
        );

        chart.addYAxis(yAxis2);
        chart.addYAxis(yAxis3);
        chart.addYAxis(yAxis4);
        chart.addYAxis(yAxis5);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }


    private YAxis setYAxis(YAxis yAxis, String yAxisName, String yAxisUnit, int textColor, int lineColor, int nameBgColor, float maxValue, float minValue) {
        yAxis.setYAxisName(yAxisName);
        yAxis.setYAxisUnit(yAxisUnit);
        yAxis.setTextColor(textColor);
        yAxis.setAxisLineColor(lineColor);
        yAxis.setYAxisNameUnitBgColor(nameBgColor);

        yAxis.setDrawScale(true);
        yAxis.setAxisLineWidth(Utils.convertDpToPixel(1.5f));
        yAxis.setTypeface(tfLight);
        yAxis.setTextSize(10f);
        yAxis.setAxisMaximum(maxValue);
        yAxis.setAxisMinimum(minValue);
        yAxis.setDrawLabels(true);
        yAxis.setCenterAxisLabels(true);
        yAxis.setDrawZeroLine(true);
        yAxis.setDrawGridLines(false);
        yAxis.setLabelCount(12,true);
        yAxis.setEnabled(true);

        return yAxis;
    }

    private YAxis createYAxis(YAxis.AxisDependency axis, String yAxisName, String yAxisUnit, int textColor, int lineColor, int nameBgColor, float maxValue, float minValue) {
        YAxis yAxis = new YAxis(axis);

        return setYAxis(yAxis, yAxisName, yAxisUnit, textColor, lineColor, nameBgColor, maxValue, minValue);
    }


    private void addEntry() {
        xAccValue++;

        LineData data = chart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(xAccValue, (float) (Math.random() * 40) + 30f), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(120);
            // chart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // chart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setHighlightEnabled(true);
        set.setAxisDependency(YAxis.AxisDependency.LEFT2);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.parseColor("#ff0000"));
        set.setVisible(true);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(ContextCompat.getColor(this, R.color.colorYAxis3));
        set.setValueTextColor(Color.BLACK);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private Thread thread;
    private int xAccValue = 0;

    private void feedMultiple() {

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addEntry();
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {

                    runOnUiThread(runnable);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.realtime, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.viewGithub: {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/RealtimeLineChartActivity.java"));
                startActivity(i);
                break;
            }
            case R.id.actionAdd: {
                addEntry();
                break;
            }
            case R.id.actionClear: {
                chart.clearValues();
                Toast.makeText(this, "Chart cleared!", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.actionFeedMultiple: {
                feedMultiple();
                break;
            }
            case R.id.actionSave: {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveToGallery();
                } else {
                    requestStoragePermission(chart);
                }
                break;
            }
        }
        return true;
    }

    @Override
    protected void saveToGallery() {
        saveToGallery(chart, "RealtimeLineChartActivity");
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (thread != null) {
            thread.interrupt();
        }
    }
}
