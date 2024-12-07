package com.github.yjz.charting.interfaces.dataprovider;

import com.github.yjz.charting.data.BubbleData;

public interface BubbleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    BubbleData getBubbleData();
}
