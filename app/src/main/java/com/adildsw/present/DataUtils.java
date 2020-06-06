package com.adildsw.present;

import android.content.Context;
import android.content.SharedPreferences;

public final class DataUtils {

    private SharedPreferences sharedPreferences;

    DataUtils(Context context) {
        sharedPreferences = context.getSharedPreferences(
                "presentPreferences", Context.MODE_PRIVATE);
    }

    /**
     * Fetches the value of the given key from the "presentPreferences" SharedPreferences.
     *
     * @param key   holds the key whose value is to be fetched
     * @return      value of the key given
     */
    String readDataFromSP(String key) {
        return sharedPreferences.getString(key,"");
    }

    /**
     * Writes key-value pair in the "presentPreferences" SharedPreferences.
     *
     * @param key   holds the key whose value is to be written
     * @param value holds the value which is to be written
     */
    void writeDataInSP(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Clears content of the "presentPreferences" SharedPreferences.
     */
    void clearSP() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}
