package com.aliahmad.savetodownloads;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Retained across configuration changes. All methods except work run on the UI thread. */
final class SaveTask {
    static final class Result {
        final int saved, total;
        final String destination;
        Result(int saved, int total, String destination) {
            this.saved = saved; this.total = total; this.destination = destination;
        }
    }
    private final Executor worker, ui;
    private final Supplier<Result> work;
    private Consumer<Result> listener;
    private Result result;
    private boolean started, delivered;

    SaveTask(Executor worker, Executor ui, Supplier<Result> work) {
        this.worker = worker; this.ui = ui; this.work = work;
    }
    void start() {
        if (started) return;
        started = true;
        worker.execute(() -> {
            Result completed = work.get();
            ui.execute(() -> { result = completed; deliver(); });
        });
    }
    void attach(Consumer<Result> listener) {
        this.listener = listener;
        deliver();
    }
    void detach(Consumer<Result> listener) {
        if (this.listener == listener) this.listener = null;
    }
    private void deliver() {
        if (result != null && listener != null && !delivered) {
            delivered = true;
            Consumer<Result> callback = listener;
            listener = null;
            callback.accept(result);
        }
    }
}
