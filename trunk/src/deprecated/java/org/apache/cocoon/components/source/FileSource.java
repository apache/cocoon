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
package org.apache.cocoon.components.source;

import org.apache.avalon.framework.component.ComponentManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;

import java.io.*;
import java.net.MalformedURLException;
import java.util.ConcurrentModificationException;

/**
 * A <code>org.apache.cocoon.environment.WriteableSource</code> for 'file:/' system IDs.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version $Id: FileSource.java,v 1.1 2003/03/09 00:07:01 pier Exp $
 * @deprecated Use the new avalon source resolving instead
 */

public class FileSource extends AbstractStreamWriteableSource
    implements org.apache.cocoon.environment.WriteableSource {

    /** The underlying file. */
    private File file;

    /** The system ID for this source (lazily created by getSystemId()) */
    private String systemId = null;

    /** Is this an html file ? */
    private boolean isHTMLContent;

    /**
     * Create a file source from a 'file:' url and a component manager.
     */
    public FileSource(String url, ComponentManager manager) {

        super(manager);

        if (!url.startsWith("file:")) {
            throw new IllegalArgumentException("Malformed url for a file source : " + url);
        }

        if (url.endsWith(".htm") || url.endsWith(".html")) {
            this.isHTMLContent = true;
        }

        this.file = new File(url.substring(5)); // 5 == "file:".length()
    }

    public boolean exists() {
        return this.file.exists();
    }

    /**
     * Returns <code>true</code> if the file name ends with ".htm" or ".html".
     */
    protected boolean isHTMLContent() {
        return this.isHTMLContent;
    }

    /**
     * Return the unique identifer for this source
     */
    public String getSystemId() {
        if (this.systemId == null) {
            try {
                this.systemId = this.file.toURL().toExternalForm();
            } catch(MalformedURLException mue) {
                // Can this really happen ?
                this.systemId = "file:" + this.file.getPath();
            }
        }
        return this.systemId;
    }

    /**
     * Get the input stream for this source.
     */
    public InputStream getInputStream() throws IOException, ProcessingException {
        try {
            return new FileInputStream(this.file);
        } catch (FileNotFoundException e) {
            throw new ResourceNotFoundException("Resource not found "
                                                + getSystemId(), e);
        }
    }

    public long getLastModified() {
        return this.file.lastModified();
    }

    public long getContentLength() {
        return this.file.length();
    }

    /**
     * Get an output stream to write to this source. The output stream returned
     * actually writes to a temp file that replaces the real one on close. This
     * temp file is used as lock to forbid multiple simultaneous writes. The
     * real file is updated atomically when the output stream is closed.
     *
     * @throws ConcurrentModificationException if another thread is currently
     *         writing to this file.
     */
    public OutputStream getOutputStream() throws IOException, ProcessingException {

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
        return new FileSourceOutputStream(tmpFile);
    }

    /**
     * Always return <code>false</code>. To be redefined by implementations that support
     * <code>cancel()</code>.
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
     * Cancels the output stream.
     */
    public void cancel(OutputStream stream) throws Exception {
        if (stream instanceof FileSourceOutputStream) {
            FileSourceOutputStream fsos = (FileSourceOutputStream)stream;
            if (fsos.getSource() == this) {
                fsos.cancel();
                return;
            }
        }

        // Not a valid stream for this source
        throw new IllegalArgumentException("The stream is not associated to this source");
    }

    /**
     * A file outputStream that will rename the temp file to the destination file upon close()
     * and discard the temp file upon cancel().
     */
    private class FileSourceOutputStream extends FileOutputStream {

        private File tmpFile;
        private boolean isClosed = false;

        public FileSourceOutputStream(File tmpFile) throws IOException {
            super(tmpFile);
            this.tmpFile = tmpFile;
        }

        public FileSource getSource() {
            return FileSource.this;
        }

        public void close() throws IOException {
            super.close();

            try {
                // Delete destination file
                if (FileSource.this.file.exists()) {
                    FileSource.this.file.delete();
                }
                // Rename temp file to destination file
                tmpFile.renameTo(FileSource.this.file);

            } finally {
                // Ensure temp file is deleted, ie lock is released.
                // If there was a failure above, written data is lost.
                if (tmpFile.exists()) {
                    tmpFile.delete();
                }
                this.isClosed = true;
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
    }
}
