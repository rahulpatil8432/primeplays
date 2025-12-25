package com.rkonline.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

public class AppUpdateManager {

    private static final int INSTALL_PERMISSION_REQUEST_CODE = 1234;

    private static ProgressDialog progressDialog;
    private static ProgressBar progressBar;

    private static String APK_URL;
    private static long downloadId = -1;

    private static final Handler handler = new Handler();


    /* ================= CHECK UPDATE ================= */

    public static void checkForUpdate(Activity activity) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("app_update");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Integer latestVersion =
                        snapshot.child("latest_version_code")
                                .getValue(Integer.class);

                Boolean forceUpdate =
                        snapshot.child("force_update")
                                .getValue(Boolean.class);

                APK_URL =
                        snapshot.child("apk_url")
                                .getValue(String.class);
                constant.link = snapshot.child("apk_url")
                        .getValue(String.class);
                if (latestVersion == null ||
                        forceUpdate == null ||
                        APK_URL == null)
                    return;

                int currentVersion =
                        getCurrentVersionCode(activity);

                if (forceUpdate && latestVersion > currentVersion) {
                    showForceUpdateDialog(activity);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private static int getCurrentVersionCode(Context context) {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .versionCode;
        } catch (Exception e) {
            return 1;
        }
    }

    /* ================= UI ================= */

    private static void showForceUpdateDialog(Activity activity) {

        new AlertDialog.Builder(activity)
                .setTitle("Update Required")
                .setMessage("New version available. Update to continue.")
                .setCancelable(false)
                .setPositiveButton("UPDATE", (d, w) -> {
                    showProgressDialog(activity);
                    checkInstallPermission(activity);
                })
                .show();
    }

    private static void showProgressDialog(Activity activity) {

        progressBar = new ProgressBar(
                activity,
                null,
                android.R.attr.progressBarStyleHorizontal
        );
        progressBar.setMax(100);
        progressBar.setProgress(0);

        progressDialog = new ProgressDialog(activity);
        progressDialog  .setMessage("Please wait...");
        progressDialog .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog   .setIndeterminate(false);
        progressDialog .setMax(100);
        progressDialog.setCancelable(false);
        progressDialog .setProgress(0);
        progressDialog   .setTitle("Downloading Update");
        progressDialog   .setView(progressBar);
        progressDialog    .setCancelable(false);
        progressDialog    .create();

        progressDialog.show();
    }

    /* ================= PERMISSION ================= */

    private static void checkInstallPermission(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                !activity.getPackageManager()
                        .canRequestPackageInstalls()) {

            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + activity.getPackageName())
            );

            activity.startActivityForResult(
                    intent,
                    INSTALL_PERMISSION_REQUEST_CODE
            );
            return;
        }

        downloadApk(activity);
    }

    /* ================= DOWNLOAD ================= */

    private static void downloadApk(Activity activity) {

        Context appContext = activity.getApplicationContext();

        DownloadManager.Request request =
                new DownloadManager.Request(Uri.parse(APK_URL));

        request.setTitle("Downloading Update");
        request.setDescription("Please wait...");
        request.setNotificationVisibility(
                DownloadManager.Request
                        .VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "app_update.apk"
        );

        DownloadManager dm =
                (DownloadManager) appContext
                        .getSystemService(Context.DOWNLOAD_SERVICE);

        downloadId = dm.enqueue(request);

        trackDownloadProgress(activity, dm);
    }

    /* ================= PROGRESS TRACK ================= */

    private static void trackDownloadProgress(
            Activity activity,
            DownloadManager dm
    ) {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                DownloadManager.Query query =
                        new DownloadManager.Query();
                query.setFilterById(downloadId);

                Cursor cursor = dm.query(query);

                if (cursor != null && cursor.moveToFirst()) {

                    int downloaded =
                            cursor.getInt(
                                    cursor.getColumnIndexOrThrow(
                                            DownloadManager
                                                    .COLUMN_BYTES_DOWNLOADED_SO_FAR));

                    int total =
                            cursor.getInt(
                                    cursor.getColumnIndexOrThrow(
                                            DownloadManager
                                                    .COLUMN_TOTAL_SIZE_BYTES));

                    int status =
                            cursor.getInt(
                                    cursor.getColumnIndexOrThrow(
                                            DownloadManager
                                                    .COLUMN_STATUS));

                    if (total > 0) {
                        int progress =
                                (int) ((downloaded * 100L) / total);
                        progressBar.setProgress(progress);
                        Log.e("progress",progress+"");
                        progressDialog.setProgress(progress);
                        if(progress==100){
                            String localUri = cursor.getString(
                                    cursor.getColumnIndexOrThrow(
                                            DownloadManager.COLUMN_LOCAL_URI));

                            File apkFile = new File(Uri.parse(localUri).getPath());

                            Uri apkUri;

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                apkUri = FileProvider.getUriForFile(
                                        activity,
                                        "com.rkonline.android.provider",
                                        apkFile
                                );
                            } else {
                                apkUri = Uri.fromFile(apkFile);
                            }

                            Intent installIntent = new Intent(Intent.ACTION_VIEW);
                            installIntent.setDataAndType(
                                    apkUri,
                                    "application/vnd.android.package-archive");
                            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            activity.startActivity(installIntent);
                        }
                    }

                    if (status ==
                            DownloadManager.STATUS_SUCCESSFUL ||
                            status ==
                                    DownloadManager.STATUS_FAILED) {

                        cursor.close();
                        return;
                    }

                    cursor.close();
                }

                handler.postDelayed(this, 500);
            }
        }, 500);
    }

    /* ================= ACTIVITY RESULT ================= */

    public static void onActivityResult(
            Activity activity,
            int requestCode
    ) {

        if (requestCode ==
                INSTALL_PERMISSION_REQUEST_CODE) {

            if (activity.getPackageManager()
                    .canRequestPackageInstalls()) {

                showProgressDialog(activity);
                downloadApk(activity);

            } else {
                activity.finishAffinity();
            }
        }
    }

}
