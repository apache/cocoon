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
package org.apache.cocoon.ant;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *   A output stream writing to a ByteArrayOutputStream, until FilOutputStream target is defined.
 *
 * @author    huber@apache.org
 * @version CVS $Id: DelayedFileOutputStream.java,v 1.2 2004/03/05 10:07:25 bdelacretaz Exp $
 */
public class DelayedFileOutputStream extends OutputStream {
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
    public DelayedFileOutputStream() {
        baos = new ByteArrayOutputStream();
        fos = null;
    }


    /**
     * Creates a file output stream to write to the file represented by the specified File object.
     *
     * @param  file                       The new fileOutputStream value
     * @exception  FileNotFoundException  thrown if creating of FileOutputStream fails
     */
    public void setFileOutputStream(File file) throws FileNotFoundException {
        if (fos == null) {
            fos = new FileOutputStream(file);
        }
    }


    /**
     * Creates a file output stream to write to the file represented by the specified File object.
     *
     * @param  file                       The new fileOutputStream value
     * @param  append                     The new fileOutputStream value
     * @exception  IOException            thrown if creating of FileOutputStream fails
     */
    public void setFileOutputStream(File file, boolean append) throws IOException {
        if (fos == null) {
            fos = new FileOutputStream(file.getCanonicalPath(), append);
        }
    }


    /**
     * Creates an output file stream to write to the specified file descriptor, which represents an existing connection to an actual file in the file system.
     *
     * @param  fdObj  The new fileOutputStream value
     */
    public void setFileOutputStream(FileDescriptor fdObj) {
        if (fos == null) {
            fos = new BufferedOutputStream(new FileOutputStream(fdObj));
        }
    }


    /**
     * Creates an output file stream to write to the file with the specified name.
     *
     * @param  name                       The new fileOutputStream value
     * @exception  FileNotFoundException  thrown if creating of FileOutputStream fails
     */
    public void setFileOutputStream(String name) throws FileNotFoundException {
        if (fos == null) {
            fos = new FileOutputStream(name);
        }
    }


    /**
     * Creates an output file stream to write to the file with the specified name.
     *
     * @param  name                       The new fileOutputStream value
     * @param  append                     The new fileOutputStream value
     * @exception  FileNotFoundException  thrown if creating of FileOutputStream fails
     */
    public void setFileOutputStream(String name, boolean append) throws FileNotFoundException {
        if (fos == null) {
            fos = new FileOutputStream(name, append);
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
            if (ioexception == null) {
                ioexception = ioe;
            }
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
            if (ioexception == null) {
                ioexception = ioe;
            }
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
            IOException ioexception = null;
            
            try {
                baos.flush();
                baos.writeTo(fos);
                baos.close();
            } catch (IOException ioe) {
                ioexception = ioe;
            } finally {
                baos = null;
            }
            
            if (ioexception != null) {
                throw ioexception;
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
}


