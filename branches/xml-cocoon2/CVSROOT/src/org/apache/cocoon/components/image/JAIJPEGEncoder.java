/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.components.image;

import org.apache.avalon.*;
import org.apache.avalon.Parameters;
import java.io.OutputStream;
import java.io.IOException;
import java.awt.image.*;
import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.ImageCodec;

/**
 * A JPEG Image Encoder.  This class delegates the actual compression to
 * the codecs supplied with the Java Advanced Imaging API.
 * @author Ross Burton <rossb@apache.org>
 * @version 1.0
 * @see Java Advanced Imaging API
 */
public class JAIJPEGEncoder implements ImageEncoder, Configurable {

    /** The quality level. The default is 0.75 (high quality) */
    private float quality;

    public void configure(Configuration conf) throws ConfigurationException {
		// Using the passed Configuration, generate a far more friendly Parameters object.
		Parameters p = Parameters.fromConfiguration(conf);
		quality = p.getParameterAsFloat("quality", 0.75f);
		//System.err.println("Quality set to " + quality);
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

		JPEGEncodeParam param = new JPEGEncodeParam();
		param.setQuality(quality);
		com.sun.media.jai.codec.ImageEncoder encoder = ImageCodec.createImageEncoder("JPEG", out, param);
		if (encoder != null) {
			encoder.encode(noalpha);
		} else {
			throw new RuntimeException("JPEG Encoder not found");
		}
    }
}
