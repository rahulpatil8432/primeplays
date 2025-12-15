package com.rkonline.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

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
    private static AlertDialog progressDialog;
    private static String APK_URL;

    private static final BroadcastReceiver downloadReceiver =
            new DownloadCompleteReceiver();

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

                if (latestVersion == null || forceUpdate == null || APK_URL == null)
                    return;

                int currentVersion = getCurrentVersionCode(activity);

                Log.d("UPDATE", "Current=" + currentVersion +
                        " Latest=" + latestVersion);

                if (forceUpdate && latestVersion > currentVersion) {
                    showForceUpdateDialog(activity);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
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
                .setMessage("A new version is available. Update to continue.")
                .setCancelable(false)
                .setPositiveButton("UPDATE", (d, w) -> {
                    showBlockingProgress(activity);
                    checkInstallPermission(activity);
                })
                .show();
    }

    private static void showBlockingProgress(Activity activity) {

        progressDialog = new AlertDialog.Builder(activity)
                .setTitle("Updating App")
                .setMessage("Please check notification and install new version.")
                .setCancelable(false)
                .create();

        progressDialog.show();
    }

    /* ================= PERMISSION ================= */

    private static void checkInstallPermission(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                !activity.getPackageManager().canRequestPackageInstalls()) {

            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + activity.getPackageName())
            );

            activity.startActivityForResult(
                    intent, INSTALL_PERMISSION_REQUEST_CODE);
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
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "app_update.apk");

        DownloadManager dm =
                (DownloadManager) appContext.getSystemService(
                        Context.DOWNLOAD_SERVICE);

        IntentFilter filter =
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(
                    downloadReceiver,
                    filter,
                    Context.RECEIVER_NOT_EXPORTED
            );
        } else {
            appContext.registerReceiver(downloadReceiver, filter);
        }

        Log.d("RECEIVER", "Receiver registered");

        long id = dm.enqueue(request);
        Log.d("DOWNLOAD", "Started id=" + id);
    }

    /* ================= ACTIVITY RESULT ================= */

    public static void onActivityResult(
            Activity activity, int requestCode) {

        if (requestCode == INSTALL_PERMISSION_REQUEST_CODE) {

            if (activity.getPackageManager()
                    .canRequestPackageInstalls()) {

                showBlockingProgress(activity);
                downloadApk(activity);

            } else {
                activity.finishAffinity();
            }
        }
    }
}
