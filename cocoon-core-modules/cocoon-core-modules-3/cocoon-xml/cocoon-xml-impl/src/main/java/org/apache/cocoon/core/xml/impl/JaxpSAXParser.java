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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.cocoon.core.xml.SAXParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * A SAX parser which uses a JAXP 1.1 compliant parsers.
 *
 * @see AbstractJaxpParser
 * @version $Id$
 * @since 2.2
 */
public final class JaxpSAXParser
    extends AbstractJaxpParser
    implements SAXParser, ErrorHandler {

    /** the SAX Parser factory */
    protected SAXParserFactory factory;

    /** do we want namespaces also as attributes ? */
    protected boolean nsPrefixes = false;

    /** do we stop on warnings ? */
    protected boolean stopOnWarning = true;

    /** do we stop on recoverable errors ? */
    protected boolean stopOnRecoverableError = true;

    /** Should comments appearing between start/endDTD events be dropped ? */
    protected boolean dropDtdComments = false;

    /** The name of the sax parser factory. */
    protected String saxParserFactoryName = "javax.xml.parsers.SAXParserFactory";

    /**
     * @see #setDropDtdComments(boolean)
     */
    public boolean isDropDtdComments() {
        return dropDtdComments;
    }

    /**
     * Should comment() events from DTD's be dropped? (Default is false.) Since this implementation
     * does not support the DeclHandler interface anyway, it is quite useless to only have the comments
     * from DTD. And the comment events from the internal DTD subset would appear in the serialized output
     * again.
     */
    public void setDropDtdComments(boolean dropDtdComments) {
        this.dropDtdComments = dropDtdComments;
    }

    /**
     * @see #setNsPrefixes(boolean)
     */
    public boolean isNsPrefixes() {
        return nsPrefixes;
    }

    /**
     * Do we want namespaces declarations also as 'xmlns:' attributes ?
     * Default is false.
     * <i>Note</i> : setting this to <code>true</code> confuses some XSL
     * processors (e.g. Saxon).
     */
    public void setNsPrefixes(boolean nsPrefixes) {
        this.nsPrefixes = nsPrefixes;
    }

    /**
     * @see #setStopOnRecoverableError(boolean)
     */
    public boolean isStopOnRecoverableError() {
        return stopOnRecoverableError;
    }

    /**
     * Should the parser stop parsing if a recoverable error occurs ?
     * Default is true.
     */
    public void setStopOnRecoverableError(boolean stopOnRecoverableError) {
        this.stopOnRecoverableError = stopOnRecoverableError;
    }

    /**
     * @see #setStopOnWarning(boolean)
     */
    public boolean isStopOnWarning() {
        return stopOnWarning;
    }

    /**
     * Should the parser stop parsing if a warning occurs ?
     * Default is true.
     */
    public void setStopOnWarning(boolean stopOnWarning) {
        this.stopOnWarning = stopOnWarning;
    }

    /**
     * Return the name of the sax parser factory.
     * @return the name of the sax parser factory.
     * @see #setSaxParserFactoryName(String)
     */
    public String getSaxParserFactoryName() {
        return this.saxParserFactoryName;
    }

    /** 
     * Set the name of the <code>SAXParserFactory</code>
     * implementation class to be used instead of using the standard JAXP mechanism
     * (<code>SAXParserFactory.newInstance()</code>). This allows to choose
     * unambiguously the JAXP implementation to be used when several of them are
     * available in the classpath.
     */
    public void setSaxParserFactoryName(String saxParserFactoryName) {
        this.saxParserFactoryName = saxParserFactoryName;
    }

    /**
     * Initialize the sax parser factory.
     */
    protected synchronized void initSaxParserFactory()
    throws Exception {
        if ( this.factory == null ) {
            if( "javax.xml.parsers.SAXParserFactory".equals( this.saxParserFactoryName ) ) {
                this.factory = SAXParserFactory.newInstance();
            } else {
                final Class factoryClass = loadClass( this.saxParserFactoryName );
                this.factory = (SAXParserFactory)factoryClass.newInstance();
            }
            this.factory.setNamespaceAware( true );
            this.factory.setValidating( this.validate );
        }
    }

    /**
     * @see org.apache.cocoon.core.xml.SAXParser#parse(org.xml.sax.InputSource, org.xml.sax.ContentHandler, org.xml.sax.ext.LexicalHandler)
     */
    public void parse( final InputSource in,
                       final ContentHandler contentHandler,
                       final LexicalHandler lexicalHandler )
    throws SAXException, IOException {
        final XMLReader tmpReader = this.setupXMLReader();

        try {
            LexicalHandler theLexicalHandler = null;
            if ( null == lexicalHandler 
                 && contentHandler instanceof LexicalHandler) {
                theLexicalHandler = (LexicalHandler)contentHandler;
            }   
            if( null != lexicalHandler ) {
                theLexicalHandler = lexicalHandler;
            }
            if (theLexicalHandler != null) {
                if (this.dropDtdComments) {
                    theLexicalHandler = new DtdCommentEater(theLexicalHandler);
                }
                tmpReader.setProperty( "http://xml.org/sax/properties/lexical-handler",
                                       theLexicalHandler );
            }
        } catch( final SAXException e ) {
            final String message =
                "SAX2 driver does not support property: " +
                "'http://xml.org/sax/properties/lexical-handler'";
            this.getLogger().warn( message );
        }
        tmpReader.setContentHandler( contentHandler );

        tmpReader.parse( in );
    }

    /**
     * @see org.apache.cocoon.core.xml.SAXParser#parse(org.xml.sax.InputSource, org.xml.sax.ContentHandler)
     */
    public void parse( InputSource in, ContentHandler consumer )
    throws SAXException, IOException {
        this.parse( in, consumer, 
                    (consumer instanceof LexicalHandler ? (LexicalHandler)consumer : null));
    }

    /**
     * Creates a new {@link XMLReader} if needed.
     */
    protected XMLReader setupXMLReader()
    throws SAXException {
        if ( this.factory == null ) {
            try {
                this.initSaxParserFactory();
            } catch (Exception e) {
                final String message = "Cannot initialize sax parser factory";
                throw new SAXException( message, e );
            }
        }
        XMLReader reader;
        // Create the XMLReader
        try {
            reader = this.factory.newSAXParser().getXMLReader();
        } catch( final ParserConfigurationException pce ) {
            final String message = "Cannot produce a valid parser";
            throw new SAXException( message, pce );
        }

        reader.setFeature( "http://xml.org/sax/features/namespaces", true );

        if( this.nsPrefixes ) {
            try {
                reader.setFeature("http://xml.org/sax/features/namespace-prefixes",
                                  this.nsPrefixes );
            } catch( final SAXException se ) {
                final String message =
                    "SAX2 XMLReader does not support setting feature: " +
                    "'http://xml.org/sax/features/namespace-prefixes'";
                this.getLogger().warn( message );
            }
        }
        reader.setErrorHandler( this );
        if( this.resolver != null  ) {
            reader.setEntityResolver( this.resolver );
        }

        return reader;
    }

    /**
     * Receive notification of a recoverable error.
     */
    public void error( final SAXParseException spe )
    throws SAXException {
        final String message =
            "Error parsing " + spe.getSystemId() + " (line " +
            spe.getLineNumber() + " col. " + spe.getColumnNumber() +
            "): " + spe.getMessage();
        if( this.stopOnRecoverableError ) {
            throw new SAXException( message, spe );
        }
        this.getLogger().error( message, spe );
    }

    /**
     * Receive notification of a fatal error.
     */
    public void fatalError( final SAXParseException spe )
    throws SAXException {
        final String message =
            "Fatal error parsing " + spe.getSystemId() + " (line " +
            spe.getLineNumber() + " col. " + spe.getColumnNumber() +
            "): " + spe.getMessage();
        throw new SAXException( message, spe );
    }

    /**
     * Receive notification of a warning.
     */
    public void warning( final SAXParseException spe )
    throws SAXException {
        final String message =
            "Warning parsing " + spe.getSystemId() + " (line " +
            spe.getLineNumber() + " col. " + spe.getColumnNumber() +
            "): " + spe.getMessage();

        if( this.stopOnWarning ) {
            throw new SAXException( message, spe );
        }
        this.getLogger().warn( message, spe );
    }

    /**
     * A LexicalHandler implementation that strips all comment events between
     * startDTD and endDTD. In all other cases the events are forwarded to another
     * LexicalHandler.
     */
    protected static class DtdCommentEater implements LexicalHandler {

        protected LexicalHandler next;
        protected boolean inDTD;

        public DtdCommentEater(LexicalHandler nextHandler) {
            this.next = nextHandler;
        }

        public void startDTD (String name, String publicId, String systemId)
        throws SAXException {
            inDTD = true;
            next.startDTD(name, publicId, systemId);
        }

        public void endDTD ()
        throws SAXException {
            inDTD = false;
            next.endDTD();
        }

        public void startEntity (String name)
        throws SAXException {
            next.startEntity(name);
        }

        public void endEntity (String name)
        throws SAXException {
            next.endEntity(name);
        }

        public void startCDATA ()
        throws SAXException {
            next.startCDATA();
        }

        public void endCDATA ()
        throws SAXException {
            next.endCDATA();
        }

        public void comment (char ch[], int start, int length)
        throws SAXException {
            if (!inDTD) {
                next.comment(ch, start, length);
            }
        }
    }

}
