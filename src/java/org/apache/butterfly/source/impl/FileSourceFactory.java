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
package org.apache.butterfly.source.impl;

import java.util.Map;

import org.apache.butterfly.source.Source;
import org.apache.butterfly.source.SourceFactory;
import org.apache.butterfly.source.SourceUtil;
import org.apache.butterfly.source.URIAbsolutizer;


/**
 * Description of FileSourceFactory.
 * 
 * @version CVS $Id: FileSourceFactory.java,v 1.1 2004/07/23 08:47:20 ugo Exp $
 */
public class FileSourceFactory implements SourceFactory, URIAbsolutizer {

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.SourceFactory#getSource(java.lang.String, java.util.Map)
     */
    public Source getSource(String location, Map parameters) {
        return new FileSource(location);
    }

    public String absolutize(String baseURI, String location)
    {
        // Call the absolutize utility method with false for the normalizePath argument.
        // This avoids the removal of "../" from the path.
        // This way, the "../" will be resolved by the operating system, which might
        // do things differently e.g. in case of symbolic links.
        return SourceUtil.absolutize(baseURI, location, false, false);
    }

}
