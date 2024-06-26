package edu.illinois.library.cantaloupe.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Filters out log messages with a level higher than INFO.
 */
public class StandardOutputLogFilter extends Filter<ILoggingEvent> {

    public FilterReply decide(ILoggingEvent event) {
        if (!isStarted()) {
            return FilterReply.NEUTRAL;
        }

        if (Level.ERROR.equals(event.getLevel()) ||
                Level.WARN.equals(event.getLevel())) {
            return FilterReply.DENY;
        }
        return FilterReply.NEUTRAL;
    }

}
