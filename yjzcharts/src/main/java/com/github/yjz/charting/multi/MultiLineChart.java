package com.github.yjz.charting.multi;

import android.content.Context;
import android.util.AttributeSet;

import com.github.yjz.charting.data.LineData;
import com.github.yjz.charting.interfaces.dataprovider.LineDataProvider;
import com.github.yjz.charting.multi.base.MultiYAXisBarLineChartBase;
import com.github.yjz.charting.renderer.LineChartRenderer;

/**
 * 作者:cl
 * 创建日期：2024/11/27
 * 描述:
 */
public class MultiLineChart extends MultiYAXisBarLineChartBase<LineData> implements LineDataProvider {

   public MultiLineChart(Context context) {
      super(context);
   }

   public MultiLineChart(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public MultiLineChart(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
   }

   @Override
   protected void init() {
      super.init();

      mRenderer = new LineChartRenderer(this, mAnimator, mViewPortHandler);
   }


   @Override
   public LineData getLineData() {
      return mData;
   }


   @Override
   protected void onDetachedFromWindow() {
      // releases the bitmap in the renderer to avoid oom error
      if (mRenderer != null && mRenderer instanceof LineChartRenderer) {
         ((LineChartRenderer) mRenderer).releaseBitmap();
      }

      super.onDetachedFromWindow();
   }
}
