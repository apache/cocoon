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
package org.apache.cocoon.generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.cocoon.util.FileFormatException;
import org.apache.cocoon.util.ImageProperties;
import org.apache.cocoon.util.ImageUtils;

import org.xml.sax.SAXException;

/**
 * Generates an XML directory listing. This is an extension of
 * the {@link DirectoryGenerator} that adds extra attributes for image files.
 *
 * @cocoon.sitemap.component.documentation
 * Generates an XML directory listing. This is an extension of 
 * the {@link DirectoryGenerator} that adds extra attributes for image files.
 * @cocoon.sitemap.component.name   imagedirectory
 * @cocoon.sitemap.component.label  content
 * @cocoon.sitemap.component.documentation.caching Yes.
 * Uses the last modification date of the directory and the contained files
 * @cocoon.sitemap.component.pooling.max  16
 *
 * @version $Id$
 */
final public class ImageDirectoryGenerator extends DirectoryGenerator {

    protected static final String IMAGE_WIDTH_ATTR_NAME = "width";
    protected static final String IMAGE_HEIGHT_ATTR_NAME = "height";
    protected static final String IMAGE_COMMENT_ATTR_NAME = "comment";

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
