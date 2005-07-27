/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.xml.sax.XMLizable;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A <code>Source</code> that generates an empty XML document or an
 * XML document just containing a root node.
 * The URI syntax is "create-document:" or "create-document:ROOT_ELEMENT_NAME".
 *
 * @version $Id:$
 * @since 2.1.8
 */
public class CreateDocumentSource
    implements XMLizable, Source {

    protected String rootElementName;
    protected String scheme;
    protected String uri;
    protected String xmlDocument;

    public CreateDocumentSource(String location) {
        this.uri = location;
        final int pos = location.indexOf(':');
        this.scheme = location.substring(0, pos);
        final String rootName = location.substring(pos+1);
        if (rootName != null && rootName.trim().length() > 0 ) {
            this.rootElementName = rootName.trim();
            this.xmlDocument = '<' + this.rootElementName + "/>";
        } else {
            this.xmlDocument = "";
        }
    }

    /**
     * @see org.apache.excalibur.xml.sax.XMLizable#toSAX(org.xml.sax.ContentHandler)
     */
    public void toSAX(ContentHandler handler) throws SAXException {
        handler.startDocument();
        if ( rootElementName != null ) {
            XMLUtils.createElement(handler, this.rootElementName);
        }
        handler.endDocument();
    }

    /**
     * @see org.apache.excalibur.source.Source#exists()
     */
    public boolean exists() {
        return true;
    }

    /**
     * @see org.apache.excalibur.source.Source#getContentLength()
     */
    public long getContentLength() {
        return this.xmlDocument.length();
    }

    /**
     * @see org.apache.excalibur.source.Source#getInputStream()
     */
    public InputStream getInputStream() throws IOException, SourceNotFoundException {
        return new ByteArrayInputStream(this.xmlDocument.getBytes("utf-8"));
    }

    /**
     * @see org.apache.excalibur.source.Source#getLastModified()
     */
    public long getLastModified() {
        // this document *never* changes
        return 1;
    }

    /**
     * @see org.apache.excalibur.source.Source#getMimeType()
     */
    public String getMimeType() {
        return "text/xml";
    }

    /**
     * @see org.apache.excalibur.source.Source#getScheme()
     */
    public String getScheme() {
        return this.scheme;
    }

    /**
     * @see org.apache.excalibur.source.Source#getURI()
     */
    public String getURI() {
        return this.uri;
    }

    /**
     * @see org.apache.excalibur.source.Source#getValidity()
     */
    public SourceValidity getValidity() {
        return NOPValidity.SHARED_INSTANCE;
    }

    /**
     * @see org.apache.excalibur.source.Source#refresh()
     */
    public void refresh() {
        // nothing to do here
    }

}
