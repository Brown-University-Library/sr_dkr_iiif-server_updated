package edu.illinois.library.cantaloupe.config;

import edu.illinois.library.cantaloupe.async.ThreadPool;
import edu.illinois.library.cantaloupe.util.FilesystemWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Watches the configuration file (if available) for changes.
 */
public final class ConfigurationFileWatcher {

    /**
     * Listens for changes to a configuration file and reloads it when it has
     * changed.
     */
    private static class FileChangeHandlerRunner implements Runnable {

        private static final Logger LOGGER =
                LoggerFactory.getLogger(FileChangeHandlerRunner.class);

        private Path file;
        private FilesystemWatcher filesystemWatcher;

        FileChangeHandlerRunner(Path file) {
            this.file = file;
        }

        @Override
        public void run() {
            try {
                Path path = file.getParent();
                filesystemWatcher = new FilesystemWatcher(path, new FileChangeHandler());
                filesystemWatcher.start();
            } catch (IOException e) {
                LOGGER.error("run(): {}", e.getMessage());
            }
        }

        void stop() {
            if (filesystemWatcher != null) {
                filesystemWatcher.stop();
            }
        }

    }

    private static final Set<FileChangeHandlerRunner> CHANGE_HANDLERS =
            ConcurrentHashMap.newKeySet();

    public static void startWatching() {
        final Configuration config = Configuration.getInstance();
        ((ConfigurationProvider) config).getWrappedConfigurations()
                .stream()
                .filter(c -> c instanceof FileConfiguration)
                .map(c -> (FileConfiguration) c)
                .forEach(c -> {
                    Optional<Path> file = c.getFile();
                    if (file.isPresent()) {
                        FileChangeHandlerRunner runner =
                                new FileChangeHandlerRunner(file.get());
                        CHANGE_HANDLERS.add(runner);
                        ThreadPool.getInstance().submit(runner, ThreadPool.Priority.LOW);
                    }
                });
    }

    public static void stopWatching() {
        CHANGE_HANDLERS.forEach(FileChangeHandlerRunner::stop);
        CHANGE_HANDLERS.clear();
    }

    private ConfigurationFileWatcher() {}

}
