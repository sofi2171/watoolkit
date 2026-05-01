package com.sufian.watoolkit;

import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.json.JSONArray;

public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}

// Yeh Plugin Logic hai jo direct Statuses utha kar JS ko degi
@CapacitorPlugin(name = "StatusFetch")
class StatusFetchPlugin extends Plugin {

    @PluginMethod
    public void getWhatsAppStatuses(PluginCall call) {
        String path = Environment.getExternalStorageDirectory().toString() + 
                     "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses";
        
        File directory = new File(path);
        File[] files = directory.listFiles();
        JSObject ret = new JSObject();
        JSONArray statusArray = new JSONArray();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && (file.getName().endsWith(".jpg") || file.getName().endsWith(".mp4"))) {
                    statusArray.put(file.getAbsolutePath());
                }
            }
        }
        
        ret.put("statuses", statusArray);
        call.resolve(ret);
    }
}

