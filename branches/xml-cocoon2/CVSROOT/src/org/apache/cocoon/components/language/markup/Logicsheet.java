/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.components.language.markup;

import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.DTDHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLReaderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.sax.SAXTransformerFactory;
import org.apache.avalon.AbstractLoggable;

import org.apache.cocoon.util.TraxErrorHandler;

/**
 * A code-generation logicsheet. This class is actually a wrapper for
 * a "standard" XSLT stylesheet stored as <code>trax.Templates</code> object.
 * Though this will change shortly: a new markup language will be used
 * for logicsheet authoring; logicsheets written in this language will be
 * transformed into an equivalent XSLT stylesheet anyway... This class should probably be based on an interface...
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Revision: 1.1.2.15 $ $Date: 2001-04-20 14:48:26 $
 */
public class Logicsheet extends AbstractLoggable {
    /** The trax TransformerFactory */
    protected SAXTransformerFactory tfactory;

    /** The trax templates */
    protected Templates templates;

    /**
    * the template namespace's list
    */
    protected Map namespaces = new HashMap();


    /**
     * This will return the list of namespaces in this logicsheet.
     */
    public Map getNamespaces()
    {
        return namespaces;
    }

    /**
     * The constructor. It does preserve the namespace from the stylesheet.
     * @param inputSource The stylesheet's input source
     * @exception IOException IOError processing input source
     * @exception SAXException Input source parse error
     */
    public void setInputSource(InputSource inputSource) throws SAXException, IOException {
        try {
            tfactory = (SAXTransformerFactory)TransformerFactory.newInstance();
            tfactory.setErrorListener(new TraxErrorHandler(getLogger()));

            // Create a Templates ContentHandler to handle parsing of the 
            // stylesheet.
            javax.xml.transform.sax.TemplatesHandler templatesHandler = 
                                                tfactory.newTemplatesHandler();

            // Create an XMLReader and set its ContentHandler.
            org.xml.sax.XMLReader reader = 
                           org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
            reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

            // Create a XMLFilter that save the namespace hold in the stylesheet
            XMLFilter saveNSFilter = new SaveNamespaceFilter(namespaces);
            saveNSFilter.setParent(reader);

            saveNSFilter.setContentHandler(templatesHandler);
            //reader.setContentHandler(templatesHandler);
    
            // Parse the stylesheet.                       
            reader.parse(inputSource);

            // Get the Templates object (generated during the parsing of the stylesheet)
            // from the TemplatesHandler.
            templates = templatesHandler.getTemplates();
        } catch (TransformerConfigurationException e) {
            getLogger().error("Logicsheet.setInputSource", e);
        }
    }

    /**
     * Get the TransformerHandler that performs the stylesheet transformation.
     * @return The TransformerHandler for the associated stylesheet.
     */
    public TransformerHandler getTransformerHandler() {
        try {
            TransformerHandler handler = tfactory.newTransformerHandler(templates);
            handler.getTransformer().setErrorListener(new TraxErrorHandler(getLogger()));
            return handler;
        } catch (TransformerConfigurationException e) {
            getLogger().error("Logicsheet.getTransformerHandler:TransformerConfigurationException", e);
        } catch (Exception e) {
            getLogger().error("Logicsheet.getTransformerHandler:Exception", e);
        }
        return null;
    }

    /**
     * This filter listen for source SAX events, and register the declared
     * namespaces into a <code>Map</code> object.
     *
     */
    protected class SaveNamespaceFilter extends XMLFilterImpl {

        private Map originalNamepaces;

        /**
         * The contructor needs an initialized <code>Map</code> object where it
         * can store the found namespace declarations.
         * @param originalNamepaces a initialized <code>Map</code> instance.
         */
        public SaveNamespaceFilter(Map originalNamepaces) {
            this.originalNamepaces = originalNamepaces;
        }

        /**
         * @param reader the parent reader
         * @see XMLFilter
         */
        public void setParent(XMLReader reader) {
            super.setParent(reader);
            reader.setContentHandler(this);
        }

        /**
         * @see ContentHandler
         */
        public void startDocument () throws SAXException {
            super.startDocument();
        }

        /**
         * @see ContentHandler
         */
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            originalNamepaces.put(prefix,uri);
            super.startPrefixMapping(prefix, uri);
        }

        public void startElement (String namespaceURI, String localName,
        			      String qName, Attributes atts) throws SAXException {
            super.startElement(namespaceURI, localName, qName, atts);
        }
    }

}
