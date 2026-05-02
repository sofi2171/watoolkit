package com.sufian.watoolkit;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // یہ لائن پہلے آنا لازمی ہے!
        registerPlugin(AppNativePlugin.class);
        super.onCreate(savedInstanceState);
    }
}
