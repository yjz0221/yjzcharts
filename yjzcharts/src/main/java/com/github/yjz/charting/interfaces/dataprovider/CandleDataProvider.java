package com.github.yjz.charting.interfaces.dataprovider;

import com.github.yjz.charting.data.CandleData;

public interface CandleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    CandleData getCandleData();
}
