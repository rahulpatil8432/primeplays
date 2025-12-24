package com.rkonline.android.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CommonUtils {


    public static boolean canPlaceBet(
            Context context,
            String selectedGameType,
            String openTimeStr,
            String closeTimeStr
    ) {

        long now = getCurrentISTMillis();
        long openMillis = getTimeInISTMillis(openTimeStr);
        long closeMillis = getTimeInISTMillis(closeTimeStr);

        // After market opens → OPEN not allowed
        if (now >= openMillis) {
            if ("Open".equalsIgnoreCase(selectedGameType)) {
                Toast.makeText(
                        context,
                        "OPEN betting is closed now. Please play CLOSE",
                        Toast.LENGTH_SHORT
                ).show();
                return false;
            }
        }
        Log.e("Cancel Time",openMillis +" "+closeMillis + " "+now);
        if (now > closeMillis) {
            Toast.makeText(context, "Market is closed now", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    public static boolean canPlaceSangamBet(Context context,
                                                String openTime,
                                                String closeTime,
                                                String sangamType) {

        long now = getCurrentISTMillis();
        long openMillis = getTimeInISTMillis(openTime);
        long closeMillis = getTimeInISTMillis(closeTime);
Log.d("sangam",openTime +" "+ closeTime + " "+ openMillis +" "+closeMillis + " "+ now);
        if (now >= openMillis) {
            Toast.makeText(
                    context,
                    sangamType+" betting is closed after market open",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        if (now > closeMillis) {
            Toast.makeText(context, "Market is closed now", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static long getCurrentISTMillis() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
        return cal.getTimeInMillis();
    }

    public static long getTimeInISTMillis(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

            Date parsedTime = sdf.parse(time);

            // Get today's date in IST
            Calendar today = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
            Calendar timeCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));

            timeCal.setTime(parsedTime);

            // Set today's date with parsed time
            today.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            today.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            return today.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


}
