/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.reading;

import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.xml.sax.SAXException;

import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGDecodeParam;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

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
 *     <dt>&lt;scale(Red|Green|Blue)&gt;</dt>
 *     <dd>This parameter is optional. When specified it will cause the 
 *         specified color component in the image to be multiplied by the 
 *         specified floating point value.
 *     </dd>
 *     <dt>&lt;offset(Red|Green|Blue)&gt;</dt>
 *     <dd>This parameter is optional. When specified it will cause the 
 *         specified color component in the image to be incremented by the 
 *         specified floating point value.
 *     </dd>
 *     <dt>&lt;grayscale&gt;</dt>
 *     <dd>This parameter is optional. When specified and set to true it
 *         will cause each image pixel to be normalized. Default is "false".
 *     </dd>
 *     <dt>&lt;allow-enlarging&gt;</dt>
 *     <dd>This parameter is optional. By default, if the image is smaller
 *         than the specified width and height, the image will be enlarged.
 *         In some circumstances this behaviour is undesirable, and can be
 *         switched off by setting this parameter to "no" so that images will
 *         be reduced in size, but not enlarged. The default is "yes".

 *     </dd>
 *   </dl>
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id: ImageReader.java,v 1.9 2004/03/21 03:49:48 crossley Exp $
 */
final public class ImageReader extends ResourceReader {

    private int width;
    private int height;

    private float[] scaleColor = new float[3];
    private float[] offsetColor = new float[3];
    private RescaleOp colorFilter = null;

    private boolean enlarge;
    private final static String ENLARGE_DEFAULT = "true";

    private ColorConvertOp grayscaleFilter = null;
    private final static String GRAYSCALE_DEFAULT = "false";

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
            throws ProcessingException, SAXException, IOException {

        super.setup(resolver, objectModel, src, par);

        width = par.getParameterAsInteger("width", 0);
        height = par.getParameterAsInteger("height", 0);

        scaleColor[0] = par.getParameterAsFloat("scaleRed", -1.0f);
        scaleColor[1] = par.getParameterAsFloat("scaleGreen", -1.0f);
        scaleColor[2] = par.getParameterAsFloat("scaleBlue", -1.0f);
        offsetColor[0] = par.getParameterAsFloat("offsetRed", 0.0f);
        offsetColor[1] = par.getParameterAsFloat("offsetGreen", 0.0f);
        offsetColor[2] = par.getParameterAsFloat("offsetBlue", 0.0f);

        boolean filterColor = false;

        for (int i = 0; i < 3; ++i) {
            if (scaleColor[i] != -1.0f) {
                filterColor = true;
            } else {
                scaleColor[i] = 1.0f;
            }
            if (offsetColor[i] != 0.0f) {
                filterColor = true;
            }
        }

        if (filterColor) {
            colorFilter = new RescaleOp(scaleColor, offsetColor, null);
        } else {
            colorFilter = null;
        }

        String grayscalePar = par.getParameter("grayscale", GRAYSCALE_DEFAULT);
        if ("true".equalsIgnoreCase(grayscalePar) || "yes".equalsIgnoreCase(grayscalePar)){            
            grayscaleFilter = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        } else {
            grayscaleFilter = null;
        }   

        String enlargePar = par.getParameter("allow-enlarging", ENLARGE_DEFAULT);
        if ("true".equalsIgnoreCase(enlargePar) || "yes".equalsIgnoreCase(enlargePar)){
            enlarge = true;
        } else {
            enlarge = false;
        }
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

        if (!enlarge) {
            if ((nw > ow && nh <= 0) || (nh > oh && nw <=0)) {
                wm = 1.0d;
                hm = 1.0d;
            } else if (nw > ow) {
                wm = 1.0d;
            } else if (nh > oh) {
                hm = 1.0d;
            }
        }
        return new AffineTransform(wm, 0.0d, 0.0d, hm, 0.0d, 0.0d);
    }

    protected void processStream() throws IOException, ProcessingException {
        if (width > 0 || height > 0 || null != colorFilter || null != grayscaleFilter) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("image " + ((width == 0) ? "?" : Integer.toString(width))
                                  + "x"    + ((height == 0) ? "?" : Integer.toString(height))
                                  + " expires: " + expires);
            }

            // since we create the image on the fly
            response.setHeader("Accept-Ranges", "none");

            /*
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
                BufferedImage original = decoder.decodeAsBufferedImage();
                BufferedImage currentImage = original;

                if (width > 0 || height > 0) {
                    JPEGDecodeParam decodeParam = decoder.getJPEGDecodeParam();
                    double ow = decodeParam.getWidth();
                    double oh = decodeParam.getHeight();

                    AffineTransformOp filter = new AffineTransformOp(getTransform(ow, oh, width, height), AffineTransformOp.TYPE_BILINEAR);
                    WritableRaster scaledRaster = filter.createCompatibleDestRaster(currentImage.getRaster());

                    filter.filter(currentImage.getRaster(), scaledRaster);

                    currentImage = new BufferedImage(original.getColorModel(), scaledRaster, true, null);
                }

                if (null != grayscaleFilter) {
                    grayscaleFilter.filter(currentImage, currentImage);
                }

                if (null != colorFilter) {
                    colorFilter.filter(currentImage, currentImage);
                }

                if (!handleJVMBug()) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug( "No need to handle JVM bug" );
                    }
                    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
                    encoder.encode(currentImage);
                } else {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug( "Need to handle JVM bug" );
                    }
                    ByteArrayOutputStream bstream = new ByteArrayOutputStream();
                    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bstream);
                    encoder.encode(currentImage);
                    out.write(bstream.toByteArray());
                }

                out.flush();
            } catch (ImageFormatException e) {
                throw new ProcessingException("Error reading the image. Note that only JPEG images are currently supported.");
            } finally {
              // Bugzilla Bug 25069, close inputStream in finally block
              // this will close inputStream even if processStream throws
              // an exception
              inputStream.close();
            }
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
     * @return The generated key consists of the src and width and height, and the color transform
     * parameters
    */
    public Serializable getKey() {
        return this.inputSource.getURI() 
                + ':' + this.width 
                + ':' + this.height
                + ":" + this.scaleColor[0]
                + ":" + this.scaleColor[1]
                + ":" + this.scaleColor[2]
                + ":" + this.offsetColor[0]
                + ":" + this.offsetColor[1]
                + ":" + this.offsetColor[2]
                + ":" + ((null == this.grayscaleFilter) ? "color" : "grayscale")
                + ":" + super.getKey();
    }

    /**
     * Determine if workaround for Bug Id 4502892 is neccessary.
     * This method assumes that Bug is present if 
     * java.version is undeterminable, and for java.version
     * 1.1, 1.2, 1.3, all other java.version do not need the Bug handling
     *
     * @return true if we should handle the JVM bug, else false
     */
    protected boolean handleJVMBug() {
        // java.version=1.4.0
        String java_version = System.getProperty( "java.version", "0.0.0" );
        boolean handleJVMBug = true;

        char major = java_version.charAt(0);
        char minor = java_version.charAt(2);

        // make 0.0, 1.1, 1.2, 1.3 handleJVMBug = true
        if (major == '0' || major == '1') {
            if (minor == '0' || minor == '1' || minor == '2' || minor == '3') {
                handleJVMBug = true;
            } else {
                handleJVMBug = false;
            }
        } else {
            handleJVMBug = true;
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug( "Running java.version " + String.valueOf(java_version) + 
              " need to handle JVM bug " + String.valueOf(handleJVMBug) );
        }

        return handleJVMBug;
    }
}
