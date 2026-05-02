package com.sufian.watoolkit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.JSObject;

public class MainActivity extends BridgeActivity {

    public static Uri folderUri = null;
    public static final int PICK_FOLDER = 999;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerPlugin(AppNativePlugin.class);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FOLDER && data != null) {
            folderUri = data.getData();
        }
    }

    @CapacitorPlugin(name = "AppNativePlugin")
    public static class AppNativePlugin extends Plugin {

        @PluginMethod
        public void pickFolder(PluginCall call) {
            try {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                getActivity().startActivityForResult(intent, PICK_FOLDER);
                call.resolve();
            } catch (Exception e) {
                call.reject("Error opening picker");
            }
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
