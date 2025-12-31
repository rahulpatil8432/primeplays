package com.rkonline.android.utils;

import android.content.SharedPreferences;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class WithdrawUtils {

    // Prevent instantiation
    private WithdrawUtils() {}

    /**
     * Check if current time is within allowed withdraw window (11 AM - 11 PM)
     */
    public static boolean isWithdrawAllowed(SharedPreferences preferences) {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        int startHour = preferences.getInt("withdrawStartHour", 11);
        int endHour = preferences.getInt("withdrawEndHour", 23);

        return currentHour < startHour || currentHour >= endHour;
    }

    /**
     * Fetch withdraw terms from Firestore and store in SharedPreferences
     * @param db Firestore instance
     * @param preferences SharedPreferences instance
     */
    public static void getTeamsAndConditions(FirebaseFirestore db, SharedPreferences preferences, Runnable onComplete) {
        SharedPreferences.Editor editor = preferences.edit();
        db.collection("app_config")
                .document("withdraw")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String terms = documentSnapshot.getString("terms");
                    if (terms == null || terms.isEmpty()) {
                        terms = "You cannot withdraw deposited money. You can only withdraw winning money.";
                    }
                    editor.putString("withdrawTerms", terms);

                    Long minAmountLong = documentSnapshot.getLong("minAmount");
                    int minAmount = minAmountLong != null ? minAmountLong.intValue() : 1000;
                    editor.putInt("minAmount", minAmount);

                    Long startHour = documentSnapshot.getLong("withdrawStartHour");
                    Long endHour = documentSnapshot.getLong("withdrawEndHour");
                    editor.putInt("withdrawStartHour", startHour != null ? startHour.intValue() : 11);
                    editor.putInt("withdrawEndHour", endHour != null ? endHour.intValue() : 23);

                    editor.apply();

                    if (onComplete != null) onComplete.run();
                })
                .addOnFailureListener(e -> {
                    editor.putString("withdrawTerms", "You cannot withdraw deposited money. You can only withdraw winning money.");
                    editor.putInt("withdrawStartHour", 11);
                    editor.putInt("withdrawEndHour", 23);
                    editor.apply();

                    if (onComplete != null) onComplete.run();
                });
    }

}
