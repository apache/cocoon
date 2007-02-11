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
 * @version CVS $Id: MIMEUtils.java,v 1.1 2003/03/09 00:09:43 pier Exp $
 */

public class MIMEUtils {

    public static String getMIMEType(File file) throws FileNotFoundException, IOException {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            byte[] buf = new byte[3];
            int count = in.read(buf, 0, 3);

            if (count < 3)
                return (null);

            if ((buf[0]) == (byte) 'G' && (buf[1]) == (byte) 'I' && (buf[2]) == (byte) 'F')
                return ("image/gif");

            if ((buf[0]) == (byte) 0xFF && (buf[1]) == (byte) 0xD8)
                return ("image/jpeg");

            return (null);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                }
            }
        }
    }

    public static String getMIMEType(String ext) {
        // TODO
        return null;
    }

    public static String getDefaultExtension(String type) {
        if (type == null) {
            return ".html";
        } else if ("text/html".equals(type)) {
            return ".html";
        } else if ("text/xml".equals(type)) {
            return ".xml";
        } else if ("text/css".equals(type)) {
            return ".css";
        } else if ("text/vnd.wap.wml".equals(type)) {
            return ".wml";
        } else if ("image/jpg".equals(type)) {
            return ".jpg";
        } else if ("image/jpeg".equals(type)) {
            return ".jpg";
        } else if ("image/png".equals(type)) {
            return ".png";
        } else if ("image/gif".equals(type)) {
            return ".gif";
        } else if ("image/svg-xml".equals(type)) {
            return ".svg";
        } else if ("application/pdf".equals(type)) {
            return ".pdf";
        } else if ("model/vrml".equals(type)) {
            return ".wrl";
        } else if ("text/plain".equals(type)) {
            return ".txt";
        } else if ("application/rtf".equals(type)) {
            return ".rtf";
        } else if ("text/rtf".equals(type)) {
            return ".rtf";
        } else if ("application/smil".equals(type)) {
            return ".smil";
        } else if ("application/x-javascript".equals(type)) {
            return ".js";
        } else if ("application/zip".equals(type)) {
            return ".zip";
        } else if ("video/mpeg".equals(type)) {
            return ".mpg";
        } else if ("video/quicktime".equals(type)) {
            return ".mov";
        } else if ("audio/midi".equals(type)) {
            return ".mid";
        } else if ("audio/mpeg".equals(type)) {
            return ".mp3";
        } else if ("text/x-vcard".equals(type)) {
            return ".vcf";
        } else if ("text/rdf".equals(type)) {
            return ".rdf";
        } else {
            return "";
        }
    }
}
