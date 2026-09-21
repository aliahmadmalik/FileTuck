package com.aliahmad.savetodownloads;

import org.junit.Test;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import static org.junit.Assert.*;

public class SaveTaskTest {
    @Test public void recreationRetainsOneCopyAndOnlyNotifiesNewListener() {
        ArrayDeque<Runnable> worker = new ArrayDeque<>(), ui = new ArrayDeque<>();
        int[] copies = {0};
        SaveTask task = new SaveTask(worker::add, ui::add, () -> {
            copies[0]++; return new SaveTask.Result(1, 1, "Downloads");
        });
        List<SaveTask.Result> oldResults = new ArrayList<>(), newResults = new ArrayList<>();
        Consumer<SaveTask.Result> old = oldResults::add, replacement = newResults::add;
        task.attach(old);
        task.start();
        task.detach(old);
        task.attach(replacement);
        task.start(); // Even accidental re-entry must not schedule a second copy.
        assertEquals(1, worker.size());
        worker.remove().run(); ui.remove().run();
        assertEquals(1, copies[0]);
        assertTrue(oldResults.isEmpty());
        assertEquals(1, newResults.size());
    }
    @Test public void completionBetweenActivitiesWaitsForNewListenerAndDeliversOnce() {
        ArrayDeque<Runnable> worker = new ArrayDeque<>(), ui = new ArrayDeque<>();
        SaveTask task = new SaveTask(worker::add, ui::add, () -> new SaveTask.Result(1, 2, "Folder"));
        task.start(); worker.remove().run(); ui.remove().run();
        List<SaveTask.Result> results = new ArrayList<>();
        task.attach(results::add); task.attach(results::add);
        assertEquals(1, results.size());
        assertEquals(2, results.get(0).total);
    }
    @Test public void oldListenerCannotDetachReplacement() {
        SaveTask task = new SaveTask(Runnable::run, Runnable::run, () -> new SaveTask.Result(1, 1, "Folder"));
        Consumer<SaveTask.Result> old = value -> fail("Old listener called");
        List<SaveTask.Result> results = new ArrayList<>();
        task.attach(old); task.attach(results::add); task.detach(old); task.start();
        assertEquals(1, results.size());
    }
}
