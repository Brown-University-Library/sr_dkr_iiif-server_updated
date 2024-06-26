package edu.illinois.library.cantaloupe.processor;

import edu.illinois.library.cantaloupe.image.Dimension;
import edu.illinois.library.cantaloupe.image.Rectangle;
import edu.illinois.library.cantaloupe.image.ScaleConstraint;
import edu.illinois.library.cantaloupe.operation.ColorTransform;
import edu.illinois.library.cantaloupe.operation.Crop;
import edu.illinois.library.cantaloupe.operation.Operation;
import edu.illinois.library.cantaloupe.operation.OperationList;
import edu.illinois.library.cantaloupe.operation.ReductionFactor;
import edu.illinois.library.cantaloupe.operation.Sharpen;
import edu.illinois.library.cantaloupe.operation.Rotate;
import edu.illinois.library.cantaloupe.operation.Scale;
import edu.illinois.library.cantaloupe.operation.Transpose;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.OpImage;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.TransposeDescriptor;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.renderable.ParameterBlock;

/**
 * @see <a href="http://docs.oracle.com/cd/E19957-01/806-5413-10/806-5413-10.pdf">
 *     Programming in Java Advanced Imaging</a>
 * @deprecated Since version 4.0.
 */
@Deprecated
final class JAIUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JAIUtil.class);

    /**
     * @param inImage    Image to convert.
     * @param colorSpace Target color space.
     * @return           Converted image.
     * @since 5.0
     */
    static RenderedOp convertColor(RenderedOp inImage, ColorSpace colorSpace) {
        if (!colorSpace.equals(inImage.getColorModel().getColorSpace())) {
            LOGGER.debug("convertColor(): converting to {}", colorSpace);
            ColorModel model = new ComponentColorModel(
                    colorSpace,
                    inImage.getColorModel().hasAlpha(),
                    inImage.getColorModel().isAlphaPremultiplied(),
                    inImage.getColorModel().getTransparency(),
                    inImage.getColorModel().getTransferType());
            ParameterBlock pb = new ParameterBlock();
            pb.addSource(inImage);
            pb.add(model);
            inImage = JAI.create("ColorConvert", pb);
        }
        return inImage;
    }

    /**
     * @param inImage Image to crop.
     * @param crop    Crop operation. {@link
     *                Operation#hasEffect(Dimension, OperationList)} should be
     *                called before invoking.
     * @return        Cropped image, or the input image if the given operation
     *                is a no-op.
     */
    static RenderedOp cropImage(RenderedOp inImage, Crop crop) {
        return cropImage(inImage, new ScaleConstraint(1, 1),
                crop, new ReductionFactor(0));
    }

    /**
     * Crops the given image taking into account a reduction factor. In other
     * words, the dimensions of the input image have already been halved
     * <code>rf</code> times but the given crop region is relative to the
     * full-sized image.
     *
     * @param inImage         Image to crop.
     * @param scaleConstraint Scale constraint.
     * @param crop            Crop operation. {@link
     *                        Operation#hasEffect(Dimension, OperationList)}
     *                        should be called before invoking.
     * @param rf              Number of times the dimensions of {@literal
     *                        inImage} have already been halved relative to the
     *                        full-sized version.
     * @return                Cropped image, or the input image if the given
     *                        operation is a no-op.
     */
    static RenderedOp cropImage(RenderedOp inImage,
                                ScaleConstraint scaleConstraint,
                                Crop crop,
                                ReductionFactor rf) {
        if (crop.hasEffect()) {
            final Rectangle cropRegion = crop.getRectangle(
                    new Dimension(inImage.getWidth(), inImage.getHeight()),
                    rf, scaleConstraint);
            LOGGER.debug("cropImage(): x: {}; y: {}; width: {}; height: {}",
                    cropRegion.intX(), cropRegion.intY(),
                    cropRegion.intWidth(), cropRegion.intHeight());
            final ParameterBlock pb = new ParameterBlock();
            pb.addSource(inImage);
            pb.add((float) cropRegion.x());
            pb.add((float) cropRegion.y());
            pb.add((float) cropRegion.width());
            pb.add((float) cropRegion.height());
            inImage = JAI.create("crop", pb);
        }
        return inImage;
    }

    /**
     * @param inImage Image to get a RenderedOp of.
     * @return RenderedOp version of <code>inImage</code>.
     */
    static RenderedOp getAsRenderedOp(PlanarImage inImage) {
        final ParameterBlock pb = new ParameterBlock();
        pb.addSource(inImage);
        return JAI.create("null", pb);
    }

    /**
     * <p>Reduces an image's component size to 8 bits if greater.</p>
     *
     * <p>Pixel values will not be rescaled.</p>
     *
     * @param inImage Image to reduce.
     * @return Reduced image, or the input image if it already is 8 bits or
     *         less.
     * @see #rescalePixels(RenderedOp)
     */
    static RenderedOp reduceTo8Bits(RenderedOp inImage) {
        final int componentSize = inImage.getColorModel().getComponentSize(0);
        if (componentSize > 8) {
            final ParameterBlock pb = new ParameterBlock();
            pb.addSource(inImage);

            LOGGER.debug("reduceTo8Bits(): converting {}-bit to 8-bit",
                    componentSize);

            // See Programming in Java Advanced Imaging sec. 4.5 for an
            // explanation of the Format operation.
            inImage = JAI.create("format", pb, inImage.getRenderingHints());
        }
        return inImage;
    }

    /**
     * Linearly scales the pixel values of the given image into an 8-bit range.
     *
     * @param inImage Image to rescale.
     * @return Rescaled image.
     * @see #reduceTo8Bits(RenderedOp)
     */
    static RenderedOp rescalePixels(RenderedOp inImage) {
        final int targetSize = 8;
        final int componentSize = inImage.getColorModel().getComponentSize(0);
        if (componentSize != targetSize) {
            ParameterBlock pb = new ParameterBlock();
            pb.addSource(inImage);

            final double multiplier = Math.pow(2, targetSize) /
                    Math.pow(2, componentSize);
            // Per-band constants to multiply by.
            final double[] constants = {multiplier};
            pb.add(constants);

            // Per-band offsets to be added.
            final double[] offsets = {0};
            pb.add(offsets);

            LOGGER.debug("rescalePixels(): multiplying by {}", multiplier);
            inImage = JAI.create("rescale", pb);
        }
        return inImage;
    }

    /**
     * @param inImage Image to rotate.
     * @param degrees Degrees to rotate.
     * @return        Rotated image, or the input image if the {@literal
     *                degrees} value is too small.
     */
    static RenderedOp rotateImage(RenderedOp inImage, double degrees) {
        if (degrees > 0.0001) {
            LOGGER.debug("rotateImage(): rotating {} degrees", degrees);
            final ParameterBlock pb = new ParameterBlock();
            pb.addSource(inImage);
            pb.add(inImage.getWidth() / 2.0f);       // x origin
            pb.add(inImage.getHeight() / 2.0f);      // y origin
            pb.add((float) Math.toRadians(degrees)); // radians
            pb.add(Interpolation.getInstance(Interpolation.INTERP_BILINEAR));
            inImage = JAI.create("rotate", pb);
        }
        return inImage;
    }

    /**
     * @param inImage Image to rotate.
     * @param rotate  Rotate operation.
     * @return        Rotated image, or the input image if the given operation
     *                is a no-op.
     */
    static RenderedOp rotateImage(RenderedOp inImage, Rotate rotate) {
        return rotateImage(inImage, rotate.getDegrees());
    }

    /**
     * <p>Scales an image using the JAI {@literal Scale} operator, taking an
     * already-applied reduction factor into account. (In other words, the
     * dimensions of the input image have already been halved {@literal rf}
     * times but the given size is relative to the full-sized image.)</p>
     *
     * <p>N.B.: The output quality of the {@literal Scale} operator is very
     * poor and so {@link #scaleImageUsingSubsampleAverage} should be used
     * instead, if possible.</p>
     *
     * @param inImage         Image to scale.
     * @param scale           Requested size ignoring any reduction factor.
     *                        {@link Operation#hasEffect(Dimension, OperationList)}
     *                        should be called before invoking.
     * @param scaleConstraint Scale constraint.
     * @param interpolation   Interpolation.
     * @param rf              Reduction factor that has already been applied to
     *                        {@literal inImage}.
     * @return Scaled image, or the input image if the given scale is a no-op.
     */
    static RenderedOp scaleImage(RenderedOp inImage,
                                 Scale scale,
                                 ScaleConstraint scaleConstraint,
                                 Interpolation interpolation,
                                 ReductionFactor rf) {
        if (scale.hasEffect() || scaleConstraint.hasEffect()) {
            final int sourceWidth = inImage.getWidth();
            final int sourceHeight = inImage.getHeight();
            final Dimension scaledSize = scale.getResultingSize(
                    new Dimension(sourceWidth, sourceHeight),
                    rf, scaleConstraint);

            double xScale = scaledSize.width() / (double) sourceWidth;
            double yScale = scaledSize.height() / (double) sourceHeight;

            LOGGER.debug("scaleImage(): width: {}%; height: {}%",
                    xScale * 100, yScale * 100);
            final ParameterBlock pb = new ParameterBlock();
            pb.addSource(inImage);
            pb.add((float) xScale);
            pb.add((float) yScale);
            pb.add(0.0f);
            pb.add(0.0f);
            pb.add(interpolation);
            inImage = JAI.create("scale", pb);
        }
        return inImage;
    }

    /**
     * <p>Better-quality alternative to {@link #scaleImage} using JAI's
     * {@literal SubsampleAverage} operator.</p>
     *
     * <p>N.B. The {@literal SubsampleAverage} operator is not capable of
     * upscaling. If asked to upscale, this method will use the inferior-quality
     * {@literal Scale} operator instead.</p>
     *
     * @param inImage         Image to scale.
     * @param scale           Requested size ignoring any reduction factor.
     *                        {@link Operation#hasEffect(Dimension, OperationList)}
     *                        should be called before invoking.
     * @param scaleConstraint Scale constraint.
     * @param rf              Reduction factor that has already been applied to
     *                        {@literal inImage}.
     * @return                Scaled image, or the input image if the given
     *                        scale is a no-op.
     */
    static RenderedOp scaleImageUsingSubsampleAverage(RenderedOp inImage,
                                                      Scale scale,
                                                      ScaleConstraint scaleConstraint,
                                                      ReductionFactor rf) {
        final Dimension fullSize = new Dimension(
                inImage.getWidth(), inImage.getHeight());

        if (scale.isUp(fullSize, scaleConstraint)) {
            LOGGER.trace("scaleImageUsingSubsampleAverage(): can't upscale; " +
                    "invoking scaleImage() instead");
            return scaleImage(inImage, scale, scaleConstraint,
                    Interpolation.getInstance(Interpolation.INTERP_BILINEAR),
                    rf);
        } else if (scale.hasEffect() || scaleConstraint.hasEffect()) {
            final Dimension scaledSize = scale.getResultingSize(
                    fullSize, rf, scaleConstraint);
            final double xScale = scaledSize.width() / fullSize.width();
            final double yScale = scaledSize.height() / fullSize.height();

            LOGGER.trace("scaleImageUsingSubsampleAverage(): " +
                            "width: {}%; height: {}%",
                    xScale * 100, yScale * 100);
            final ParameterBlock pb = new ParameterBlock();
            pb.addSource(inImage);
            pb.add(xScale);
            pb.add(yScale);
            pb.add(0.0); // X translation
            pb.add(0.0); // Y translation

            final RenderingHints hints = new RenderingHints(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            inImage = JAI.create("SubsampleAverage", pb, hints);
        }
        return inImage;
    }

    /**
     * @param inImage Image to sharpen.
     * @param sharpen The sharpen operation.
     * @return Sharpened image.
     */
    static RenderedOp sharpenImage(RenderedOp inImage,
                                   final Sharpen sharpen) {
        if (sharpen.hasEffect()) {
            ParameterBlock pb = new ParameterBlock();
            pb.addSource(inImage);
            pb.add(null);
            pb.add((float) sharpen.getAmount());
            inImage = JAI.create("UnsharpMask", pb);
        }
        return inImage;
    }

    /**
     * @param inImage        Image to filter
     * @param colorTransform Color transform operation
     * @return Transformed image, or the input image if the given operation
     *         is a no-op.
     */
    @SuppressWarnings({"deprecation"}) // really, JAI itself is basically deprecated
    static RenderedOp transformColor(RenderedOp inImage,
                                     ColorTransform colorTransform) {
        // convert to grayscale
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(inImage);
        final int numBands = OpImage.getExpandedNumBands(
                inImage.getSampleModel(), inImage.getColorModel());
        double[][] matrix = new double[1][numBands + 1];
        matrix[0][0] = 0.114;
        matrix[0][1] = 0.587;
        matrix[0][2] = 0.299;
        for (int i = 3; i <= numBands; i++) {
            matrix[0][i] = 0;
        }
        pb.add(matrix);
        RenderedOp filteredImage = JAI.create("bandcombine", pb, null);
        if (ColorTransform.BITONAL.equals(colorTransform)) {
            pb = new ParameterBlock();
            pb.addSource(filteredImage);
            pb.add(1.0 * 128);
            filteredImage = JAI.create("binarize", pb);
        }
        return filteredImage;
    }

    /**
     * @param inImage   Image to transpose.
     * @param transpose The transpose operation.
     * @return Transposed image, or the input image if the given transpose
     *         operation is a no-op.
     */
    static RenderedOp transposeImage(RenderedOp inImage, Transpose transpose) {
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(inImage);
        switch (transpose) {
            case HORIZONTAL:
                LOGGER.debug("transposeImage(): horizontal");
                pb.add(TransposeDescriptor.FLIP_HORIZONTAL);
                break;
            case VERTICAL:
                LOGGER.debug("transposeImage(): vertical");
                pb.add(TransposeDescriptor.FLIP_VERTICAL);
                break;
        }
        return JAI.create("transpose", pb);
    }

    private JAIUtil() {}

}
