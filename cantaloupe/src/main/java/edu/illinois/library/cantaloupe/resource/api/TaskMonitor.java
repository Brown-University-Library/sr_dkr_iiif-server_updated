package edu.illinois.library.cantaloupe.resource.api;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Retains tasks submitted via {@link
 * edu.illinois.library.cantaloupe.resource.api.TasksResource} for observation
 * by {@link edu.illinois.library.cantaloupe.resource.api.TaskResource}.
 */
public final class TaskMonitor {

    private static TaskMonitor instance;

    private final List<APITask<?>> tasks = new CopyOnWriteArrayList<>();

    /**
     * @return Shared instance.
     */
    public static synchronized TaskMonitor getInstance() {
        if (instance == null) {
            instance = new TaskMonitor();
        }
        return instance;
    }

    static synchronized void clearInstance() {
        instance = null;
    }

    private TaskMonitor() {}

    void add(APITask<?> task) {
        tasks.add(task);
    }

    public APITask<?> get(final UUID uuid) {
        return tasks.stream()
                .filter(t -> t.getUUID().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return Immutable list of all tasks.
     */
    public List<APITask<?>> getAll() {
        return Collections.unmodifiableList(tasks);
    }

}
