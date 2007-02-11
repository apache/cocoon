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
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @author <a href="mailto:balld@webslingerZ.com">Donald A. Ball Jr.</a>
 * @version CVS $Id: ImageUtils.java,v 1.2 2003/03/16 17:49:16 vgritsenko Exp $
 */
final public class ImageUtils {

    final public static ImageProperties getImageProperties(File file) throws FileNotFoundException, IOException, FileFormatException {
        String type = MIMEUtils.getMIMEType(file);

        if ("image/gif".equals(type)) {
             return (getGifProperties(file));
        }
        else if ("image/jpeg".equals(type)) {
            return (getJpegProperties(file));
        }
        else {
            return (null);
        }
    }

    final public static ImageProperties getJpegProperties(File file) throws FileNotFoundException, IOException, FileFormatException {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));

            // check for "magic" header
            byte[] buf = new byte[2];
            int count = in.read(buf, 0, 2);
            if (count < 2) {
                throw new FileFormatException("Not a valid Jpeg file!");
            }
            if ((buf[0]) != (byte) 0xFF || (buf[1]) != (byte) 0xD8) {
                throw new FileFormatException("Not a valid Jpeg file!");
            }

            int width = 0;
            int height = 0;
            char[] comment = null;

            boolean hasDims = false;
            boolean hasComment = false;
            int ch = 0;

            while (ch != 0xDA && !(hasDims && hasComment)) {
                /* Find next marker (JPEG markers begin with 0xFF) */
                while (ch != 0xFF) {
                    ch = in.read();
                }
                /* JPEG markers can be padded with unlimited 0xFF's */
                while (ch == 0xFF) {
                    ch = in.read();
                }
                /* Now, ch contains the value of the marker. */

                int length = 256 * in.read();
                length += in.read();
                if (length < 2) {
                    throw new FileFormatException("Not a valid Jpeg file!");
                }
                /* Now, length contains the length of the marker. */

                if (ch >= 0xC0 && ch <= 0xC3) {
                    in.read();
                    height = 256 * in.read();
                    height += in.read();
                    width = 256 * in.read();
                    width += in.read();
                    for (int foo = 0; foo < length - 2 - 5; foo++) {
                        in.read();
                    }
                    hasDims = true;
                }
                else if (ch == 0xFE) {
                    // that's the comment marker
                    comment = new char[length-2];
                    for (int foo = 0; foo < length - 2; foo++)
                        comment[foo] = (char) in.read();
                    hasComment = true;
                }
                else {
                    // just skip marker
                    for (int foo = 0; foo < length - 2; foo++) {
                        in.read();
                    }
                }
            }
            return (new ImageProperties(width, height, comment, "jpeg"));

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

    final public static ImageProperties getGifProperties(File file) throws FileNotFoundException, IOException, FileFormatException {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            byte[] buf = new byte[10];
            int count = in.read(buf, 0, 10);
            if (count < 10) {
                throw new FileFormatException("Not a valid Gif file!");
            }
            if ((buf[0]) != (byte) 'G' || (buf[1]) != (byte) 'I' || (buf[2]) != (byte) 'F') {
                throw new FileFormatException("Not a valid Gif file!");
            }

            int w1 = ((int) buf[6] & 0xff) | (buf[6] & 0x80);
            int w2 = ((int) buf[7] & 0xff) | (buf[7] & 0x80);
            int h1 = ((int) buf[8] & 0xff) | (buf[8] & 0x80);
            int h2 = ((int) buf[9] & 0xff) | (buf[9] & 0x80);

            int width = w1 + (w2 << 8);
            int height = h1 + (h2 << 8);

            return (new ImageProperties(width, height, null,"gif"));

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
}
