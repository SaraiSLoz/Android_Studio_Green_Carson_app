package com.example.bit_3;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {
    private static final String PREFERENCES_NAME = "MyPreferences";
    private static final String KEY_USER_UID = "userUid";

    // Método para guardar el UID del usuario en SharedPreferences
    public static void saveUserUidToSharedPreferences(Context context, String uid) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER_UID, uid);
        editor.apply();
    }

    // Método para obtener el UID del usuario desde SharedPreferences
    public static String getUserUidFromSharedPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_USER_UID, null);
    }
}
