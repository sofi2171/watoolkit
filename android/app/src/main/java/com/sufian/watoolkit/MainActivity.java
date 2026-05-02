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
        // JS ko Java se connect karne ke liye Plugins register karna lazmi hai
        registerPlugin(StatusBridge.class);
    }
}

// --- STATUS SAVER PLUGIN ---
@CapacitorPlugin(name = "StatusBridge")
class StatusBridge extends Plugin {
    @PluginMethod
    public void getStatuses(PluginCall call) {
        JSObject ret = new JSObject();
        JSONArray statusArray = new JSONArray();

        // Android 11+ "All Files Access" Check
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

        // WhatsApp Path Scan
        String path = Environment.getExternalStorageDirectory().toString() + "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses";
        File dir = new File(path);

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".jpg") || file.getName().endsWith(".mp4")) {
                        statusArray.put(file.getAbsolutePath());
                    }
                }
            }
        }
        ret.put("statuses", statusArray);
        call.resolve(ret);
    }
}
