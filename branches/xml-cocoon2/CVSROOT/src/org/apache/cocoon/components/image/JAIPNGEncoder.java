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
import java.awt.image.BufferedImage;
import com.sun.media.jai.codec.PNGEncodeParam;
import com.sun.media.jai.codec.ImageCodec;

/**
 * A PNG Image Encoder.  This class delegates the actual compression to
 * the codecs supplied with the Java Advanced Imaging API.
 *
 * @author Ross Burton <rossb@apache.org>
 * @version 1.0
 * @see Java Advanced Imaging API
 */
public class JAIPNGEncoder implements ImageEncoder, Configurable {

    private boolean interlaced;
    private boolean alpha;

    public void setConfiguration(Configuration conf) throws ConfigurationException {
		// Using the passed Configuration, generate a far more friendly Parameters object.
		Parameters p = Parameters.fromConfiguration(conf);
		interlaced = p.getParameterAsBoolean("interlaced", true);
		alpha = p.getParameterAsBoolean("alpha", true);
		// TODO: colour depth
        //System.err.println("Interlaced set to " + interlaced);
    }

    public String getMimeType() {
		return "image/png";
    }

    public void encode(BufferedImage image, OutputStream out) throws IOException {
		PNGEncodeParam param = PNGEncodeParam.getDefaultEncodeParam(image);
		// Set the alpha (defaults to on)
		if (!alpha)
			param.unsetTransparency();
	
		// Encode the image (damn class name collisions!)
		com.sun.media.jai.codec.ImageEncoder encoder = ImageCodec.createImageEncoder("PNG", out, param);
		if (encoder != null) {
			encoder.encode(image);
		} else {
			throw new RuntimeException("PNG Encoder not found");
		}
    }
}
