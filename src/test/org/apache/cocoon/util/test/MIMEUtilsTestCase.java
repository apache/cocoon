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
package org.apache.cocoon.util.test;

import java.util.HashMap;
import java.util.Map;
import java.io.StringReader;
import junit.framework.TestCase;

import org.apache.cocoon.util.MIMEUtils;

/**
 * Test Cases for the MIMEUtils class.
 * @see org.apache.cocoon.util.MIMEUtils
 * Specifically, code for testing the parsing of mime.types files.
 *
 * @author <a href="mailto:jefft@apache.org">Jeff Turner</a>
 * @version CVS $Id: MIMEUtilsTestCase.java,v 1.3 2004/03/08 14:04:20 cziegeler Exp $
 */
public class MIMEUtilsTestCase extends TestCase
{

    final String NL = System.getProperty("line.separator");
    Map mimeMap;
    Map extMap;
    final String M2E = "MIME to extension mappings";
    final String E2M = "Extension to MIME mappings";

    public MIMEUtilsTestCase(String name) {
        super(name);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(MIMEUtilsTestCase.class);
    }

    public void setUp() {
        mimeMap = new HashMap();
        extMap = new HashMap();
    }

    public void tearDown() {
        mimeMap = null;
        extMap = null;
    }

    public void tstGetMimeType() {
        assertEquals(E2M, "text/plain", MIMEUtils.getMIMEType(".txt"));
        assertEquals(E2M, "image/jpeg", MIMEUtils.getMIMEType(".jpg"));
        assertEquals(E2M, "video/mpeg", MIMEUtils.getMIMEType(".mpg"));
        assertEquals(E2M, null, MIMEUtils.getMIMEType(".undefined"));
        assertEquals(M2E, ".txt", MIMEUtils.getDefaultExtension("text/plain"));
        assertEquals(M2E, ".jpeg", MIMEUtils.getDefaultExtension("image/jpeg"));
        assertEquals(M2E, ".mpeg", MIMEUtils.getDefaultExtension("video/mpeg"));
        assertEquals(M2E, null, MIMEUtils.getDefaultExtension("application/octet-stream"));

    }

    public void testTypicalUsage() throws Exception {
        String mime_types="# MIME type mappings"+NL+
            "text/plain  txt text "+NL+
            "text/html   html htm"+NL+
            "   "+NL+
            "text/xml    xml"+NL+
            "text/css    css"+NL+
            "text/javascript		js "+NL+
            "application/x-javascript	js";

        MIMEUtils.loadMimeTypes(new StringReader(mime_types), extMap, mimeMap);
        assertEquals(".txt", extMap.get("text/plain"));
        assertEquals(".html", extMap.get("text/html"));
        assertEquals(".xml", extMap.get("text/xml"));
        assertEquals(".css", extMap.get("text/css"));
        assertEquals(".js", extMap.get("text/javascript"));
        assertEquals(".js", extMap.get("application/x-javascript"));

        assertEquals("text/plain", mimeMap.get(".text"));
        assertEquals("text/plain", mimeMap.get(".txt"));
        assertEquals("text/html", mimeMap.get(".html"));
        assertEquals("text/html", mimeMap.get(".htm"));
        assertEquals("text/xml", mimeMap.get(".xml"));
        assertEquals("text/css", mimeMap.get(".css"));
        assertEquals("text/javascript", mimeMap.get(".js"));

        assertEquals(M2E, 6, extMap.size());
        assertEquals(E2M, 7, mimeMap.size());
    }

    public void tstEmpty() throws Exception {
        String mime_types="";
        MIMEUtils.loadMimeTypes(new StringReader(mime_types), extMap, mimeMap);
        assertEquals(M2E, 0, extMap.size());
        assertEquals(E2M, 0, mimeMap.size());
    }

    public void tstCommentsAndWhitespace() throws Exception {
        String NL = System.getProperty("line.separator");
        String mime_types="## A commented line"+NL+
            "   "+NL+
            "# Another comment";
        MIMEUtils.loadMimeTypes(new StringReader(mime_types), extMap, mimeMap);
        assertEquals(M2E, 0, extMap.size());
        assertEquals(E2M, 0, mimeMap.size());
    }

    public void tstMimeTypeWithoutExtension() throws Exception {
        String NL = System.getProperty("line.separator");
        String mime_types=
            "text/plain  txt text"+NL+
            "application/octet-stream"+NL+NL;
        MIMEUtils.loadMimeTypes(new StringReader(mime_types), extMap, mimeMap);
        assertEquals(".txt", extMap.get("text/plain"));
        assertEquals("text/plain", mimeMap.get(".txt"));
        assertEquals("text/plain", mimeMap.get(".text"));
        assertEquals(M2E, 1, extMap.size());
        assertEquals(E2M, 2, mimeMap.size());
    }

}
