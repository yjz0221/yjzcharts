package com.ludak.batterytestchart.util;

/**
 * 作者:cl
 * 创建日期：2024/11/30
 * 描述:demo工具
 */
public class BatteryUtils {


    public static String convertToHMS(long time) {
        long hour = 0;
        long minutes = 0;
        long sencond = 0;
        long temp = time % 3600;

        if (time > 3600) {
            hour = time / 3600;
            if (temp != 0) {
                if (temp > 60) {
                    minutes = temp / 60;
                    if (temp % 60 != 0) {
                        sencond = temp % 60;
                    }
                } else {
                    sencond = temp;
                }
            }
        } else {
            minutes = time / 60;
            if (time % 60 != 0) {
                sencond = time % 60;
            }
        }
        return (hour < 10 ? ("0" + hour) : hour) + ":" + (minutes < 10 ? ("0" + minutes) : minutes) + ":" + (sencond < 10 ? ("0" + sencond) : sencond);
    }
}
