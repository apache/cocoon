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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

/**
 * A collection of <code>File</code>, <code>URL</code> and filename
 * utility methods
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:tk-cocoon@datas-world.de">Torsten Knodt</a>
 * @version CVS $Id: MIMEUtils.java,v 1.2 2003/05/15 08:30:02 stephan Exp $
 */
public class MIMEUtils {

    final private static HashMap extMap = new HashMap() {
        {
            put(null, "application/octet-stream");
            put(".text", "text/plain");
            put(".txt", "text/plain");
            put(".html", "text/html");
            put(".htm", "text/html");
            put(".xml", "text/xml");
            put(".css", "text/css");
            put(".rtf", "text/rtf");
            put(".wml", "text/vnd.wap.wml");
            put(".jpg", "image/jpeg");
            put(".jpeg", "image/jpeg");
            put(".jpe", "image/jpeg");
            put(".png", "image/png");
            put(".gif", "image/gif");
            put(".tif", "image/tiff");
            put(".tiff", "image/tiff");
            put(".svg", "image/svg-xml");
            put(".pdf", "application/pdf");
            put(".wrl", "model/vrml");
            put(".smil", "application/smil");
            put(".js", "application/x-javascript");
            put(".zip", "application/zip");
            put(".mpeg", "video/mpeg");
            put(".mpg", "video/mpeg");
            put(".mpe", "video/mpeg");
            put(".mov", "video/quicktime");
            put(".mid", "audio/midi");
            put(".mp3", "audio/mpeg");
            put(".vcf", "text/x-vcard");
            put(".rdf", "text/rdf");
        }
    };

    final private static HashMap mimeMap = new HashMap() {
        {
            put(null, null);
            put("text/plain", ".text");
            put("text/html", ".html");
            put("text/xml", ".xml");
            put("text/css", ".css");
            put("text/rtf", ".rtf");
            put("application/rtf", ".rtf");
            put("text/vnd.wap.wml", ".wml");
            put("image/jpeg", ".jpeg");
            put("image/jpg", ".jpeg");
            put("image/jpe", ".jpeg");
            put("image/png", ".png");
            put("image/gif", ".gif");
            put("image/tiff", ".tiff");
            put("image/tif", ".tiff");
            put("image/svg-xml", ".svg");
            put("image/svg+xml", ".svg");
            put("application/pdf", ".pdf");
            put("model/vrml", ".wrl");
            put("application/smil", ".smil");
            put("application/x-javascript", ".js");
            put("application/zip", ".zip");
            put("video/mpeg", ".mpeg");
            put("video/mpeg", ".mpe");
            put("video/mpeg", ".mpg");
            put("video/quicktime", ".mov");
            put("audio/midi", ".mid");
            put("audio/mpeg", ".mp3");
            put("text/x-vcard", ".vcf");
            put("text/rdf", ".rdf");
        }
    };

    /**
     * Return the MIME type for a given file.
     *
     * @param file File.
     *
     * @return MIME type.
     */
    public static String getMIMEType(final File file)
      throws FileNotFoundException, IOException {
        BufferedInputStream in = null;

        try {
            in = new BufferedInputStream(new FileInputStream(file));
            byte[] buf = new byte[3];
            int count = in.read(buf, 0, 3);

            if (count<3) {
                return (null);
            }

            if ((buf[0])==(byte) 'G' && (buf[1])==(byte) 'I' &&
                (buf[2])==(byte) 'F') {
                return ("image/gif");
            }

            if ((buf[0])==(byte) 0xFF && (buf[1])==(byte) 0xD8) {
                return ("image/jpeg");
            }

        } finally {
            if (in!=null) {
                try {
                    in.close();
                } catch (IOException e) {}
            }
        }
        final String name = file.getName();

        return getMIMEType(name.substring(name.lastIndexOf(".")));
    }

    /**
     * Return the MIME type for a given filename extension.
     *
     * @param ext Filename extension.
     *
     * @return MIME type.
     */
    public static String getMIMEType(final String ext) {
        return (String) extMap.get(ext);
    }

    /**
     * Return the default filename extension for a given MIME type.
     *
     * @param type MIME type.
     *
     * @return Filename extension.
     */
    public static String getDefaultExtension(final String type) {
        return (String) mimeMap.get(type);
    }
}
