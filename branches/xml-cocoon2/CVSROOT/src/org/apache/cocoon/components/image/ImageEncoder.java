package org.apache.cocoon.components.image;

import org.apache.avalon.Component;
import java.io.OutputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;

/**
 * This interface defines a method of encoding a <code>BufferedImage</code>
 * into a format suitable for transmission, such as JPEG.
 *
 * @author Ross Burton <rossb@apache.org>
 * @version 1.0
 */
public interface ImageEncoder extends Component {

    /**
     * Get the content type of this image format
     * @return The MIME content type, e.g. "image/jpeg"
     */
    String getMimeType();

    /**
     * Encode the given image and output it to the given stream.
     * @param image BufferedImage to compress
     * @param out Stream to output the image to
     */
    void encode(BufferedImage image, OutputStream out) throws IOException;
}
