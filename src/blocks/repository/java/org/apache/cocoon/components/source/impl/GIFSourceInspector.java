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
 * @version CVS $Id: GIFSourceInspector.java,v 1.4 2004/03/05 13:02:21 bdelacretaz Exp $
 */
public class GIFSourceInspector extends AbstractImageSourceInspector implements ThreadSafe {
    
    
    public GIFSourceInspector() {
    }
    
    /**
     * Checks the source uri for the .gif extension.
     */
    protected final boolean isImageMimeType(Source source) {
        final String uri = source.getURI();
        final int index = uri.lastIndexOf('.');
        if (index != -1) {
            String extension = uri.substring(index);
            return extension.equalsIgnoreCase(".gif");
        }
        return false;
    }
    
    /**
     * Checks that this is in fact a gif file.
     */
    protected final boolean isImageFileType(Source source) throws SourceException {
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
    protected final int[] getImageSize(Source source) throws SourceException {
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

