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
package org.apache.cocoon.core.container.spring;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.Resource;

/**
 * Utility class for Spring resource handling
 * @version $Id$
 */
public class ResourceUtils {

    /**
     * Get the uri of a resource.
     * This method corrects the uri in the case of the file protocol
     * on windows.
     * @param resource The resource.
     * @return The uri.
     * @throws IOException
     */
    public static String getUri(Resource resource)
    throws IOException {
        if ( resource == null ) {
            return null;
        }
        return correctUri(resource.getURL().toExternalForm());
    }

    protected static String correctUri(String uri) {
        // if it is a file we have to recreate the url,
        // otherwise we get problems under windows with some file
        // references starting with "/DRIVELETTER" and some
        // just with "DRIVELETTER"
        if ( uri.startsWith("file:") ) {
            final File f = new File(uri.substring(5));
            return "file://" + f.getAbsolutePath();
        }
        return uri;
    }
}
