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

    public static Uri pickedFolderUri = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerPlugin(AppNativePlugin.class);
    }

    @CapacitorPlugin(name = "AppNativePlugin")
    public static class AppNativePlugin extends Plugin {

        ActivityResultLauncher<Intent> folderPicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null) {
                    pickedFolderUri = result.getData().getData();
                }
            });

        @PluginMethod
        public void pickStatusFolder(PluginCall call) {
            try {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                folderPicker.launch(intent);
                call.resolve();
            } catch (Exception e) {
                call.reject("Error opening picker");
            }
        }

        @PluginMethod
        public void getPickedFolder(PluginCall call) {
            JSObject ret = new JSObject();
            if (pickedFolderUri != null) {
                ret.put("uri", pickedFolderUri.toString());
                call.resolve(ret);
            } else {
                call.reject("No folder selected");
            }
        }
    }
}
