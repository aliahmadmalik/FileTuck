package com.aliahmad.savetodownloads;

import android.content.ClipData;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.Parcelable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Arrays;
import java.util.Locale;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {29, 35})
public class ShareReceiverTest {
    public static class ControlledActivity extends ShareReceiverActivity {
        static ArrayDeque<Runnable> queue;
        static int created, copied;
        @Override SaveTask createTask(List<Uri> files) {
            created++;
            return new SaveTask(queue::add, Runnable::run, () -> {
                copied++; return new SaveTask.Result(1, 1, "Downloads");
            });
        }
    }
    @Test public void activityRecreationKeepsOneCopyAndDeliversToReplacement() {
        ControlledActivity.queue = new ArrayDeque<>();
        ControlledActivity.created = ControlledActivity.copied = 0;
        Intent share = new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, Uri.parse("content://example/file"));
        try (var controller = Robolectric.buildActivity(ControlledActivity.class, share).setup()) {
            ControlledActivity original = controller.get();
            controller.recreate();
            assertNotSame(original, controller.get());
            assertEquals(1, ControlledActivity.created);
            assertEquals(1, ControlledActivity.queue.size());
            ControlledActivity.queue.remove().run();
            assertEquals(1, ControlledActivity.copied);
            assertTrue(controller.get().isFinishing());
        }
    }
    @Test public void backgroundedShareStillReportsAndFinishes() {
        ControlledActivity.queue = new ArrayDeque<>();
        ControlledActivity.created = ControlledActivity.copied = 0;
        Intent share = new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, Uri.parse("content://example/file"));
        try (var controller = Robolectric.buildActivity(ControlledActivity.class, share).setup()) {
            controller.pause().stop();
            ControlledActivity.queue.remove().run();
            assertTrue(controller.get().isFinishing());
            assertNotNull(ShadowToast.getTextOfLatestToast());
        }
    }
    @Test public void destroyedShareStillReportsResult() {
        ControlledActivity.queue = new ArrayDeque<>();
        ControlledActivity.created = ControlledActivity.copied = 0;
        Intent share = new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, Uri.parse("content://example/file"));
        var controller = Robolectric.buildActivity(ControlledActivity.class, share).setup();
        String expected = controller.get().getResources()
                .getQuantityString(R.plurals.files_saved, 1, 1, "Downloads");
        controller.pause().stop().destroy();
        ControlledActivity.queue.remove().run();
        assertEquals(expected, ShadowToast.getTextOfLatestToast());
    }
    @Test public void rejectsWrongActionsAndRawPaths() {
        assertTrue(ShareReceiverActivity.collectSharedUris(null).isEmpty());
        Intent view = new Intent(Intent.ACTION_VIEW).putExtra(Intent.EXTRA_STREAM, Uri.parse("content://example/file"));
        assertTrue(ShareReceiverActivity.collectSharedUris(view).isEmpty());
        Intent file = new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///private/file"));
        assertTrue(ShareReceiverActivity.collectSharedUris(file).isEmpty());
    }
    @Test public void ignoresWrongParcelableType() {
        Intent intent = new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, new Bundle());
        assertTrue(ShareReceiverActivity.collectSharedUris(intent).isEmpty());
    }
    @Test public void rejectsBadParcelableWithoutCrashing() {
        Intent intent = new Intent(Intent.ACTION_SEND) {
            @Override public <T extends Parcelable> T getParcelableExtra(String name) {
                throw new BadParcelableException("Invalid sender payload");
            }
        };
        assertTrue(ShareReceiverActivity.collectSharedUris(intent).isEmpty());
    }
    @Test public void filtersMixedListsAndDeduplicatesClipData() {
        Uri uri = Uri.parse("content://example/shared");
        ArrayList<Parcelable> inputs = new ArrayList<>(Arrays.asList(uri, new Bundle(), null,
                Uri.parse("file:///private/file")));
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE).putParcelableArrayListExtra(Intent.EXTRA_STREAM, inputs);
        intent.setClipData(ClipData.newRawUri("file", uri));
        assertEquals(Arrays.asList(uri), ShareReceiverActivity.collectSharedUris(intent));
    }
    @Test public void restoredProcessDoesNotRestartShare() {
        Intent share = new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, Uri.parse("content://example/file"));
        try (var controller = Robolectric.buildActivity(ShareReceiverActivity.class, share).create(new Bundle())) {
            assertTrue(controller.get().isFinishing());
            assertEquals(controller.get().getString(R.string.save_interrupted), ShadowToast.getTextOfLatestToast());
        }
    }
    @Test public void appDirectoryDoesNotDependOnLocale() {
        var context = RuntimeEnvironment.getApplication();
        SaveSettings settings = new SaveSettings(context);
        settings.select(SaveSettings.APP_FOLDER);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(Locale.FRENCH);
        SaveSettings localized = new SaveSettings(context.createConfigurationContext(config));
        assertEquals(android.os.Environment.DIRECTORY_DOWNLOADS + "/FileTuck", localized.relativePath());
    }
}
