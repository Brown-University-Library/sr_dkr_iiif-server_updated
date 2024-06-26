package edu.illinois.library.cantaloupe.delegate;

import org.slf4j.LoggerFactory;

/**
 * Logger for use by delegates.
 *
 * @since 3.4
 */
public final class Logger {

    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(DelegateProxy.class);

    public static void trace(String message) {
        LOGGER.trace(message);
    }

    public static void trace(String message, Throwable throwable) {
        LOGGER.trace(message, throwable);
    }

    public static void debug(String message) {
        LOGGER.debug(message);
    }

    public static void debug(String message, Throwable throwable) {
        LOGGER.debug(message, throwable);
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void info(String message, Throwable throwable) {
        LOGGER.info(message, throwable);
    }

    public static void warn(String message) {
        LOGGER.warn(message);
    }

    public static void warn(String message, Throwable throwable) {
        LOGGER.warn(message, throwable);
    }

    public static void error(String message) {
        LOGGER.error(message);
    }

    public static void error(String message, Throwable throwable) {
        LOGGER.error(message, throwable);
    }

    private Logger() {}

}
