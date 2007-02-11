/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache Cocoon" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
 * @version CVS $Id: JPEGSourceInspector.java,v 1.3 2003/03/24 14:33:54 stefano Exp $
 */
public class JPEGSourceInspector extends AbstractLogEnabled implements 
    SourceInspector, ThreadSafe {

    private String PROPERTY_NS = "http://xml.apache.org/cocoon/JPEGSourceInspector";
    private static String IMAGE_WIDTH_PROPERTY_NAME = "width";
    private static String IMAGE_HEIGHT_PROPERTY_NAME = "height";

    public SourceProperty getSourceProperty(Source source, String namespace, String name) 
        throws SourceException {

        if ((namespace.equals(PROPERTY_NS)) && 
            ((name.equals(IMAGE_WIDTH_PROPERTY_NAME)) || (name.equals(IMAGE_HEIGHT_PROPERTY_NAME))) && 
            (source.getURI().endsWith(".jpg")) && (isJPEGFile(source))) {

            if (name.equals(IMAGE_WIDTH_PROPERTY_NAME))
                return new SourceProperty(PROPERTY_NS, IMAGE_WIDTH_PROPERTY_NAME, 
                                          String.valueOf(getJpegSize(source)[0]));
            if (name.equals(IMAGE_HEIGHT_PROPERTY_NAME))
                return new SourceProperty(PROPERTY_NS, IMAGE_HEIGHT_PROPERTY_NAME,
                                          String.valueOf(getJpegSize(source)[1]));
        }
        return null;  
    }

    public SourceProperty[] getSourceProperties(Source source) throws SourceException {

        if ((source.getURI().endsWith(".jpg")) &&
            (isJPEGFile(source))) {
            int[] size = getJpegSize(source);
            return new SourceProperty[] {
                new SourceProperty(PROPERTY_NS, IMAGE_WIDTH_PROPERTY_NAME, String.valueOf(size[0])),
                new SourceProperty(PROPERTY_NS, IMAGE_HEIGHT_PROPERTY_NAME, String.valueOf(size[1]))
            };
        }
        return null;
    }

    private boolean isJPEGFile(Source source) throws SourceException {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(source.getInputStream());
            byte[] buf = new byte[2];
            int count = in.read(buf, 0, 2);
            if (count < 2) 
                return false;

            if ((buf[0] == (byte)0xFF) &&
                (buf[1] == (byte)0xD8))
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
     * returns width as first element, height as second
     */
    private int[] getJpegSize(Source source) throws SourceException {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(source.getInputStream());
            // check for "magic" header
            byte[] buf = new byte[2];
            int count = in.read(buf, 0, 2);
            if (count < 2) throw new SourceException("Not a valid Jpeg file!");
            if((buf[0]) != (byte)0xFF
            || (buf[1]) != (byte)0xD8 )
            throw new SourceException("Not a valid Jpeg file!");

            int width = 0;
            int height = 0;

            boolean done = false;
            int ch = 0;

            try {
                while(ch != 0xDA && !done) {
                    /* Find next marker (JPEG markers begin with 0xFF) */
                    while (ch != 0xFF) { ch = in.read(); }
                    /* JPEG markers can be padded with unlimited 0xFF's */
                    while (ch == 0xFF) { ch = in.read(); }
                    /* Now, ch contains the value of the marker. */
                    if(ch >= 0xC0 && ch <= 0xC3) {
                        // skip 3 bytes 
                        in.read();
                        in.read();
                        in.read();
                        height = 256 * in.read();
                        height += in.read();
                        width = 256 * in.read();
                        width += in.read();
                        done = true;
                    } else { 
                        /* We MUST skip variables, since FF's within variable names 
                           are NOT valid JPEG markers */
                        int length = 256 * in.read();
                        length += in.read();
                        if(length < 2) throw new RuntimeException("Erroneous JPEG marker length");
                        for(int foo = 0; foo<length-2; foo++)
                            in.read();
                    }
                }
            } catch (Exception e) {
                throw new SourceException("Not a valid Jpeg file!", e);
            }

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

