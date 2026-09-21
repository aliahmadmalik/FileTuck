package com.aliahmad.savetodownloads;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ShareReceiverActivity extends Activity {
    private String destinationMode;
    private String destinationPath;
    private Uri destinationTree;
    private String destinationLabel;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SaveSettings settings = new SaveSettings(this);
        destinationMode = settings.mode();
        destinationPath = settings.relativePath();
        destinationTree = settings.tree();
        destinationLabel = settings.label();
        List<Uri> sharedFiles = collectSharedUris(getIntent());
        if (sharedFiles.isEmpty()) {
            Toast.makeText(this, R.string.no_file_shared, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        executor.execute(() -> {
            int saved = 0;
            for (Uri source : sharedFiles) {
                if (copyToDestination(source)) {
                    saved++;
                }
            }

            int savedCount = saved;
            runOnUiThread(() -> {
                showResult(savedCount, sharedFiles.size());
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    private List<Uri> collectSharedUris(Intent intent) {
        Set<Uri> uris = new LinkedHashSet<>();
        if (intent == null) return new ArrayList<>();
        String action = intent.getAction();
        if (!Intent.ACTION_SEND.equals(action) && !Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            return new ArrayList<>();
        }

        if (Intent.ACTION_SEND.equals(action)) {
            Uri stream = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (stream != null) {
                uris.add(stream);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            ArrayList<Uri> streams = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (streams != null) {
                uris.addAll(streams);
            }
        }

        ClipData clipData = intent.getClipData();
        if (clipData != null) {
            for (int index = 0; index < clipData.getItemCount(); index++) {
                Uri uri = clipData.getItemAt(index).getUri();
                if (uri != null) {
                    uris.add(uri);
                }
            }
        }

        // This activity is exported. Never let callers copy private app files via file://.
        uris.removeIf(uri -> uri == null || !ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()));
        return new ArrayList<>(uris);
    }

    private boolean copyToDestination(Uri source) {
        ContentResolver resolver = getContentResolver();
        Uri destination = null;
        boolean custom = SaveSettings.CUSTOM.equals(destinationMode);
        try {
            String mimeType = resolver.getType(source);
            if (mimeType == null || mimeType.trim().isEmpty()) mimeType = "application/octet-stream";
            String fileName = findDisplayName(source, mimeType);
            if (custom) {
                Uri tree = destinationTree;
                if (tree == null) throw new IOException("No destination selected");
                Uri parent = DocumentsContract.buildDocumentUriUsingTree(tree,
                        DocumentsContract.getTreeDocumentId(tree));
                destination = DocumentsContract.createDocument(resolver, parent, mimeType, fileName);
            } else {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, mimeType);
                values.put(MediaStore.Downloads.RELATIVE_PATH, destinationPath);
                values.put(MediaStore.Downloads.IS_PENDING, 1);
                destination = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            }
            if (destination == null) throw new IOException("Unable to create destination");
            try (InputStream input = openInputStream(source);
                 OutputStream output = resolver.openOutputStream(destination)) {
                if (input == null || output == null) throw new IOException("Unable to open shared file");
                byte[] buffer = new byte[32 * 1024];
                int count;
                while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            }
            if (!custom) {
                ContentValues completed = new ContentValues();
                completed.put(MediaStore.Downloads.IS_PENDING, 0);
                if (resolver.update(destination, completed, null, null) == 0)
                    throw new IOException("Unable to finish save");
            }
            return true;
        } catch (IOException | RuntimeException exception) {
            if (destination != null) {
                try {
                    if (custom) DocumentsContract.deleteDocument(resolver, destination);
                    else resolver.delete(destination, null, null);
                } catch (IOException | RuntimeException ignored) { }
            }
            return false;
        }
    }

    private InputStream openInputStream(Uri source) throws IOException {
        return getContentResolver().openInputStream(source);
    }

    private String findDisplayName(Uri source, String mimeType) {
        String name = null;
        try (Cursor cursor = getContentResolver().query(
                source,
                new String[]{OpenableColumns.DISPLAY_NAME},
                null,
                null,
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (column >= 0) {
                    name = cursor.getString(column);
                }
            }
        } catch (RuntimeException ignored) {
            // Some content providers do not implement metadata queries.
        }

        if (name == null || name.trim().isEmpty()) {
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            name = "shared_" + System.currentTimeMillis()
                    + (extension == null ? "" : "." + extension);
        }

        // A provider-supplied name must not be allowed to create a nested path.
        name = name.replace('/', '_').replace('\\', '_').trim();
        return name.isEmpty() ? "shared_" + System.currentTimeMillis() : name;
    }

    private void showResult(int saved, int total) {
        String message;
        if (saved == total) {
            message = getResources().getQuantityString(
                    R.plurals.files_saved, saved, saved, destinationLabel);
        } else if (saved == 0) {
            message = getString(R.string.save_failed);
        } else {
            message = getString(R.string.partially_saved, saved, total, destinationLabel);
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
