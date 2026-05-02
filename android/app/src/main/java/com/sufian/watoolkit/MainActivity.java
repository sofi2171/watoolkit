package com.sufian.watoolkit;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.JSObject;
import org.json.JSONArray;
import java.io.File;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerPlugin(AppNativePlugin.class);
    }

    @CapacitorPlugin(name = "AppNativePlugin")
    public static class AppNativePlugin extends Plugin {
        
        @PluginMethod
        public void getStatuses(PluginCall call) {
            JSObject ret = new JSObject();
            JSONArray statusArray = new JSONArray();

            // Permission Check for Android 11+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                    call.reject("needs_permission");
                    return;
                }
            }

            // 4 Different Paths for Normal WA, Business WA, Android 11+ and Android 10-
            String[] possiblePaths = {
                Environment.getExternalStorageDirectory() + "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses",
                Environment.getExternalStorageDirectory() + "/WhatsApp/Media/.Statuses",
                Environment.getExternalStorageDirectory() + "/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses",
                Environment.getExternalStorageDirectory() + "/WhatsApp Business/Media/.Statuses"
            };

            for (String path : possiblePaths) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            // Sirf Asli Images aur Videos uthao
                            if (file.isFile() && (file.getName().endsWith(".jpg") || file.getName().endsWith(".mp4"))) {
                                statusArray.put(file.getAbsolutePath());
                            }
                        }
                    }
                }
            }

            ret.put("statuses", statusArray);
            call.resolve(ret);
        }

        @PluginMethod
        public void openNotificationSettings(PluginCall call) {
            try {
                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
                call.resolve();
            } catch (Exception e) {
                call.reject("Error");
            }
        }
    }
}
