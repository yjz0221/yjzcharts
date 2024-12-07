package com.github.yjz.charting.renderer;

import android.graphics.Canvas;
import android.graphics.Path;

import com.github.yjz.charting.animation.ChartAnimator;
import com.github.yjz.charting.components.YAxis;
import com.github.yjz.charting.interfaces.dataprovider.LineDataProvider;
import com.github.yjz.charting.interfaces.datasets.ILineScatterCandleRadarDataSet;
import com.github.yjz.charting.utils.ViewPortHandler;

/**
 * Created by Philipp Jahoda on 11/07/15.
 */
public abstract class LineScatterCandleRadarRenderer extends BarLineScatterCandleBubbleRenderer {

    /**
     * path that is used for drawing highlight-lines (drawLines(...) cannot be used because of dashes)
     */
    private Path mHighlightLinePath = new Path();

    public LineScatterCandleRadarRenderer(ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
    }

    /**
     * Draws vertical & horizontal highlight-lines if enabled.
     *
     * @param c
     * @param x x-position of the highlight line intersection
     * @param y y-position of the highlight line intersection
     * @param set the currently drawn dataset
     */
    protected void drawHighlightLines(Canvas c, float x, float y, ILineScatterCandleRadarDataSet set) {
        drawHighlightLines(c,x,y,set,null);
    }



    /**
     * Draws vertical & horizontal highlight-lines if enabled.
     *
     * @param c
     * @param x x-position of the highlight line intersection
     * @param y y-position of the highlight line intersection
     * @param set the currently drawn dataset
     */
    protected void drawHighlightLines(Canvas c, float x, float y, ILineScatterCandleRadarDataSet set, LineDataProvider chart) {

        // set color and stroke-width
        mHighlightPaint.setColor(set.getHighLightColor());
        mHighlightPaint.setStrokeWidth(set.getHighlightLineWidth());

        // draw highlighted lines (if enabled)
        mHighlightPaint.setPathEffect(set.getDashPathEffectHighlight());

        // draw vertical highlight lines
        if (set.isVerticalHighlightIndicatorEnabled()) {

            // create vertical path
            mHighlightLinePath.reset();
            mHighlightLinePath.moveTo(x, mViewPortHandler.contentTop());
            mHighlightLinePath.lineTo(x, mViewPortHandler.contentBottom());

            c.drawPath(mHighlightLinePath, mHighlightPaint);
        }

        // draw horizontal highlight lines
        if (set.isHorizontalHighlightIndicatorEnabled()) {

            float horizontalLeft = mViewPortHandler.contentLeft();
            if (chart != null){
                YAxis yAxis = chart.getAxis(set.getAxisDependency());
                if (yAxis != null){

                    //处理多Y轴的情况
                    horizontalLeft -= yAxis.getYInXAxisOffset();
                }
            }

            // create horizontal path
            mHighlightLinePath.reset();
            mHighlightLinePath.moveTo(horizontalLeft, y);
            mHighlightLinePath.lineTo(mViewPortHandler.contentRight(), y);

            c.drawPath(mHighlightLinePath, mHighlightPaint);
        }
    }
}
