/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.excalibur.sourceresolve.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;

import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.ModifiableTraversableSource;
import org.apache.excalibur.source.MoveableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.SourceValidity;

/**
 * A {@link ModifiableTraversableSource} for filesystem objects.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id$
 */

public class FileSource implements ModifiableTraversableSource, MoveableSource {

    /** The file */
    private File m_file;

    /** The scheme */
    private String m_scheme;

    /** The URI of this source */
    private String m_uri;

    /**
     * Builds a FileSource given an URI, which doesn't necessarily have to start
     * with "file:"
     * 
     * @param uri
     * @throws SourceException
     * @throws MalformedURLException
     */
    public FileSource(String uri) throws SourceException, MalformedURLException {
        int pos = SourceUtil.indexOfSchemeColon(uri);
        if (pos == -1) {
            throw new MalformedURLException("Invalid URI : " + uri);
        }

        String scheme = uri.substring(0, pos);
        String fileName = uri.substring(pos + 1);
        fileName = SourceUtil.decodePath(fileName);
        this.init(scheme, new File(fileName));
    }

    /**
     * Builds a FileSource, given an URI scheme and a File.
     * 
     * @param scheme
     * @param file
     * @throws SourceException
     */
    public FileSource(String scheme, File file) throws SourceException {
        this.init(scheme, file);
    }

    private void init(String scheme, File file) throws SourceException {
        this.m_scheme = scheme;

        String uri;
        try {
            uri = file.toURL().toExternalForm();
            // toExternalForm() is buggy, see e.g.
            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4924415
            // therefore we check if file: is followed by just one slash
            // TODO when we move to JDK 1.4+, we should use
            // file.toURI().toASCIIString() instead.
            if (uri.length() > 6 && uri.startsWith("file:/") && uri.charAt(6) != '/') {
                uri = "file:///" + uri.substring(6);
            }
        } catch (MalformedURLException mue) {
            // Can this really happen ?
            throw new SourceException("Failed to get URL for file " + file, mue);
        }

        if (!uri.startsWith(scheme)) {
            // Scheme is not "file:"
            uri = scheme + ':' + uri.substring(uri.indexOf(':') + 1);
        }

        this.m_uri = uri;

        this.m_file = file;
    }

    /**
     * Get the associated file
     */
    public File getFile() {
        return this.m_file;
    }

    // ----------------------------------------------------------------------------------
    // Source interface methods
    // ----------------------------------------------------------------------------------

    /**
     * @see org.apache.excalibur.source.Source#getContentLength()
     */
    public long getContentLength() {
        return this.m_file.length();
    }

    /**
     * @see org.apache.excalibur.source.Source#getInputStream()
     */
    public InputStream getInputStream() throws IOException, SourceNotFoundException {
        try {
            return new FileInputStream(this.m_file);
        } catch (FileNotFoundException fnfe) {
            throw new SourceNotFoundException(this.m_uri + " doesn't exist.", fnfe);
        }
    }

    /**
     * @see org.apache.excalibur.source.Source#getLastModified()
     */
    public long getLastModified() {
        return this.m_file.lastModified();
    }

    /**
     * @see org.apache.excalibur.source.Source#getMimeType()
     */
    public String getMimeType() {
        return URLConnection.getFileNameMap().getContentTypeFor(this.m_file.getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.excalibur.source.Source#getScheme()
     */
    public String getScheme() {
        return this.m_scheme;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.excalibur.source.Source#getURI()
     */
    public String getURI() {
        return this.m_uri;
    }

    /*
     * FIXME don't use specific SourceValidity stuff here!
     */
    /**
     * Return a validity object based on the file's modification date.
     * 
     * @see org.apache.excalibur.source.Source#getValidity()
     */
    public SourceValidity getValidity() {
        return null;
    }

    /**
     * @see org.apache.excalibur.source.Source#refresh()
     */
    public void refresh() {
        // Nothing to do...
    }

    /**
     * Does this source actually exist ?
     * 
     * @return true if the resource exists.
     */
    public boolean exists() {
        return this.getFile().exists();
    }

    // ----------------------------------------------------------------------------------
    // TraversableSource interface methods
    // ----------------------------------------------------------------------------------

    /**
     * @see org.apache.excalibur.source.TraversableSource#getChild(java.lang.String)
     */
    public Source getChild(String name) throws SourceException {
        if (!this.m_file.isDirectory()) {
            throw new SourceException(this.getURI() + " is not a directory");
        }

        return new FileSource(this.getScheme(), new File(this.m_file, name));

    }

    /**
     * @see org.apache.excalibur.source.TraversableSource#getChildren()
     */
    public Collection getChildren() throws SourceException {

        if (!this.m_file.isDirectory()) {
            throw new SourceException(this.getURI() + " is not a directory");
        }

        // Build a FileSource object for each of the children
        File[] files = this.m_file.listFiles();

        FileSource[] children = new FileSource[files.length];
        for (int i = 0; i < files.length; i++) {
            children[i] = new FileSource(this.getScheme(), files[i]);
        }

        // Return it as a list
        return Arrays.asList(children);
    }

    /**
     * @see org.apache.excalibur.source.TraversableSource#getName()
     */
    public String getName() {
        return this.m_file.getName();
    }

    /**
     * @see org.apache.excalibur.source.TraversableSource#getParent()
     */
    public Source getParent() throws SourceException {
        return new FileSource(this.getScheme(), this.m_file.getParentFile());
    }

    /**
     * @see org.apache.excalibur.source.TraversableSource#isCollection()
     */
    public boolean isCollection() {
        return this.m_file.isDirectory();
    }

    // ----------------------------------------------------------------------------------
    // ModifiableSource interface methods
    // ----------------------------------------------------------------------------------

    /**
     * Get an <code>InputStream</code> where raw bytes can be written to. The
     * signification of these bytes is implementation-dependent and is not
     * restricted to a serialized XML document.
     * 
     * The output stream returned actually writes to a temp file that replaces
     * the real one on close. This temp file is used as lock to forbid multiple
     * simultaneous writes. The real file is updated atomically when the output
     * stream is closed.
     * 
     * The returned stream must be closed or cancelled by the calling code.
     * 
     * @return a stream to write to
     * @throws ConcurrentModificationException
     *             if another thread is currently writing to this file.
     */
    public OutputStream getOutputStream() throws IOException {
        // Create a temp file. It will replace the right one when writing
        // terminates,
        // and serve as a lock to prevent concurrent writes.
        File tmpFile = new File(this.getFile().getPath() + ".tmp");

        // Ensure the directory exists
        tmpFile.getParentFile().mkdirs();

        // Can we write the file ?
        if (this.getFile().exists() && !this.getFile().canWrite()) {
            throw new IOException("Cannot write to file " + this.getFile().getPath());
        }

        // Check if it temp file already exists, meaning someone else currently
        // writing
        if (!tmpFile.createNewFile()) {
            throw new ConcurrentModificationException("File " + this.getFile().getPath()
                    + " is already being written by another thread");
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
            FileSourceOutputStream fsos = (FileSourceOutputStream) stream;
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
            FileSourceOutputStream fsos = (FileSourceOutputStream) stream;
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
    public void delete() throws SourceException {
        if (!this.m_file.exists()) {
            throw new SourceNotFoundException("Cannot delete non-existing file " + this.m_file.toString());
        }

        if (!this.m_file.delete()) {
            throw new SourceException("Could not delete " + this.m_file.toString() + " (unknown reason)");
        }
    }

    // ----------------------------------------------------------------------------------
    // ModifiableTraversableSource interface methods
    // ----------------------------------------------------------------------------------

    /**
     * @see org.apache.excalibur.source.ModifiableTraversableSource#makeCollection()
     */
    public void makeCollection() throws SourceException {
        this.m_file.mkdirs();
    }

    // ----------------------------------------------------------------------------------
    // MoveableSource interface methods
    // ----------------------------------------------------------------------------------

    /**
     * @see org.apache.excalibur.source.MoveableSource#copyTo(org.apache.excalibur.source.Source)
     */
    public void copyTo(Source destination) throws SourceException {
        try {
            SourceUtil.copy(this.getInputStream(), ((ModifiableSource) destination).getOutputStream());
        } catch (IOException ioe) {
            throw new SourceException("Couldn't copy " + this.getURI() + " to " + destination.getURI(), ioe);
        }
    }

    /**
     * @see org.apache.excalibur.source.MoveableSource#moveTo(org.apache.excalibur.source.Source)
     */
    public void moveTo(Source destination) throws SourceException {
        if (destination instanceof FileSource) {
            final File dest = ((FileSource) destination).getFile();
            final File parent = dest.getParentFile();

            if (parent != null) {
                parent.mkdirs(); // ensure parent directories exist
            }

            if (!this.m_file.renameTo(dest)) {
                throw new SourceException("Couldn't move " + this.getURI() + " to " + destination.getURI());
            }
        } else {
            SourceUtil.move(this, destination);
        }

    }

    // ----------------------------------------------------------------------------------
    // Private helper class for ModifiableSource implementation
    // ----------------------------------------------------------------------------------

    /**
     * A file outputStream that will rename the temp file to the destination
     * file upon close() and discard the temp file upon cancel().
     */
    private static class FileSourceOutputStream extends FileOutputStream {

        private File m_tmpFile;

        private boolean m_isClosed = false;

        private FileSource m_source;

        public FileSourceOutputStream(File tmpFile, FileSource source) throws IOException {
            super(tmpFile);
            this.m_tmpFile = tmpFile;
            this.m_source = source;
        }

        @Override
        public void close() throws IOException {
            if (!this.m_isClosed) {
                super.close();
                try {
                    // Delete destination file
                    if (this.m_source.getFile().exists()) {
                        this.m_source.getFile().delete();
                    }
                    // Rename temp file to destination file
                    if (!this.m_tmpFile.renameTo(this.m_source.getFile())) {
                        throw new IOException("Could not rename " + this.m_tmpFile.getAbsolutePath() + " to "
                                + this.m_source.getFile().getAbsolutePath());
                    }

                } finally {
                    // Ensure temp file is deleted, ie lock is released.
                    // If there was a failure above, written data is lost.
                    if (this.m_tmpFile.exists()) {
                        this.m_tmpFile.delete();
                    }
                    this.m_isClosed = true;
                }
            }

        }

        public boolean canCancel() {
            return !this.m_isClosed;
        }

        public void cancel() throws Exception {
            if (this.m_isClosed) {
                throw new IllegalStateException("Cannot cancel : outputstrem is already closed");
            }

            this.m_isClosed = true;
            super.close();
            this.m_tmpFile.delete();
        }

        @Override
        public void finalize() {
            if (!this.m_isClosed && this.m_tmpFile.exists()) {
                // Something wrong happened while writing : delete temp file
                this.m_tmpFile.delete();
            }
        }

        public FileSource getSource() {
            return this.m_source;
        }
    }
}
