package edu.illinois.library.cantaloupe.operation.overlay;

import edu.illinois.library.cantaloupe.config.Configuration;
import edu.illinois.library.cantaloupe.config.ConfigurationException;
import edu.illinois.library.cantaloupe.config.Key;
import edu.illinois.library.cantaloupe.image.Dimension;

abstract class BasicOverlayService {

    private int inset;
    private Position position;

    /**
     * @return Whether an overlay should be applied to an output image with
     * the given dimensions.
     */
    static boolean shouldApplyToImage(Dimension outputImageSize) {
        final Configuration config = Configuration.getInstance();
        final int minOutputWidth =
                config.getInt(Key.OVERLAY_OUTPUT_WIDTH_THRESHOLD, 0);
        final int minOutputHeight =
                config.getInt(Key.OVERLAY_OUTPUT_HEIGHT_THRESHOLD, 0);
        return (outputImageSize.width() >= minOutputWidth &&
                outputImageSize.height() >= minOutputHeight);
    }

    BasicOverlayService() throws ConfigurationException {
        readPosition();
        readInset();
    }

    /**
     * @return Overlay inset.
     */
    protected int getInset() {
        return inset;
    }

    /**
     * @return Overlay position.
     */
    protected Position getPosition() {
        return position;
    }

    public boolean isAvailable() {
        return Configuration.getInstance().
                getBoolean(Key.OVERLAY_ENABLED, false);
    }

    private void readInset() {
        inset = Configuration.getInstance().getInt(Key.OVERLAY_INSET, 0);
    }

    private void readPosition() throws ConfigurationException {
        final Configuration config = Configuration.getInstance();
        final String configValue = config.getString(Key.OVERLAY_POSITION, "");
        if (!configValue.isEmpty()) {
            try {
                position = Position.fromString(configValue);
            } catch (IllegalArgumentException e) {
                throw new ConfigurationException("Invalid " +
                        Key.OVERLAY_POSITION + " value: " + configValue);
            }
        } else {
            throw new ConfigurationException(Key.OVERLAY_POSITION +
                    " is not set.");
        }
    }

}
