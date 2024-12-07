package com.github.yjz.charting.interfaces.dataprovider;

import com.github.yjz.charting.data.ScatterData;

public interface ScatterDataProvider extends BarLineScatterCandleBubbleDataProvider {

    ScatterData getScatterData();
}
