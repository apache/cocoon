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
 * @version CVS $Id: MIMEUtils.java,v 1.7 2004/03/05 13:03:00 bdelacretaz Exp $
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
