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
        registerPlugin(AppNativePlugin.class);
        super.onCreate(savedInstanceState);
    }

    @CapacitorPlugin(name = "AppNativePlugin")
    public static class AppNativePlugin extends Plugin {
        
        @PluginMethod
        public void getStatuses(PluginCall call) {
            JSObject ret = new JSObject();
            JSONArray statusArray = new JSONArray();
            try {
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

                String base = Environment.getExternalStorageDirectory().getAbsolutePath();
                String[] paths = {
                    base + "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses",
                    base + "/Android/media/com.whatsapp/WhatsApp/Media/.statuses",
                    base + "/WhatsApp/Media/.Statuses",
                    base + "/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses",
                    base + "/WhatsApp Business/Media/.Statuses"
                };

                for (String p : paths) {
                    File dir = new File(p);
                    if (dir.exists() && dir.isDirectory()) {
                        File[] files = dir.listFiles();
                        if (files != null) {
                            for (File f : files) {
                                String n = f.getName().toLowerCase();
                                if (f.isFile() && (n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".mp4"))) {
                                    statusArray.put(f.getAbsolutePath());
                                }
                            }
                        }
                    }
                }
                ret.put("statuses", statusArray);
                call.resolve(ret);
            } catch (Exception e) {
                call.reject(e.getMessage());
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
                call.reject("Native Error: Could not open settings");
            }
        }
    }
}
