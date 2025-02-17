package com.github.yjz.charting.multi.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Log;

import com.github.yjz.charting.components.LimitLine;
import com.github.yjz.charting.components.YAxis;
import com.github.yjz.charting.renderer.AxisRenderer;
import com.github.yjz.charting.utils.MPPointD;
import com.github.yjz.charting.utils.Transformer;
import com.github.yjz.charting.utils.Utils;
import com.github.yjz.charting.utils.ViewPortHandler;

import java.util.List;

import kotlin.Unit;


/**
 * 作者:cl
 * 创建日期：2024/9/11
 * 描述:
 */
public class MultiYAxisRenderer extends AxisRenderer {

    protected YAxis mYAxis;

    protected Paint mZeroLinePaint;

    protected Paint mYAxisNameUnitPaint;


    public MultiYAxisRenderer(ViewPortHandler viewPortHandler, YAxis yAxis, Transformer trans) {
        super(viewPortHandler, trans, yAxis);

        mYAxisNameUnitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        this.mYAxis = yAxis;

        if (mViewPortHandler != null) {

            mAxisLabelPaint.setColor(Color.BLACK);
            mAxisLabelPaint.setTextSize(Utils.convertDpToPixel(10f));

            mYAxisNameUnitPaint.setColor(Color.BLACK);
            mYAxisNameUnitPaint.setTextSize(Utils.convertDpToPixel(10f));

            mZeroLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mZeroLinePaint.setColor(Color.GRAY);
            mZeroLinePaint.setStrokeWidth(1f);
            mZeroLinePaint.setStyle(Paint.Style.STROKE);
        }
    }

    /**
     * draws the y-axis labels to the screen
     */
    @Override
    public void renderAxisLabels(Canvas c) {

        if (!mYAxis.isEnabled() || !mYAxis.isDrawLabelsEnabled())
            return;

        float[] positions = getTransformedPositions();

        mAxisLabelPaint.setTypeface(mYAxis.getTypeface());
        mAxisLabelPaint.setTextSize(mYAxis.getTextSize());
        mAxisLabelPaint.setColor(mYAxis.getTextColor());

        mYAxisNameUnitPaint.setTypeface(mYAxis.getTypeface());
        mYAxisNameUnitPaint.setTextSize(mYAxis.getTextSize() * 0.95f);
        mYAxisNameUnitPaint.setColor(mYAxis.getAxisLineColor());
        mYAxisNameUnitPaint.setTextAlign(Paint.Align.CENTER);


        float xoffset = mYAxis.getXOffset();
        float yoffset = Utils.calcTextHeight(mAxisLabelPaint, "A") / 2.5f + mYAxis.getYOffset();

        YAxis.YAxisLabelPosition labelPosition = mYAxis.getLabelPosition();

        float xPos = 0f;

        if (mYAxis.isAxisDependencyLeft()) {

            if (labelPosition == YAxis.YAxisLabelPosition.OUTSIDE_CHART) {
                mAxisLabelPaint.setTextAlign(Paint.Align.RIGHT);
                if (mYAxis.isDrawScale()) {
                    xPos = mViewPortHandler.offsetLeft() - xoffset - mYAxis.getYInXAxisOffset() - mYAxis.getLongScaleLineLength();
                } else {
                    xPos = mViewPortHandler.offsetLeft() - xoffset - mYAxis.getYInXAxisOffset();
                }

//                xPos = mViewPortHandler.offsetLeft() - xoffset - mYAxis.getYInXAxisOffset();

            } else {
                mAxisLabelPaint.setTextAlign(Paint.Align.LEFT);
                xPos = mViewPortHandler.offsetLeft() + xoffset + mYAxis.getYInXAxisOffset();
            }

        } else {

            if (labelPosition == YAxis.YAxisLabelPosition.OUTSIDE_CHART) {
                mAxisLabelPaint.setTextAlign(Paint.Align.LEFT);
                xPos = mViewPortHandler.contentRight() + xoffset;
            } else {
                mAxisLabelPaint.setTextAlign(Paint.Align.RIGHT);
                xPos = mViewPortHandler.contentRight() - xoffset;
            }
        }

        drawYLabels(c, xPos, positions, yoffset);
        drawYAxisNameUnit(c, mAxisLabelPaint.getFontSpacing());
    }

    @Override
    public void renderAxisLine(Canvas c) {

        if (!mYAxis.isEnabled() || !mYAxis.isDrawAxisLineEnabled())
            return;

        mAxisLinePaint.setColor(mYAxis.getAxisLineColor());
        mAxisLinePaint.setStrokeWidth(mYAxis.getAxisLineWidth());


        if (mYAxis.isAxisDependencyLeft()) {
            c.drawLine(
                    mViewPortHandler.contentLeft() - mYAxis.getYInXAxisOffset(),
                    mViewPortHandler.contentTop(),
                    mViewPortHandler.contentLeft() - mYAxis.getYInXAxisOffset(),
                    mViewPortHandler.contentBottom(),
                    mAxisLinePaint
            );
        } else {
            c.drawLine(
                    mViewPortHandler.contentRight(),
                    mViewPortHandler.contentTop(),
                    mViewPortHandler.contentRight(),
                    mViewPortHandler.contentBottom(),
                    mAxisLinePaint
            );
        }
    }

    /**
     * draws the y-labels on the specified x-position
     *
     * @param fixedPosition
     * @param positions
     */
    protected void drawYLabels(Canvas c, float fixedPosition, float[] positions, float offset) {

        final int from = mYAxis.isDrawBottomYLabelEntryEnabled() ? 0 : 1;
        final int to = mYAxis.isDrawTopYLabelEntryEnabled()
                ? mYAxis.mEntryCount
                : (mYAxis.mEntryCount - 1);

        // draw
        for (int i = from; i < to; i++) {
            String text = mYAxis.getFormattedLabel(i);

            c.drawText(text, fixedPosition, positions[i * 2 + 1] + offset, mAxisLabelPaint);
        }
    }

    protected void drawYAxisNameUnit(Canvas canvas, float offset) {
        String unitTxt = mYAxis.getYAxisUnit();
        String nameTxt = mYAxis.getYAxisName();
        String demoUnitTxt = "A";
        String demoNameTxt = "A";

        if (!TextUtils.isEmpty(unitTxt)) {
            demoUnitTxt = unitTxt;
        }

        if (!TextUtils.isEmpty(nameTxt)) {
            demoNameTxt = nameTxt.charAt(0) + "";
        }

        float startX = mViewPortHandler.contentLeft() - mYAxis.getYInXAxisOffset() - mYAxis.getNextYAxisDistance();
        float startY = mViewPortHandler.contentBottom() - offset / 2f;
        float txtWidth = Math.max(Utils.calcTextWidth(mYAxisNameUnitPaint, demoUnitTxt),
                Utils.calcTextWidth(mYAxisNameUnitPaint, demoNameTxt));
        float charHeight = mYAxisNameUnitPaint.getFontSpacing();
        int totalTxtLen = mYAxis.getYAxisName().length() + 1;


        float drawStartX = startX + Utils.convertDpToPixel(3f);
        float drawStartY = startY - totalTxtLen * charHeight;
        float drawWidth  = Math.max(txtWidth,mYAxis.getYNameBgWidth());
        float drawHeight = totalTxtLen * charHeight;


        RectF rectF = new RectF(0, 0, drawWidth * 1.2f, drawHeight * 1.1f);

        int restoreCount = canvas.save();
        canvas.translate(drawStartX, drawStartY);
        //绘制文字背景
        canvas.clipRect(rectF);
        canvas.drawColor(mYAxis.getYAxisNameUnitBgColor());

        //绘制单位
        if (!TextUtils.isEmpty(unitTxt)) {
            canvas.drawText(
                    unitTxt,
                    rectF.width() / 2f,
                    drawHeight + (rectF.height() - drawHeight) / 2f,
                    mYAxisNameUnitPaint);
        }


        //绘制名字
        if (!TextUtils.isEmpty(nameTxt)) {
            for (int i = 0; i < nameTxt.length(); i++) {
                canvas.drawText(
                        String.valueOf(nameTxt.charAt(i)),
                        rectF.width() / 2f,
                        drawHeight + (rectF.height() - drawHeight) / 2f - ((nameTxt.length() - i) * charHeight),
                        mYAxisNameUnitPaint
                );
            }
        }

        canvas.restoreToCount(restoreCount);
    }


    protected Path mRenderGridLinesPath = new Path();

    @Override
    public void renderGridLines(Canvas c) {

        if (!mYAxis.isEnabled())
            return;

        if (mYAxis.isDrawGridLinesEnabled()) {

            int clipRestoreCount = c.save();
            c.clipRect(getGridClippingRect());

            float[] positions = getTransformedPositions();

            mGridPaint.setColor(mYAxis.getGridColor());
            mGridPaint.setStrokeWidth(mYAxis.getGridLineWidth());
            mGridPaint.setPathEffect(mYAxis.getGridDashPathEffect());

            Path gridLinePath = mRenderGridLinesPath;
            gridLinePath.reset();

            // draw the grid
            for (int i = 0; i < positions.length; i += 2) {

                // draw a path because lines don't support dashing on lower android versions
                c.drawPath(linePath(gridLinePath, i, positions), mGridPaint);
                gridLinePath.reset();
            }

            c.restoreToCount(clipRestoreCount);
        }

        if (mYAxis.isDrawZeroLineEnabled()) {
            drawZeroLine(c);
        }
    }

    protected RectF mGridClippingRect = new RectF();

    public RectF getGridClippingRect() {
        mGridClippingRect.set(mViewPortHandler.getContentRect());
        mGridClippingRect.inset(0.f, -mAxis.getGridLineWidth());
        return mGridClippingRect;
    }

    /**
     * Calculates the path for a grid line.
     *
     * @param p
     * @param i
     * @param positions
     * @return
     */
    protected Path linePath(Path p, int i, float[] positions) {

        if (mYAxis.isAxisDependencyLeft()) {
            p.moveTo(mViewPortHandler.offsetLeft() - mYAxis.getYInXAxisOffset(), positions[i + 1]);
        } else {
            p.moveTo(mViewPortHandler.offsetLeft(), positions[i + 1]);
        }

        p.lineTo(mViewPortHandler.contentRight(), positions[i + 1]);

        return p;
    }

    protected float[] mGetTransformedPositionsBuffer = new float[2];

    /**
     * Transforms the values contained in the axis entries to screen pixels and returns them in form of a float array
     * of x- and y-coordinates.
     *
     * @return
     */
    protected float[] getTransformedPositions() {

        if (mGetTransformedPositionsBuffer.length != mYAxis.mEntryCount * 2) {
            mGetTransformedPositionsBuffer = new float[mYAxis.mEntryCount * 2];
        }
        float[] positions = mGetTransformedPositionsBuffer;

        for (int i = 0; i < positions.length; i += 2) {
            // only fill y values, x values are not needed for y-labels
            positions[i + 1] = mYAxis.mEntries[i / 2];
        }

        mTrans.pointValuesToPixel(positions);
        return positions;
    }

    protected Path mDrawZeroLinePath = new Path();
    protected RectF mZeroLineClippingRect = new RectF();

    /**
     * Draws the zero line.
     */
    protected void drawZeroLine(Canvas c) {

        int clipRestoreCount = c.save();
        mZeroLineClippingRect.set(mViewPortHandler.getContentRect());
        mZeroLineClippingRect.inset(0.f, -mYAxis.getZeroLineWidth());
        c.clipRect(mZeroLineClippingRect);

        // draw zero line
        MPPointD pos = mTrans.getPixelForValues(0f, 0f);

        mZeroLinePaint.setColor(mYAxis.getZeroLineColor());
        mZeroLinePaint.setStrokeWidth(mYAxis.getZeroLineWidth());

        Path zeroLinePath = mDrawZeroLinePath;
        zeroLinePath.reset();

        zeroLinePath.moveTo(mViewPortHandler.contentLeft(), (float) pos.y);
        zeroLinePath.lineTo(mViewPortHandler.contentRight(), (float) pos.y);

        // draw a path because lines don't support dashing on lower android versions
        c.drawPath(zeroLinePath, mZeroLinePaint);

        c.restoreToCount(clipRestoreCount);
    }

    protected Path mRenderLimitLines = new Path();
    protected float[] mRenderLimitLinesBuffer = new float[2];
    protected RectF mLimitLineClippingRect = new RectF();

    /**
     * Draws the LimitLines associated with this axis to the screen.
     *
     * @param c
     */
    @Override
    public void renderLimitLines(Canvas c) {

        List<LimitLine> limitLines = mYAxis.getLimitLines();

        if (limitLines == null || limitLines.size() <= 0)
            return;

        float[] pts = mRenderLimitLinesBuffer;
        pts[0] = 0;
        pts[1] = 0;
        Path limitLinePath = mRenderLimitLines;
        limitLinePath.reset();

        for (int i = 0; i < limitLines.size(); i++) {

            LimitLine l = limitLines.get(i);

            if (!l.isEnabled())
                continue;

            int clipRestoreCount = c.save();
            mLimitLineClippingRect.set(mViewPortHandler.getContentRect());
            mLimitLineClippingRect.inset(0.f, -l.getLineWidth());
            c.clipRect(mLimitLineClippingRect);

            mLimitLinePaint.setStyle(Paint.Style.STROKE);
            mLimitLinePaint.setColor(l.getLineColor());
            mLimitLinePaint.setStrokeWidth(l.getLineWidth());
            mLimitLinePaint.setPathEffect(l.getDashPathEffect());

            pts[1] = l.getLimit();

            mTrans.pointValuesToPixel(pts);

            limitLinePath.moveTo(mViewPortHandler.contentLeft(), pts[1]);
            limitLinePath.lineTo(mViewPortHandler.contentRight(), pts[1]);

            c.drawPath(limitLinePath, mLimitLinePaint);
            limitLinePath.reset();
            // c.drawLines(pts, mLimitLinePaint);

            String label = l.getLabel();

            // if drawing the limit-value label is enabled
            if (label != null && !label.equals("")) {

                mLimitLinePaint.setStyle(l.getTextStyle());
                mLimitLinePaint.setPathEffect(null);
                mLimitLinePaint.setColor(l.getTextColor());
                mLimitLinePaint.setTypeface(l.getTypeface());
                mLimitLinePaint.setStrokeWidth(0.5f);
                mLimitLinePaint.setTextSize(l.getTextSize());

                final float labelLineHeight = Utils.calcTextHeight(mLimitLinePaint, label);
                float xOffset = Utils.convertDpToPixel(4f) + l.getXOffset();
                float yOffset = l.getLineWidth() + labelLineHeight + l.getYOffset();

                final LimitLine.LimitLabelPosition position = l.getLabelPosition();

                if (position == LimitLine.LimitLabelPosition.RIGHT_TOP) {

                    mLimitLinePaint.setTextAlign(Paint.Align.RIGHT);
                    c.drawText(label,
                            mViewPortHandler.contentRight() - xOffset,
                            pts[1] - yOffset + labelLineHeight, mLimitLinePaint);

                } else if (position == LimitLine.LimitLabelPosition.RIGHT_BOTTOM) {

                    mLimitLinePaint.setTextAlign(Paint.Align.RIGHT);
                    c.drawText(label,
                            mViewPortHandler.contentRight() - xOffset,
                            pts[1] + yOffset, mLimitLinePaint);

                } else if (position == LimitLine.LimitLabelPosition.LEFT_TOP) {

                    mLimitLinePaint.setTextAlign(Paint.Align.LEFT);
                    c.drawText(label,
                            mViewPortHandler.contentLeft() + xOffset,
                            pts[1] - yOffset + labelLineHeight, mLimitLinePaint);

                } else {

                    mLimitLinePaint.setTextAlign(Paint.Align.LEFT);
                    c.drawText(label,
                            mViewPortHandler.offsetLeft() + xOffset,
                            pts[1] + yOffset, mLimitLinePaint);
                }
            }

            c.restoreToCount(clipRestoreCount);
        }
    }


    /**
     * 绘制Y轴刻度线
     */
    public void renderScaleLines(Canvas c) {
        if (!mYAxis.isDrawScale() || !mYAxis.isEnabled())
            return;


        mAxisScaleLinePaint.setColor(mYAxis.getAxisLineColor());

        float[] positions = getTransformedPositions();
        //因为 正常情况下 图表坐标轴在 左下方，所以此处 倒序 由下至上 绘制刻度
        for (int i = positions.length; i > 2; i -= 2) {
            float offset = (positions[i - 1] - positions[i - 3]) / 5; //偏移量
            drawScale(c, positions[i - 3], offset);
        }
    }

    protected void drawScale(Canvas canvas, float startY, float offset) {
        float leftX = mViewPortHandler.contentLeft() - mYAxis.getYInXAxisOffset(); //Y轴在左边的位置
        float rightX = mViewPortHandler.contentRight();//Y轴在右边的位置
        canvas.save();
        if (mYAxis.isAxisDependencyLeft()) { //Y轴位置在左边时
            for (int i = 0; i <= 5; i++) {
                canvas.save();
                canvas.translate(0, offset * i);
                if (i % 5 == 0) {
                    canvas.drawLine(leftX, startY, leftX - mYAxis.getLongScaleLineLength(), startY, mAxisScaleLinePaint);//画长刻度线
                } else {
                    canvas.drawLine(leftX, startY, leftX - mYAxis.getShortScaleLineLength(), startY, mAxisScaleLinePaint);//画短刻度线
                }
                canvas.restore();
            }
        }
        if (mYAxis.getAxisDependency() == YAxis.AxisDependency.RIGHT) { //Y轴位置在右边时
            for (int i = 0; i <= 5; i++) {
                canvas.save();
                canvas.translate(0, offset * i);
                if (i % 5 == 0) {
                    canvas.drawLine(rightX, startY, rightX - mYAxis.getLongScaleLineLength(), startY, mAxisScaleLinePaint);//画长刻度线
                } else {
                    canvas.drawLine(rightX, startY, rightX - mYAxis.getShortScaleLineLength(), startY, mAxisScaleLinePaint);//画短刻度线
                }
                canvas.restore();
            }
        }
        canvas.restore();
    }


}
