/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.transformation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.AbstractTransformer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This transformer is an utility transformer for dealing with (x)html content.
 * It has two operating modes:
 *
 * Add Mode (default): The transformer simply adds an html and a body element
 * around the sax stream.
 *
 * Remove Mode: The transformer removes all surrounding elements like html and body
 * and only passes everything on to the next pipeline component that's contained
 * in a body element.
 *
 * @version $Id$
 */
public class HTMLRootTransformer 
    extends AbstractTransformer 
    implements CacheableProcessingComponent{

    /** the operating mode: true means adding the root elements, false means removing them */
    protected boolean addMode;

    /** do we remove the root tag? */
    protected boolean ignoreRootElement;
    protected int     ignoreRootElementCount;

    protected boolean insideBodyTag;

    /**
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver,
                      Map objectModel,
                      String src,
                      Parameters par)
    throws ProcessingException, SAXException, IOException {
        this.addMode = par.getParameterAsBoolean("add-mode", true);
        this.ignoreRootElement = par.getParameterAsBoolean("ignore-root", false);
        this.ignoreRootElementCount = 0;
        this.insideBodyTag = false;
    }

    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        if ( this.addMode ) {
            XMLUtils.endElement(this.xmlConsumer, "body");
            XMLUtils.endElement(this.xmlConsumer, "html");
        }
        super.endDocument();
    }

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        super.startDocument();
        if ( this.addMode ) {
            XMLUtils.startElement(this.xmlConsumer, "html");
            XMLUtils.startElement(this.xmlConsumer, "body");
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String local, String qName, Attributes attr) throws SAXException {
        if ( !this.ignoreRootElement || this.ignoreRootElementCount > 0) {
            if ( this.addMode || this.insideBodyTag ) {
                this.contentHandler.startElement(uri,local,qName,attr);
            }
        }
        if ( "body".equals(local) ) {
            this.insideBodyTag = true;
        }
        this.ignoreRootElementCount++;
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String local, String qName) throws SAXException {
        if ( "body".equals(local) ) {
            this.insideBodyTag = false;
        }
        this.ignoreRootElementCount--;
        if (!this.ignoreRootElement || this.ignoreRootElementCount > 0) {
            if ( this.addMode || this.insideBodyTag ) {
                this.contentHandler.endElement(uri, local, qName);
            }
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] c, int start, int len) throws SAXException {
        if ( this.addMode || this.insideBodyTag ) {
            super.characters(c, start, len);
        }
    }

    /**
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getKey()
     */
    public Serializable getKey() {
        return new Boolean[] { BooleanUtils.toBooleanObject(this.addMode), BooleanUtils.toBooleanObject(this.ignoreRootElement)};
    }

    /**
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getValidity()
     */
    public SourceValidity getValidity() {
        return NOPValidity.SHARED_INSTANCE;
    }
}
