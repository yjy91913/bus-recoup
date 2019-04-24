package com.ziroom.busrecoup.internal;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 日期utils
 *
 * @author liyingjie
 */
public class DateUtils {
    // 北京时间时区
    private static TimeZone TIMEZONE = TimeZone.getTimeZone("GMT+8");

    private DateUtils() {
    }

    /**
     * 格式化Date日期为 20160415112036 格式
     *
     * @param date 日期
     * @return long
     */
    public static long format2Long(Date date) {
        return format2Long(date, false);
    }

    /**
     * 格式化Date日期为 20160415112036 格式 或 20160415112036007 格式
     *
     * @param date           日期
     * @param hasMillisecond 是否包含毫秒
     * @return long
     */
    public static long format2Long(Date date, boolean hasMillisecond) {
        Calendar calendar = Calendar.getInstance(TIMEZONE);
        calendar.setTime(date);
        String year = String.format("%04d", calendar.get(Calendar.YEAR));
        String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        String day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
        String hour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
        String minute = String.format("%02d", calendar.get(Calendar.MINUTE));
        String second = String.format("%02d", calendar.get(Calendar.SECOND));
        String str = year + month + day + hour + minute + second;
        if (hasMillisecond) {
            String millisecond = String.format("%03d", calendar.get(Calendar.MILLISECOND));
            str = str.concat(millisecond);
        }
        return Long.parseLong(str);
    }

    /**
     * 当前时间格式化为 20160415112036 格式
     *
     * @return long
     */
    public static long formatNow2Long() {
        return format2Long(new Date());
    }
}
