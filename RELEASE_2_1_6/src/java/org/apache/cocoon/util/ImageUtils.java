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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

/**
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @author <a href="mailto:balld@webslingerZ.com">Donald A. Ball Jr.</a>
 * @version CVS $Id: ImageUtils.java,v 1.4 2004/03/05 13:03:00 bdelacretaz Exp $
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

            int w1 = (buf[6] & 0xff) | (buf[6] & 0x80);
            int w2 = (buf[7] & 0xff) | (buf[7] & 0x80);
            int h1 = (buf[8] & 0xff) | (buf[8] & 0x80);
            int h2 = (buf[9] & 0xff) | (buf[9] & 0x80);

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
