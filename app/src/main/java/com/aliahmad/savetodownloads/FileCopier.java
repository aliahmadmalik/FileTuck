package com.aliahmad.savetodownloads;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/** A destination snapshot using only the application context, never an Activity. */
final class FileCopier {
    private final Context context;
    private final String destinationMode, destinationPath, destinationLabel;
    private final Uri destinationTree;

    FileCopier(Context context) {
        this.context = context.getApplicationContext();
        SaveSettings settings = new SaveSettings(this.context);
        destinationMode = settings.mode();
        destinationPath = settings.relativePath();
        destinationTree = settings.tree();
        destinationLabel = settings.label();
    }

    SaveTask.Result copy(List<Uri> sources) {
        int saved = 0;
        for (Uri source : sources) if (copyToDestination(source)) saved++;
        return new SaveTask.Result(saved, sources.size(), destinationLabel);
    }

    private boolean copyToDestination(Uri source) {
        ContentResolver resolver = context.getContentResolver();
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
                byte[] buffer = new byte[128 * 1024];
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
        return context.getContentResolver().openInputStream(source);
    }

    private String findDisplayName(Uri source, String mimeType) {
        String name = null;
        try (Cursor cursor = context.getContentResolver().query(
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

}
