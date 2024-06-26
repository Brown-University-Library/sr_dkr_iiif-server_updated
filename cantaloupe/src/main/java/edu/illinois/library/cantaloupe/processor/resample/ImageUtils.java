/*
 * Copyright 2013, Morten Nobel-Joergensen
 *
 * License: The BSD 3-Clause License
 * http://opensource.org/licenses/BSD-3-Clause
 */
package edu.illinois.library.cantaloupe.processor.resample;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * @author Heinz Doerr
 * @author Morten Nobel-Joergensen
 * @author Alex Dolski UIUC
 */
class ImageUtils {

    /**
     * Returns one row (height == 1) of byte packed image data in BGR or AGBR
     * form.
     *
     * @param img
     * @param y
     * @param w
     * @param outArray Array into which the pixels will be read.
     * @param temp     Must be either null or a array with length of
     *                 {@code width * height}.
     */
    static void readPixelsBGR(BufferedImage img,
                              int y,
                              int w,
                              byte[] outArray,
                              int[] temp) {
        final int x = 0, h = 1;

        final int numBands = img.getSampleModel().getNumBands();
        assert outArray.length == temp.length * numBands;
        assert (temp.length == w);

        int imageType = img.getType();
        Raster raster;
        switch (imageType) {
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
            case BufferedImage.TYPE_BYTE_GRAY:
                raster = img.getRaster();
                raster.getDataElements(x, y, w, h, outArray);
                break;
            case BufferedImage.TYPE_INT_BGR:
                raster = img.getRaster();
                raster.getDataElements(x, y, w, h, temp);
                ints2bytes(temp, outArray, 0, 1, 2);  // bgr -->  bgr
                break;
            case BufferedImage.TYPE_INT_RGB:
                raster = img.getRaster();
                raster.getDataElements(x, y, w, h, temp);
                ints2bytes(temp, outArray, 2, 1, 0);  // rgb -->  bgr
                break;
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
                raster = img.getRaster();
                raster.getDataElements(x, y, w, h, temp);
                ints2bytes(temp, outArray, 2, 1, 0, 3);  // argb -->  abgr
                break;
            case BufferedImage.TYPE_CUSTOM:
                raster = img.getRaster();
                switch (raster.getTransferType()) {
                    case DataBuffer.TYPE_USHORT:
                    case DataBuffer.TYPE_SHORT:
                    case DataBuffer.TYPE_INT:
                        raster.getDataElements(x, y, w, h, temp);
                        ints2bytes(temp, outArray, 2, 1, 0, 3);  // argb -->  abgr
                        break;
                    case DataBuffer.TYPE_BYTE:
                        raster.getDataElements(x, y, w, h, outArray);
                        break;
                    default:
                        throw new IllegalArgumentException("Incompatible raster type");
                }
                break;
            default:
                // Hopefully we never get here; pixels obtained from
                // BufferedImage.getRGB() have gamma applied, but we want to
                // remain in linear space.
                img.getRGB(x, y, w, h, temp, 0, w);
                ints2bytes(temp, outArray, 2, 1, 0);  // rgb -->  bgr
                break;
        }
    }

    /**
     * <p>Converts and copies byte-packed BGR or ABGR into the given {@literal
     * image} buffer. The type of {@literal image} may vary (e.g. RGB or BGR,
     * int- or byte-packed) but the number of components (w/o alpha, w alpha,
     * gray) must match.</p>
     *
     * <p>Does not unmange the image for all (A)RGN and (A)BGR and gray
     * images.</p>
     */
    static void setBGRPixels(byte[] bgrPixels,
                             BufferedImage image,
                             int x, int y, int w, int h) {
        int imageType = image.getType();
        WritableRaster raster = image.getRaster();
        if (imageType == BufferedImage.TYPE_3BYTE_BGR ||
                imageType == BufferedImage.TYPE_4BYTE_ABGR ||
                imageType == BufferedImage.TYPE_4BYTE_ABGR_PRE ||
                imageType == BufferedImage.TYPE_BYTE_GRAY ||
                imageType == BufferedImage.TYPE_CUSTOM) {
            raster.setDataElements(x, y, w, h, bgrPixels);
        } else {
            int[] pixels;
            if (imageType == BufferedImage.TYPE_INT_BGR) {
                pixels = bytes2int(bgrPixels, 2, 1, 0);  // BGR -> BGR
            } else if (imageType == BufferedImage.TYPE_INT_ARGB ||
                    imageType == BufferedImage.TYPE_INT_ARGB_PRE) {
                pixels = bytes2int(bgrPixels, 3, 0, 1, 2);  // ABGR -> ARGB
            } else {
                pixels = bytes2int(bgrPixels, 0, 1, 2);  // BGR -> RGB
            }
            if (w == 0 || h == 0) {
                return;
            } else if (pixels.length < w * h) {
                throw new IllegalArgumentException(
                        "pixels array must have a length" + " >= w*h");
            }
            if (imageType == BufferedImage.TYPE_INT_ARGB ||
                    imageType == BufferedImage.TYPE_INT_RGB ||
                    imageType == BufferedImage.TYPE_INT_ARGB_PRE ||
                    imageType == BufferedImage.TYPE_INT_BGR) {
                raster.setDataElements(x, y, w, h, pixels);
            } else {
                // Unmanage the image.
                image.setRGB(x, y, w, h, pixels, 0, w);
            }
        }
    }

    private static void ints2bytes(int[] in, byte[] out,
                                   int index1, int index2, int index3) {
        for (int i = 0; i < in.length; i++) {
            int index = i * 3;
            int value = in[i];
            out[index + index1] = (byte) value;
            value = value >> 8;
            out[index + index2] = (byte) value;
            value = value >> 8;
            out[index + index3] = (byte) value;
        }
    }

    private static void ints2bytes(int[] in, byte[] out,
                                   int index1, int index2, int index3, int index4) {
        for (int i = 0; i < in.length; i++) {
            int index = i * 4;
            int value = in[i];
            out[index + index1] = (byte) value;
            value = value >> 8;
            out[index + index2] = (byte) value;
            value = value >> 8;
            out[index + index3] = (byte) value;
            value = value >> 8;
            out[index + index4] = (byte) value;
        }
    }

    private static int[] bytes2int(byte[] in,
                                   int index1, int index2, int index3) {
        int[] out = new int[in.length / 3];
        for (int i = 0; i < out.length; i++) {
            int index = i * 3;
            int b1 = (in[index + index1] & 0xff) << 16;
            int b2 = (in[index + index2] & 0xff) << 8;
            int b3 = in[index + index3] & 0xff;
            out[i] = b1 | b2 | b3;
        }
        return out;
    }

    private static int[] bytes2int(byte[] in,
                                   int index1, int index2, int index3, int index4) {
        int[] out = new int[in.length / 4];
        for (int i = 0; i < out.length; i++) {
            int index = i * 4;
            int b1 = (in[index + index1] & 0xff) << 24;
            int b2 = (in[index + index2] & 0xff) << 16;
            int b3 = (in[index + index3] & 0xff) << 8;
            int b4 = in[index + index4] & 0xff;
            out[i] = b1 | b2 | b3 | b4;
        }
        return out;
    }

    static BufferedImage convert(BufferedImage src, int bufImgType) {
        BufferedImage img = new BufferedImage(src.getWidth(), src.getHeight(), bufImgType);
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();
        return img;
    }

    private ImageUtils() {}

}
