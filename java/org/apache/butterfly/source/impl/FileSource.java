/*
 * Copyright 2004, Ugo Cei.
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.butterfly.source.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLConnection;

import org.apache.butterfly.source.Source;
import org.apache.butterfly.source.SourceException;
import org.apache.butterfly.source.SourceNotFoundException;
import org.apache.butterfly.source.SourceUtil;
import org.apache.butterfly.source.SourceValidity;
import org.apache.butterfly.source.impl.validity.FileTimeStampValidity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Description of FileSource.
 * 
 * @version CVS $Id: FileSource.java,v 1.1 2004/07/23 08:47:20 ugo Exp $
 */
public class FileSource implements Source {

    /** The file */
    private File file;

    /** The scheme */
    private String scheme;

    /** The URI of this source */
    private String uri;
    
    /** The logger */
    protected static final Log logger = LogFactory.getLog(FileSource.class);

    public FileSource(String uri)  {
        int pos = SourceUtil.indexOfSchemeColon(uri);
        if (pos < 0) {
            throw new SourceException("Invalid URI : " + uri);
        }
        String scheme = uri.substring(0, pos);
        String fileName = uri.substring(pos + 1);
        fileName = SourceUtil.decodePath(fileName);
        init(scheme, new File(fileName));
    }

    /**
     * Builds a FileSource, given an URI scheme and a File.
     * 
     * @param scheme
     * @param file
     * @throws SourceException
     */
    public FileSource(String scheme, File file) throws SourceException
    {
        init(scheme, file);
    }

    private void init(String scheme, File file) throws SourceException
    {
        this.scheme = scheme;

        String uri;
        try {
            uri = file.toURL().toExternalForm();
        } catch (MalformedURLException mue) {
            // Can this really happen ?
            throw new SourceException("Failed to get URL for file " + file, mue);
        }
        if (!uri.startsWith(scheme)) {
            // Scheme is not "file:"
            uri = scheme + ':' + uri.substring(uri.indexOf(':') + 1);
        }
        this.uri = uri;
        this.file = file;
    }

    /**
     * Get the associated file
     */
    public File getFile()
    {
        return this.file;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.Source#exists()
     */
    public boolean exists() {
        return getFile().exists();
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.Source#getInputStream()
     */
    public InputStream getInputStream() {
        try {
            return new FileInputStream(this.file);
        } catch (FileNotFoundException fnfe) {
            throw new SourceNotFoundException(this.uri + " doesn't exist.", fnfe);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.Source#getURI()
     */
    public String getURI() {
        return this.uri;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.Source#getScheme()
     */
    public String getScheme() {
        return this.scheme;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.Source#getValidity()
     */
    public SourceValidity getValidity() {
        if (this.file.exists()) {
            return new FileTimeStampValidity(this.file);
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.Source#refresh()
     */
    public void refresh() {
        // Nothing to do
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.Source#getMimeType()
     */
    public String getMimeType() {
        return URLConnection.getFileNameMap().getContentTypeFor(this.file.getName());
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.Source#getContentLength()
     */
    public long getContentLength() {
        return this.file.length();
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.Source#getLastModified()
     */
    public long getLastModified() {
        return this.file.lastModified();
    }

}
