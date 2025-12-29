package com.rkonline.android.utils;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.rkonline.android.R;

public class AlertHelper {

    public static void showCustomAlert(Context context, String title, String message, @DrawableRes int imageRes, int color) {
        // Create dialog
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Set the window background to transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setContentView(LayoutInflater.from(context).inflate(R.layout.custom_alert_dialog, null));
        dialog.setCancelable(true);

        // Set Image
        ImageView alertImage = dialog.findViewById(R.id.alert_image);
        if (imageRes == 0) {
            alertImage.setImageResource(R.drawable.close_icon);
        }
        alertImage.setImageResource(imageRes);
        if(color == 0) color = R.color.md_red_900;
        alertImage.setColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.SRC_IN);
        // Set Title
        TextView alertTitle = dialog.findViewById(R.id.alert_title);
        alertTitle.setText(title);

        // Set Message
        TextView alertMessage = dialog.findViewById(R.id.alert_message);
        alertMessage.setText(message);

        // OK Button
        Button okButton = dialog.findViewById(R.id.alert_ok_button);
        okButton.setOnClickListener(v -> dialog.dismiss());

        // Show dialog
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int width = (int) (displayMetrics.widthPixels * 0.8);
            layoutParams.width = width;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

            window.setAttributes(layoutParams);
        }
    }
}
