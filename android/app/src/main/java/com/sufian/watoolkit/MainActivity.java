package com.sufian.watoolkit;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // STEP 1: پہلے کیپیسیٹر کا انجن سٹارٹ کریں
        super.onCreate(savedInstanceState);
        // STEP 2: پھر اپنا نیٹو پلگ ان لوڈ کریں
        registerPlugin(AppNativePlugin.class);
    }
}
