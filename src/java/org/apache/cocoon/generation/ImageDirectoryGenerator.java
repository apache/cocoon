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
package org.apache.cocoon.generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.cocoon.util.FileFormatException;
import org.apache.cocoon.util.ImageProperties;
import org.apache.cocoon.util.ImageUtils;

import org.xml.sax.SAXException;

/**
 * An extension of DirectoryGenerators that adds extra attributes for image
 * files.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald A. Ball Jr.</a>
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id: ImageDirectoryGenerator.java,v 1.2 2004/03/05 13:02:55 bdelacretaz Exp $
 */
final public class ImageDirectoryGenerator extends DirectoryGenerator {

    protected static String IMAGE_WIDTH_ATTR_NAME = "width";
    protected static String IMAGE_HEIGHT_ATTR_NAME = "height";
    protected static String IMAGE_COMMENT_ATTR_NAME = "comment";

    /**
     * Extends the <code>setNodeAttributes</code> method from the
     * <code>DirectoryGenerator</code> by adding width, height and comment attributes
     * if the path is a GIF or a JPEG file.
     */
    protected void setNodeAttributes(File path) throws SAXException {
        super.setNodeAttributes(path);
        if (path.isDirectory()) {
            return;
        }
        try {
            ImageProperties p = ImageUtils.getImageProperties(path);
            if (p != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(String.valueOf(path) + " = " + String.valueOf(p));
                }
                attributes.addAttribute("", IMAGE_WIDTH_ATTR_NAME, IMAGE_WIDTH_ATTR_NAME, "CDATA", String.valueOf(p.width));
                attributes.addAttribute("", IMAGE_HEIGHT_ATTR_NAME, IMAGE_HEIGHT_ATTR_NAME, "CDATA", String.valueOf(p.height));
                if (p.comment != null) attributes.addAttribute("", IMAGE_COMMENT_ATTR_NAME, IMAGE_COMMENT_ATTR_NAME, "CDATA", String.valueOf(p.comment));
            }
        }
        catch (FileFormatException e) {
            throw new SAXException(e);
        }
        catch (FileNotFoundException e) {
            throw new SAXException(e);
        }
        catch (IOException e) {
            throw new SAXException(e);
        }
    }

}
