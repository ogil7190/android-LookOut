package com.mycampusdock.dock.utils;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mycampusdock.dock.R;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.mycampusdock.dock.Config.URL_BASE_MEDIA;

/**
 * Created by ogil on 14/01/18.
 */

public class NotiUtil {
    private Context mContext;

    public NotiUtil(Context mContext) {
        this.mContext = mContext;
    }

    public void showNotificationMessage(String title, String message, Intent intent) {
        showNotificationMessage(title, message, intent, null);
    }

    public void showNotificationMessage(final String title, final String message, Intent intent, String imageUrl) {
        if (TextUtils.isEmpty(message))
            return;

        final int icon = R.mipmap.ic_launcher_round;

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        0,
                        intent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );

        if (!TextUtils.isEmpty(imageUrl)) {
            Bitmap bitmap = getImageFromURL(imageUrl);
            if (bitmap != null) {
                showBigNotification(bitmap, icon, title, message, resultPendingIntent);
            } else {
                showSmallNotification(icon, title, message, resultPendingIntent);
            }
        } else {
            showSmallNotification(icon, title, message, resultPendingIntent);
        }
    }


    private void showSmallNotification(int icon, String title, String message, PendingIntent resultPendingIntent) {
        Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
        inboxStyle.addLine(message);
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder mBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("dock", "event", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            mBuilder = new Notification.Builder(mContext, channel.getId());
        } else
            mBuilder = new Notification.Builder(mContext);
        Notification notification;
        notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(Html.fromHtml(title))
                .setStyle(inboxStyle)
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher_new_round)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher_new_round))
                .setContentText(message)
                .build();

        notificationManager.notify(new Random().nextInt(Integer.MAX_VALUE), notification);
    }

    private void showBigNotification(Bitmap bitmap, int icon, String title, String message, PendingIntent resultPendingIntent) {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder mBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("dock", "notification", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            mBuilder = new Notification.Builder(mContext, channel.getId());
        } else
            mBuilder = new Notification.Builder(mContext);
        Notification.BigPictureStyle bigPictureStyle = new Notification.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        bigPictureStyle.setSummaryText(Html.fromHtml(message).toString());
        bigPictureStyle.bigPicture(bitmap);
        Notification notification;
        notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setStyle(bigPictureStyle)
                .setSmallIcon(R.mipmap.ic_launcher_new_round)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher_new_round))
                .setContentText(message)
                .build();
        notificationManager.notify(new Random().nextInt(Integer.MAX_VALUE), notification);
    }

    /**
     * Downloading push notification image before displaying it in
     * the notification tray
     */
    private static Bitmap image;

    public Bitmap getImageFromURL(final String strURL) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String imgUrl = URL_BASE_MEDIA + strURL;
                try {
                    File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Dock");
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                    folder = new File(folder, strURL);
                    if (folder.exists()) {
                        if (folder.length() > 0) {
                            image = BitmapFactory.decodeFile(folder.getAbsolutePath());
                        } else {
                            folder.delete();
                            try {
                                image = getBitmapFromURL(imgUrl);
                                FileOutputStream fos = new FileOutputStream(folder);
                                image.compress(Bitmap.CompressFormat.JPEG, 40, fos);
                                fos.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        try {
                            image = getBitmapFromURL(imgUrl);
                            FileOutputStream fos = new FileOutputStream(folder);
                            image.compress(Bitmap.CompressFormat.JPEG, 40, fos);
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).run();
        return image;
    }

    public interface OnBitmapDownloadListener {
        void onBitmapDownloaded(Bitmap bitmap);
    }

    private void getImageFromGlide(final String url, final OnBitmapDownloadListener listener) {
        GlideApp.with(mContext).asBitmap().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).load(URL_BASE_MEDIA + url).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Dock");
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                folder = new File(folder, url);
                if (folder.exists()) {
                    folder.delete();
                }
                try {
                    FileOutputStream fos = new FileOutputStream(folder);
                    resource.compress(Bitmap.CompressFormat.JPEG, 60, fos);
                    fos.close();
                    listener.onBitmapDownloaded(resource);
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onBitmapDownloaded(null);
                }
            }


        });
    }


    private Bitmap getBitmapFromURL(String imgUrl) throws Exception {
        Bitmap bitmap = Glide.with(mContext)
                .asBitmap()
                .load(imgUrl)
                .submit(1024, 1024)
                .get();
        return bitmap;
        /*URL url = new URL(imgUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        InputStream input = connection.getInputStream();
        Bitmap myBitmap = BitmapFactory.decodeStream(input);
        return myBitmap; */
    }

    /**
     * Method checks if the app is in background or not
     */
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    /* Clears notification tray messages */
    public static void clearNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static long getTimeMilliSec(String timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(timeStamp);
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}