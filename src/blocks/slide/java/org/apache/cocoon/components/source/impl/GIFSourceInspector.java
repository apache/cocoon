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
package org.apache.cocoon.components.source.impl;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.components.source.SourceInspector;
import org.apache.cocoon.components.source.helpers.SourceProperty;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;

/**
 * This source inspector adds extra attributes for image files.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:balld@webslingerZ.com">Donald A. Ball Jr.</a>
 * @version CVS $Id: GIFSourceInspector.java,v 1.5 2003/09/24 22:34:53 cziegeler Exp $
 */
public class GIFSourceInspector extends AbstractLogEnabled implements 
    SourceInspector, ThreadSafe {

    private String PROPERTY_NS = "http://apache.org/cocoon/inspector/gif/1.0";
    private static String IMAGE_WIDTH_PROPERTY_NAME = "width";
    private static String IMAGE_HEIGHT_PROPERTY_NAME = "height";

    public SourceProperty getSourceProperty(Source source, String namespace, String name) 
        throws SourceException {

        if ((namespace.equals(PROPERTY_NS)) && 
            ((name.equals(IMAGE_WIDTH_PROPERTY_NAME)) || (name.equals(IMAGE_HEIGHT_PROPERTY_NAME))) && 
            (source.getURI().endsWith(".gif")) && (isGIFFile(source))) {

            if (name.equals(IMAGE_WIDTH_PROPERTY_NAME))
                return new SourceProperty(PROPERTY_NS, IMAGE_WIDTH_PROPERTY_NAME, 
                                          String.valueOf(getGifSize(source)[0]));
            if (name.equals(IMAGE_HEIGHT_PROPERTY_NAME))
                return new SourceProperty(PROPERTY_NS, IMAGE_HEIGHT_PROPERTY_NAME,
                                          String.valueOf(getGifSize(source)[1]));
        }
        return null;  
    }

    public SourceProperty[] getSourceProperties(Source source) throws SourceException {

        if ((source.getURI().endsWith(".gif")) &&
            (isGIFFile(source))) {

            int[] size = getGifSize(source);
            return new SourceProperty[] {
                new SourceProperty(PROPERTY_NS, IMAGE_WIDTH_PROPERTY_NAME, String.valueOf(size[0])),
                new SourceProperty(PROPERTY_NS, IMAGE_HEIGHT_PROPERTY_NAME, String.valueOf(size[1]))
            };
        }
        return null;
    }

    private boolean isGIFFile(Source source) throws SourceException {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(source.getInputStream());
            byte[] buf = new byte[3];
            int count = in.read(buf, 0, 3);
            if(count < 3) return false;
            if ((buf[0] == (byte)'G') &&
                (buf[1] == (byte)'I') &&
                (buf[2] == (byte)'F'))
                return true;
        } catch (IOException ioe) {
            throw new SourceException("Could not read source", ioe);
        } finally {
            if (in != null) 
                try { 
                    in.close(); 
                } catch(Exception e) {}
        }
        return false;
    }

    /**
     * Returns width as first element, height as second
     */
    private int[] getGifSize(Source source) throws SourceException {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(source.getInputStream());
            byte[] buf = new byte[10];
            int count = in.read(buf, 0, 10);
            if(count < 10) throw new SourceException("Not a valid GIF file!");
            if((buf[0]) != (byte)'G'
            || (buf[1]) != (byte)'I'
            || (buf[2]) != (byte)'F' )
            throw new SourceException("Not a valid GIF file!");

            int w1 = (buf[6] & 0xff) | (buf[6] & 0x80);
            int w2 = (buf[7] & 0xff) | (buf[7] & 0x80);
            int h1 = (buf[8] & 0xff) | (buf[8] & 0x80);
            int h2 = (buf[9] & 0xff) | (buf[9] & 0x80);

            int width = w1 + (w2 << 8);
            int height = h1 + (h2 << 8);

            int[] dim = { width, height };
            return dim;
        } catch (IOException ioe) {
            throw new SourceException("Could not read source", ioe);
        } finally {
            if (in != null) 
                try { 
                    in.close(); 
                } catch (Exception e) {}
        }
    }
}

