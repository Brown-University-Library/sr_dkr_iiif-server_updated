package edu.illinois.library.cantaloupe.operation.redaction;

import edu.illinois.library.cantaloupe.image.Dimension;
import edu.illinois.library.cantaloupe.image.Rectangle;
import edu.illinois.library.cantaloupe.image.ScaleConstraint;
import edu.illinois.library.cantaloupe.operation.Crop;
import edu.illinois.library.cantaloupe.operation.CropByPixels;
import edu.illinois.library.cantaloupe.operation.OperationList;
import edu.illinois.library.cantaloupe.operation.ScaleByPercent;
import edu.illinois.library.cantaloupe.test.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RedactionTest extends BaseTest {

    private Redaction instance;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        instance = new Redaction(new Rectangle(50, 60, 200, 100));
    }

    @Test
    void equalsWithEqualInstances() {
        assertEquals(instance, new Redaction(new Rectangle(50, 60, 200, 100)));
    }

    @Test
    void equalsWithUnequalInstances() {
        assertNotEquals(instance, new Redaction(new Rectangle(51, 60, 200, 100)));
        assertNotEquals(instance, new Redaction(new Rectangle(50, 61, 200, 100)));
        assertNotEquals(instance, new Redaction(new Rectangle(50, 60, 201, 100)));
        assertNotEquals(instance, new Redaction(new Rectangle(50, 60, 200, 101)));
    }

    @Test
    void getResultingRegion() {
        Dimension sourceSize = new Dimension(500, 500);
        ScaleConstraint scaleConstraint = new ScaleConstraint(1, 1);

        // redaction within source image bounds
        Crop crop = new CropByPixels(0, 0, 300, 300);
        Rectangle resultingRegion = instance.getResultingRegion(
                sourceSize, scaleConstraint, crop);
        assertEquals(new Rectangle(50, 60, 200, 100), resultingRegion);

        // redaction partially within source image bounds
        crop = new CropByPixels(0, 0, 100, 100);
        resultingRegion = instance.getResultingRegion(
                sourceSize, scaleConstraint, crop);
        assertEquals(new Rectangle(50, 60, 200, 100), resultingRegion);

        // redaction outside source image bounds
        crop = new CropByPixels(300, 300, 100, 100);
        resultingRegion = instance.getResultingRegion(
                sourceSize, scaleConstraint, crop);
        assertEquals(new Rectangle(0, 0, 0, 0), resultingRegion);
    }

    @Test
    void getResultingRegionWithScaleConstraint() {
        Dimension sourceSize = new Dimension(1000, 1000);
        ScaleConstraint scaleConstraint = new ScaleConstraint(1, 2);

        // redaction within source image bounds
        Crop crop = new CropByPixels(0, 0, 300, 300);
        Rectangle resultingRegion = instance.getResultingRegion(
                sourceSize, scaleConstraint, crop);
        assertEquals(new Rectangle(50, 60, 200, 100), resultingRegion);

        // redaction partially within source image bounds
        crop = new CropByPixels(0, 0, 200, 200);
        resultingRegion = instance.getResultingRegion(
                sourceSize, scaleConstraint, crop);
        assertEquals(new Rectangle(50, 60, 200, 100), resultingRegion);

        // redaction outside source image bounds
        crop = new CropByPixels(300, 300, 100, 100);
        resultingRegion = instance.getResultingRegion(
                sourceSize, scaleConstraint, crop);
        assertEquals(new Rectangle(0, 0, 0, 0), resultingRegion);
    }

    @Test
    void getResultingSize() {
        Dimension fullSize = new Dimension(500, 500);

        // scale constraint 1:1
        ScaleConstraint scaleConstraint = new ScaleConstraint(1, 1);
        assertEquals(fullSize,
                instance.getResultingSize(fullSize, scaleConstraint));

        // scale constraint 1:2
        scaleConstraint = new ScaleConstraint(1, 2);
        assertEquals(new Dimension(500, 500),
                instance.getResultingSize(fullSize, scaleConstraint));
    }

    @Test
    void hasEffect() {
        assertTrue(instance.hasEffect());

        // zero width
        instance = new Redaction(new Rectangle(50, 60, 0, 100));
        assertFalse(instance.hasEffect());

        // zero height
        instance = new Redaction(new Rectangle(50, 60, 50, 0));
        assertFalse(instance.hasEffect());

        // null region
        instance = new Redaction(null);
        assertFalse(instance.hasEffect());
    }

    @Test
    void hasEffectWithArguments() {
        final Dimension fullSize = new Dimension(600, 400);

        // N.B.: hasEffect() shouldn't be looking at the Scales. They are
        // added to ensure that it doesn't.

        // in bounds
        OperationList opList = OperationList.builder()
                .withOperations(
                        new CropByPixels(0, 0, 400, 300),
                        new ScaleByPercent(0.25))
                .build();
        assertTrue(instance.hasEffect(fullSize, opList));

        // partially in bounds
        opList = OperationList.builder()
                .withOperations(
                        new CropByPixels(100, 100, 100, 100),
                        new ScaleByPercent(0.25))
                .build();
        assertTrue(instance.hasEffect(fullSize, opList));

        // out of bounds
        opList = OperationList.builder()
                .withOperations(
                        new CropByPixels(0, 0, 400, 300),
                        new ScaleByPercent(0.25))
                .build();
        instance = new Redaction(new Rectangle(420, 305, 20, 20));
        assertFalse(instance.hasEffect(fullSize, opList));
    }

    @Test
    void setRegionWhenInstanceIsFrozen() {
        instance.freeze();
        assertThrows(IllegalStateException.class,
                () -> instance.setRegion(new Rectangle(0, 0, 10, 10)));
    }

    @Test
    void toMap() {
        Dimension fullSize = new Dimension(500, 500);
        ScaleConstraint scaleConstraint = new ScaleConstraint(1, 1);

        Map<String,Object> map = instance.toMap(fullSize, scaleConstraint);
        assertEquals(instance.getClass().getSimpleName(), map.get("class"));
        assertEquals(50, map.get("x"));
        assertEquals(60, map.get("y"));
        assertEquals(200, map.get("width"));
        assertEquals(100, map.get("height"));
    }

    @Test
    void toMapReturnsUnmodifiableMap() {
        Dimension fullSize = new Dimension(100, 100);
        ScaleConstraint scaleConstraint = new ScaleConstraint(1, 1);
        Map<String,Object> map = instance.toMap(fullSize, scaleConstraint);
        assertThrows(UnsupportedOperationException.class,
                () -> map.put("test", "test"));
    }

    @Test
    void testToString() {
        assertEquals("50,60/200x100", instance.toString());
    }

}
