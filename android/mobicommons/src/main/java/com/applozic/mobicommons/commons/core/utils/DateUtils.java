package com.applozic.mobicommons.commons.core.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by devashish on 28/11/14.
 */
public class DateUtils {

    public static boolean isSameDay(Long timestamp) {
        Calendar calendarForCurrent = Calendar.getInstance();
        Calendar calendarForScheduled = Calendar.getInstance();
        Date currentDate = new Date();
        Date date = new Date(timestamp);
        calendarForCurrent.setTime(currentDate);
        calendarForScheduled.setTime(date);
        return calendarForCurrent.get(Calendar.YEAR) == calendarForScheduled.get(Calendar.YEAR) &&
                calendarForCurrent.get(Calendar.DAY_OF_YEAR) == calendarForScheduled.get(Calendar.DAY_OF_YEAR);
    }

    public static String getFormattedDate(Long timestamp) {
        boolean sameDay = isSameDay(timestamp);
        Date date = new Date(timestamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd MMM");
        return sameDay ? simpleDateFormat.format(date) : fullDateFormat.format(date) + " " + simpleDateFormat.format(date);
    }
}
