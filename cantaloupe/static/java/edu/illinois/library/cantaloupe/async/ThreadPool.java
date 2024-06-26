package edu.illinois.library.cantaloupe.async;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * Global application thread pool Singleton.
 */
public final class ThreadPool {

    public enum Priority {
        LOW, NORMAL, HIGH
    }

    private static abstract class AbstractThreadFactory {

        private static final int maxID = 9999999;
        private static final Random rng = new Random();

        private String getThreadID() {
            // Get a random number
            final int id = rng.nextInt(maxID + 1);
            // Left-pad it with zeroes
            return String.format("%06d", id);
        }

        abstract String getThreadNamePrefix();

        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName(getThreadNamePrefix() + "-" + getThreadID());
            thread.setDaemon(true);
            return thread;
        }
    }

    private static class LowPriorityThreadFactory
            extends AbstractThreadFactory implements ThreadFactory {
        @Override
        String getThreadNamePrefix() {
            return "work-lo";
        }
    }

    private static class NormalPriorityThreadFactory
            extends AbstractThreadFactory implements ThreadFactory {
        @Override
        String getThreadNamePrefix() {
            return "work-nm";
        }
    }

    private static class HighPriorityThreadFactory
            extends AbstractThreadFactory implements ThreadFactory {
        @Override
        String getThreadNamePrefix() {
            return "work-hi";
        }
    }

    private static ThreadPool instance;

    private boolean isShutdown = false;
    private final ExecutorService lowPriorityPool =
            Executors.newCachedThreadPool(new LowPriorityThreadFactory());
    private final ExecutorService normalPriorityPool =
            Executors.newCachedThreadPool(new NormalPriorityThreadFactory());
    private final ExecutorService highPriorityPool =
            Executors.newCachedThreadPool(new HighPriorityThreadFactory());

    /**
     * @return Shared instance.
     */
    public static synchronized ThreadPool getInstance() {
        if (instance == null || instance.isShutdown()) {
            instance = new ThreadPool();
        }
        return instance;
    }

    /**
     * For testing.
     */
    static synchronized void clearInstance() {
        instance.shutdown();
        instance = null;
    }

    private ThreadPool() {
    }

    public boolean isShutdown() {
        return isShutdown;
    }

    public void shutdown() {
        lowPriorityPool.shutdownNow();
        normalPriorityPool.shutdownNow();
        highPriorityPool.shutdownNow();
        isShutdown = true;
    }

    /**
     * Submits a task for immediate execution.
     */
    public Future<?> submit(Callable<?> task) {
        return submit(task, Priority.NORMAL);
    }

    /**
     * Submits a task for immediate execution.
     */
    public Future<?> submit(Callable<?> task, Priority priority) {
        switch (priority) {
            case LOW:
                return lowPriorityPool.submit(task);
            case HIGH:
                return highPriorityPool.submit(task);
            default:
                return normalPriorityPool.submit(task);
        }
    }

    /**
     * Submits a task for immediate execution.
     */
    public Future<?> submit(Runnable task) {
        return submit(task, Priority.NORMAL);
    }

    /**
     * Submits a task for immediate execution.
     */
    public Future<?> submit(Runnable task, Priority priority) {
        switch (priority) {
            case LOW:
                return lowPriorityPool.submit(task);
            case HIGH:
                return highPriorityPool.submit(task);
            default:
                return normalPriorityPool.submit(task);
        }
    }

}
