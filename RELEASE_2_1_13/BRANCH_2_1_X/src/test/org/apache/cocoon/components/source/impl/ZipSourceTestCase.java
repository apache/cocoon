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
package org.apache.cocoon.components.source.impl;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.core.container.ContainerTestCase;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;

public class ZipSourceTestCase extends ContainerTestCase {

    public void testURIHandling() throws Exception {
        final String zipFilePath = "/test.zip";
        final String filePath = "test.xml";
        final String zipSourceUri = "zip:file:/" + zipFilePath + "!/" + filePath;
        Source zipSource;
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver) getManager().lookup(SourceResolver.ROLE);
            zipSource = resolver.resolveURI(zipSourceUri);
        } catch (ServiceException se) {
            throw new SourceException("SourceResolver is not available.", se);
        } finally {
            getManager().release(resolver);
        }
        assertTrue("Resolved Source is not an instance of ZipSource.",
                   zipSource instanceof ZipSource);
        assertEquals("Scheme/protocol is wrong.", "zip", zipSource.getScheme());
        // test file path
        int index = zipSource.getURI().lastIndexOf(':');
        String testFilePath = zipSource.getURI().substring(index + 2);
        // depending on the operation system, the uri can either contain two or three slashes
        // so we remove all slashed and then append one slash
        while ( testFilePath.startsWith("/") ) {
            testFilePath = testFilePath.substring(1);
        }
        testFilePath = "/" + testFilePath;
        assertEquals("Uri is wrong.", zipSourceUri, zipSource.getURI().substring(0, index + 2) + testFilePath);
    }

}
