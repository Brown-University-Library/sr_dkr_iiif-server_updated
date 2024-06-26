package edu.illinois.library.cantaloupe.image;

import edu.illinois.library.cantaloupe.test.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DimensionTest extends BaseTest {

    private static final double DELTA = 0.00000001;

    private Dimension instance;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        instance = new Dimension(1000, 800);
    }

    /* isPyramid() */

    @Test
    void testIsPyramidWithSingleLevel() {
        List<Dimension> levels = Collections.singletonList(new Dimension(500, 500));
        assertFalse(Dimension.isPyramid(levels));
    }

    @Test
    void testIsPyramidWithPyramidalLevels() {
        List<Dimension> levels = List.of(
                new Dimension(1000, 800),
                new Dimension(500, 400),
                new Dimension(250, 200),
                new Dimension(125, 100),
                new Dimension(63, 50),
                new Dimension(32, 25));
        assertTrue(Dimension.isPyramid(levels));
    }

    @Test
    void testIsPyramidWithNonPyramidalLevels() {
        List<Dimension> levels = List.of(
                new Dimension(1000, 800),
                new Dimension(1200, 600),
                new Dimension(900, 200));
        assertFalse(Dimension.isPyramid(levels));
    }

    /* ofScaledArea() */

    @Test
    void testOfScaledArea() {
        instance = new Dimension(1000, 800);
        assertEquals(new Dimension(500, 400),
                Dimension.ofScaledArea(instance, 200000));
    }

    @Test
    void testDoubleConstructor() {
        instance = new Dimension(5.5, 4.4);
        assertEquals(5.5, instance.width(), DELTA);
        assertEquals(4.4, instance.height(), DELTA);
    }

    @Test
    void testDoubleConstructorWithNegativeArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new Dimension(-5.5, -4.4));
    }

    @Test
    void testIntegerConstructor() {
        instance = new Dimension(5, 4);
        assertEquals(5, instance.width(), DELTA);
        assertEquals(4, instance.height(), DELTA);
    }

    @Test
    void testIntegerConstructorWithNegativeArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new Dimension(-5, -4));
    }

    @Test
    void testCopyConstructor() {
        Dimension other = new Dimension(instance);
        assertEquals(other, instance);
    }

    @Test
    void testArea() {
        assertEquals(800000, instance.area());
    }

    @Test
    void testEqualsWithEqualInstances() {
        assertEquals(instance, new Dimension(1000, 800));
    }

    @Test
    void testEqualsWithUnequalInstances() {
        assertNotEquals(instance, new Dimension(1001, 800));
        assertNotEquals(instance, new Dimension(1000, 801));
    }

    @Test
    void testHashCode() {
        int expected = Long.hashCode(Double.hashCode(instance.width()) +
                Double.hashCode(instance.height()));
        assertEquals(expected, instance.hashCode());
    }

    @Test
    void testWidth() {
        assertEquals(1000, instance.width(), DELTA);
    }

    @Test
    void testHeight() {
        assertEquals(800, instance.height(), DELTA);
    }

    @Test
    void testIntArea() {
        instance.setWidth(45.2);
        instance.setHeight(45.6);
        assertEquals(2061, instance.intArea());
    }

    @Test
    void testIntWidth() {
        instance.setWidth(45.2);
        assertEquals(45, instance.intWidth());

        instance.setWidth(45.6);
        assertEquals(46, instance.intWidth());
    }

    @Test
    void testIntHeight() {
        instance.setHeight(45.2);
        assertEquals(45, instance.intHeight());

        instance.setHeight(45.6);
        assertEquals(46, instance.intHeight());
    }

    @Test
    void testInvert() {
        instance.invert();
        assertEquals(800, instance.width(), DELTA);
        assertEquals(1000, instance.height(), DELTA);
    }

    @Test
    void testIsEmpty() {
        // width > 0.5, height > 0.5
        assertFalse(instance.isEmpty());

        // width < 0.5, height > 0.5
        instance = new Dimension(0.4, 0.6);
        assertTrue(instance.isEmpty());

        // width > 0.5, height < 0.5
        instance = new Dimension(0.6, 0.4);
        assertTrue(instance.isEmpty());

        // width < 0.5, height < 0.5
        instance = new Dimension(0.4, 0.4);
        assertTrue(instance.isEmpty());
    }

    @Test
    void testScale() {
        instance.scale(1.5);
        assertEquals(1500, instance.width(), DELTA);
        assertEquals(1200, instance.height(), DELTA);
    }

    @Test
    void testScaleX() {
        instance.scaleX(0.8);
        assertEquals(800, instance.width(), DELTA);
        assertEquals(800, instance.height(), DELTA);
    }

    @Test
    void testScaleXWithZeroArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> instance.scaleX(0));
    }

    @Test
    void testScaleXWithNegativeArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> instance.scaleX(-0.5));
    }

    @Test
    void testScaleY() {
        instance.scaleY(0.8);
        assertEquals(1000, instance.width(), DELTA);
        assertEquals(640, instance.height(), DELTA);
    }

    @Test
    void testScaleYWithZeroArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> instance.scaleY(0));
    }

    @Test
    void testScaleYWithNegativeArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> instance.scaleY(-0.5));
    }

    @Test
    void testSetWidthWithDouble() {
        instance.setWidth(2.0);
    }

    @Test
    void testSetWidthWithDoubleAndNegativeArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> instance.setWidth(-1.0));
    }

    @Test
    void testSetWidthWithInt() {
        instance.setWidth(5);
        assertEquals(5, instance.width(), DELTA);
    }

    @Test
    void testSetWidthWithIntAndNegativeArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> instance.setWidth(-1));
    }

    @Test
    void testSetHeightWithDouble() {
        instance.setHeight(5.0);
        assertEquals(5.0, instance.height(), DELTA);
    }

    @Test
    void testSetHeightWithDoubleAndNegativeArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> instance.setHeight(-2.0));
    }

    @Test
    void testSetHeightWithInt() {
        instance.setHeight(5);
        assertEquals(5, instance.height(), DELTA);
    }

    @Test
    void testSetHeightWithIntAndNegativeArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> instance.setHeight(-1));
    }

    @Test
    void testToString() {
        instance.setWidth(1000.5);
        instance.setHeight(800.5);
        assertEquals("1000.5x800.5", instance.toString());
    }

}