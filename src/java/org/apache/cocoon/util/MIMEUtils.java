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
package org.apache.cocoon.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A collection of <code>File</code>, <code>URL</code> and filename
 * utility methods.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:tk-cocoon@datas-world.de">Torsten Knodt</a>
 * @author <a href="mailto:jefft@apache.org">Jeff Turner</a>
 * @version CVS $Id: MIMEUtils.java,v 1.6 2003/07/03 21:35:43 joerg Exp $
 */
public class MIMEUtils {

    private static final String MIME_MAPPING_FILE = "org/apache/cocoon/util/mime.types";

    /** Default extensions for MIME types. */
    final private static Map extMap = new HashMap();
    /** MIME types for extensions. */
    final private static Map mimeMap = new HashMap();

    /**
     * Load the MIME type mapping
     */
    static {
        try {
            final InputStream is = MIMEUtils.class.getClassLoader().getResourceAsStream(MIME_MAPPING_FILE);
            if (null == is) {
                throw new RuntimeException("Cocoon cannot load MIME type mappings from " + MIME_MAPPING_FILE);
            }
            loadMimeTypes(new InputStreamReader(is), extMap, mimeMap);
        } catch (IOException ioe) {
            throw new RuntimeException("Cocoon cannot load MIME type mappings from " + MIME_MAPPING_FILE);
        }
    }

    /**
     * Return the MIME type for a given file.
     *
     * @param file File.
     * @return MIME type.
     */
    public static String getMIMEType(final File file)
            throws FileNotFoundException, IOException {
        BufferedInputStream in = null;

        try {
            in = new BufferedInputStream(new FileInputStream(file));
            byte[] buf = new byte[3];
            int count = in.read(buf, 0, 3);

            if (count < 3) {
                return (null);
            }

            if ((buf[0]) == (byte)'G' && (buf[1]) == (byte)'I' && (buf[2]) == (byte)'F') {
                return ("image/gif");
            }

            if ((buf[0]) == (byte)0xFF && (buf[1]) == (byte)0xD8) {
                return ("image/jpeg");
            }

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        final String name = file.getName();
        int index = name.lastIndexOf(".");
        String fileExt = ".";
        if (index != -1) {
            fileExt = name.substring(index);
        }
        return getMIMEType(fileExt);
    }

    /**
     * Return the MIME type for a given filename extension.
     *
     * @param ext Filename extension.
     *
     * @return MIME type.
     */
    public static String getMIMEType(final String ext) {
        return (String)mimeMap.get(ext);
    }

    /**
     * Return the default filename extension for a given MIME type.
     *
     * @param type MIME type.
     *
     * @return Filename extension.
     */
    public static String getDefaultExtension(final String type) {
        return (String)extMap.get(type);
    }

    /**
     * Parses a <code>mime.types</code> file, and generates mappings between
     * MIME types and extensions.
     * For example, if a line contains:
     * <pre>text/html   html htm</pre>
     * Then 'html' will be the default extension for text/html, and both 'html'
     * and 'htm' will have MIME type 'text/html'.
     * Lines starting with '#' are treated as comments and ignored.  If an
     * extension is listed for two MIME types, the first will be chosen.
     *
     * @param in Reader of bytes from <code>mime.types</code> file content
     * @param extMap Empty map of default extensions, keyed by MIME type. Will
     * be filled in by this method.
     * @param mimeMap Empty map of MIME types, keyed by extension.  Will be
     * filled in by this method.
     */
    public static void loadMimeTypes(Reader in, Map extMap, Map mimeMap) throws IOException {
        BufferedReader br = new BufferedReader(in);
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }
            if (line.trim().equals("")) {
                continue;
            }
            StringTokenizer tok = new StringTokenizer(line, " \t");
            String mimeType = tok.nextToken();
            if (tok.hasMoreTokens()) {
                String defaultExt = tok.nextToken();
                if (!extMap.containsKey(mimeType)) {
                    extMap.put(mimeType, "." + defaultExt);
                }
                if (!mimeMap.containsKey("." + defaultExt)) {
                    mimeMap.put("." + defaultExt, mimeType);
                }
                while (tok.hasMoreTokens()) {
                    String ext = tok.nextToken();
                    if (!mimeMap.containsKey("." + ext)) {
                        mimeMap.put("." + ext, mimeType);
                    }
                }
            }
        }
    }
}
