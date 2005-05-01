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
import java.io.InputStream;
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
 *     <dd> This parameter is optional. When specified, it determines the
 *          width of the binary image.
 *          If no height parameter is specified, the aspect ratio
 *          of the image is kept.
 *     </dd>
 *     <dt>&lt;height&gt;</dt>
 *     <dd> This parameter is optional. When specified, it determines the
 *          height of the binary image.
 *          If no width parameter is specified, the aspect ratio
 *          of the image is kept.
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
 *         switched off by setting this parameter to "<code>false</code>" so that
 *         images will be reduced in size, but not enlarged. The default is
 *         "<code>true</code>".
 *     </dd>
 *   </dl>
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id$
 */
final public class ImageReader extends ResourceReader {
    private static final boolean GRAYSCALE_DEFAULT = false;
    private static final boolean ENLARGE_DEFAULT = true;
    private static final boolean FIT_DEFAULT = false;

    private int width;
    private int height;
    private float[] scaleColor = new float[3];
    private float[] offsetColor = new float[3];

    private boolean enlarge;
    private boolean fitUniform;
    private RescaleOp colorFilter;
    private ColorConvertOp grayscaleFilter;


    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {

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
            this.colorFilter = new RescaleOp(scaleColor, offsetColor, null);
        }

        if (par.getParameterAsBoolean("grayscale", GRAYSCALE_DEFAULT)) {
            this.grayscaleFilter = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        }

        this.enlarge = par.getParameterAsBoolean("allow-enlarging", ENLARGE_DEFAULT);
        this.fitUniform = par.getParameterAsBoolean("fit-uniform", FIT_DEFAULT);

        super.setup(resolver, objectModel, src, par);
    }

    protected void setupHeaders() {
        // Reset byte ranges support for dynamic response
        if (byteRanges && hasTransform()) {
            byteRanges = false;
        }

        super.setupHeaders();
    }

    /**
     * @return True if image transform is specified
     */
    private boolean hasTransform() {
        return width > 0 || height > 0 || null != colorFilter || null != grayscaleFilter;
    }

    /**
     * Returns the affine transform that implements the scaling.
     * The behavior is the following: if both the new width and height values
     * are positive, the image is rescaled according to these new values and
     * the original aspect ratio is lost.
     * Otherwise, if one of the two parameters is zero or negative, the
     * aspect ratio is maintained and the positive parameter indicates the
     * scaling.
     * If both new values are zero or negative, no scaling takes place (a unit
     * transformation is applied).
     */
    private AffineTransform getTransform(double ow, double oh, double nw, double nh) {
        double wm = 1.0d;
        double hm = 1.0d;

        if (fitUniform) {
            //
            // Compare aspect ratio of image vs. that of the "box"
            // defined by nw and nh
            //
            if (ow/oh > nw/nh) {
                nh = 0;    // Original image is proportionately wider than the box,
                        // so scale to fit width
            } else {
                nw = 0;    // Scale to fit height
            }
        }

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

    protected void processStream(InputStream inputStream) throws IOException, ProcessingException {
        if (hasTransform()) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("image " + ((width == 0) ? "?" : Integer.toString(width))
                                  + "x"    + ((height == 0) ? "?" : Integer.toString(height))
                                  + " expires: " + expires);
            }

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
                JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
                encoder.encode(currentImage);
                out.flush();
            } catch (ImageFormatException e) {
                throw new ProcessingException("Error reading the image. " +
                                              "Note that only JPEG images are currently supported.");
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
            super.processStream(inputStream);
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
}
