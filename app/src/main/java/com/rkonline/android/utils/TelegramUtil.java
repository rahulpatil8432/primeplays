package com.rkonline.android.utils;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.rkonline.android.constant;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public final class TelegramUtil {

    private static final String TAG = "TelegramUtil";

    private static final String CHAT_ID = "-1003721588864";

    private TelegramUtil() {}

    public static void sendMessage(String htmlMessage) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String urlString =
                        "https://api.telegram.org/bot" + constant.TelegramToken + "/sendMessage";
                Log.d(TAG, "sendMessage: "+constant.TelegramToken);
                String data =
                        "chat_id=" + URLEncoder.encode(CHAT_ID, "UTF-8") +
                                "&message_thread_id=" + 14 +
                                "&parse_mode=HTML" +
                                "&text=" + URLEncoder.encode(htmlMessage, "UTF-8");

                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded; charset=UTF-8"
                );

                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes("UTF-8"));
                os.flush();
                os.close();

                int code = conn.getResponseCode();
                String response = readResponse(conn, code);

                Log.d(TAG, "HTTP " + code + " → " + response);

            } catch (Exception e) {
                Log.e(TAG, "Telegram send failed", e);
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
    
    private static String readResponse(HttpURLConnection conn, int code) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            code >= 400 ? conn.getErrorStream() : conn.getInputStream()
                    )
            );
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return sb.toString();
        } catch (Exception e) {
            return "No response body";
        }
    }
}
