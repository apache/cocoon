/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache Cocoon" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.cocoon.components.source.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ConcurrentModificationException;
import java.util.Map;

import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.impl.URLSource;

/**
 * A <code>ModifiableSource</code> for 'file:/' system IDs.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: FileSource.java,v 1.2 2003/03/16 17:49:13 vgritsenko Exp $
 */
public class FileSource
    extends URLSource
    implements ModifiableSource {

    /**
     * Initialize a new object from a <code>URL</code>.
     * @param parameters This is optional
     */
    public void init(URL url,
                     Map parameters )
    throws IOException {
        super.init( url, parameters );

        if ( null == this.file ) {
            throw new IllegalArgumentException("Malformed url for a file source : " + url);
        }
    }

    /**
     * Get the associated file
     */
    public File getFile() {
        return this.file;
    }

    /**
     * A file outputStream that will rename the temp file to the destination file upon close()
     * and discard the temp file upon cancel().
     */
    private class FileSourceOutputStream extends FileOutputStream {

        private File tmpFile;
        private boolean isClosed = false;
        private FileSource source;

        public FileSourceOutputStream(File tmpFile, FileSource source) throws IOException {
            super(tmpFile);
            this.tmpFile = tmpFile;
            this.source = source;
        }

        public void close() throws IOException {
            if (!this.isClosed) {
                super.close();
                try {
                    // Delete destination file
                    if (this.source.getFile().exists()) {
                        this.source.getFile().delete();
                    }
                    // Rename temp file to destination file
                    tmpFile.renameTo(this.source.getFile());
    
                } finally {
                    // Ensure temp file is deleted, ie lock is released.
                    // If there was a failure above, written data is lost.
                    if (tmpFile.exists()) {
                        tmpFile.delete();
                    }
                    this.isClosed = true;
                }
            }

        }

        public boolean canCancel() {
            return !this.isClosed;
        }

        public void cancel() throws Exception {
            if (this.isClosed) {
                throw new IllegalStateException("Cannot cancel : outputstrem is already closed");
            }

            this.isClosed = true;
            super.close();
            this.tmpFile.delete();
        }

        public void finalize() {
            if (!this.isClosed && tmpFile.exists()) {
                // Something wrong happened while writing : delete temp file
                tmpFile.delete();
            }
        }

        public FileSource getSource() {
            return this.source;
        }
    }

    /**
     * Does this source actually exist ?
     *
     * @return true if the resource exists.
     */
    public boolean exists() {
        return this.file.exists();
    }

    /**
     * Get an <code>InputStream</code> where raw bytes can be written to.
     * The signification of these bytes is implementation-dependent and
     * is not restricted to a serialized XML document.
     *
     * Get an output stream to write to this source. The output stream returned
     * actually writes to a temp file that replaces the real one on close. This
     * temp file is used as lock to forbid multiple simultaneous writes. The
     * real file is updated atomically when the output stream is closed.
     *
     * @return a stream to write to
     * @throws ConcurrentModificationException if another thread is currently
     *         writing to this file.
     */
    public OutputStream getOutputStream()
    throws IOException, SourceException {
        // Create a temp file. It will replace the right one when writing terminates,
        // and serve as a lock to prevent concurrent writes.
        File tmpFile = new File(this.file.getPath() + ".tmp");

        // Ensure the directory exists
        tmpFile.getParentFile().mkdirs();

        // Can we write the file ?
        if (this.file.exists() && !this.file.canWrite()) {
            throw new IOException("Cannot write to file " + this.file.getPath());
        }

        // Check if it temp file already exists, meaning someone else currently writing
        if (!tmpFile.createNewFile()) {
            throw new ConcurrentModificationException("File " + this.file.getPath() +
              " is already being written by another thread");
        }

        // Return a stream that will rename the temp file on close.
        return new FileSourceOutputStream(tmpFile, this);
    }

    /**
     * Can the data sent to an <code>OutputStream</code> returned by
     * {@link #getOutputStream()} be cancelled ?
     *
     * @return true if the stream can be cancelled
     */
    public boolean canCancel(OutputStream stream) {
        if (stream instanceof FileSourceOutputStream) {
            FileSourceOutputStream fsos = (FileSourceOutputStream)stream;
            if (fsos.getSource() == this) {
                return fsos.canCancel();
            }
        }

        // Not a valid stream for this source
        throw new IllegalArgumentException("The stream is not associated to this source");
    }

    /**
     * Cancel the data sent to an <code>OutputStream</code> returned by
     * {@link #getOutputStream()}.
     * <p>
     * After cancel, the stream should no more be used.
     */
    public void cancel(OutputStream stream) throws SourceException {
        if (stream instanceof FileSourceOutputStream) {
            FileSourceOutputStream fsos = (FileSourceOutputStream)stream;
            if (fsos.getSource() == this) {
                try {
                    fsos.cancel();
                } catch (Exception e) {
                    throw new SourceException("Exception during cancel.", e);
                }
                return;
            }
        }

        // Not a valid stream for this source
        throw new IllegalArgumentException("The stream is not associated to this source");
    }

    /**
     * Delete the source.
     */
    public boolean delete()  {
        return this.file.delete();
    }
}
