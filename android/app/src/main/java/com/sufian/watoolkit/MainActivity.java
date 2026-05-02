package com.sufian.watoolkit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.JSObject;

public class MainActivity extends BridgeActivity {

    public static Uri folderUri = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerPlugin(AppNativePlugin.class);
    }

    @CapacitorPlugin(name = "AppNativePlugin")
    public static class AppNativePlugin extends Plugin {

        ActivityResultLauncher<Intent> picker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null) {
                    folderUri = result.getData().getData();
                }
            });

        @PluginMethod
        public void pickFolder(PluginCall call) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            picker.launch(intent);
            call.resolve();
        }

        @PluginMethod
        public void getFolder(PluginCall call) {
            JSObject ret = new JSObject();
            if (folderUri != null) {
                ret.put("uri", folderUri.toString());
                call.resolve(ret);
            } else {
                call.reject("No folder selected");
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
                call.reject("Error");
            }
        }
    }
}
