package com.github.yjz.charting.multi.base;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;


import com.github.yjz.charting.charts.Chart;
import com.github.yjz.charting.components.XAxis;
import com.github.yjz.charting.components.YAxis;
import com.github.yjz.charting.data.BarLineScatterCandleBubbleData;
import com.github.yjz.charting.data.Entry;
import com.github.yjz.charting.highlight.Highlight;
import com.github.yjz.charting.interfaces.dataprovider.BarLineScatterCandleBubbleDataProvider;
import com.github.yjz.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet;
import com.github.yjz.charting.jobs.AnimatedMoveViewJob;
import com.github.yjz.charting.jobs.AnimatedZoomJob;
import com.github.yjz.charting.jobs.MoveViewJob;
import com.github.yjz.charting.jobs.ZoomJob;
import com.github.yjz.charting.listener.BarLineChartTouchListener;
import com.github.yjz.charting.listener.OnDrawListener;
import com.github.yjz.charting.multi.highlight.MultiChartHighlighter;
import com.github.yjz.charting.multi.renderer.MultiYAxisRenderer;
import com.github.yjz.charting.multi.touch.MultiBarLineChartTouchListener;
import com.github.yjz.charting.renderer.AxisRenderer;
import com.github.yjz.charting.renderer.XAxisRenderer;
import com.github.yjz.charting.renderer.YAxisRenderer;
import com.github.yjz.charting.utils.MPPointD;
import com.github.yjz.charting.utils.MPPointF;
import com.github.yjz.charting.utils.Transformer;
import com.github.yjz.charting.utils.Utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 作者:cl
 * 创建日期：2024/9/11
 * 描述:基于AndroidMPChar修改
 */
@SuppressLint("RtlHardcoded")
public abstract class MultiYAXisBarLineChartBase<T extends BarLineScatterCandleBubbleData<? extends
        IBarLineScatterCandleBubbleDataSet<? extends Entry>>>
        extends Chart<T> implements BarLineScatterCandleBubbleDataProvider {


    /**
     * the maximum number of entries to which values will be drawn
     * (entry numbers greater than this value will cause value-labels to disappear)
     */
    protected int mMaxVisibleCount = 100;

    /**
     * flag that indicates if auto scaling on the y axis is enabled
     */
    protected boolean mAutoScaleMinMaxEnabled = false;

    /**
     * flag that indicates if pinch-zoom is enabled. if true, both x and y axis
     * can be scaled with 2 fingers, if false, x and y axis can be scaled
     * separately
     */
    protected boolean mPinchZoomEnabled = false;

    /**
     * flag that indicates if double tap zoom is enabled or not
     */
    protected boolean mDoubleTapToZoomEnabled = true;

    /**
     * flag that indicates if highlighting per dragging over a fully zoomed out
     * chart is enabled
     */
    protected boolean mHighlightPerDragEnabled = true;

    /**
     * if true, dragging is enabled for the chart
     */
    private boolean mDragXEnabled = true;
    private boolean mDragYEnabled = true;

    private boolean mScaleXEnabled = true;
    private boolean mScaleYEnabled = true;

    /**
     * paint object for the (by default) lightgrey background of the grid
     */
    protected Paint mGridBackgroundPaint;

    protected Paint mBorderPaint;

    /**
     * flag indicating if the grid background should be drawn or not
     */
    protected boolean mDrawGridBackground = false;

    protected boolean mDrawBorders = false;

    protected boolean mClipValuesToContent = false;

    /**
     * Sets the minimum offset (padding) around the chart, defaults to 15
     */
    protected float mMinOffset = 15.f;

    /**
     * flag indicating if the chart should stay at the same position after a rotation. Default is false.
     */
    protected boolean mKeepPositionOnRotation = false;

    /**
     * the listener for user drawing on the chart
     */
    protected OnDrawListener mDrawListener;


    /**
     * the object representing the labels on the right y-axis
     */
    protected YAxis mAxisRight;

    protected YAxisRenderer mAxisRendererRight;

    protected Transformer mRightAxisTransformer;

    protected XAxisRenderer mXAxisRenderer;


    private  Map<YAxis.AxisDependency, YAxis> mYAxisMap;
    private  Map<YAxis.AxisDependency, MultiYAxisRenderer> mYAxisRenderMap;
    private  Map<YAxis.AxisDependency, Transformer> mYAxisTransMap;


    public MultiYAXisBarLineChartBase(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MultiYAXisBarLineChartBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiYAXisBarLineChartBase(Context context) {
        super(context);
    }

    @Override
    protected void init() {
        super.init();


        mYAxisMap = new LinkedHashMap<>();
        mYAxisRenderMap = new LinkedHashMap<>();
        mYAxisTransMap = new LinkedHashMap<>();

        //创建一个默认的左边Y轴
        addYAxis(new YAxis(YAxis.AxisDependency.LEFT));

        mAxisRight = new YAxis(YAxis.AxisDependency.RIGHT);

        mRightAxisTransformer = new Transformer(mViewPortHandler);

        mAxisRendererRight = new YAxisRenderer(mViewPortHandler, mAxisRight, mRightAxisTransformer);

        mXAxisRenderer = new XAxisRenderer(mViewPortHandler, mXAxis, mYAxisTransMap.get(YAxis.AxisDependency.LEFT));


        mHighlighter = new MultiChartHighlighter<>(this);

        mChartTouchListener = new MultiBarLineChartTouchListener(this, mViewPortHandler.getMatrixTouch(), 3f);

        mGridBackgroundPaint = new Paint();
        mGridBackgroundPaint.setStyle(Paint.Style.FILL);
        // mGridBackgroundPaint.setColor(Color.WHITE);
        mGridBackgroundPaint.setColor(Color.rgb(240, 240, 240)); // light
        // grey

        mBorderPaint = new Paint();
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(Color.BLACK);
        mBorderPaint.setStrokeWidth(Utils.convertDpToPixel(1f));
    }


    public void addYAxis(YAxis yAxis) {
        if (yAxis == null) throw new NullPointerException("yAxis is null");

        Transformer transformer = new Transformer(mViewPortHandler);

        mYAxisMap.put(yAxis.getAxisDependency(), yAxis);
        mYAxisTransMap.put(yAxis.getAxisDependency(), transformer);
        mYAxisRenderMap.put(yAxis.getAxisDependency(), new MultiYAxisRenderer(mViewPortHandler, yAxis, transformer));
    }


    // for performance tracking
    private long totalTime = 0;
    private long drawCycles = 0;


    private void yAxisListRenderAxisLine(Canvas canvas) {
        for (MultiYAxisRenderer renderer : mYAxisRenderMap.values()) {
            renderer.renderAxisLine(canvas);
        }
    }

    private void yAxisListRenderAxisScaleLines(Canvas canvas){
        for (MultiYAxisRenderer renderer : mYAxisRenderMap.values()) {
            renderer.renderScaleLines(canvas);
        }
    }

    private void yAxisListRenderGridLines(Canvas canvas, boolean isDrawGridLinesBehindDataEnabled) {
        for (YAxis yAxis : mYAxisMap.values()) {
            MultiYAxisRenderer multiYAxisRenderer = mYAxisRenderMap.get(yAxis.getAxisDependency());

            if (isDrawGridLinesBehindDataEnabled) {
                if (yAxis.isDrawGridLinesBehindDataEnabled()) {
                    if (multiYAxisRenderer != null) {
                        multiYAxisRenderer.renderGridLines(canvas);
                    }
                }
            } else {
                if (!yAxis.isDrawGridLinesBehindDataEnabled()) {
                    if (multiYAxisRenderer != null) {
                        multiYAxisRenderer.renderGridLines(canvas);
                    }
                }
            }
        }
    }

    private void yAxisListRenderLimitLines(Canvas canvas, boolean isDrawLimitLinesBehindDataEnabled) {
        for (YAxis yAxis : mYAxisMap.values()) {
            MultiYAxisRenderer multiYAxisRenderer = mYAxisRenderMap.get(yAxis.getAxisDependency());

            if (isDrawLimitLinesBehindDataEnabled) {
                if (yAxis.isEnabled() && yAxis.isDrawLimitLinesBehindDataEnabled()) {
                    if (multiYAxisRenderer != null) {
                        multiYAxisRenderer.renderLimitLines(canvas);
                    }
                }
            } else {
                if (yAxis.isEnabled() && !yAxis.isDrawLimitLinesBehindDataEnabled()) {
                    if (multiYAxisRenderer != null) {
                        multiYAxisRenderer.renderLimitLines(canvas);
                    }
                }
            }
        }
    }

    private void yAxisListRenderAxisLabels(Canvas canvas) {
        for (MultiYAxisRenderer renderer : mYAxisRenderMap.values()) {
            renderer.renderAxisLabels(canvas);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mData == null)
            return;

        long starttime = System.currentTimeMillis();

        // execute all drawing commands
        drawGridBackground(canvas);

        if (mAutoScaleMinMaxEnabled) {
            autoScale();
        }

        yAxisListComputeAxis();

        if (mAxisRight.isEnabled())
            mAxisRendererRight.computeAxis(mAxisRight.mAxisMinimum, mAxisRight.mAxisMaximum, mAxisRight.isInverted());

        if (mXAxis.isEnabled())
            mXAxisRenderer.computeAxis(mXAxis.mAxisMinimum, mXAxis.mAxisMaximum, false);

        yAxisListRenderAxisLine(canvas);

        mXAxisRenderer.renderAxisLine(canvas);
        mAxisRendererRight.renderAxisLine(canvas);

        //绘制X轴刻度线
        mXAxisRenderer.renderScaleLines(canvas);

        //绘制左边Y轴刻度线
        yAxisListRenderAxisScaleLines(canvas);


        if (mXAxis.isDrawGridLinesBehindDataEnabled())
            mXAxisRenderer.renderGridLines(canvas);


        yAxisListRenderGridLines(canvas, true);


        if (mAxisRight.isDrawGridLinesBehindDataEnabled())
            mAxisRendererRight.renderGridLines(canvas);

        if (mXAxis.isEnabled() && mXAxis.isDrawLimitLinesBehindDataEnabled())
            mXAxisRenderer.renderLimitLines(canvas);


        yAxisListRenderLimitLines(canvas, true);


        if (mAxisRight.isEnabled() && mAxisRight.isDrawLimitLinesBehindDataEnabled())
            mAxisRendererRight.renderLimitLines(canvas);

        // make sure the data cannot be drawn outside the content-rect
        int clipRestoreCount = canvas.save();
        canvas.clipRect(mViewPortHandler.getContentRect());

        mRenderer.drawData(canvas);

        if (!mXAxis.isDrawGridLinesBehindDataEnabled())
            mXAxisRenderer.renderGridLines(canvas);

        yAxisListRenderGridLines(canvas, false);

        if (!mAxisRight.isDrawGridLinesBehindDataEnabled())
            mAxisRendererRight.renderGridLines(canvas);

        // Removes clipping rectangle
//        canvas.restoreToCount(clipRestoreCount);


        // if highlighting is enabled
        if (valuesToHighlight())
            mRenderer.drawHighlighted(canvas, mIndicesToHighlight);

        // Removes clipping rectangle
        canvas.restoreToCount(clipRestoreCount);

        mRenderer.drawExtras(canvas);

        if (mXAxis.isEnabled() && !mXAxis.isDrawLimitLinesBehindDataEnabled())
            mXAxisRenderer.renderLimitLines(canvas);

        yAxisListRenderLimitLines(canvas, false);

        if (mAxisRight.isEnabled() && !mAxisRight.isDrawLimitLinesBehindDataEnabled())
            mAxisRendererRight.renderLimitLines(canvas);

        yAxisListRenderAxisLabels(canvas);
        mXAxisRenderer.renderAxisLabels(canvas);
        mAxisRendererRight.renderAxisLabels(canvas);

        if (isClipValuesToContentEnabled()) {
            clipRestoreCount = canvas.save();
            canvas.clipRect(mViewPortHandler.getContentRect());

            mRenderer.drawValues(canvas);

            canvas.restoreToCount(clipRestoreCount);
        } else {
            mRenderer.drawValues(canvas);
        }

        mLegendRenderer.renderLegend(canvas);

        drawDescription(canvas);

        drawMarkers(canvas);

        if (mLogEnabled) {
            long drawtime = (System.currentTimeMillis() - starttime);
            totalTime += drawtime;
            drawCycles += 1;
            long average = totalTime / drawCycles;
            Log.i(LOG_TAG, "Drawtime: " + drawtime + " ms, average: " + average + " ms, cycles: "
                    + drawCycles);
        }
    }

    /**
     * RESET PERFORMANCE TRACKING FIELDS
     */
    public void resetTracking() {
        totalTime = 0;
        drawCycles = 0;
    }

    private void yAxisPrepareMatrixValuePx() {
        for (YAxis yAxis : mYAxisMap.values()) {
            Transformer transformer = mYAxisTransMap.get(yAxis.getAxisDependency());

            if (transformer != null) {
                transformer.prepareMatrixValuePx(
                        mXAxis.mAxisMinimum, mXAxis.mAxisRange, yAxis.mAxisRange, yAxis.mAxisMinimum);
            }
        }
    }

    private void yAxisPrepareOffsetMatrix() {
        for (YAxis yAxis : mYAxisMap.values()) {
            Transformer transformer = mYAxisTransMap.get(yAxis.getAxisDependency());

            if (transformer != null) {
                transformer.prepareMatrixOffset(yAxis.isInverted());
            }
        }
    }

    protected void prepareValuePxMatrix() {

        if (mLogEnabled)
            Log.i(LOG_TAG, "Preparing Value-Px Matrix, xmin: " + mXAxis.mAxisMinimum + ", xmax: "
                    + mXAxis.mAxisMaximum + ", xdelta: " + mXAxis.mAxisRange);

        mRightAxisTransformer.prepareMatrixValuePx(mXAxis.mAxisMinimum,
                mXAxis.mAxisRange,
                mAxisRight.mAxisRange,
                mAxisRight.mAxisMinimum);

        yAxisPrepareMatrixValuePx();
    }

    protected void prepareOffsetMatrix() {
        mRightAxisTransformer.prepareMatrixOffset(mAxisRight.isInverted());

        yAxisPrepareOffsetMatrix();
    }

    @Override
    public void notifyDataSetChanged() {

        if (mData == null) {
            if (mLogEnabled)
                Log.i(LOG_TAG, "Preparing... DATA NOT SET.");
            return;
        } else {
            if (mLogEnabled)
                Log.i(LOG_TAG, "Preparing...");
        }

        if (mRenderer != null)
            mRenderer.initBuffers();

        calcMinMax();

        yAxisListComputeAxis();
        mAxisRendererRight.computeAxis(mAxisRight.mAxisMinimum, mAxisRight.mAxisMaximum, mAxisRight.isInverted());
        mXAxisRenderer.computeAxis(mXAxis.mAxisMinimum, mXAxis.mAxisMaximum, false);

        if (mLegend != null)
            mLegendRenderer.computeLegend(mData);

        calculateOffsets();
    }

    private void yAxisListCalculate(boolean needEnable) {
        for (YAxis yAxis : mYAxisMap.values()) {
            if (needEnable) {
                if (yAxis.isEnabled() && yAxis.isAxisDependencyLeft()) {
                    yAxis.calculate(mData.getYMin(yAxis.getAxisDependency()),
                            mData.getYMax(yAxis.getAxisDependency()));
                }
            } else {
                if (yAxis.isAxisDependencyLeft()) {
                    yAxis.calculate(mData.getYMin(yAxis.getAxisDependency()),
                            mData.getYMax(yAxis.getAxisDependency()));
                }
            }
        }
    }

    private void yAxisListComputeAxis() {
        for (YAxis yAxis : mYAxisMap.values()) {
            MultiYAxisRenderer renderer = mYAxisRenderMap.get(yAxis.getAxisDependency());

            if (renderer != null) {
                renderer.computeAxis(yAxis.mAxisMinimum, yAxis.mAxisMaximum, yAxis.isInverted());
            }
        }
    }

    private float yAxisCalculateOffsets(float offsetLeft) {
        float result = 0f;
        boolean firstEnter = true;

        for (YAxis yAxis : mYAxisMap.values()) {
            MultiYAxisRenderer renderer = mYAxisRenderMap.get(yAxis.getAxisDependency());


            if (yAxis.needsOffset()) {

                if (firstEnter) {
                    result += (offsetLeft);
                }

                yAxis.setYInXAxisOffset(result);

                if (renderer != null) {
//                    result += (yAxis.getRequiredWidthSpace(renderer.getPaintAxisLabels()) + baseOffset);

                    yAxis.setNextYAxisDistance(yAxis.getRequiredWidthSpace(renderer.getPaintAxisLabels())
                            + yAxis.getLabelAndNameInterval());


                    if (yAxis.isDrawScale()){
                        yAxis.setNextYAxisDistance(yAxis.getNextYAxisDistance() + yAxis.getLongScaleLineLength());
                    }

                    result+=yAxis.getNextYAxisDistance();
                }


                firstEnter = false;
            }
        }

        return result;
    }

    /**
     * Performs auto scaling of the axis by recalculating the minimum and maximum y-values based on the entries currently in view.
     */
    protected void autoScale() {

        final float fromX = getLowestVisibleX();
        final float toX = getHighestVisibleX();

        mData.calcMinMaxY(fromX, toX);

        mXAxis.calculate(mData.getXMin(), mData.getXMax());

        // calculate axis range (min / max) according to provided data

        yAxisListCalculate(true);


        if (mAxisRight.isEnabled())
            mAxisRight.calculate(mData.getYMin(YAxis.AxisDependency.RIGHT),
                    mData.getYMax(YAxis.AxisDependency.RIGHT));

        calculateOffsets();
    }

    @Override
    protected void calcMinMax() {

        mXAxis.calculate(mData.getXMin(), mData.getXMax());

        // calculate axis range (min / max) according to provided data
        yAxisListCalculate(false);
        mAxisRight.calculate(mData.getYMin(YAxis.AxisDependency.RIGHT), mData.getYMax(YAxis.AxisDependency
                .RIGHT));
    }

    protected void calculateLegendOffsets(RectF offsets) {

        offsets.left = 0.f;
        offsets.right = 0.f;
        offsets.top = 0.f;
        offsets.bottom = 0.f;

        // setup offsets for legend
        if (mLegend != null && mLegend.isEnabled() && !mLegend.isDrawInsideEnabled()) {
            switch (mLegend.getOrientation()) {
                case VERTICAL:

                    switch (mLegend.getHorizontalAlignment()) {
                        case LEFT:
                            offsets.left += Math.min(mLegend.mNeededWidth,
                                    mViewPortHandler.getChartWidth() * mLegend.getMaxSizePercent())
                                    + mLegend.getXOffset();
                            break;

                        case RIGHT:
                            offsets.right += Math.min(mLegend.mNeededWidth,
                                    mViewPortHandler.getChartWidth() * mLegend.getMaxSizePercent())
                                    + mLegend.getXOffset();
                            break;

                        case CENTER:

                            switch (mLegend.getVerticalAlignment()) {
                                case TOP:
                                    offsets.top += Math.min(mLegend.mNeededHeight,
                                            mViewPortHandler.getChartHeight() * mLegend.getMaxSizePercent())
                                            + mLegend.getYOffset();
                                    break;

                                case BOTTOM:
                                    offsets.bottom += Math.min(mLegend.mNeededHeight,
                                            mViewPortHandler.getChartHeight() * mLegend.getMaxSizePercent())
                                            + mLegend.getYOffset();
                                    break;

                                default:
                                    break;
                            }
                    }

                    break;

                case HORIZONTAL:

                    switch (mLegend.getVerticalAlignment()) {
                        case TOP:
                            offsets.top += Math.min(mLegend.mNeededHeight,
                                    mViewPortHandler.getChartHeight() * mLegend.getMaxSizePercent())
                                    + mLegend.getYOffset();
                            break;

                        case BOTTOM:
                            offsets.bottom += Math.min(mLegend.mNeededHeight,
                                    mViewPortHandler.getChartHeight() * mLegend.getMaxSizePercent())
                                    + mLegend.getYOffset();
                            break;

                        default:
                            break;
                    }
                    break;
            }
        }
    }

    private RectF mOffsetsBuffer = new RectF();


    /**
     * 重新计算Chart的内容大小并计算好数据和像素的缩放比
     */
    @Override
    public void calculateOffsets() {

        if (!mCustomViewPortEnabled) {

            float offsetLeft = 0f, offsetRight = 0f, offsetTop = 0f, offsetBottom = 0f;

            calculateLegendOffsets(mOffsetsBuffer);

            offsetLeft += mOffsetsBuffer.left;
            offsetTop += mOffsetsBuffer.top;
            offsetRight += mOffsetsBuffer.right;
            offsetBottom += mOffsetsBuffer.bottom;

            // offsets for y-labels
            offsetLeft = yAxisCalculateOffsets(offsetLeft);


            if (mAxisRight.needsOffset()) {
                offsetRight += mAxisRight.getRequiredWidthSpace(mAxisRendererRight
                        .getPaintAxisLabels());
            }

            if (mXAxis.isEnabled()){
                if (mXAxis.isDrawLabelsEnabled()){
                    float xLabelHeight = mXAxis.mLabelRotatedHeight + mXAxis.getYOffset();

                    // offsets for x-labels
                    if (mXAxis.getPosition() == XAxis.XAxisPosition.BOTTOM) {

                        offsetBottom += xLabelHeight;

                    } else if (mXAxis.getPosition() == XAxis.XAxisPosition.TOP) {

                        offsetTop += xLabelHeight;

                    } else if (mXAxis.getPosition() == XAxis.XAxisPosition.BOTH_SIDED) {

                        offsetBottom += xLabelHeight;
                        offsetTop += xLabelHeight;
                    }
                }

                if (mXAxis.isDrawScale()){
                    // offsets for scale line
                    if (mXAxis.getPosition() == XAxis.XAxisPosition.BOTTOM) {

                        offsetBottom += mXAxis.getLongScaleLineLength();

                    } else if (mXAxis.getPosition() == XAxis.XAxisPosition.TOP) {

                        offsetTop += mXAxis.getLongScaleLineLength();

                    } else if (mXAxis.getPosition() == XAxis.XAxisPosition.BOTH_SIDED) {

                        offsetBottom += mXAxis.getLongScaleLineLength();
                        offsetTop += mXAxis.getLongScaleLineLength();
                    }
                }
            }

            offsetTop += getExtraTopOffset();
            offsetRight += getExtraRightOffset();
            offsetBottom += getExtraBottomOffset();
            offsetLeft += getExtraLeftOffset();

            float minOffset = Utils.convertDpToPixel(mMinOffset);

            mViewPortHandler.restrainViewPort(
                    Math.max(minOffset, offsetLeft),
                    Math.max(minOffset, offsetTop),
                    Math.max(minOffset, offsetRight),
                    Math.max(minOffset, offsetBottom));

            if (mLogEnabled) {
                Log.i(LOG_TAG, "offsetLeft: " + offsetLeft + ", offsetTop: " + offsetTop
                        + ", offsetRight: " + offsetRight + ", offsetBottom: " + offsetBottom);
                Log.i(LOG_TAG, "Content: " + mViewPortHandler.getContentRect().toString());
            }
        }

        prepareOffsetMatrix();
        prepareValuePxMatrix();
    }

    /**
     * draws the grid background
     */
    protected void drawGridBackground(Canvas c) {

        if (mDrawGridBackground) {

            // draw the grid background
            c.drawRect(mViewPortHandler.getContentRect(), mGridBackgroundPaint);
        }

        if (mDrawBorders) {
            c.drawRect(mViewPortHandler.getContentRect(), mBorderPaint);
        }
    }

    /**
     * Returns the Transformer class that contains all matrices and is
     * responsible for transforming values into pixels on the screen and
     * backwards.
     *
     * @return
     */
    public Transformer getTransformer(YAxis.AxisDependency which) {
        return findTransformerByDependency(which);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (mChartTouchListener == null || mData == null)
            return false;

        // check if touch gestures are enabled
        if (!mTouchEnabled)
            return false;
        else
            return mChartTouchListener.onTouch(this, event);
    }

    @Override
    public void computeScroll() {

        if (mChartTouchListener instanceof BarLineChartTouchListener)
            ((BarLineChartTouchListener) mChartTouchListener).computeScroll();
    }

    /**
     * ################ ################ ################ ################
     */
    /**
     * CODE BELOW THIS RELATED TO SCALING AND GESTURES AND MODIFICATION OF THE
     * VIEWPORT
     */

    protected Matrix mZoomMatrixBuffer = new Matrix();

    /**
     * Zooms in by 1.4f, into the charts center.
     */
    public void zoomIn() {

        MPPointF center = mViewPortHandler.getContentCenter();

        mViewPortHandler.zoomIn(center.x, -center.y, mZoomMatrixBuffer);
        mViewPortHandler.refresh(mZoomMatrixBuffer, this, false);

        MPPointF.recycleInstance(center);

        // Range might have changed, which means that Y-axis labels
        // could have changed in size, affecting Y-axis size.
        // So we need to recalculate offsets.
        calculateOffsets();
        postInvalidate();
    }

    /**
     * Zooms out by 0.7f, from the charts center.
     */
    public void zoomOut() {

        MPPointF center = mViewPortHandler.getContentCenter();

        mViewPortHandler.zoomOut(center.x, -center.y, mZoomMatrixBuffer);
        mViewPortHandler.refresh(mZoomMatrixBuffer, this, false);

        MPPointF.recycleInstance(center);

        // Range might have changed, which means that Y-axis labels
        // could have changed in size, affecting Y-axis size.
        // So we need to recalculate offsets.
        calculateOffsets();
        postInvalidate();
    }

    /**
     * Zooms out to original size.
     */
    public void resetZoom() {

        mViewPortHandler.resetZoom(mZoomMatrixBuffer);
        mViewPortHandler.refresh(mZoomMatrixBuffer, this, false);

        // Range might have changed, which means that Y-axis labels
        // could have changed in size, affecting Y-axis size.
        // So we need to recalculate offsets.
        calculateOffsets();
        postInvalidate();
    }

    /**
     * Zooms in or out by the given scale factor. x and y are the coordinates
     * (in pixels) of the zoom center.
     *
     * @param scaleX if < 1f --> zoom out, if > 1f --> zoom in
     * @param scaleY if < 1f --> zoom out, if > 1f --> zoom in
     * @param x
     * @param y
     */
    public void zoom(float scaleX, float scaleY, float x, float y) {

        mViewPortHandler.zoom(scaleX, scaleY, x, -y, mZoomMatrixBuffer);
        mViewPortHandler.refresh(mZoomMatrixBuffer, this, false);

        // Range might have changed, which means that Y-axis labels
        // could have changed in size, affecting Y-axis size.
        // So we need to recalculate offsets.
        calculateOffsets();
        postInvalidate();
    }

    /**
     * Zooms in or out by the given scale factor.
     * x and y are the values (NOT PIXELS) of the zoom center..
     *
     * @param scaleX
     * @param scaleY
     * @param xValue
     * @param yValue
     * @param axis   the axis relative to which the zoom should take place
     */
    public void zoom(float scaleX, float scaleY, float xValue, float yValue, YAxis.AxisDependency axis) {

        Runnable job = ZoomJob.getInstance(mViewPortHandler, scaleX, scaleY, xValue, yValue, getTransformer(axis), axis, this);
        addViewportJob(job);
    }

    /**
     * Zooms to the center of the chart with the given scale factor.
     *
     * @param scaleX
     * @param scaleY
     */
    public void zoomToCenter(float scaleX, float scaleY) {

        MPPointF center = getCenterOffsets();

        Matrix save = mZoomMatrixBuffer;
        mViewPortHandler.zoom(scaleX, scaleY, center.x, -center.y, save);
        mViewPortHandler.refresh(save, this, false);
    }

    /**
     * Zooms by the specified scale factor to the specified values on the specified axis.
     *
     * @param scaleX
     * @param scaleY
     * @param xValue
     * @param yValue
     * @param axis
     * @param duration
     */
    @TargetApi(11)
    public void zoomAndCenterAnimated(float scaleX, float scaleY, float xValue, float yValue, YAxis.AxisDependency axis,
                                      long duration) {

        MPPointD origin = getValuesByTouchPoint(mViewPortHandler.contentLeft(), mViewPortHandler.contentTop(), axis);

        Runnable job = AnimatedZoomJob.getInstance(mViewPortHandler, this, getTransformer(axis), getAxis(axis), mXAxis
                        .mAxisRange, scaleX, scaleY, mViewPortHandler.getScaleX(), mViewPortHandler.getScaleY(),
                xValue, yValue, (float) origin.x, (float) origin.y, duration);
        addViewportJob(job);

        MPPointD.recycleInstance(origin);
    }

    protected Matrix mFitScreenMatrixBuffer = new Matrix();

    /**
     * Resets all zooming and dragging and makes the chart fit exactly it's
     * bounds.
     */
    public void fitScreen() {
        Matrix save = mFitScreenMatrixBuffer;
        mViewPortHandler.fitScreen(save);
        mViewPortHandler.refresh(save, this, false);

        calculateOffsets();
        postInvalidate();
    }

    /**
     * Sets the minimum scale factor value to which can be zoomed out. 1f =
     * fitScreen
     *
     * @param scaleX
     * @param scaleY
     */
    public void setScaleMinima(float scaleX, float scaleY) {
        mViewPortHandler.setMinimumScaleX(scaleX);
        mViewPortHandler.setMinimumScaleY(scaleY);
    }

    /**
     * Sets the size of the area (range on the x-axis) that should be maximum
     * visible at once (no further zooming out allowed). If this is e.g. set to
     * 10, no more than a range of 10 on the x-axis can be viewed at once without
     * scrolling.
     *
     * @param maxXRange The maximum visible range of x-values.
     */
    public void setVisibleXRangeMaximum(float maxXRange) {
        float xScale = mXAxis.mAxisRange / (maxXRange);
        mViewPortHandler.setMinimumScaleX(xScale);
    }

    /**
     * Sets the size of the area (range on the x-axis) that should be minimum
     * visible at once (no further zooming in allowed). If this is e.g. set to
     * 10, no less than a range of 10 on the x-axis can be viewed at once without
     * scrolling.
     *
     * @param minXRange The minimum visible range of x-values.
     */
    public void setVisibleXRangeMinimum(float minXRange) {
        float xScale = mXAxis.mAxisRange / (minXRange);
        mViewPortHandler.setMaximumScaleX(xScale);
    }

    /**
     * Limits the maximum and minimum x range that can be visible by pinching and zooming. e.g. minRange=10, maxRange=100 the
     * smallest range to be displayed at once is 10, and no more than a range of 100 values can be viewed at once without
     * scrolling
     *
     * @param minXRange
     * @param maxXRange
     */
    public void setVisibleXRange(float minXRange, float maxXRange) {
        float minScale = mXAxis.mAxisRange / minXRange;
        float maxScale = mXAxis.mAxisRange / maxXRange;
        mViewPortHandler.setMinMaxScaleX(minScale, maxScale);
    }

    /**
     * Sets the size of the area (range on the y-axis) that should be maximum
     * visible at once.
     *
     * @param maxYRange the maximum visible range on the y-axis
     * @param axis      the axis for which this limit should apply
     */
    public void setVisibleYRangeMaximum(float maxYRange, YAxis.AxisDependency axis) {
        float yScale = getAxisRange(axis) / maxYRange;
        mViewPortHandler.setMinimumScaleY(yScale);
    }

    /**
     * Sets the size of the area (range on the y-axis) that should be minimum visible at once, no further zooming in possible.
     *
     * @param minYRange
     * @param axis      the axis for which this limit should apply
     */
    public void setVisibleYRangeMinimum(float minYRange, YAxis.AxisDependency axis) {
        float yScale = getAxisRange(axis) / minYRange;
        mViewPortHandler.setMaximumScaleY(yScale);
    }

    /**
     * Limits the maximum and minimum y range that can be visible by pinching and zooming.
     *
     * @param minYRange
     * @param maxYRange
     * @param axis
     */
    public void setVisibleYRange(float minYRange, float maxYRange, YAxis.AxisDependency axis) {
        float minScale = getAxisRange(axis) / minYRange;
        float maxScale = getAxisRange(axis) / maxYRange;
        mViewPortHandler.setMinMaxScaleY(minScale, maxScale);
    }


    /**
     * Moves the left side of the current viewport to the specified x-position.
     * This also refreshes the chart by calling invalidate().
     *
     * @param xValue
     */
    public void moveViewToX(float xValue) {
        Runnable job = MoveViewJob.getInstance(mViewPortHandler, xValue, 0f,
                getTransformer(YAxis.AxisDependency.LEFT), this);

        addViewportJob(job);
    }



    /**
     * This will move the left side of the current viewport to the specified
     * x-value on the x-axis, and center the viewport to the specified y value on the y-axis.
     * This also refreshes the chart by calling invalidate().
     *
     * @param xValue
     * @param yValue
     * @param axis   - which axis should be used as a reference for the y-axis
     */
    public void moveViewTo(float xValue, float yValue, YAxis.AxisDependency axis) {

        float yInView = getAxisRange(axis) / mViewPortHandler.getScaleY();

        Runnable job = MoveViewJob.getInstance(mViewPortHandler, xValue, yValue + yInView / 2f,
                getTransformer(axis), this);

        addViewportJob(job);
    }

    /**
     * This will move the left side of the current viewport to the specified x-value
     * and center the viewport to the y value animated.
     * This also refreshes the chart by calling invalidate().
     *
     * @param xValue
     * @param yValue
     * @param axis
     * @param duration the duration of the animation in milliseconds
     */
    @TargetApi(11)
    public void moveViewToAnimated(float xValue, float yValue, YAxis.AxisDependency axis, long duration) {

        MPPointD bounds = getValuesByTouchPoint(mViewPortHandler.contentLeft(), mViewPortHandler.contentTop(), axis);

        float yInView = getAxisRange(axis) / mViewPortHandler.getScaleY();

        Runnable job = AnimatedMoveViewJob.getInstance(mViewPortHandler, xValue, yValue + yInView / 2f,
                getTransformer(axis), this, (float) bounds.x, (float) bounds.y, duration);

        addViewportJob(job);

        MPPointD.recycleInstance(bounds);
    }

    /**
     * Centers the viewport to the specified y value on the y-axis.
     * This also refreshes the chart by calling invalidate().
     *
     * @param yValue
     * @param axis   - which axis should be used as a reference for the y-axis
     */
    public void centerViewToY(float yValue, YAxis.AxisDependency axis) {

        float valsInView = getAxisRange(axis) / mViewPortHandler.getScaleY();

        Runnable job = MoveViewJob.getInstance(mViewPortHandler, 0f, yValue + valsInView / 2f,
                getTransformer(axis), this);

        addViewportJob(job);
    }

    /**
     * This will move the center of the current viewport to the specified
     * x and y value.
     * This also refreshes the chart by calling invalidate().
     *
     * @param xValue
     * @param yValue
     * @param axis   - which axis should be used as a reference for the y axis
     */
    public void centerViewTo(float xValue, float yValue, YAxis.AxisDependency axis) {

        float yInView = getAxisRange(axis) / mViewPortHandler.getScaleY();
        float xInView = getXAxis().mAxisRange / mViewPortHandler.getScaleX();

        Runnable job = MoveViewJob.getInstance(mViewPortHandler,
                xValue - xInView / 2f, yValue + yInView / 2f,
                getTransformer(axis), this);

        addViewportJob(job);
    }

    /**
     * This will move the center of the current viewport to the specified
     * x and y value animated.
     *
     * @param xValue
     * @param yValue
     * @param axis
     * @param duration the duration of the animation in milliseconds
     */
    @TargetApi(11)
    public void centerViewToAnimated(float xValue, float yValue, YAxis.AxisDependency axis, long duration) {

        MPPointD bounds = getValuesByTouchPoint(mViewPortHandler.contentLeft(), mViewPortHandler.contentTop(), axis);

        float yInView = getAxisRange(axis) / mViewPortHandler.getScaleY();
        float xInView = getXAxis().mAxisRange / mViewPortHandler.getScaleX();

        Runnable job = AnimatedMoveViewJob.getInstance(mViewPortHandler,
                xValue - xInView / 2f, yValue + yInView / 2f,
                getTransformer(axis), this, (float) bounds.x, (float) bounds.y, duration);

        addViewportJob(job);

        MPPointD.recycleInstance(bounds);
    }

    /**
     * flag that indicates if a custom viewport offset has been set
     */
    private boolean mCustomViewPortEnabled = false;

    /**
     * Sets custom offsets for the current ViewPort (the offsets on the sides of
     * the actual chart window). Setting this will prevent the chart from
     * automatically calculating it's offsets. Use resetViewPortOffsets() to
     * undo this. ONLY USE THIS WHEN YOU KNOW WHAT YOU ARE DOING, else use
     * setExtraOffsets(...).
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setViewPortOffsets(final float left, final float top,
                                   final float right, final float bottom) {

        mCustomViewPortEnabled = true;
        post(new Runnable() {

            @Override
            public void run() {

                mViewPortHandler.restrainViewPort(left, top, right, bottom);
                prepareOffsetMatrix();
                prepareValuePxMatrix();
            }
        });
    }

    /**
     * Resets all custom offsets set via setViewPortOffsets(...) method. Allows
     * the chart to again calculate all offsets automatically.
     */
    public void resetViewPortOffsets() {
        mCustomViewPortEnabled = false;
        calculateOffsets();
    }

    /**
     * ################ ################ ################ ################
     */
    /** CODE BELOW IS GETTERS AND SETTERS */

    /**
     * Returns the range of the specified axis.
     *
     * @param axis
     * @return
     */
    protected float getAxisRange(YAxis.AxisDependency axis) {
        return findYAxisByDependency(axis).mAxisRange;
    }


    protected YAxis findYAxisByDependency(YAxis.AxisDependency axis) {
        if (axis == YAxis.AxisDependency.RIGHT) {
            return mAxisRight;
        } else {
            YAxis yAxis = mYAxisMap.get(axis);

            if (yAxis != null) return yAxis;
        }

        return mYAxisMap.get(YAxis.AxisDependency.LEFT);
    }


    protected Transformer findTransformerByDependency(YAxis.AxisDependency axis) {
        if (axis == YAxis.AxisDependency.RIGHT) {
            return mRightAxisTransformer;
        } else {
            Transformer transformer = mYAxisTransMap.get(axis);

            if (transformer != null){
                return transformer;
            }
        }

        return mYAxisTransMap.get(YAxis.AxisDependency.LEFT);
    }


    /**
     * Sets the OnDrawListener
     *
     * @param drawListener
     */
    public void setOnDrawListener(OnDrawListener drawListener) {
        this.mDrawListener = drawListener;
    }

    /**
     * Gets the OnDrawListener. May be null.
     *
     * @return
     */
    public OnDrawListener getDrawListener() {
        return mDrawListener;
    }

    protected float[] mGetPositionBuffer = new float[2];

    /**
     * Returns a recyclable MPPointF instance.
     * Returns the position (in pixels) the provided Entry has inside the chart
     * view or null, if the provided Entry is null.
     *
     * @param e
     * @return
     */
    public MPPointF getPosition(Entry e, YAxis.AxisDependency axis) {

        if (e == null)
            return null;

        mGetPositionBuffer[0] = e.getX();
        mGetPositionBuffer[1] = e.getY();

        getTransformer(axis).pointValuesToPixel(mGetPositionBuffer);

        return MPPointF.getInstance(mGetPositionBuffer[0], mGetPositionBuffer[1]);
    }

    /**
     * sets the number of maximum visible drawn values on the chart only active
     * when setDrawValues() is enabled
     *
     * @param count
     */
    public void setMaxVisibleValueCount(int count) {
        this.mMaxVisibleCount = count;
    }

    public int getMaxVisibleCount() {
        return mMaxVisibleCount;
    }

    /**
     * Set this to true to allow highlighting per dragging over the chart
     * surface when it is fully zoomed out. Default: true
     *
     * @param enabled
     */
    public void setHighlightPerDragEnabled(boolean enabled) {
        mHighlightPerDragEnabled = enabled;
    }

    public boolean isHighlightPerDragEnabled() {
        return mHighlightPerDragEnabled;
    }

    /**
     * Sets the color for the background of the chart-drawing area (everything
     * behind the grid lines).
     *
     * @param color
     */
    public void setGridBackgroundColor(int color) {
        mGridBackgroundPaint.setColor(color);
    }

    /**
     * Set this to true to enable dragging (moving the chart with the finger)
     * for the chart (this does not effect scaling).
     *
     * @param enabled
     */
    public void setDragEnabled(boolean enabled) {
        this.mDragXEnabled = enabled;
        this.mDragYEnabled = enabled;
    }

    /**
     * Returns true if dragging is enabled for the chart, false if not.
     *
     * @return
     */
    public boolean isDragEnabled() {
        return mDragXEnabled || mDragYEnabled;
    }

    /**
     * Set this to true to enable dragging on the X axis
     *
     * @param enabled
     */
    public void setDragXEnabled(boolean enabled) {
        this.mDragXEnabled = enabled;
    }

    /**
     * Returns true if dragging on the X axis is enabled for the chart, false if not.
     *
     * @return
     */
    public boolean isDragXEnabled() {
        return mDragXEnabled;
    }

    /**
     * Set this to true to enable dragging on the Y axis
     *
     * @param enabled
     */
    public void setDragYEnabled(boolean enabled) {
        this.mDragYEnabled = enabled;
    }

    /**
     * Returns true if dragging on the Y axis is enabled for the chart, false if not.
     *
     * @return
     */
    public boolean isDragYEnabled() {
        return mDragYEnabled;
    }

    /**
     * Set this to true to enable scaling (zooming in and out by gesture) for
     * the chart (this does not effect dragging) on both X- and Y-Axis.
     *
     * @param enabled
     */
    public void setScaleEnabled(boolean enabled) {
        this.mScaleXEnabled = enabled;
        this.mScaleYEnabled = enabled;
    }

    public void setScaleXEnabled(boolean enabled) {
        mScaleXEnabled = enabled;
    }

    public void setScaleYEnabled(boolean enabled) {
        mScaleYEnabled = enabled;
    }

    public boolean isScaleXEnabled() {
        return mScaleXEnabled;
    }

    public boolean isScaleYEnabled() {
        return mScaleYEnabled;
    }

    /**
     * Set this to true to enable zooming in by double-tap on the chart.
     * Default: enabled
     *
     * @param enabled
     */
    public void setDoubleTapToZoomEnabled(boolean enabled) {
        mDoubleTapToZoomEnabled = enabled;
    }

    /**
     * Returns true if zooming via double-tap is enabled false if not.
     *
     * @return
     */
    public boolean isDoubleTapToZoomEnabled() {
        return mDoubleTapToZoomEnabled;
    }

    /**
     * set this to true to draw the grid background, false if not
     *
     * @param enabled
     */
    public void setDrawGridBackground(boolean enabled) {
        mDrawGridBackground = enabled;
    }

    /**
     * When enabled, the borders rectangle will be rendered.
     * If this is enabled, there is no point drawing the axis-lines of x- and y-axis.
     *
     * @param enabled
     */
    public void setDrawBorders(boolean enabled) {
        mDrawBorders = enabled;
    }

    /**
     * When enabled, the borders rectangle will be rendered.
     * If this is enabled, there is no point drawing the axis-lines of x- and y-axis.
     *
     * @return
     */
    public boolean isDrawBordersEnabled() {
        return mDrawBorders;
    }

    /**
     * When enabled, the values will be clipped to contentRect,
     * otherwise they can bleed outside the content rect.
     *
     * @param enabled
     */
    public void setClipValuesToContent(boolean enabled) {
        mClipValuesToContent = enabled;
    }

    /**
     * When enabled, the values will be clipped to contentRect,
     * otherwise they can bleed outside the content rect.
     *
     * @return
     */
    public boolean isClipValuesToContentEnabled() {
        return mClipValuesToContent;
    }

    /**
     * Sets the width of the border lines in dp.
     *
     * @param width
     */
    public void setBorderWidth(float width) {
        mBorderPaint.setStrokeWidth(Utils.convertDpToPixel(width));
    }

    /**
     * Sets the color of the chart border lines.
     *
     * @param color
     */
    public void setBorderColor(int color) {
        mBorderPaint.setColor(color);
    }

    /**
     * Gets the minimum offset (padding) around the chart, defaults to 15.f
     */
    public float getMinOffset() {
        return mMinOffset;
    }

    /**
     * Sets the minimum offset (padding) around the chart, defaults to 15.f
     */
    public void setMinOffset(float minOffset) {
        mMinOffset = minOffset;
    }

    /**
     * Returns true if keeping the position on rotation is enabled and false if not.
     */
    public boolean isKeepPositionOnRotation() {
        return mKeepPositionOnRotation;
    }

    /**
     * Sets whether the chart should keep its position (zoom / scroll) after a rotation (orientation change)
     */
    public void setKeepPositionOnRotation(boolean keepPositionOnRotation) {
        mKeepPositionOnRotation = keepPositionOnRotation;
    }

    /**
     * Returns a recyclable MPPointD instance
     * Returns the x and y values in the chart at the given touch point
     * (encapsulated in a MPPointD). This method transforms pixel coordinates to
     * coordinates / values in the chart. This is the opposite method to
     * getPixelForValues(...).
     *
     * @param x
     * @param y
     * @return
     */
    public MPPointD getValuesByTouchPoint(float x, float y, YAxis.AxisDependency axis) {
        MPPointD result = MPPointD.getInstance(0, 0);
        getValuesByTouchPoint(x, y, axis, result);
        return result;
    }

    public void getValuesByTouchPoint(float x, float y, YAxis.AxisDependency axis, MPPointD outputPoint) {
        getTransformer(axis).getValuesByTouchPoint(x, y, outputPoint);
    }

    /**
     * Returns a recyclable MPPointD instance
     * Transforms the given chart values into pixels. This is the opposite
     * method to getValuesByTouchPoint(...).
     *
     * @param x
     * @param y
     * @return
     */
    public MPPointD getPixelForValues(float x, float y, YAxis.AxisDependency axis) {
        return getTransformer(axis).getPixelForValues(x, y);
    }

    /**
     * returns the Entry object displayed at the touched position of the chart
     *
     * @param x
     * @param y
     * @return
     */
    public Entry getEntryByTouchPoint(float x, float y) {
        Highlight h = getHighlightByTouchPoint(x, y);
        if (h != null) {
            return mData.getEntryForHighlight(h);
        }
        return null;
    }

    /**
     * returns the DataSet object displayed at the touched position of the chart
     *
     * @param x
     * @param y
     * @return
     */
    public IBarLineScatterCandleBubbleDataSet getDataSetByTouchPoint(float x, float y) {
        Highlight h = getHighlightByTouchPoint(x, y);
        if (h != null) {
            return mData.getDataSetByIndex(h.getDataSetIndex());
        }
        return null;
    }

    /**
     * buffer for storing lowest visible x point
     */
    protected MPPointD posForGetLowestVisibleX = MPPointD.getInstance(0, 0);

    /**
     * Returns the lowest x-index (value on the x-axis) that is still visible on
     * the chart.
     *
     * @return
     */
    @Override
    public float getLowestVisibleX() {
        return getLowestVisibleX(YAxis.AxisDependency.LEFT);
    }


    @Override
    public float getLowestVisibleX(YAxis.AxisDependency axis) {
        getTransformer(axis).getValuesByTouchPoint(mViewPortHandler.contentLeft(),
                mViewPortHandler.contentBottom(), posForGetLowestVisibleX);
        return (float) Math.max(mXAxis.mAxisMinimum, posForGetLowestVisibleX.x);
    }

    /**
     * buffer for storing highest visible x point
     */
    protected MPPointD posForGetHighestVisibleX = MPPointD.getInstance(0, 0);

    /**
     * Returns the highest x-index (value on the x-axis) that is still visible
     * on the chart.
     *
     * @return
     */
    @Override
    public float getHighestVisibleX() {
        return getHighestVisibleX(YAxis.AxisDependency.LEFT);
    }

    @Override
    public float getHighestVisibleX(YAxis.AxisDependency axis) {
        getTransformer(axis).getValuesByTouchPoint(mViewPortHandler.contentRight(),
                mViewPortHandler.contentBottom(), posForGetHighestVisibleX);
        return (float) Math.min(mXAxis.mAxisMaximum, posForGetHighestVisibleX.x);
    }

    /**
     * Returns the range visible on the x-axis.
     *
     * @return
     */
    public float getVisibleXRange() {
        return Math.abs(getHighestVisibleX() - getLowestVisibleX());
    }

    /**
     * returns the current x-scale factor
     */
    public float getScaleX() {
        if (mViewPortHandler == null)
            return 1f;
        else
            return mViewPortHandler.getScaleX();
    }

    /**
     * returns the current y-scale factor
     */
    public float getScaleY() {
        if (mViewPortHandler == null)
            return 1f;
        else
            return mViewPortHandler.getScaleY();
    }

    /**
     * if the chart is fully zoomed out, return true
     *
     * @return
     */
    public boolean isFullyZoomedOut() {
        return mViewPortHandler.isFullyZoomedOut();
    }

    /**
     * Returns the left y-axis object. In the horizontal bar-chart, this is the
     * top axis.
     *
     * @return
     */
    public YAxis getAxisLeft() {
        return mYAxisMap.get(YAxis.AxisDependency.LEFT);
    }


    /**
     * Returns the right y-axis object. In the horizontal bar-chart, this is the
     * bottom axis.
     *
     * @return
     */
    public YAxis getAxisRight() {
        return mAxisRight;
    }

    /**
     * Returns the y-axis object to the corresponding AxisDependency. In the
     * horizontal bar-chart, LEFT == top, RIGHT == BOTTOM
     *
     * @param axis
     * @return
     */
    public YAxis getAxis(YAxis.AxisDependency axis) {
        return findYAxisByDependency(axis);
    }

    @Override
    public boolean isInverted(YAxis.AxisDependency axis) {
        return getAxis(axis).isInverted();
    }

    /**
     * If set to true, both x and y axis can be scaled simultaneously with 2 fingers, if false,
     * x and y axis can be scaled separately. default: false
     *
     * @param enabled
     */
    public void setPinchZoom(boolean enabled) {
        mPinchZoomEnabled = enabled;
    }

    /**
     * returns true if pinch-zoom is enabled, false if not
     *
     * @return
     */
    public boolean isPinchZoomEnabled() {
        return mPinchZoomEnabled;
    }

    /**
     * Set an offset in dp that allows the user to drag the chart over it's
     * bounds on the x-axis.
     *
     * @param offset
     */
    public void setDragOffsetX(float offset) {
        mViewPortHandler.setDragOffsetX(offset);
    }

    /**
     * Set an offset in dp that allows the user to drag the chart over it's
     * bounds on the y-axis.
     *
     * @param offset
     */
    public void setDragOffsetY(float offset) {
        mViewPortHandler.setDragOffsetY(offset);
    }

    /**
     * Returns true if both drag offsets (x and y) are zero or smaller.
     *
     * @return
     */
    public boolean hasNoDragOffset() {
        return mViewPortHandler.hasNoDragOffset();
    }

    public XAxisRenderer getRendererXAxis() {
        return mXAxisRenderer;
    }

    /**
     * Sets a custom XAxisRenderer and overrides the existing (default) one.
     *
     * @param xAxisRenderer
     */
    public void setXAxisRenderer(XAxisRenderer xAxisRenderer) {
        mXAxisRenderer = xAxisRenderer;
    }




    public YAxisRenderer getRendererRightYAxis() {
        return mAxisRendererRight;
    }

    /**
     * Sets a custom axis renderer for the right acis and overwrites the existing one.
     *
     * @param rendererRightYAxis
     */
    public void setRendererRightYAxis(YAxisRenderer rendererRightYAxis) {
        mAxisRendererRight = rendererRightYAxis;
    }

    @Override
    public float getYChartMax() {
        float yAxisRightMax = mAxisRight.mAxisMaximum;
        float yAxisLeftMax = 0f;

        for (YAxis yAxis : mYAxisMap.values()) {
            yAxisLeftMax = yAxis.mAxisMaximum;
        }

        return Math.max(yAxisLeftMax,yAxisRightMax);
    }

    @Override
    public float getYChartMin() {
        float yAxisRightMin = mAxisRight.mAxisMinimum;
        float yAxisLeftMin = 0f;

        for (YAxis yAxis : mYAxisMap.values()) {
            yAxisLeftMin = yAxis.mAxisMinimum;
        }

        return Math.min(yAxisLeftMin, yAxisRightMin);
    }

    /**
     * Returns true if either the left or the right or both axes are inverted.
     *
     * @return
     */
    public boolean isAnyAxisInverted() {
        for (YAxis yAxis : mYAxisMap.values()){
            if (yAxis.isInverted()){
                return true;
            }
        }

        return mAxisRight.isInverted();
    }

    /**
     * Flag that indicates if auto scaling on the y axis is enabled. This is
     * especially interesting for charts displaying financial data.
     *
     * @param enabled the y axis automatically adjusts to the min and max y
     *                values of the current x axis range whenever the viewport
     *                changes
     */
    public void setAutoScaleMinMaxEnabled(boolean enabled) {
        mAutoScaleMinMaxEnabled = enabled;
    }

    /**
     * @return true if auto scaling on the y axis is enabled.
     * @default false
     */
    public boolean isAutoScaleMinMaxEnabled() {
        return mAutoScaleMinMaxEnabled;
    }

    @Override
    public void setPaint(Paint p, int which) {
        super.setPaint(p, which);

        switch (which) {
            case PAINT_GRID_BACKGROUND:
                mGridBackgroundPaint = p;
                break;
        }
    }

    @Override
    public Paint getPaint(int which) {
        Paint p = super.getPaint(which);
        if (p != null)
            return p;

        switch (which) {
            case PAINT_GRID_BACKGROUND:
                return mGridBackgroundPaint;
        }

        return null;
    }

    protected float[] mOnSizeChangedBuffer = new float[2];

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        // Saving current position of chart.
        mOnSizeChangedBuffer[0] = mOnSizeChangedBuffer[1] = 0;

        if (mKeepPositionOnRotation) {
            mOnSizeChangedBuffer[0] = mViewPortHandler.contentLeft();
            mOnSizeChangedBuffer[1] = mViewPortHandler.contentTop();

            for (YAxis yAxis : mYAxisMap.values()){
                getTransformer(yAxis.getAxisDependency()).pixelsToValue(mOnSizeChangedBuffer);
            }
        }

        //Superclass transforms chart.
        super.onSizeChanged(w, h, oldw, oldh);

        if (mKeepPositionOnRotation) {

            //Restoring old position of chart.
            for (YAxis yAxis : mYAxisMap.values()){
                getTransformer(yAxis.getAxisDependency()).pointValuesToPixel(mOnSizeChangedBuffer);
            }

            mViewPortHandler.centerViewPort(mOnSizeChangedBuffer, this);
        } else {
            mViewPortHandler.refresh(mViewPortHandler.getMatrixTouch(), this, true);
        }
    }

}
