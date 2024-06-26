package edu.illinois.library.cantaloupe.processor.codec;

import edu.illinois.library.cantaloupe.operation.Encode;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractIIOImageWriter {

    protected javax.imageio.ImageWriter iioWriter;
    protected Encode encode;

    /**
     * <p>Embeds metadata from {@link #encode} into the given tree.</p>
     *
     * <p>Writers for formats that don't support metadata may simply do
     * nothing.</p>
     *
     * @param baseTree Tree to embed the metadata into.
     */
    abstract protected void addMetadata(IIOMetadataNode baseTree)
            throws IOException;

    private List<javax.imageio.ImageWriter> availableIIOWriters() {
        final Iterator<javax.imageio.ImageWriter> it =
                ImageIO.getImageWritersByMIMEType(
                    encode.getFormat().getPreferredMediaType().toString());

        final List<javax.imageio.ImageWriter> iioWriters = new ArrayList<>();
        while (it.hasNext()) {
            iioWriters.add(it.next());
        }
        return iioWriters;
    }

    private void createWriter() {
        this.iioWriter = negotiateImageWriter();

        getLogger().debug("Using {}", iioWriter.getClass().getName());
    }

    /**
     * Should be called when the instance is no longer needed.
     */
    public void dispose() {
        if (iioWriter != null) {
            iioWriter.dispose();
            iioWriter = null;
        }
    }

    /**
     * May be overridden by {@link #getUserPreferredIIOImplementation()}.
     *
     * @return ImageIO implementations preferred by the application, in order
     *         of most to least preferred, or an empty array if there is no
     *         preference.
     */
    abstract protected String[] getApplicationPreferredIIOImplementations();

    abstract protected Logger getLogger();

    /**
     * @param writeParam Write parameters on which to base the metadata.
     * @param image      Image to apply the metadata to.
     * @return           Image metadata with added metadata corresponding to
     *                   any writer-specific operations applied.
     */
    protected IIOMetadata getMetadata(final ImageWriteParam writeParam,
                                      final RenderedImage image) throws IOException {
        final IIOMetadata derivativeMetadata = iioWriter.getDefaultImageMetadata(
                ImageTypeSpecifier.createFromRenderedImage(image),
                writeParam);
        final String formatName =
                derivativeMetadata.getNativeMetadataFormatName();
        final IIOMetadataNode baseTree =
                (IIOMetadataNode) derivativeMetadata.getAsTree(formatName);

        addMetadata(baseTree);

        derivativeMetadata.mergeTree(formatName, baseTree);
        return derivativeMetadata;
    }

    /**
     * N.B.: This method returns a list of strings rather than {@link Class
     * classes} because some readers reside under the {@link com.sun} package,
     * which is encapsulated in Java 9.
     *
     * @return Preferred reader implementation classes, in order of highest to
     *         lowest priority, or an empty array if there is no preference.
     */
    public String[] getPreferredIIOImplementations() {
        final List<String> impls = new ArrayList<>();

        // Prefer a user-specified implementation.
        final String userImpl = getUserPreferredIIOImplementation();
        if (userImpl != null && !userImpl.isEmpty()) {
            impls.add(userImpl);
        }
        // Fall back to an application-preferred implementation.
        final String[] appImpls = getApplicationPreferredIIOImplementations();
        impls.addAll(List.of(appImpls));

        return impls.toArray(new String[0]);
    }

    /**
     * If this returns a non-{@literal null} value, it will override {@link
     * #getApplicationPreferredIIOImplementations()}.
     *
     * @return Preferred ImageIO implementation as specified by the user. May
     *         be {@literal null}.
     */
    abstract protected String getUserPreferredIIOImplementation();

    private javax.imageio.ImageWriter negotiateImageWriter() {
        javax.imageio.ImageWriter negotiatedWriter = null;

        final List<javax.imageio.ImageWriter> iioWriters =
                availableIIOWriters();

        if (!iioWriters.isEmpty()) {
            final String[] preferredImplClasses = getPreferredIIOImplementations();

            getLogger().debug("ImageIO plugin preferences: {}",
                    (preferredImplClasses.length > 0) ?
                            String.join(", ", preferredImplClasses) : "none");

            Found:
            for (String implClass : preferredImplClasses) {
                for (javax.imageio.ImageWriter candidateWriter : iioWriters) {
                    if (implClass.equals(candidateWriter.getClass().getName())) {
                        negotiatedWriter = candidateWriter;
                        break Found;
                    }
                }
            }

            if (negotiatedWriter == null) {
                negotiatedWriter = iioWriters.get(0);
            }
        }
        return negotiatedWriter;
    }

    public void setEncode(Encode encode) {
        this.encode = encode;
        createWriter();
    }

    /**
     * Writes the given image sequence to the given output stream. This
     * implementation throws an {@link UnsupportedOperationException} and must
     * be overridden by writers that support image sequences.
     *
     * @param sequence      Image sequence to write.
     * @param outputStream  Stream to write the image to
     */
    public void write(BufferedImageSequence sequence,
                      OutputStream outputStream) throws IOException {
        throw new UnsupportedOperationException();
    }

}
