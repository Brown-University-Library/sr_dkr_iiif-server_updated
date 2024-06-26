package edu.illinois.library.cantaloupe.processor;

import edu.illinois.library.cantaloupe.config.Configuration;
import edu.illinois.library.cantaloupe.config.Key;
import edu.illinois.library.cantaloupe.test.BaseTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SelectionStrategyTest extends BaseTest {

    @Test
    void testFromConfiguration() {
        Configuration config = Configuration.getInstance();

        config.setProperty(Key.PROCESSOR_SELECTION_STRATEGY,
                "AutomaticSelectionStrategy");
        assertTrue(SelectionStrategy.fromConfiguration() instanceof AutomaticSelectionStrategy);

        config.setProperty(Key.PROCESSOR_SELECTION_STRATEGY,
                "ManualSelectionStrategy");
        assertTrue(SelectionStrategy.fromConfiguration() instanceof ManualSelectionStrategy);
    }

}
