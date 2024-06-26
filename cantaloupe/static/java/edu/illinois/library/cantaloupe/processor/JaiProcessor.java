package edu.illinois.library.cantaloupe.processor;

import edu.illinois.library.cantaloupe.image.Dimension;
import edu.illinois.library.cantaloupe.image.Info;
import edu.illinois.library.cantaloupe.image.Metadata;
import edu.illinois.library.cantaloupe.image.Orientation;
import edu.illinois.library.cantaloupe.image.ScaleConstraint;
import edu.illinois.library.cantaloupe.operation.ColorTransform;
import edu.illinois.library.cantaloupe.image.Format;
import edu.illinois.library.cantaloupe.operation.Encode;
import edu.illinois.library.cantaloupe.operation.Operation;
import edu.illinois.library.cantaloupe.operation.OperationList;
import edu.illinois.library.cantaloupe.operation.ReductionFactor;
import edu.illinois.library.cantaloupe.operation.Rotate;
import edu.illinois.library.cantaloupe.operation.Scale;
import edu.illinois.library.cantaloupe.operation.Crop;
import edu.illinois.library.cantaloupe.operation.Sharpen;
import edu.illinois.library.cantaloupe.operation.Transpose;
import edu.illinois.library.cantaloupe.operation.overlay.Overlay;
import edu.illinois.library.cantaloupe.image.Compression;
import edu.illinois.library.cantaloupe.processor.codec.ImageReader;
import edu.illinois.library.cantaloupe.processor.codec.ImageReaderFactory;
import edu.illinois.library.cantaloupe.processor.codec.ImageWriter;
import edu.illinois.library.cantaloupe.processor.codec.ImageWriterFactory;
import edu.illinois.library.cantaloupe.processor.codec.ReaderHint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.jai.Interpolation;
import javax.media.jai.RenderedOp;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * <p>Processor using the Java Advanced Imaging (JAI) library.</p>
 *
 * <p>Because they both use Image I/O, this processor has a lot in common with
 * {@link Java2dProcessor} and so common functionality has been extracted into
 * a base class.</p>
 *
 * @deprecated Since version 4.0.
 * @see <a href="http://docs.oracle.com/cd/E19957-01/806-5413-10/806-5413-10.pdf">
 *     Programming in Java Advanced Imaging</a>
 */
@Deprecated
class JaiProcessor extends AbstractImageIOProcessor
        implements FileProcessor, StreamProcessor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(JaiProcessor.class);

    /**
     * Override that disables support for GIF source images.
     */
    @Override
    public Set<Format> getAvailableOutputFormats() {
        Set<Format> formats;
        if (Format.get("gif").equals(getSourceFormat())) {
            formats = Collections.emptySet();
        } else {
            formats = super.getAvailableOutputFormats();
        }
        return formats;
    }

    @Override
    public boolean supportsSourceFormat(Format format) {
        return ImageReaderFactory.supportedFormats().
                stream().
                filter(f -> !Format.get("gif").equals(f)).
                anyMatch(f -> f.equals(format));
    }

    @Override
    public void process(final OperationList opList,
                        final Info info,
                        final OutputStream outputStream)
            throws ProcessorException, FormatException {
        super.process(opList, info, outputStream);

        ImageReader reader = null;
        try {

            reader                        = getReader();
            final Format outputFormat     = opList.getOutputFormat();
            final Metadata metadata       = info.getMetadata();
            final Orientation orientation = metadata.getOrientation();
            final Dimension fullSize      = info.getSize();
            final Crop crop               = (Crop) opList.getFirst(Crop.class);
            Scale scale                   = (Scale) opList.getFirst(Scale.class);
            final ScaleConstraint sc      = opList.getScaleConstraint();
            final ReductionFactor rf      = new ReductionFactor();
            final Set<ReaderHint> hints   = EnumSet.noneOf(ReaderHint.class);

            final RenderedImage renderedImage = reader.readRendered(
                    opList.getPageIndex(), crop, scale, sc, rf, hints);
            RenderedOp renderedOp = JAIUtil.getAsRenderedOp(
                    RenderedOp.wrapRenderedImage(renderedImage));

            Encode encode = (Encode) opList.getFirst(Encode.class);
            if (encode != null && !Format.get("gif").equals(outputFormat)) {
                renderedOp = JAIUtil.rescalePixels(renderedOp);
                renderedOp = JAIUtil.reduceTo8Bits(renderedOp);
            }

            // Apply the Crop operation, if present.
            if (crop != null) {
                renderedOp = JAIUtil.cropImage(
                        renderedOp, opList.getScaleConstraint(), crop, rf);
            }

            // Correct for orientation -- this will be a no-op if the
            // orientation is 0.
            renderedOp = JAIUtil.rotateImage(
                    renderedOp, orientation.getDegrees());

            // Apply remaining operations, except Overlay.
            for (Operation op : opList) {
                if (op.hasEffect(fullSize, opList)) {
                    if (op instanceof Scale) {
                        scale = (Scale) op;
                        final boolean isLinear = scale.isLinear() &&
                                !scale.isUp(fullSize, opList.getScaleConstraint());
                        if (isLinear) {
                            renderedOp = JAIUtil.convertColor(renderedOp,
                                    ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB));
                        }

                        /*
                        JAI has a bug that causes it to fail on certain right-
                        edge compressed TIFF tiles when using the
                        SubsampleAverage operation, as well as the Scale
                        operation with any interpolation other than nearest-
                        neighbor. The error is an ArrayIndexOutOfBoundsException
                        in PlanarImage.cobbleByte().

                        Issue: https://github.com/cantaloupe-project/cantaloupe/issues/94
                        Example: /iiif/2/champaign-pyramidal-tiled-lzw.tif/8048,0,800,6928/99,/0/default.jpg

                        So, the strategy here is:
                        1) if the TIFF is compressed, use the Scale operation with
                           nearest-neighbor interpolation, which is horrible, but
                           better than nothing.
                        2) otherwise, use the SubsampleAverage operation.
                        */
                        if (Format.get("tif").equals(getSourceFormat()) &&
                                (!Compression.UNCOMPRESSED.equals(reader.getCompression(0)) &&
                                        !Compression.UNDEFINED.equals(reader.getCompression(0)))) {
                            LOGGER.debug("process(): detected compressed TIFF; " +
                                    "using the Scale operation with nearest-" +
                                    "neighbor interpolation.");
                            renderedOp = JAIUtil.scaleImage(
                                    renderedOp, (Scale) op,
                                    opList.getScaleConstraint(),
                                    Interpolation.getInstance(Interpolation.INTERP_NEAREST),
                                    rf);
                        } else if (renderedOp.getWidth() < 3 ||
                                renderedOp.getHeight() < 3) {
                            // SubsampleAverage requires the image to be at least 3
                            // pixels on a side. So, again use the Scale operation,
                            // with a better (but still bad [but it doesn't matter
                            // because of the tiny dimension(s)]) filter.
                            renderedOp = JAIUtil.scaleImage(
                                    renderedOp, (Scale) op,
                                    opList.getScaleConstraint(),
                                    Interpolation.getInstance(Interpolation.INTERP_BILINEAR),
                                    rf);
                        } else {
                            // All clear to use SubsampleAverage.
                            renderedOp = JAIUtil.scaleImageUsingSubsampleAverage(
                                    renderedOp, (Scale) op,
                                    opList.getScaleConstraint(), rf);
                        }

                        if (isLinear) {
                            renderedOp = JAIUtil.convertColor(renderedOp,
                                    ColorSpace.getInstance(ColorSpace.CS_sRGB));
                        }
                    } else if (op instanceof Transpose) {
                        renderedOp = JAIUtil.
                                transposeImage(renderedOp, (Transpose) op);
                    } else if (op instanceof Rotate) {
                        renderedOp = JAIUtil.rotateImage(renderedOp, (Rotate) op);
                    } else if (op instanceof ColorTransform) {
                        renderedOp = JAIUtil.
                                transformColor(renderedOp, (ColorTransform) op);
                    } else if (op instanceof Sharpen) {
                        renderedOp = JAIUtil.
                                sharpenImage(renderedOp, (Sharpen) op);
                    }
                }
            }

            // Apply the Overlay operation, if present. This will be done using
            // Java 2D because it's harder with JAI, or impossible in the case
            // of drawing text.
            BufferedImage image = null;
            for (Operation op : opList) {
                if (op instanceof Overlay && op.hasEffect(fullSize, opList)) {
                    image = renderedOp.getAsBufferedImage();
                    Java2DUtil.applyOverlay(image, (Overlay) op);
                }
            }

            final ImageWriter writer = new ImageWriterFactory()
                    .newImageWriter((Encode) opList.getFirst(Encode.class));
            if (image != null) {
                writer.write(image, outputStream);
            } else {
                writer.write(renderedOp, outputStream);
            }
        } catch (SourceFormatException e) {
            throw e;
        } catch (IOException e) {
            throw new ProcessorException(e);
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }
    }

}
