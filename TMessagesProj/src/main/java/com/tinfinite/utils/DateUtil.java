package com.tinfinite.utils;

import org.telegram.android.LocaleController;
import org.telegram.messenger.R;

import java.util.Date;

/**
 * Created by caiying on 12/29/14.
 */
public class DateUtil {

    public static String lifeTime(Date date) {
        return lifeTime(date.getTime());
    }

    public static String lifeTime(long ts) {
        long l = (System.currentTimeMillis() - ts ) / 1000l;

        if (l < 60 * 60) {
            int j = (int) Math.max(l / 60L, 1L);
            StringBuilder sbminute = new StringBuilder(String.valueOf(j));
            return sbminute.toString() + (j == 1 ? LocaleController.getString("", R.string.MinuteAgo) : LocaleController.getString("", R.string.MinutesAgo));
        } else if (l <= 24 * 60 * 60) {
            int i = (int) (l / 3600L);
            StringBuilder sbhour = new StringBuilder(String.valueOf(i)).append(i == 1 ? LocaleController.getString("", R.string.HourAgo) : LocaleController.getString("", R.string.HoursAgo));
            return sbhour.toString();
        } else if (l <= 30 * 24 * 60 * 60) {
            Integer i = (int) (l / (24 * 3600L));
            return i + (i == 1 ? LocaleController.getString("", R.string.DayAgo) : LocaleController.getString("", R.string.DaysAgo));
        } else if (l <= 12 * 30 * 24 * 60 * 60) {
            Integer i = (int) (l / (12 * 24 * 3600L));
            return i + (i == 1 ? LocaleController.getString("", R.string.MonthAgo) : LocaleController.getString("", R.string.MonthsAgo));
        } else {
            return LocaleController.getString("", R.string.YearAgo);
        }
    }
}
