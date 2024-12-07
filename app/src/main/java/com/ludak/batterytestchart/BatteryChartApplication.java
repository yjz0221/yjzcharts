package com.ludak.batterytestchart;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

/**
 * 作者:cl
 * 创建日期：2024/11/29
 * 描述:
 */
public class BatteryChartApplication extends Application {

   @Override
   protected void attachBaseContext(Context base) {
      super.attachBaseContext(base);
      MultiDex.install(base);
   }

   @Override
   public void onCreate() {
      super.onCreate();
   }
}
