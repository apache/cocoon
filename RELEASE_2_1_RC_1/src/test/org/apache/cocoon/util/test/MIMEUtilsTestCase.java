/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: MIMEUtilsTestCase.java,v 1.2 2003/07/10 00:25:25 ghoward Exp $
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
