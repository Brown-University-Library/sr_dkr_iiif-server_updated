package edu.illinois.library.cantaloupe.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Watches a directory for changes to files contained within.
 *
 * Based on <a href="http://docs.oracle.com/javase/tutorial/essential/io/notification.html">
 *     Watching a Directory for Changes</a>.
 */
public class FilesystemWatcher {

    public interface Callback {
        void created(Path path);
        void deleted(Path path);
        void modified(Path path);
    }

    private final Callback callback;
    private final Map<WatchKey,Path> keys;
    private final WatchService watcher;
    private volatile boolean shouldStop;

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    /**
     * Creates a {@link WatchService} and registers the given directory.
     */
    public FilesystemWatcher(Path dir, Callback callback) throws IOException {
        this.callback = callback;
        this.watcher  = FileSystems.getDefault().newWatchService();
        this.keys     = new HashMap<>();

        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE,
                ENTRY_MODIFY);
        keys.put(key, dir);
    }

    /**
     * Starts watching for changes. This will block, so is normally invoked in
     * a separate thread.
     */
    public void start() {
        while (!shouldStop) {
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException e) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name           = ev.context();
                Path child          = dir.resolve(name);

                switch (kind.name()) {
                    case "ENTRY_CREATE":
                        callback.created(child);
                        break;
                    case "ENTRY_DELETE":
                        callback.deleted(child);
                        break;
                    case "ENTRY_MODIFY":
                        callback.modified(child);
                        break;
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    public void stop() {
        shouldStop = true;
    }

}