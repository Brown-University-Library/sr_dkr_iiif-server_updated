package edu.illinois.library.cantaloupe.operation.overlay;

import edu.illinois.library.cantaloupe.test.BaseTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PositionTest extends BaseTest {

    @Test
    void testFromString() {
        assertEquals(Position.TOP_LEFT, Position.fromString("top left"));
        assertEquals(Position.TOP_CENTER, Position.fromString("top center"));
        assertEquals(Position.TOP_RIGHT, Position.fromString("top right"));
        assertEquals(Position.LEFT_CENTER, Position.fromString("left center"));
        assertEquals(Position.CENTER, Position.fromString("center"));
        assertEquals(Position.RIGHT_CENTER, Position.fromString("right CENTER"));
        assertEquals(Position.BOTTOM_LEFT, Position.fromString("BOTTOM left"));
        assertEquals(Position.BOTTOM_CENTER, Position.fromString("bottom center"));
        assertEquals(Position.BOTTOM_RIGHT, Position.fromString("bottom right"));
        assertEquals(Position.TOP_LEFT, Position.fromString("left top"));
        assertEquals(Position.TOP_CENTER, Position.fromString("center top"));
        assertEquals(Position.TOP_RIGHT, Position.fromString("right top"));
        assertEquals(Position.REPEAT, Position.fromString("repeat"));
    }

    @Test
    void testToString() {
        assertEquals("N", Position.TOP_CENTER.toString());
        assertEquals("NE", Position.TOP_RIGHT.toString());
        assertEquals("E", Position.RIGHT_CENTER.toString());
        assertEquals("SE", Position.BOTTOM_RIGHT.toString());
        assertEquals("S", Position.BOTTOM_CENTER.toString());
        assertEquals("SW", Position.BOTTOM_LEFT.toString());
        assertEquals("W", Position.LEFT_CENTER.toString());
        assertEquals("NW", Position.TOP_LEFT.toString());
        assertEquals("C", Position.CENTER.toString());
        assertEquals("REPEAT", Position.REPEAT.toString());
    }

}
