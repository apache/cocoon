package org.apache.cocoon.components.image;

import org.apache.cocoon.Parameters;
import org.apache.avalon.*;
import java.io.OutputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;
import com.sun.image.codec.jpeg.*;
import com.keypoint.PngEncoder;

/**
 * A Portable Network Graphics (PNG) Image Encoder.  This class delegates the
 * actual compression to J. David Eisenberg's PngEncoder package.
 *
 * @author Ross Burton <rossb@apache.org>
 * @version 1.0
 * @see <a href="http://catcode.com/pngencoder/">PngEncoder package</a>
 */
public class PNGEncoder implements ImageEncoder, Configurable {

    /** Compression level for gzip (default: 7) */
    private int compression;
    /** Whether to encode the alpha channel (default: yes) */
    private boolean alpha;

    public void setConfiguration(Configuration conf) throws ConfigurationException {
		// Using the passed Configuration, generate a far more friendly Parameters object.
		Parameters p = Parameters.fromConfiguration(conf);
		compression = p.getParameterAsInteger("compression", 7);
		alpha = p.getParameterAsBoolean("alpha", true);
		System.err.println("PNG Encoder[compression: " + compression + ", alpha: " + alpha + "]");
    }

    public String getMimeType() {
		return "image/png";
    }

    public void encode(BufferedImage image, OutputStream out) throws IOException {
		// Get an instance of the compressor
		PngEncoder enc = new PngEncoder(image, alpha);
		// Set the compression ratio
		enc.setCompressionLevel(compression);
		// Encode and output
		out.write(enc.pngEncode());
    }
}
