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
import org.apache.avalon.ThreadSafe;
import java.io.OutputStream;
import java.io.IOException;
import java.awt.image.*;
import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.ImageCodec;

import org.apache.log.Logger;
import org.apache.log.LogKit;

/**
 * A JPEG Image Encoder.  This class delegates the actual compression to
 * the codecs supplied with the Java Advanced Imaging API.
 * @author Ross Burton <rossb@apache.org>
 * @version 1.0
 * @see Java Advanced Imaging API
 */
public class JAIJPEGEncoder implements ImageEncoder, Configurable, ThreadSafe {

    private Logger log = LogKit.getLoggerFor("cocoon");

    /** The quality level. The default is 0.75 (high quality) */
    private float quality = -1.0f;

    /**
     * Configure the JAIJPEGEncoder.  The implementation enforces proper
     * semantics on Configurable (write once), and as such enforces no
     * race conditions on <code>quality</code>.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        if (quality == -1.0f) {
            // Using the passed Configuration, generate a far more friendly Parameters object.
            Parameters p = Parameters.fromConfiguration(conf);
            quality = p.getParameterAsFloat("quality", 0.75f);
            log.debug("Quality set to " + quality);
        }
    }

    public String getMimeType() {
        return "image/jpeg";
    }

    /**
     * This method starts the whole shebang.  The JPEG encoder requires
     * 3 band byte-based images, so this encoder checks to see if the
     * type is correct.  If the image type is not correct, it creates
     * a new image with the proper type.
     *
     * @param image The <code>BufferedImage</code> we are getting ready
     *              to serialize.
     * @param out   The <code>OutputStream</code> we are serializing.
     */
    public void encode(BufferedImage image, OutputStream out) throws IOException {
        // The JPEG encoder requires 3 band byte-based images, so create a new image
        // TODO: find a faster way of doing this
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage noalpha;

        if (image.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            noalpha = image;
        } else {
            noalpha = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
            Raster raster = image.getRaster().createChild(0, 0, w, h, 0, 0, new int[] {0, 1, 2});
            noalpha.setData(raster);
        }

        // Make sure quality is set properly: if configure() has not
        // been called then too bad.
        if (quality == -1.0f) quality = 0.75f;

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
