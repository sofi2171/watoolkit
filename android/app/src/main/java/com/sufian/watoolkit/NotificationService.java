package com.sufian.watoolkit;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.content.Context;
import android.content.SharedPreferences;

public class NotificationService extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        if (packageName.equals("com.whatsapp") || packageName.equals("com.whatsapp.w4b")) {
            String title = sbn.getNotification().extras.getString("android.title");
            String text = sbn.getNotification().extras.getString("android.text");
            
            // Local storage mein save karein
            SharedPreferences pref = getSharedPreferences("DeletedMsgs", Context.MODE_PRIVATE);
            pref.edit().putString(String.valueOf(System.currentTimeMillis()), title + ": " + text).apply();
        }
    }
}
