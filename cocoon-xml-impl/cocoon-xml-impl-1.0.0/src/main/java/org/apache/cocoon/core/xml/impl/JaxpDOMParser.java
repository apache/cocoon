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
package org.apache.cocoon.core.xml.impl;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.cocoon.core.xml.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A dom parser which uses a JAXP 1.1 compliant parsers.
 *
 * @see AbstractJaxpParser
 * @version $Id$
 * @since 2.2
 */
public final class JaxpDOMParser
    extends AbstractJaxpParser
    implements DOMParser {

    /** the Document Builder factory */
    protected DocumentBuilderFactory factory;

    protected String documentBuilderFactoryName = "javax.xml.parsers.DocumentBuilderFactory";

    public String getDocumentBuilderFactoryName() {
        return documentBuilderFactoryName;
    }

    public void setDocumentBuilderFactoryName(String documentBuilderFactoryName) {
        this.documentBuilderFactoryName = documentBuilderFactoryName;
    }

    /**
     * Initialize the dom builder factory.
     */
    protected synchronized void initDomBuilderFactory()
    throws Exception {
        if ( this.factory == null ) {
            if( "javax.xml.parsers.DocumentBuilderFactory".equals( this.documentBuilderFactoryName ) ) {
                this.factory = DocumentBuilderFactory.newInstance();
            } else {
                final Class factoryClass = loadClass( this.documentBuilderFactoryName );
                this.factory = (DocumentBuilderFactory)factoryClass.newInstance();
            }
            this.factory.setNamespaceAware( true );
            this.factory.setValidating( this.validate );
        }
    }

    /**
     * @see org.apache.cocoon.core.xml.DOMParser#parseDocument(org.xml.sax.InputSource)
     */
    public Document parseDocument( final InputSource input )
    throws SAXException, IOException {
        final DocumentBuilder tmpBuilder = this.setupDocumentBuilder();

        final Document result = tmpBuilder.parse( input );

        return result;
    }

    /**
     * Creates a new {@link DocumentBuilder} if needed.
     */
    protected DocumentBuilder setupDocumentBuilder()
    throws SAXException {
        if ( this.factory == null ) {
            try {
                this.initDomBuilderFactory();
            } catch (Exception e) {
                final String message = "Cannot initialize dom builder factory";
                throw new SAXException( message, e );
            }
        }
        DocumentBuilder docBuilder;
        try {
            docBuilder = this.factory.newDocumentBuilder();
        } catch( final ParserConfigurationException pce ) {
            final String message = "Could not create DocumentBuilder";
            throw new SAXException( message, pce );
        }
        if( this.resolver != null ) {
            docBuilder.setEntityResolver( this.resolver );
        }

        return docBuilder;
    }

    /**
     * @see org.apache.cocoon.core.xml.DOMParser#createDocument()
     */
    public Document createDocument()
    throws SAXException {
        return this.setupDocumentBuilder().newDocument();
    }
}
