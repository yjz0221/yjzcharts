package com.github.yjz.charting.interfaces.dataprovider;

import com.github.yjz.charting.components.YAxis;
import com.github.yjz.charting.data.LineData;

public interface LineDataProvider extends BarLineScatterCandleBubbleDataProvider {

    LineData getLineData();

    YAxis getAxis(YAxis.AxisDependency dependency);
}
