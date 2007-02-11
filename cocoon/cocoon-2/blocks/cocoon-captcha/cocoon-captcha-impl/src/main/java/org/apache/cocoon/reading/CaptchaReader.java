/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Random;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * <p>The {@link CaptchaReader} is a simple tool generating JPEG images for the text
 * supplied as its source in a way so that it's hard to parse automatically.</p>
 * 
 * <p><i>CAPTCHA</i> means quite literally <i>Completely Automated Public Turing
 * Test to Tell Computers and Humans Apart</i> and one of the best resources on
 * this can be found at the <a href="http://www.captcha.net/">Carnegie Mellon
 * School of Computer Science CAPTCHA project.</a>.
 * 
 * <p>This reader creates very simple <i>CAPTCHAs</i> from within a Cocoon pipeline,
 * enabling quick and safe end-user presence identificat. As an example, look at the
 * following pipeline snippet:</p>
 * 
 * <pre>
 * &lt;map:match pattern="*"&gt;
 *   &lt;map:read type="captcha" src="{1}"/&gt;
 * &lt;/map:match&gt;
 * </pre>
 * 
 * <p>The example will produce an image containing the text in <code>{1}</code>
 * "warped" or "bent" in a way similar to the Adobe&reg; Photoshop&reg; "Wave"
 * filter plugin.</p>
 * 
 * <p>Few pipeline parameters control the operation of the {@link CaptchaReader}
 * (this component is not configurable):</p>
 * 
 * <ul>
 *   <li><code>width</code>: the width of the image to generate (default: 100).</li>
 *   <li><code>height</code>: the height of the image to generate (default: 50).</li>
 *   <li><code>foreground</code>: the text foreground color (default: random).</li>
 *   <li><code>background</code>: the image background color (default: white).</li>
 *   <li><code>font</code>: the font to use for the text (default: serif).</li>
 *   <li><code>scale</code>: the scaling factor for interim images (default: 5).</li>
 *   <li><code>amount</code>: the amount of text warping to apply (default: 1).</li>
 *   <li><code>quality</code>: the JPEG encoding quality (default: 0.75).</li>
 * </ul>
 * 
 * <p>Note that when the <code>foreground</code> parameter is not specified, the
 * color used to write the text will be randomly chosen in a way that it contrasts
 * well with the background color to avoid problems of illegible text.</p>
 * 
 * <p>Both the <code>foreground</code> and <code>background</code> parameters accept
 * strings in the format specified by {@link Color#decode(String)} (for example
 * <code>fff</code>, or <code>0099CC</code>) or one of the field names of the
 * {@link Color} class (for example {@link Color#BLACK BLACK} or {@link Color#cyan
 * cyan} ...).</p>
 * 
 * <p>The <code>scale</code> parameter controls how much the specified size should
 * be scaled while processing the interim images: the bigger the scaling factor, the
 * better the image quality, but also the memory used while generating the final
 * image will be bigger. In other words, use with care.</p>
 * 
 * <p>The <code>amount</code> parameter is interpreted as a floating point number
 * and must be greater than zero. This controls how much text should be warped, and
 * normally a value of <code>1</code> produce quite-good warping. Increasing (or
 * decreasing) this value will produce more (ore less) warping.</p>
 *
 * <p>Remember that in no way the {@link CaptchaReader} claims to be able to
 * generate "unbreakable" text (that will be impossible), and improvements to the
 * algorithm are welcome.</p>
 *
 */
public class CaptchaReader extends AbstractReader {
    
    /** <p>A unique {@link Random} instance to use.</p> */
    private static final Random RANDOM = new Random();

    /**
     * <p>The content type of the generated content: <code>image/jpeg</code>.</p>
     *
     * @return Always <code>image/jpeg</code>.
     */
    public String getMimeType() {
        return "image/jpeg";
    }

    /**
     * <p>Return a {@link Color} instance from a specified parameter.</p>
     *
     * @param parameterName The name of the parameter whose to use as the color.
     * @param defaultColor The default {@link Color} to return.
     * @return the interpreted color or the default color specified.
     */
    private Color getColor(String parameterName, Color defaultColor) {
        String colorString = this.parameters.getParameter(parameterName, null);
        if (colorString == null) return defaultColor;
        try {
            return Color.decode(colorString);
        } catch (Exception e1) {
            try {
                Field colorField = Color.class.getDeclaredField(colorString);
                return (Color) colorField.get(Color.class);
            } catch (Exception e2) {
                return defaultColor;
            }
        }
    }
    
    private Graphics2D antialiasedGraphics(BufferedImage image) {
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                  RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return graphics;
    }

    /**
     * <p>Create an image containing the text specified as this reader source
     * warped to avoid automatic interpretation.</p>
     *
     * @throws IOException if an I/O error occurred generating the image.
     */
    public void generate()
    throws IOException {

        /* Retrieve the current operational parameters from Cocoon's sitemap */
        final int width = this.parameters.getParameterAsInteger("width", 100);
        final int height = this.parameters.getParameterAsInteger("height", 50);
        Color background = this.getColor("background", Color.white);
        Color foreground = this.getColor("foreground", null);
        if (foreground == null) {
            int r = (RANDOM.nextInt(64) + 96 + background.getRed()) & 0x0ff;
            int g = (RANDOM.nextInt(64) + 96 + background.getGreen()) & 0x0ff;
            int b = (RANDOM.nextInt(64) + 96 + background.getBlue()) & 0x0ff;
            foreground = new Color(r, g, b);
        }
        final String fontName = this.parameters.getParameter("font", "serif");
        final int scale = this.parameters.getParameterAsInteger("scale", 5);
        final float amount = this.parameters.getParameterAsFloat("amount", 2);
        final float quality = this.parameters.getParameterAsFloat("quality", 0.75F);
        final String text = this.source;

        /* Create the final buffered image we will be writing to at the bottom */
        final BufferedImage result = new BufferedImage(width, height,
                                                       BufferedImage.TYPE_INT_RGB);

        /* Starting with a size of 100, evaluate how big the real font should be */
        final Font baseFont = new Font(fontName, Font.PLAIN, 100);
        final Graphics2D graphics = this.antialiasedGraphics(result);
        final FontMetrics metrics = graphics.getFontMetrics(baseFont);
        final Rectangle2D tempSize = metrics.getStringBounds(text, graphics);

        /* Evaluate the image size of the resulting image and prepare a ratio */
        final double tempWidth = tempSize.getWidth() + (2 * tempSize.getHeight());
        final double tempHeight = (tempSize.getHeight() * (1 + amount));
        final double ratioWidth = width * scale / tempWidth;
        final double ratioHeight = height * scale / tempHeight;
        final double ratio = ratioWidth < ratioHeight? ratioWidth: ratioHeight;
        final Font font = baseFont.deriveFont((float) (100 * ratio));

        /* Evaluate the final size of the text to write */
        final FontMetrics sourceMetrics = graphics.getFontMetrics(font);
        final Rectangle2D size = sourceMetrics.getStringBounds(text, graphics);
        final double textWidth = size.getWidth();
        final double textHeight = size.getHeight();

        /* Evaluate the final size of the interim images */
        int scaledWidth = (int) (tempWidth * ratio);
        int scaledHeight = (int) (tempHeight * ratio);
        
        /* Create a couple of images to write the plain string and the warped one */
        BufferedImage source = new BufferedImage(scaledWidth, scaledHeight,
                                                 BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage warped = new BufferedImage(scaledWidth, scaledHeight,
                                                 BufferedImage.TYPE_INT_ARGB);

        /* Prepare the background and the font of the source image */
        final Graphics2D sourceGraphics = this.antialiasedGraphics(source);
        sourceGraphics.setColor(Color.black);
        sourceGraphics.fillRect(0, 0, scaledWidth, scaledHeight);
        sourceGraphics.setFont(font);

        /* Write the string exactly in the middle of the source image */
        float textX = (float) ((scaledWidth  - textWidth)  / 2); 
        float textY = (float) ((scaledHeight - textHeight) / 2);
        sourceGraphics.setColor(Color.white);
        sourceGraphics.drawString(text, textX, textY + sourceMetrics.getAscent());

        /* Randomize displacement factors for sine-waves */
        final int displaceTop = RANDOM.nextInt(scaledWidth);
        final int displaceBtm = RANDOM.nextInt(scaledWidth);
        final int displaceVer = RANDOM.nextInt(scaledHeight);

        /* Calculate the horizontal and vertical amplitude and wavelength of sines */
        final double amplitHor = textHeight * amount / 4;
        final double amplitVer = textHeight / 8;
        final double t = (RANDOM.nextDouble() * textWidth / 2) + (textWidth * 0.75);
        final double b = (RANDOM.nextDouble() * textWidth / 2) + (textWidth * 0.75);
        final double wlenTop = textHeight > t? textHeight: t;
        final double wlenBtm = textHeight > b? textHeight: b;

        /* Calculate the offsets for horizontal (top and bottom) sine waves */
        final double offsetTop = amplitHor;
        final double offsetBtm = scaledHeight - amplitHor;

        /* Prepare an array for vertical displacement sine wave */
        final double vert[] = new double[scaledHeight];
        for (int v = 0; v < scaledHeight ; v++) {
            vert[v] = Math.sin((Math.PI * (v + displaceVer)) / textHeight) * amplitVer;
        }

        /* Iterate all the target image pixels and render the distortion */
        int x1 = Integer.MAX_VALUE;
        int x2 = Integer.MIN_VALUE;
        int y1 = Integer.MAX_VALUE;
        int y2 = Integer.MIN_VALUE;
        final WritableRaster sourceRaster = source.getRaster();
        final WritableRaster warpedRaster = warped.getRaster();
        final double src[] = new double[9];
        final double col[] = new double[] { foreground.getRed(),
                                            foreground.getGreen(),
                                            foreground.getBlue(), 0};
        for (int h = 0; h < scaledWidth; h++) {
            final double baseTop = (Math.PI * (h + displaceTop)) / wlenTop;
            final double baseBtm = (Math.PI * (h + displaceBtm)) / wlenBtm; 
            final double top = offsetTop + Math.sin(baseTop) * amplitHor;
            final double btm = offsetBtm - Math.sin(baseBtm) * amplitHor;

            for (int v = 0; v < scaledHeight; v ++) {
                final double x = (h + vert[v]);
                final double y = (v * ((btm - top) / scaledHeight)) + top;

                if ((y > 0) && (y < scaledHeight - 1) &&
                    (x > 0) && (x < scaledWidth - 1)) {

                    /* Retrieve the nine pixels around the source one */
                    sourceRaster.getPixels((int)(x-1), (int)(y-1), 3, 3, src);

                    /* Average their value (it's grayscale) to have a better warp */
                    double alpha = ((src[1] + src[3] + src[5] + src[7]) * 0.1) +
                                   ((src[0] + src[2] + src[6] + src[8]) * 0.025) +
                                   (src[4] * 0.5);

                    /* Write the resultin pixel in the target image if necessary */
                    if (alpha > 0) {
                        col[3] = alpha;
                        warpedRaster.setPixel(h, v, col);
                        if (h < x1) x1 = h;
                        if (h > x2) x2 = h;
                        if (v < y1) y1 = v;
                        if (v > y2) y2 = v;
                    }
                }
            }
        }

        /* Crop the image to the maximum extent of the warped text (if visible) */
        source = null;
        int xd = x2 - x1 + 1;
        int yd = y2 - y1 + 1; 
        if ((xd > 1) && (yd > 1)) {
            warped = warped.getSubimage(x1, y1, xd, yd);
        }

        /* Rescale the cropped image to the required size */
        Image image = warped.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        graphics.setBackground(background);
        graphics.setColor(background);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(foreground);
        graphics.drawImage(image, 0, 0, null);
        warped = null;

        /* Write the processed image as a JPEG image */
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(buffer);
        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(result);
        param.setQuality(quality, true);
        encoder.encode(result, param);
        buffer.flush();
        buffer.close();
        this.out.write(buffer.toByteArray());
        this.out.flush();
    }
}
