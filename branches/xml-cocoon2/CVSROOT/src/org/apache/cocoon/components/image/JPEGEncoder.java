/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.components.image;

import org.apache.avalon.*;
import org.apache.avalon.utils.Parameters;
import java.io.OutputStream;
import java.io.IOException;
import java.awt.image.*;
import com.sun.image.codec.jpeg.*;

/**
 * A JPEG Image Encoder.  This class delegates the actual compression to
 * Sun's JPEG codec (supplied with Sun's JDK).
 *
 * @author Ross Burton <ross@itzinteractive.com>
 * @version 1.0
 * @see com.sun.image.codec.jpeg
 */
public class JPEGEncoder implements ImageEncoder, Configurable {

    /** The quality level. The default is 0.75 (high quality) */
    private float quality;
    /** Force baseline flag.  The default is true; */
    private boolean baseline;

    public void setConfiguration(Configuration conf) throws ConfigurationException {
		// Using the passed Configuration, generate a far more friendly Parameters object.
		Parameters p = Parameters.fromConfiguration(conf);
		quality = p.getParameterAsFloat("quality", 0.75f);
		baseline = p.getParameterAsBoolean("baseline", true);
		System.err.println("JPEG Encoder[quality: " + quality + ", baseline: " + baseline + "]");
    }

    public String getMimeType() {
		return "image/jpeg";
    }

    public void encode(BufferedImage image, OutputStream out) throws IOException {
		// The JPEG encoder requires 3 band byte-based images, so create a new image
		// TODO: find a faster way of doing this
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage noalpha = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
		Raster raster = image.getRaster().createChild(0, 0, w, h, 0, 0, new int[] {0, 1, 2});
		noalpha.setData(raster);

		// Get an instance of the JPEG compression parameters
		JPEGEncodeParam jpar=JPEGCodec.getDefaultJPEGEncodeParam(noalpha);
		// Configure the quality
		jpar.setQuality(quality, baseline);
		// Get a JPEG encoder
		JPEGImageEncoder jenc=JPEGCodec.createJPEGEncoder(out,jpar);
		// And output the image
		jenc.encode(noalpha);
    }
}
