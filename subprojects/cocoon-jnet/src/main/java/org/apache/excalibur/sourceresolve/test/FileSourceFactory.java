/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.excalibur.sourceresolve.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.URIAbsolutizer;

/**
 * A factory for filesystem-based sources (see {@link FileSource}).
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id$
 */
public class FileSourceFactory implements SourceFactory, URIAbsolutizer {

    /**
     * @see org.apache.excalibur.source.SourceFactory#getSource(java.lang.String,
     *      java.util.Map)
     */
    public Source getSource(String location, Map parameters) throws IOException, MalformedURLException {
        return new FileSource(location);
    }

    /**
     * Does nothing, since {@link FileSource}s don't need to be released.
     * 
     * @see org.apache.excalibur.source.SourceFactory#release(org.apache.excalibur.source.Source)
     */
    public void release(Source source) {
        // Nothing to do here
    }

    public String absolutize(String baseURI, String location) {
        // Call the absolutize utility method with false for the normalizePath
        // argument.
        // This avoids the removal of "../" from the path.
        // This way, the "../" will be resolved by the operating system, which
        // might
        // do things differently e.g. in case of symbolic links.
        return SourceUtil.absolutize(baseURI, location, false, false);
    }
}
