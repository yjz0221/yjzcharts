package com.ludak.batterytestchart.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.core.content.ContextCompat;

import com.github.yjz.charting.components.AxisBase;
import com.github.yjz.charting.components.Description;
import com.github.yjz.charting.components.XAxis;
import com.github.yjz.charting.components.YAxis;
import com.github.yjz.charting.data.Entry;
import com.github.yjz.charting.data.LineData;
import com.github.yjz.charting.data.LineDataSet;
import com.github.yjz.charting.formatter.IAxisValueFormatter;
import com.github.yjz.charting.interfaces.datasets.ILineDataSet;
import com.github.yjz.charting.utils.ColorTemplate;
import com.ludak.batterytestchart.BuildConfig;
import com.ludak.batterytestchart.R;
import com.ludak.batterytestchart.databinding.ActivityBatteryChartBinding;
import com.ludak.batterytestchart.ui.base.BaseActivity;
import com.ludak.batterytestchart.ui.view.MyMarkerView;
import com.ludak.batterytestchart.util.BatteryUtils;
import com.rxjava.rxlife.RxLife;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 作者:cl
 * 创建日期：2024/11/28
 * 描述:多Y轴图表主页
 */
public class BatteryChartActivity extends BaseActivity<ActivityBatteryChartBinding> {

    private final long COLLECT_INTERVAL = 1000;

    protected Typeface tfRegular;
    protected Typeface tfLight;

    @Override
    public ActivityBatteryChartBinding getViewBinding(LayoutInflater inflater) {
        return ActivityBatteryChartBinding.inflate(inflater);
    }


    @Override
    public void initData(Bundle savedInstanceState) {
        tfRegular = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
        tfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");
    }


    @Override
    public void initView() {
        initChart();

        //添加一个默认数据，用于解决没有添加数据时Y轴刻度线缩放异常问题(临时解决方案)
        addEntryByAxisDependency(new Entry(0, 0), YAxis.AxisDependency.LEFT);

//        dynamicAddEntry();
    }


    private void initChart() {

        IAxisValueFormatter axisValueFormatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (value == 0) return "";

                return BatteryUtils.convertToHMS((long) (value * COLLECT_INTERVAL/1000f));
            }
        };

        MyMarkerView mv = new MyMarkerView(this,axisValueFormatter);
        mv.setChartView(viewBinding.multiLineChart); // For bounds control
        viewBinding.multiLineChart.setMarker(mv); // Set the marker to the chart


        Description description = new Description();
        description.setText("图表名称");

        viewBinding.multiLineChart.setLogEnabled(BuildConfig.DEBUG);
        viewBinding.multiLineChart.setDescription(description);
        viewBinding.multiLineChart.getLegend().setEnabled(false);
        viewBinding.multiLineChart.setExtraBottomOffset(10f);
        viewBinding.multiLineChart.getDescription().setEnabled(true);
        viewBinding.multiLineChart.setDrawMarkers(true);
        viewBinding.multiLineChart.setTouchEnabled(true);
        viewBinding.multiLineChart.setDragEnabled(true);
        viewBinding.multiLineChart.setScaleEnabled(true);
        viewBinding.multiLineChart.setDrawGridBackground(false);
        // if disabled, scaling can be done on x- and y-axis separately
        viewBinding.multiLineChart.setPinchZoom(true);
        viewBinding.multiLineChart.setBackgroundColor(Color.WHITE);

        getLineData();

        XAxis xl = viewBinding.multiLineChart.getXAxis();
        xl.setDrawScale(true);
        xl.setTypeface(tfLight);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawLabels(true);
        xl.setLabelCount(8);
        xl.setValueFormatter(axisValueFormatter);

        setYAxis(
                viewBinding.multiLineChart.getAxisLeft(),
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

        viewBinding.multiLineChart.addYAxis(yAxis2);
        viewBinding.multiLineChart.addYAxis(yAxis3);
        viewBinding.multiLineChart.addYAxis(yAxis4);
        viewBinding.multiLineChart.addYAxis(yAxis5);

        YAxis rightAxis = viewBinding.multiLineChart.getAxisRight();
        rightAxis.setEnabled(false);
    }


    private YAxis setYAxis(YAxis yAxis, String yAxisName, String yAxisUnit, int textColor, int lineColor, int nameBgColor, float maxValue, float minValue) {
        yAxis.setYAxisName(yAxisName);
        yAxis.setYAxisUnit(yAxisUnit);
        yAxis.setTextColor(textColor);
        yAxis.setAxisLineColor(lineColor);
        yAxis.setYAxisNameUnitBgColor(nameBgColor);

        yAxis.setDrawScale(true);
//        yAxis.setAxisLineWidth(Utils.convertDpToPixel(1f));
        yAxis.setTypeface(tfLight);
        yAxis.setTextSize(10f);
        yAxis.setAxisMaximum(maxValue);
        yAxis.setAxisMinimum(minValue);
        yAxis.setDrawLabels(true);
        yAxis.setCenterAxisLabels(true);
        yAxis.setDrawZeroLine(true);
        yAxis.setDrawGridLines(false);
        yAxis.setLabelCount(12, true);
        yAxis.setEnabled(true);

        return yAxis;
    }

    private YAxis createYAxis(YAxis.AxisDependency axis, String yAxisName, String yAxisUnit, int textColor, int lineColor, int nameBgColor, float maxValue, float minValue) {
        YAxis yAxis = new YAxis(axis);

        return setYAxis(yAxis, yAxisName, yAxisUnit, textColor, lineColor, nameBgColor, maxValue, minValue);
    }


    private LineDataSet createSet(YAxis.AxisDependency axis, int lineColor) {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");

        set.setAxisDependency(axis);
        set.setColor(lineColor);
        set.setCircleColor(Color.parseColor("#ff0000"));
        set.setCircleRadius(4f);
        set.setDrawCircles(false);
        set.setVisible(true);
        set.setLineWidth(2f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setValueTextColor(Color.BLACK);
        set.setHighLightColor(Color.TRANSPARENT);
        set.setHighlightLineWidth(2f);
        set.setHighlightEnabled(true);
        set.setValueTextSize(9f);
        set.setDrawValues(false);

        return set;
    }


    private LineData getLineData() {
        LineData lineData = viewBinding.multiLineChart.getData();

        if (lineData == null) {
            lineData = new LineData();

            lineData.setValueTextColor(Color.BLACK);

            viewBinding.multiLineChart.setData(lineData);
        }

        return lineData;
    }

    private void addEntryByAxisDependency(Entry entry, YAxis.AxisDependency axis) {
        LineData lineData = getLineData();

        ILineDataSet set = null;

        for (ILineDataSet tmpSet : lineData.getDataSets()) {
            if (tmpSet.getAxisDependency() == axis) {
                set = tmpSet;
                break;
            }
        }

        if (set == null) {
            int color = -1;

            if (axis == YAxis.AxisDependency.LEFT) {
                color = ContextCompat.getColor(this, R.color.colorYAxis1);
            } else if (axis == YAxis.AxisDependency.LEFT1) {
                color = ContextCompat.getColor(this, R.color.colorYAxis2);
            } else if (axis == YAxis.AxisDependency.LEFT2) {
                color = ContextCompat.getColor(this, R.color.colorYAxis3);
            } else if (axis == YAxis.AxisDependency.LEFT3) {
                color = ContextCompat.getColor(this, R.color.colorYAxis4);
            } else if (axis == YAxis.AxisDependency.LEFT4) {
                color = ContextCompat.getColor(this, R.color.colorYAxis5);
            }

            if (color != -1) {
                set = createSet(axis, color);
            }

            if (set != null) lineData.addDataSet(set);
        }


        if (set == null) return;

        set.addEntry(entry);
        lineData.notifyDataChanged();

        viewBinding.multiLineChart.notifyDataSetChanged();

        // limit the number of visible entries
        viewBinding.multiLineChart.setVisibleXRangeMaximum(120);
        // chart.setVisibleYRange(30, AxisDependency.LEFT);


        // move to the latest entry
        viewBinding.multiLineChart.moveViewToX(lineData.getEntryCount());
    }

    private void addEntryByDataSetIndex(Entry entry, int index) {
        LineData lineData = getLineData();

        ILineDataSet set = lineData.getDataSetByIndex(index);

        if (set == null) {
            if (index == 0) {
                set = createSet(YAxis.AxisDependency.LEFT, ContextCompat.getColor(this, R.color.colorYAxis1));
            } else if (index == 1) {
                set = createSet(YAxis.AxisDependency.LEFT1, ContextCompat.getColor(this, R.color.colorYAxis2));
            } else if (index == 2) {
                set = createSet(YAxis.AxisDependency.LEFT2, ContextCompat.getColor(this, R.color.colorYAxis3));
            } else if (index == 3) {
                set = createSet(YAxis.AxisDependency.LEFT3, ContextCompat.getColor(this, R.color.colorYAxis4));
            } else if (index == 4) {
                set = createSet(YAxis.AxisDependency.LEFT4, ContextCompat.getColor(this, R.color.colorYAxis5));
            }

            if (set != null) lineData.addDataSet(set);
        }

        if (set == null) return;

        lineData.addEntry(entry, index);
        lineData.notifyDataChanged();

        viewBinding.multiLineChart.notifyDataSetChanged();

        // limit the number of visible entries
        viewBinding.multiLineChart.setVisibleXRangeMaximum(120);
        // chart.setVisibleYRange(30, AxisDependency.LEFT);

        // move to the latest entry
        viewBinding.multiLineChart.moveViewToX(lineData.getEntryCount());

    }


    private void dynamicAddEntry() {
        Observable.interval(COLLECT_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .as(RxLife.as(this))
                .subscribe(index -> {
                    float x = Float.parseFloat(index + "");
                    float y = (float) (Math.random() * 40);

                    addEntryByAxisDependency(new Entry(x, (float) (Math.random() * 0.5f) + 3.325f), YAxis.AxisDependency.LEFT);
                    addEntryByAxisDependency(new Entry(x, (float) (Math.random() * 35f) + 25f), YAxis.AxisDependency.LEFT1);
                    addEntryByAxisDependency(new Entry(x, (float) (Math.random() * -2f) + 89f), YAxis.AxisDependency.LEFT2);
                    addEntryByAxisDependency(new Entry(x, (float) (Math.random() * 30f) + 332f), YAxis.AxisDependency.LEFT3);
                    addEntryByAxisDependency(new Entry(x, (float) (Math.random() * 60f) + 500f), YAxis.AxisDependency.LEFT4);

                });
    }

}
