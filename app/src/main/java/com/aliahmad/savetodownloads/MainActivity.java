package com.aliahmad.savetodownloads;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
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
        scroll.setBackgroundColor(Color.rgb(246, 247, 251));
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
        text("FILETUCK  /  2.0", 13, 0xFF526078, true, 16);
        text("A home for every\nshared file.", 32, 0xFF172033, true, 12);
        text("Share it. Tuck it away. Find it right where you want it.", 16, 0xFF526078, false, 28);
        LinearLayout destination = new LinearLayout(this);
        destination.setOrientation(LinearLayout.VERTICAL);
        destination.setPadding(dp(20), dp(20), dp(20), dp(20));
        destination.setBackground(background(0xFF172033));
        TextView caption = new TextView(this);
        caption.setText("SAVING TO"); caption.setTextSize(12); caption.setTextColor(0xFFB9C6DD);
        destination.addView(caption);
        TextView path = new TextView(this);
        path.setText(settings.label()); path.setTextSize(22); path.setTextColor(Color.WHITE);
        path.setPadding(0, dp(10), 0, 0);
        destination.addView(path);
        content.addView(destination);
        text("Download settings", 21, 0xFF172033, true, 8).setPadding(0, dp(28), 0, 0);
        text("Choose where new shared files are saved.", 14, 0xFF526078, false, 16);
        option(SaveSettings.DOWNLOADS, "Downloads", "Save directly to your Downloads folder.");
        option(SaveSettings.APP_FOLDER, "Downloads / FileTuck", "Keep files together. Created automatically on your first save.");
        option(SaveSettings.CUSTOM, "Choose another folder", "Pick an existing folder or create one in the folder picker.");
        text("How to save", 19, 0xFF172033, true, 8).setPadding(0, dp(24), 0, 0);
        text("1.  Select one or more files in another app.\n2.  Tap Share, then Save with FileTuck.\n3.  Your files go to the folder selected above.", 15, 0xFF526078, false, 18);
        text("Changes apply to future saves. Existing files stay where they are.", 13, 0xFF526078, false, 0);
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
        button.setText((selected ? "●  " : "○  ") + title + "\n" + description);
        button.setTextSize(15); button.setTextColor(0xFF172033);
        button.setPadding(dp(18), dp(16), dp(18), dp(16));
        GradientDrawable bg = background(selected ? 0xFFE9EEFF : Color.WHITE);
        bg.setStroke(dp(selected ? 2 : 1), selected ? 0xFF526DE0 : 0xFFE0E5EF);
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
                    Toast.makeText(this, "No folder picker is available on this device.", Toast.LENGTH_LONG).show();
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
            String name = "Selected folder";
            Uri document = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri));
            try (Cursor cursor = getContentResolver().query(document,
                    new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) name = cursor.getString(0);
            }
            if (name == null || name.trim().isEmpty()) name = "Selected folder";
            Uri old = settings.tree();
            settings.selectCustom(uri, name);
            if (old != null && !old.equals(uri)) {
                try { getContentResolver().releasePersistableUriPermission(old,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION); }
                catch (SecurityException ignored) { }
            }
            render();
        } catch (RuntimeException exception) {
            Toast.makeText(this, "Could not access this folder. Please choose another folder.", Toast.LENGTH_LONG).show();
        }
    }
}
