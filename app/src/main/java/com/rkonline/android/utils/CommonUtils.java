package com.rkonline.android.utils;

import android.content.Context;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        long openMillis = parseTimeToMillis(openTimeStr);
        long closeMillis = parseTimeToMillis(closeTimeStr);

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
        long openMillis = parseTimeToMillis(openTime);
        long closeMillis = parseTimeToMillis(closeTime);

        if (now >= openMillis) {
            Toast.makeText(
                    context,
                    sangamType+" betting is closed after market open",
                    Toast.LENGTH_SHORT
            ).show();
            return true;
        }

        if (now > closeMillis) {
            Toast.makeText(context, "Market is closed now", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public static long getCurrentISTMillis() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
        return cal.getTimeInMillis();
    }
    public static long parseTimeToMillis(String dateTime) {
        try {
            SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            return sdf.parse(dateTime).getTime();
        } catch (Exception e) {
            return 0;
        }
    }

}
