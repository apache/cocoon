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
package org.apache.cocoon.components.url;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Label;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.RedRable;
import org.apache.batik.ext.awt.image.spi.AbstractRegistryEntry;
import org.apache.batik.ext.awt.image.spi.MagicNumberRegistryEntry;
import org.apache.batik.ext.awt.image.spi.URLRegistryEntry;
import org.apache.batik.util.ParsedURL;


/**
 * This Image tag registy entry is setup to wrap the core JDK Image stream tools.  
 * 
 * @version CVS $Id: StreamJDKRegistryEntry.java,v 1.2 2004/02/04 18:07:51 joerg Exp $
 */
public class StreamJDKRegistryEntry extends AbstractRegistryEntry 
    implements URLRegistryEntry {

    /**
     * The priority of this entry.
     * This entry should in most cases be the last entry.
     * but if one wishes one could set a priority higher and be called
     * afterwords
     */
    public final static float PRIORITY = 
        1000*MagicNumberRegistryEntry.PRIORITY;

    public StreamJDKRegistryEntry() {
        super ("Stream-JDK", PRIORITY, new String[0], new String [] {"image/gif"});
    }

    /**
     * Check if the Stream references an image that can be handled by
     * this format handler.  The input stream passed in should be
     * assumed to support mark and reset.
     *
     * If this method throws a StreamCorruptedException then the
     * InputStream will be closed and a new one opened (if possible).
     *
     * This method should only throw a StreamCorruptedException if it
     * is unable to restore the state of the InputStream
     * (i.e. mark/reset fails basically).  
     */
    public boolean isCompatibleURL(ParsedURL purl) {
        String contentType = purl.getContentType();
        if (contentType == null)
            return false;

        Iterator iter = this.getMimeTypes().iterator();
        while (iter.hasNext()) {
            if (contentType.equals(iter.next())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Decode the URL into a RenderableImage
     *
     * @param purl The URLto decode
     * @param needRawData If true the image returned should not have
     *                    any default color correction the file may 
     *                    specify applied.  
     */
    public Filter handleURL(ParsedURL purl, boolean needRawData) {
        
        // Read all bytes from the ParsedURL (too bad, there's no Toolkit.createImage(InputStream))
        InputStream is = null;
        byte[] buffer = new byte[1024];
        int len;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            is = purl.openStream();
            while((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
        } catch(IOException ioe) {
            return null;
        } finally {
            try {
                if (is != null) is.close();
            } catch (Exception e) {}
        }
        
        buffer = bos.toByteArray();

        Toolkit tk = Toolkit.getDefaultToolkit();
        final Image img = tk.createImage(buffer);
        if (img == null)
            return null;

        RenderedImage ri = loadImage(img);
        if (ri == null)
            return null;

        return new RedRable(GraphicsUtil.wrap(ri));
    }

    // Stuff for Image Loading.
    static Component mediaComponent = new Label();
    static MediaTracker mediaTracker = new MediaTracker(mediaComponent);
    static int id = 0;

    public RenderedImage loadImage(Image img) {
        // In some cases the image will be a
        // BufferedImage (subclass of RenderedImage).
        if (img instanceof RenderedImage)
            return (RenderedImage)img;

                    // Setup the mediaTracker.
        int myID;
        synchronized (mediaTracker) {
            myID = id++;
        }

        // Add our image to the media tracker and wait....
        mediaTracker.addImage(img, myID);
        while (true) {
            try {
                mediaTracker.waitForID(myID);
            } catch(InterruptedException ie) {
                // Something woke us up but the image
                // isn't done yet, so try again.
                continue;
            }
            // All done!
            break;
        }

        // Clean up our registraction
        mediaTracker.removeImage(img, myID);

        if ((img.getWidth(null)  == -1)||
            (img.getHeight(null) == -1))
            return null;

                    // Build the image to .
        BufferedImage bi = null;
        bi = new BufferedImage(img.getWidth(null),
                               img.getHeight(null),
                               BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();

        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();
        return bi;
    }
}
