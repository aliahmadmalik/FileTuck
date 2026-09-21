package com.aliahmad.savetodownloads;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private static final int PICK_FOLDER = 42;
    private SaveSettings settings;
    private LinearLayout content;
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        settings = new SaveSettings(this);
        render();
    }

    private void render() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(getColor(R.color.screen_background));
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(24), dp(24), dp(32));
        scroll.addView(content);
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        setContentView(scroll);
        scroll.requestApplyInsets();
        text(getString(R.string.version_header, versionName()), 13, getColor(R.color.text_secondary), true, 16);
        text(getString(R.string.headline), 32, getColor(R.color.text_primary), true, 12);
        text(getString(R.string.tagline), 16, getColor(R.color.text_secondary), false, 28);
        LinearLayout destination = new LinearLayout(this);
        destination.setOrientation(LinearLayout.VERTICAL);
        destination.setPadding(dp(20), dp(20), dp(20), dp(20));
        destination.setBackground(background(getColor(R.color.destination_background)));
        TextView caption = new TextView(this);
        caption.setText(getString(R.string.saving_to)); caption.setTextSize(12); caption.setTextColor(getColor(R.color.card_caption));
        destination.addView(caption);
        TextView path = new TextView(this);
        path.setText(settings.label()); path.setTextSize(22); path.setTextColor(getColor(R.color.card_text));
        path.setPadding(0, dp(10), 0, 0);
        destination.addView(path);
        content.addView(destination);
        text(getString(R.string.download_settings), 21, getColor(R.color.text_primary), true, 8).setPadding(0, dp(28), 0, 0);
        text(getString(R.string.choose_destination), 14, getColor(R.color.text_secondary), false, 16);
        option(SaveSettings.DOWNLOADS, getString(R.string.downloads), getString(R.string.downloads_description));
        option(SaveSettings.APP_FOLDER, getString(R.string.app_folder), getString(R.string.app_folder_description));
        option(SaveSettings.CUSTOM, getString(R.string.custom_folder), getString(R.string.custom_folder_description));
        text(getString(R.string.how_to_save), 19, getColor(R.color.text_primary), true, 8).setPadding(0, dp(24), 0, 0);
        text(getString(R.string.save_steps), 15, getColor(R.color.text_secondary), false, 18);
        text(getString(R.string.future_saves), 13, getColor(R.color.text_secondary), false, 0);
    }

    private String versionName() {
        try { return getPackageManager().getPackageInfo(getPackageName(), 0).versionName; }
        catch (android.content.pm.PackageManager.NameNotFoundException impossible) { return ""; }
    }

    private GradientDrawable background(int color) {
        GradientDrawable result = new GradientDrawable();
        result.setColor(color); result.setCornerRadius(dp(20)); return result;
    }
    private TextView text(String value, int size, int color, boolean bold, int bottom) {
        TextView view = new TextView(this);
        view.setText(value); view.setTextSize(size); view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(bottom);
        content.addView(view, params); return view;
    }
    private void option(String mode, String title, String description) {
        boolean selected = mode.equals(settings.mode());
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
        button.setText(getString(R.string.destination_option, selected ? "●" : "○", title, description));
        button.setTextSize(15); button.setTextColor(getColor(R.color.text_primary));
        button.setPadding(dp(18), dp(16), dp(18), dp(16));
        GradientDrawable bg = background(selected ? getColor(R.color.selected_background) : getColor(R.color.surface));
        bg.setStroke(dp(selected ? 2 : 1), selected ? getColor(R.color.selected_border) : getColor(R.color.border));
        button.setBackground(bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, -2);
        params.bottomMargin = dp(10); content.addView(button, params);
        button.setOnClickListener(view -> {
            if (SaveSettings.CUSTOM.equals(mode)) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                try { startActivityForResult(intent, PICK_FOLDER); }
                catch (android.content.ActivityNotFoundException exception) {
                    Toast.makeText(this, getString(R.string.no_folder_picker), Toast.LENGTH_LONG).show();
                }
            } else { settings.select(mode); render(); }
        });
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != PICK_FOLDER || result != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            if ((flags & Intent.FLAG_GRANT_WRITE_URI_PERMISSION) == 0) throw new SecurityException();
            getContentResolver().takePersistableUriPermission(uri,
                    (flags & Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0
                            ? Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            : Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            String name = getString(R.string.selected_folder);
            Uri document = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri));
            try (Cursor cursor = getContentResolver().query(document,
                    new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) name = cursor.getString(0);
            }
            if (name == null || name.trim().isEmpty()) name = getString(R.string.selected_folder);
            Uri old = settings.tree();
            settings.selectCustom(uri, name);
            if (old != null && !old.equals(uri)) {
                try { getContentResolver().releasePersistableUriPermission(old,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION); }
                catch (SecurityException ignored) { }
            }
            render();
        } catch (RuntimeException exception) {
            Toast.makeText(this, getString(R.string.folder_access_failed), Toast.LENGTH_LONG).show();
        }
    }
}
