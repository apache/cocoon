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
package org.apache.cocoon.components.source.impl;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;

/**
 * This source inspector adds extra attributes for image files.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:balld@webslingerZ.com">Donald A. Ball Jr.</a>
 * @version CVS $Id: JPEGSourceInspector.java,v 1.4 2004/03/05 13:02:21 bdelacretaz Exp $
 */
public class JPEGSourceInspector extends AbstractImageSourceInspector implements ThreadSafe {


    public JPEGSourceInspector() {
    }
    
    /**
     * Checks the source uri for the .jp(e)g extension.
     */
    protected final boolean isImageMimeType(Source source) {
        final String uri = source.getURI();
        final int index = uri.lastIndexOf('.');
        if (index != -1) {
            String extension = uri.substring(index);
            return extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".JPEG");
        }
        return false;
    }
    
    /**
     * Checks that this is in fact a jpeg file.
     */
    protected final boolean isImageFileType(Source source) throws SourceException {
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
    protected final int[] getImageSize(Source source) throws SourceException {
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

