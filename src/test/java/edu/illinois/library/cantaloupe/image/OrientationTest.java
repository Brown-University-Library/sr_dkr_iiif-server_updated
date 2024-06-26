package edu.illinois.library.cantaloupe.image;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.library.cantaloupe.test.BaseTest;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

public class OrientationTest extends BaseTest {

    @Test
    void testSerialization() throws Exception {
        Orientation orientation = Orientation.ROTATE_270;
        try (StringWriter writer = new StringWriter()) {
            new ObjectMapper().writeValue(writer, orientation);
            assertEquals(Orientation.ROTATE_270.getEXIFValue(),
                    Integer.parseInt(writer.toString()));
        }
    }

    @Test
    void testDeserialization() throws Exception {
        Orientation orientation = new ObjectMapper().readValue("8",
                Orientation.class);
        assertSame(Orientation.ROTATE_270, orientation);
    }

    @Test
    void testForEXIFOrientation() {
        assertEquals(Orientation.ROTATE_0, Orientation.forEXIFOrientation(1));
        assertEquals(Orientation.ROTATE_180, Orientation.forEXIFOrientation(3));
        assertEquals(Orientation.ROTATE_90, Orientation.forEXIFOrientation(6));
        assertEquals(Orientation.ROTATE_270, Orientation.forEXIFOrientation(8));

        for (int i : new int[] { 0, 2, 4, 5, 7, 9 }) {
            assertThrows(IllegalArgumentException.class,
                    () -> Orientation.forEXIFOrientation(i));
        }
    }

    @Test
    void testAdjustedSize() {
        final Dimension size = new Dimension(100, 50);
        assertSame(size, Orientation.ROTATE_0.adjustedSize(size));
        assertEquals(new Dimension(50, 100), Orientation.ROTATE_90.adjustedSize(size));
        assertSame(size, Orientation.ROTATE_180.adjustedSize(size));
        assertEquals(new Dimension(50, 100), Orientation.ROTATE_270.adjustedSize(size));
    }

    @Test
    void testGetDegrees() {
        assertEquals(0, Orientation.ROTATE_0.getDegrees());
        assertEquals(90, Orientation.ROTATE_90.getDegrees());
        assertEquals(180, Orientation.ROTATE_180.getDegrees());
        assertEquals(270, Orientation.ROTATE_270.getDegrees());
    }

}
