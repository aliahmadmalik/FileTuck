package com.aliahmad.savetodownloads;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ShareReceiverActivity extends Activity {
    private SaveTask task;
    private final Consumer<SaveTask.Result> listener = this::showResult;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Object retained = getLastNonConfigurationInstance();
        if (retained instanceof SaveTask) {
            task = (SaveTask) retained;
            task.attach(listener);
            return;
        }
        if (savedInstanceState != null) {
            // The process was lost: do not restart a partially completed share and duplicate files.
            Toast.makeText(this, R.string.save_interrupted, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        List<Uri> files = collectSharedUris(getIntent());
        if (files.isEmpty()) {
            Toast.makeText(this, R.string.no_file_shared, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        task = createTask(files);
        task.attach(listener);
        task.start();
    }

    SaveTask createTask(List<Uri> files) {
        FileCopier copier = new FileCopier(getApplicationContext());
        Handler ui = new Handler(Looper.getMainLooper());
        return new SaveTask(command -> new Thread(command, "FileTuck-copy").start(),
                command -> ui.post(command), () -> copier.copy(files));
    }

    @Override protected void onDestroy() {
        if (task != null) {
            task.detach(listener);
            // Not being recreated: still report the outcome instead of dropping it silently.
            if (!isChangingConfigurations()) {
                Context app = getApplicationContext();
                task.attach(result -> toast(app, result));
            }
        }
        super.onDestroy();
    }
    @Override public Object onRetainNonConfigurationInstance() { return task; }

    @SuppressWarnings("deprecation")
    static List<Uri> collectSharedUris(Intent intent) {
        Set<Uri> uris = new LinkedHashSet<>();
        if (intent == null) return new ArrayList<>();
        try {
            String action = intent.getAction();
            if (!Intent.ACTION_SEND.equals(action) && !Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                return new ArrayList<>();
            }
            if (Intent.ACTION_SEND.equals(action)) {
                Parcelable stream = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                addUri(uris, stream);
            } else {
                ArrayList<?> streams = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (streams != null) for (Object stream : streams) addUri(uris, stream);
            }
            ClipData clip = intent.getClipData();
            if (clip != null) {
                for (int i = 0; i < clip.getItemCount(); i++) addUri(uris, clip.getItemAt(i).getUri());
            }
        } catch (RuntimeException malformedShare) {
            // An exported entry point must tolerate bad parcelables, extras, and ClipData.
            return new ArrayList<>();
        }
        return new ArrayList<>(uris);
    }
    private static void addUri(Set<Uri> uris, Object value) {
        if (value instanceof Uri && ContentResolver.SCHEME_CONTENT.equals(((Uri) value).getScheme())) {
            uris.add((Uri) value);
        }
    }
    private void showResult(SaveTask.Result result) {
        toast(this, result);
        finish();
    }
    static void toast(Context context, SaveTask.Result result) {
        String message;
        if (result.saved == result.total) {
            message = context.getResources().getQuantityString(R.plurals.files_saved,
                    result.saved, result.saved, result.destination);
        } else if (result.saved == 0) {
            message = context.getString(R.string.save_failed);
        } else {
            message = context.getResources().getQuantityString(R.plurals.files_partially_saved,
                    result.total, result.saved, result.total, result.destination);
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
