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
package org.apache.cocoon.bean.helpers;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 *   A output stream writing to a ByteArrayOutputStream, until an OutputStream target is defined.
 *
 * @author    huber@apache.org
 * @author    uv@upaya.co.uk
 * @version CVS $Id: DelayedOutputStream.java,v 1.5 2003/10/07 09:59:17 upayavira Exp $
 */
public class DelayedOutputStream extends OutputStream {
    /**
     * write to baos as long as fos member is still null,
     * create a ByteArrayOutputStream only
     */
    private ByteArrayOutputStream baos;
    /**
     * If fos is defined write into fos, dump content of
     * baos to fos first
     */
    private OutputStream fos;


    /**
     * Constructor for the DelayedFileOutputStream object,
     * create a ByteArrayOutputStream only
     */
    public DelayedOutputStream() {
        baos = new ByteArrayOutputStream();
        fos = null;
    }

    /**
     * Creates a file output stream to write to the file represented by the specified File object.
     *
     * @param  outputStream               The new fileOutputStream value
     * @exception  FileNotFoundException  thrown if creating of FileOutputStream fails
     */
    public void setFileOutputStream(OutputStream outputStream) throws FileNotFoundException {
        if (fos == null) {
            fos = outputStream;
        }
    }

    /**
     *   Write into ByteArrayOutputStrem, or FileOutputStream, depending on inner
     *   state of this stream
     *
     * @param  b                Description of Parameter
     * @exception  IOException  thrown iff implicitly flush of baos to fos fails, or writing
     *   of baos, or fos fails
     */
    public void write(int b) throws IOException {
        OutputStream os = getTargetOutputStream();
        os.write(b);
    }


    /**
     *   Write into ByteArrayOutputStrem, or FileOutputStream, depending on inner
     *   state of this stream
     *
     * @param  b                Description of Parameter
     * @exception  IOException  thrown iff implicitly flush of baos to fos fails, or writing
     *   of baos, or fos fails
     */
    public void write(byte b[]) throws IOException {
        OutputStream os = getTargetOutputStream();
        os.write(b);
    }


    /**
     *   Write into ByteArrayOutputStrem, or FileOutputStream, depending on inner
     *   state of this stream
     *
     * @param  b                Description of Parameter
     * @param  off              Description of Parameter
     * @param  len              Description of Parameter
     * @exception  IOException  thrown iff implicitly flush of baos to fos fails, or writing
     *   of baos, or fos fails
     */
    public void write(byte b[], int off, int len) throws IOException {
        OutputStream os = getTargetOutputStream();
        os.write(b, off, len);
    }


    /**
     *   Close ByteArrayOutputStrem, and FileOutputStream, depending on inner
     *   state of this stream
     *
     * @exception  IOException  thrown iff implicitly flush of baos to fos fails, or closing
     *   of baos, or fos fails
     */
    public void close() throws IOException {
        IOException ioexception = null;

        getTargetOutputStream();

        // close baos
        try {
            if (baos != null) {
                baos.close();
            }
        } catch (IOException ioe) {
            ioexception = ioe;
        } finally {
            baos = null;
        }

        // close fos
        try {
            if (fos != null) {
                fos.close();
            }
        } catch (IOException ioe) {
            if (ioexception == null) {
                ioexception = ioe;
            }
        } finally {
            fos = null;
        }

        if (ioexception != null) {
            throw ioexception;
        }
    }


    /**
     *   Flush ByteArrayOutputStrem, writing content to FileOutputStream,
     *   flush FileOutputStream
     *
     * @exception  IOException  thrown iff implicitly flush of baos to fos fails, or flushing
     *   of baos, or fos fails
     */
    public void flush() throws IOException {
        IOException ioexception = null;

        // flush baos, writing to fos, if neccessary
        getTargetOutputStream();

        // flush baos
        try {
            if (baos != null) {
                baos.flush();
            }
        } catch (IOException ioe) {
            ioexception = ioe;
        }

        // flush fos
        try {
            if (fos != null) {
                fos.flush();
            }
        } catch (IOException ioe) {
            if (ioexception == null) {
                ioexception = ioe;
            }
        }
        if (ioexception != null) {
            throw ioexception;
        }
    }


    /**
     *   Gets the targetOutputStream attribute of the DelayedFileOutputStream object
     *
     * @return                  The targetOutputStream value
     * @exception  IOException  thrown iff implicitly flush of baos to fos fails
     */
    private OutputStream getTargetOutputStream() throws IOException {
        if (baos != null && fos == null) {

            // no fos is defined, just write to baos in the mean time
            return baos;
        } else if (baos != null && fos != null) {
            // fos is defined, flush boas to fos, and destroy baos
            try {
                baos.flush();
                baos.writeTo(fos);
                baos.close();
            } finally {
                baos = null;
            }

            return fos;
        } else if (baos == null && fos != null) {
            // no more temporary baos writing, write directly to fos
            return fos;
        } else {
            // neither baos, nor fos are valid
            throw new IOException("No outputstream available!");
        }
    }

    /**
     * Gets the size of the content of the current output stream
     */
    public int size() {
        if (baos != null) {
            return baos.size();
        }
        return 0;
    }

    /**
     * Return the contents of the stream as a byte array
     */
    public byte[] getContent() {
        if (baos != null) {
            return baos.toByteArray();
        } else {
            return null;
        }
    }
}
