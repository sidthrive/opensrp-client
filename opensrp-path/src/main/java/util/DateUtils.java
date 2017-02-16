package util;

import android.support.v4.util.TimeUtils;

import org.joda.time.DateTime;

import java.util.concurrent.TimeUnit;

/**
 * Created by keyman on 17/02/2017.
 */
public class DateUtils {

    public static String getDuration(DateTime dateTime) {
        if (dateTime != null) {
            long timeDiff = Math.abs(dateTime.getMillis() - DateTime.now().getMillis());
            return getDuration(timeDiff);
        }
        return null;
    }

    public static String getDuration(long timeDiff) {
        StringBuilder builder = new StringBuilder();
        TimeUtils.formatDuration(timeDiff, builder);
        String duration = "";
        if (timeDiff >= 0
                && timeDiff <= TimeUnit.MILLISECONDS.convert(13, TimeUnit.DAYS)) {
            // Represent in days
            long days = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
            duration = days + "d";
        } else if (timeDiff > TimeUnit.MILLISECONDS.convert(13, TimeUnit.DAYS)
                && timeDiff <= TimeUnit.MILLISECONDS.convert(97, TimeUnit.DAYS)) {
            // Represent in weeks and days
            int weeks = (int) Math.floor((float) timeDiff /
                    TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS));
            int days = Math.round((float) (timeDiff -
                    TimeUnit.MILLISECONDS.convert(weeks * 7, TimeUnit.DAYS)) /
                    TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));

            duration = weeks + "w";
            if (days > 0) {
                duration += " " + days + "d";
            }
        } else if (timeDiff > TimeUnit.MILLISECONDS.convert(97, TimeUnit.DAYS)
                && timeDiff <= TimeUnit.MILLISECONDS.convert(363, TimeUnit.DAYS)) {
            // Represent in months and weeks
            int months = (int) Math.floor((float) timeDiff /
                    TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));
            int weeks = Math.round((float) (timeDiff - TimeUnit.MILLISECONDS.convert(
                    months * 30, TimeUnit.DAYS)) /
                    TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS));

            duration = months + "m";
            if (weeks > 0) {
                duration += " " + weeks + "w";
            }
        } else {
            // Represent in years and months
            int years = (int) Math.floor((float) timeDiff
                    / TimeUnit.MILLISECONDS.convert(365, TimeUnit.DAYS));
            int months = Math.round((float) (timeDiff -
                    TimeUnit.MILLISECONDS.convert(years * 365, TimeUnit.DAYS)) /
                    TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));

            duration = years + "y";
            if (months > 0) {
                duration += " " + months + "m";
            }
        }

        return duration;

    }

}
