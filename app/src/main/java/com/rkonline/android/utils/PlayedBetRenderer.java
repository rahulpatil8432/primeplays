package com.rkonline.android.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.rkonline.android.R;

import java.util.List;
import java.util.Map;

public final class PlayedBetRenderer {

    private PlayedBetRenderer() {}


    public static void renderFixedAmount(
            Context context,
            List<String> numbers,
            String amount,
            LinearLayout headerRow,
            LinearLayout container,
            ScrollView scrollView
    ) {

        container.removeAllViews();

        if (numbers == null || numbers.isEmpty()) {
            hide(headerRow, container, scrollView);
            return;
        }

        show(headerRow, container, scrollView);

        boolean alternate = false;

        for (String num : numbers) {
            LinearLayout row = createRow(context, alternate);
            alternate = !alternate;

            row.addView(createCell(context, num));
            row.addView(createCell(context, amount));

            container.addView(row);
        }
    }


    public static void renderVariableAmount(
            Context context,
            Map<String, String> amountMap,
            LinearLayout headerRow,
            LinearLayout container,
            ScrollView scrollView
    ) {

        container.removeAllViews();

        boolean hasData = false;
        boolean alternate = false;

        for (String num : amountMap.keySet()) {
            String amt = amountMap.get(num);
            if (TextUtils.isEmpty(amt)) continue;

            hasData = true;

            LinearLayout row = createRow(context, alternate);
            alternate = !alternate;

            row.addView(createCell(context, num));
            row.addView(createCell(context, amt));

            container.addView(row);
        }

        if (hasData) {
            show(headerRow, container, scrollView);
        } else {
            hide(headerRow, container, scrollView);
        }
    }


    private static LinearLayout createRow(Context context, boolean alternate) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(4, 4, 4, 4);
        row.setBackgroundColor(
                context.getResources().getColor(
                        alternate ? R.color.md_grey_100 : R.color.md_white_1000
                )
        );
        return row;
    }

    private static TextView createCell(Context context, String text) {
        TextView tv = new TextView(context);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        ));
        tv.setGravity(Gravity.CENTER);
        tv.setText(text);
        return tv;
    }

    private static void show(View... views) {
        for (View v : views) v.setVisibility(View.VISIBLE);
    }

    private static void hide(View... views) {
        for (View v : views) v.setVisibility(View.GONE);
    }
}
