package com.sufian.watoolkit;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

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

            try {

                // Android 11+ Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                        call.reject("Permission required");
                        return;
                    }
                }

                String base = Environment.getExternalStorageDirectory().getAbsolutePath();

                String[] paths = {
                        base + "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses",
                        base + "/Android/media/com.whatsapp/WhatsApp/Media/.statuses",
                        base + "/WhatsApp/Media/.Statuses",
                        base + "/WhatsApp/Media/.statuses",
                        base + "/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses",
                        base + "/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.statuses",
                        base + "/WhatsApp Business/Media/.Statuses",
                        base + "/WhatsApp Business/Media/.statuses"
                };

                for (String p : paths) {
                    File dir = new File(p);

                    Log.d("STATUS_DEBUG", "Checking: " + p + " exists=" + dir.exists());

                    if (dir.exists() && dir.isDirectory()) {
                        File[] files = dir.listFiles();

                        if (files != null) {
                            for (File f : files) {
                                String name = f.getName().toLowerCase();

                                if (f.isFile() &&
                                        (name.endsWith(".jpg") ||
                                         name.endsWith(".jpeg") ||
                                         name.endsWith(".mp4"))) {

                                    statusArray.put(f.getAbsolutePath());
                                }
                            }
                        }
                    }
                }

                ret.put("statuses", statusArray);
                call.resolve(ret);

            } catch (Exception e) {
                call.reject(e.toString());
            }
        }

        @PluginMethod
        public void openNotificationSettings(PluginCall call) {
            try {
                Intent i = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(i);
                call.resolve();
            } catch (Exception e) {
                call.reject("Error opening settings");
            }
        }
    }
}
