/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.parsers;

import java.io.IOException;
import org.apache.cocoon.XMLProducer;
import org.apache.cocoon.XMLConsumer;
import org.apache.cocoon.dom.DocumentFactory;
import org.apache.cocoon.framework.Configurable;
import org.apache.cocoon.framework.Configurations;
import org.apache.cocoon.framework.ConfigurationException;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DocumentTypeImpl;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.utils.StringPool;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.Document;

/**
 * The Apache Xerces parser and document factory.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>,
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-07 15:35:40 $
 */
public class XercesFactory
implements ParserFactory, DocumentFactory, ErrorHandler {
    /** Indicates wether to do validation or not */
    private boolean val=false;
    /** Indicates wether to do automatic validation or not */
    private boolean dynval=false;
    /** Wether to fail on errors or not */
    private boolean failOnError=true;
    /** Wether to fail on fatal errors or not */
    private boolean failOnFatal=true;
    /** Wether to fail on warnings or not */
    private boolean failOnWarning=true;

    /**
     * Create a new XercesFactory.
     */
    public XercesFactory() {
        super();
    }

    /**
     * Return a new Parser instance.
     */
    public XMLProducer getXMLProducer(InputSource in) {
        Parser p=new Parser();
        p.errorHandler=this;
        p.validationFlag=this.val;
        p.dynamicValidationFlag=this.dynval;
        p.inputSource=in;
        return(p);
    }

    /**
     * Return a new Document instance.
     */
    public Document newDocument() {
        return(new DocumentImpl());
    }

    /**
     * Return a new Document instance.
     */
    public Document newDocument(String name) {
        if (name==null) return(this.newDocument());
        DocumentImpl doc=new DocumentImpl();
        DocumentTypeImpl doctype=new DocumentTypeImpl(doc,name);
        return(doc);
    }

    /**
     * Configure this XercesFactory.
     * <br>
     * Valid configuration parameters are:
     * <ul>
     *   <li><b>failOnError</b> <i>(boolean)</i> Wether to fail on recoverable
     *       errors or not <i>(default=true)</i>.
     *   <li><b>failOnFatal</b> <i>(boolean)</i> Wether to fail on unrecoverable
     *       errors or not <i>(default=true)</i>.
     *   <li><b>failOnWarning</b> <i>(boolean)</i> Wether to fail on warnings
     *       or not <i>(default=true)</i>.
     *   <li><b>validate</b> <i>(true, false, dynamic)</i> Wether to validate a
     *       document or not. If this value is set to dynamic, validation is
     *       done depending on the presence of &lt;!DOCTYPE...&gt; in the
     *       source document <i>(default=false)</i>.
     * </ul>
     */
    public void configure(Configurations c)
    throws ConfigurationException {
        try {
            this.failOnError=c.getParameterAsBoolean("failOnError",true);
            this.failOnFatal=c.getParameterAsBoolean("failOnFatal",true);
            this.failOnWarning=c.getParameterAsBoolean("failOnWarning",true);
            String validate=c.getParameter("validate");
            if (validate==null) {
                this.val=false;
                this.dynval=false;
            } else if (validate.equalsIgnoreCase("dynamic")) {
                this.val=true;
                this.dynval=true;
            } else {
                this.val=c.getParameterAsBoolean("validate");
                this.dynval=false;
            }
        } catch (ConfigurationException e) {
            e.setSource(this.getClass());
            throw(e);
        }
    }

    /**
     * Receive notification of a recoverable error.
     */
    public void error(SAXParseException e)
    throws SAXException {
        if (!this.failOnError) return;
        throw new SAXException("Error parsing "+e.getSystemId()+" (line "+
                               e.getLineNumber()+" col. "+e.getColumnNumber()+
                               "): "+e.getMessage(),e);
    }

    /**
     * Receive notification of a fatal error.
     */
    public void fatalError(SAXParseException e)
    throws SAXException {
        if (!this.failOnFatal) return;
        throw new SAXException("Fatal error parsing "+e.getSystemId()+" (line "+
                               e.getLineNumber()+" col. "+e.getColumnNumber()+
                               "): "+e.getMessage(),e);
    }

    /**
     * Receive notification of a warning.
     */
    public void warning(SAXParseException e)
    throws SAXException {
        if (!this.failOnWarning) return;
        throw new SAXException("Warning parsing "+e.getSystemId()+" (line "+
                               e.getLineNumber()+" col. "+e.getColumnNumber()+
                               "): "+e.getMessage(),e);
    }

    /**
     * The Apache Xerces parser implementation required by Cocoon.
     */
    public static class Parser implements XMLProducer {
        /** The current SAXParser instance */
        private SAXParser parser=null;
        /** The current SAXParser instance */
        private ErrorHandler errorHandler=null;
        /** Indicates wether to do validation or not */
        private boolean validationFlag=false;
        /** Indicates wether to do automatic validation or not */
        private boolean dynamicValidationFlag=false;
        /** The current InputSource */
        private InputSource inputSource=null;

        /** Construct the parser */
        public Parser() {
            this.parser=new SAXParser();
        }

        /**
         * Parse a uri notifying SAX events to a given XMLConsumer.
         */
        public void produce(XMLConsumer consumer)
        throws IOException, SAXException {
            this.parser.setFeature(
                "http://xml.org/sax/features/validation",this.validationFlag);
            this.parser.setFeature(
                "http://apache.org/xml/features/validation/dynamic",
                this.dynamicValidationFlag);
            this.parser.setErrorHandler(this.errorHandler);
            this.parser.setDocumentHandler(consumer);
            this.parser.parse(this.inputSource);
        }
    }
}
