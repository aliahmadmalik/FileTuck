package com.aliahmad.savetodownloads;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;

final class SaveSettings {
    static final String DOWNLOADS = "downloads", APP_FOLDER = "app_folder", CUSTOM = "custom";
    private final SharedPreferences prefs;
    private final Context context;

    SaveSettings(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences("save_settings", Context.MODE_PRIVATE);
    }

    String mode() { return prefs.getString("mode", DOWNLOADS); }
    Uri tree() {
        String value = prefs.getString("tree", null);
        return value == null ? null : Uri.parse(value);
    }
    void select(String mode) { prefs.edit().putString("mode", mode).apply(); }
    void selectCustom(Uri uri, String name) {
        prefs.edit().putString("mode", CUSTOM).putString("tree", uri.toString())
                .putString("name", name).apply();
    }
    String label() {
        if (CUSTOM.equals(mode())) return prefs.getString("name", "Selected folder");
        return APP_FOLDER.equals(mode()) ? "Downloads/" + context.getString(R.string.app_name) : "Downloads";
    }
    String relativePath() {
        return Environment.DIRECTORY_DOWNLOADS + (APP_FOLDER.equals(mode())
                ? "/" + context.getString(R.string.app_name) : "");
    }
}
