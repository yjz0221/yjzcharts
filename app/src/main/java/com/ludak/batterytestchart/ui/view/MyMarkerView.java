
package com.ludak.batterytestchart.ui.view;

import android.content.Context;
import android.widget.TextView;

import com.github.yjz.charting.components.MarkerView;
import com.github.yjz.charting.data.CandleEntry;
import com.github.yjz.charting.data.Entry;
import com.github.yjz.charting.formatter.IAxisValueFormatter;
import com.github.yjz.charting.highlight.Highlight;
import com.github.yjz.charting.utils.MPPointF;
import com.github.yjz.charting.utils.Utils;
import com.ludak.batterytestchart.R;

import java.text.DecimalFormat;


public class MyMarkerView extends MarkerView {

    private final TextView tvContent;
    private final IAxisValueFormatter xAxisValueFormatter;

    private final DecimalFormat format;

    public MyMarkerView(Context context, IAxisValueFormatter xAxisValueFormatter) {
        super(context, R.layout.custom_marker_view);
        tvContent = findViewById(R.id.tvContent);

        this.xAxisValueFormatter = xAxisValueFormatter;
        format = new DecimalFormat("###.00");

    }


    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        if (e instanceof CandleEntry) {

            CandleEntry ce = (CandleEntry) e;

            tvContent.setText(Utils.formatNumber(ce.getHigh(), 0, true));
        } else {
            tvContent.setText(String.format("采集时间：%s\ny: %s", xAxisValueFormatter.getFormattedValue(e.getX(), null),
                    format.format(e.getY())));
        }

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
