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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.TimeStampValidity;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This source objects wraps an obsolete Cocoon Source object
 * to avoid recoding existing source objects.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CocoonToAvalonSource.java,v 1.4 2004/03/05 13:02:41 bdelacretaz Exp $
 */
public final class CocoonToAvalonSource
    implements Source, XMLizable, Recyclable {

    /** The real source */
    protected org.apache.cocoon.environment.Source source;

    /** The protocol */
    protected String protocol;

    /**
     * Constructor
     */
    public CocoonToAvalonSource(
        String location,
        org.apache.cocoon.environment.Source source) {
        this.source = source;
        int pos = location.indexOf(':');
        this.protocol = location.substring(0, pos);
    }

    /**
     * Return the protocol identifier.
     */
    public String getScheme() {
        return this.protocol;
    }

    /**
     * @see org.apache.excalibur.source.Source#exists()
     */
    public boolean exists() {
        try {
            this.getInputStream();
            return true;
        } catch (Exception local) {
            return false;
        }
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     */
    public InputStream getInputStream() throws IOException, SourceException {
        try {
            return this.source.getInputStream();
        } catch (ResourceNotFoundException rnfe) {
            throw new SourceNotFoundException("Source not found.", rnfe);
        } catch (ProcessingException pe) {
            throw new SourceException("ProcessingException", pe);
        }
    }

    /**
     * Return the unique identifer for this source
     */
    public String getURI() {
        return this.source.getSystemId();
    }

    /**
     *  Get the Validity object. This can either wrap the last modification
     *  date or the expires information or...
     *  If it is currently not possible to calculate such an information
     *  <code>null</code> is returned.
     */
    public SourceValidity getValidity() {
        if (this.source.getLastModified() > 0) {
            return new TimeStampValidity(this.source.getLastModified());
        }
        return null;
    }

    /**
     * Refresh this object and update the last modified date
     * and content length.
     */
    public void refresh() {
        if (this.source instanceof ModifiableSource) {
            ((ModifiableSource) this.source).refresh();
        }
    }

    /**
     * The mime-type of the content described by this object.
     * If the source is not able to determine the mime-type by itself
     * this can be null.
     */
    public String getMimeType() {
        return null;
    }

    /**
     * Stream content to the content handler
     */
    public void toSAX(ContentHandler contentHandler) throws SAXException {
        this.source.toSAX(contentHandler);
    }

    /**
     * Recyclable
     */
    public void recycle() {
        this.source.recycle();
    }

    /**
     * Return the content length of the content or -1 if the length is
     * unknown
     */
    public long getContentLength() {
        return this.source.getContentLength();
    }

    /**
     * Get the last modification date of the source or 0 if it
     * is not possible to determine the date.
     */
    public long getLastModified() {
        return this.source.getLastModified();
    }

    /**
     * Get the value of a parameter.
     * Using this it is possible to get custom information provided by the
     * source implementation, like an expires date, HTTP headers etc.
     */
    public String getParameter(String name) {
        return null;
    }

    /**
     * Get the value of a parameter.
     * Using this it is possible to get custom information provided by the
     * source implementation, like an expires date, HTTP headers etc.
     */
    public long getParameterAsLong(String name) {
        return 0;
    }

    /**
     * Get parameter names
     * Using this it is possible to get custom information provided by the
     * source implementation, like an expires date, HTTP headers etc.
     */
    public Iterator getParameterNames() {
        return java.util.Collections.EMPTY_LIST.iterator();
    }

}
