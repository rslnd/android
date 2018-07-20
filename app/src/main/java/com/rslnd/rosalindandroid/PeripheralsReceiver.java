package com.rslnd.rosalindandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;
import java.util.HashMap;

import static android.content.Context.BATTERY_SERVICE;

public class PeripheralsReceiver extends BroadcastReceiver {
    public static final String TAG = "PeripheralsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) { return; }
        String action = intent.getAction();
        if (action == null) { return; }
        switch (action) {
            case Intent.ACTION_BATTERY_CHANGED:
                onBatteryChange(context, intent);
                break;
            default:
                Log.w(TAG, "Received unknown intent: " + intent.getAction());
        }
    }

    void onBatteryChange(Context context, Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        int batteryLevel = (bm != null) ? bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) : 1000;

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("isCharging", isCharging);
        payload.put("batteryLevel", batteryLevel);
        EventBroker.getInstance().emit("peripherals/batteryChange", payload);

        Log.i(TAG, "Battery state changed: " + status + " isCharging=" + isCharging + " batteryLevel=" + batteryLevel);
    }
}
