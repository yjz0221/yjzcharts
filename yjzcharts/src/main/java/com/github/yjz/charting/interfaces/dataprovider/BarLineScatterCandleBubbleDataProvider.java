package com.github.yjz.charting.interfaces.dataprovider;

import com.github.yjz.charting.components.YAxis;
import com.github.yjz.charting.components.YAxis.AxisDependency;
import com.github.yjz.charting.data.BarLineScatterCandleBubbleData;
import com.github.yjz.charting.utils.Transformer;

public interface BarLineScatterCandleBubbleDataProvider extends ChartInterface {

    Transformer getTransformer(AxisDependency axis);
    boolean isInverted(AxisDependency axis);
    
    default float getLowestVisibleX(){
        return getLowestVisibleX(YAxis.AxisDependency.LEFT);
    }

    default float getHighestVisibleX(){
        return getHighestVisibleX(YAxis.AxisDependency.LEFT);
    }

    default float getLowestVisibleX(AxisDependency axis){
        return 0f;
    }

    default float getHighestVisibleX(AxisDependency axis){
        return 0f;
    }

    BarLineScatterCandleBubbleData getData();
}
