/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.reading;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;

import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGDecodeParam;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import org.xml.sax.SAXException;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * The <code>ImageReader</code> component is used to serve binary image data
 * in a sitemap pipeline. It makes use of HTTP Headers to determine if
 * the requested resource should be written to the <code>OutputStream</code>
 * or if it can signal that it hasn't changed.
 *
 * Parameters:
 *   <dl>
 *     <dt>&lt;width&gt;</dt>
 *     <dd>This parameter is optional. When specified it determines the width
 *         of the image that should be served.
 *     </dd>
 *     <dt>&lt;height&gt;</dt>
 *     <dd>This parameter is optional. When specified it determines the height
 *         of the image that should be served.
 *     </dd>
 *   </dl>
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Revision: 1.1 $ $Date: 2003/03/09 00:10:12 $
 */
final public class ImageReader extends ResourceReader {

    private int width;
    private int height;

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
            throws ProcessingException, SAXException, IOException {

        super.setup(resolver, objectModel, src, par);

        width = par.getParameterAsInteger("width", 0);
        height = par.getParameterAsInteger("height", 0);
    }

    /** 
     * Returns the affine transform that implements the scaling.
     * The behavior is the following: if both the new width and height values
     * are positive, the image is rescaled according to these new values and
     * the original aspect ration is lost.
     * Otherwise, if one of the two parameters is zero or negative, the
     * aspect ratio is maintained and the positive parameter indicates the
     * scaling.
     * If both new values are zero or negative, no scaling takes place (a unit
     * transformation is applied).
     */
    private AffineTransform getTransform(double ow, double oh, double nw, double nh) {
        double wm = 1.0d;
        double hm = 1.0d;
        
        if (nw > 0) {
            wm = nw / ow;
            if (nh > 0) {
                hm = nh / oh;
            } else {
                hm = wm;
            }
        } else {
            if (nh > 0) {
                hm = nh / oh;
                wm = hm;
            }
        }

        return new AffineTransform(wm, 0.0d, 0.0d, hm, 0.0d, 0.0d);
    }
    
    protected void processStream() throws IOException, ProcessingException {
        if (width > 0 || height > 0) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("image " + ((width==0)?"?":Integer.toString(width))
                                  + "x" + ((height==0)?"?":Integer.toString(height))
                                  + " expires: " + expires);
            }

            // since we create the image on the fly
            response.setHeader("Accept-Ranges", "none");

            /**
             * NOTE (SM):
             * Due to Bug Id 4502892 (which is found in *all* JVM implementations from
             * 1.2.x and 1.3.x on all OS!), we must buffer the JPEG generation to avoid
             * that connection resetting by the peer (user pressing the stop button,
             * for example) crashes the entire JVM (yes, dude, the bug is *that* nasty
             * since it happens in JPEG routines which are native!)
             * I'm perfectly aware of the huge memory problems that this causes (almost
             * doubling memory consuption for each image and making the GC work twice
             * as hard) but it's *far* better than restarting the JVM every 2 minutes
             * (since this is the average experience for image-intensive web application
             * such as an image gallery).
             * Please, go to the <a href="http://developer.java.sun.com/developer/bugParade/bugs/4502892.html">Sun Developers Connection</a>
             * and vote this BUG as the one you would like fixed sooner rather than
             * later and all this hack will automagically go away.
             * Many deep thanks to Michael Hartle <mhartle@hartle-klug.com> for tracking
             * this down and suggesting the workaround.
             *
             * UPDATE (SM):
             * This appears to be fixed on JDK 1.4
             */

            try {
                JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(inputStream);
                Raster original = decoder.decodeAsRaster();
                JPEGDecodeParam decodeParam = decoder.getJPEGDecodeParam();
                double ow = (double) decodeParam.getWidth();
                double oh = (double) decodeParam.getHeight();
                AffineTransformOp filter = new AffineTransformOp(getTransform(ow, oh, width, height), AffineTransformOp.TYPE_BILINEAR);
                WritableRaster scaled = filter.createCompatibleDestRaster(original);
                filter.filter(original, scaled);

                // JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);

                ByteArrayOutputStream bstream = new ByteArrayOutputStream();
                JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bstream);
                encoder.encode(scaled);
                out.write(bstream.toByteArray());

                out.flush();
            } catch (ImageFormatException e) {
                throw new ProcessingException("Error reading the image. Note that only JPEG images are currently supported.");
            }

            inputStream.close();
        } else {
            // only read the resource - no modifications requested
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("passing original resource");
            }
            super.processStream();
        }
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key consists from src and width and height
     * parameters
    */
    public Serializable generateKey() {
        if (width > 0 || height > 0) {
            return this.inputSource.getURI() + ':' + this.width + ':' + this.height;
        } else {
            return super.generateKey();
        }
    }
}
