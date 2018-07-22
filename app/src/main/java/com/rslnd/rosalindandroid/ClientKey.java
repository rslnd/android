package com.rslnd.rosalindandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.security.SecureRandom;
import static android.content.Context.MODE_PRIVATE;

class ClientKey {
    static final String TAG = "ClientKey";
    private final String PREFS_NAME = "RosalindAndroidClientKey";
    private final String PREFS_KEY = "clientKey";
    private final int minCharacters = 200;
    private Context context;

    ClientKey (Context context) {
        this.context = context;
    }

    protected String getKey() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedKey = prefs.getString(PREFS_KEY, null);
        if (savedKey == null || savedKey.length() < minCharacters) {
            return generateAndStoreKey();
        } else {
            return savedKey;
        }
    }

    private String generateAndStoreKey() {
        SecureRandom prng = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder();
        while (stringBuilder.length() < minCharacters){
            stringBuilder.append(Integer.toHexString(prng.nextInt()));
        }
        String generatedKey = stringBuilder.substring(0, minCharacters);

        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(PREFS_KEY, generatedKey);
        editor.apply();

        Log.i(TAG, "Generated and stored client key with min length " + minCharacters);

        return generatedKey;
    }
}
