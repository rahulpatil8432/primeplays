package com.rkonline.android;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
public class DownloadCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("Register","aaya");
        if (!DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction()))
            return;

        DownloadManager dm =
                (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        long downloadId =
                intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        try (Cursor cursor = dm.query(query)) {

            if (cursor == null || !cursor.moveToFirst())
                return;

            int status = cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            DownloadManager.COLUMN_STATUS));

            if (status == DownloadManager.STATUS_SUCCESSFUL) {

                String localUri = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DownloadManager.COLUMN_LOCAL_URI));

                File apkFile = new File(Uri.parse(localUri).getPath());

                Uri apkUri;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    apkUri = FileProvider.getUriForFile(
                            context,
                            context.getPackageName() + ".provider",
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

                context.startActivity(installIntent);
            }

        } catch (Exception e) {
            Log.e("Update", "Install failed", e);
        } finally {
            // ✅ ALWAYS unregister
            try {
                context.unregisterReceiver(this);
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
