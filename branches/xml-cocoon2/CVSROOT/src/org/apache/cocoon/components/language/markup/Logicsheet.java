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


import trax.Templates;
import trax.Processor;
import trax.Transformer;
import trax.TemplatesBuilder;

/**
 * A code-generation logicsheet. This class is actually a wrapper for
 * a "standard" XSLT stylesheet stored as <code>trax.Templates</code> object.
 * Though this will change shortly: a new markup language will be used
 * for logicsheet authoring; logicsheets written in this language will be
 * transformed into an equivalent XSLT stylesheet anyway...
 * This class should probably be based on an interface...
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2000-10-12 16:43:15 $
 */
public class Logicsheet {
    /**
    * The trax templates
    */
    protected Templates templates;

    /**
    * the template namespace's list
    */
    protected Map namespaces;


    /**
    * The constructor. It does preserve the namespace from the stylesheet.
    *
    * @param inputSource The stylesheet's input source
    * @exception IOException IOError processing input source
    * @exception SAXException Input source parse error
    */
    public void setInputSource(InputSource inputSource)
        throws SAXException, IOException
    {
        Processor processor = Processor.newInstance("xslt");
        // Create a XMLReader with the namespace-prefixes feature
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        namespaces = new HashMap();
        // Create a XMLFilter that save the namespace hold in the stylesheet
        XMLFilter saveNSFilter = new SaveNamespaceFilter(namespaces);
        saveNSFilter.setParent(reader);
        // Create the TemplatesBuilder and register as ContentHandler
        TemplatesBuilder temBuilder = processor.getTemplatesBuilder();
        saveNSFilter.setContentHandler(temBuilder);
        // Parse and get the templates
        reader.parse(inputSource);
        this.templates = temBuilder.getTemplates();
    }

    /**
    * Get the XMLFilter that performs the stylesheet transformation.
    * The XMLFilter might be an aggregation of XMLFilter that does additional
    * namespace preserving as stylesheet processing may drop namespace required
    * by further code-generation steps.
    *
    * @return The XMLFilter for the associated stylesheet.
    */
    public XMLFilter getXMLFilter() {
        XMLFilter transformer = templates.newTransformer();

        // The collection that hold the original namespace's declarations
        Map originalNamespaces = new HashMap();

        // 'Wraps' the transformer with two filters that do the the namespace preserving.
        XMLFilter saveNSFilter = new SaveNamespaceFilter(originalNamespaces);
        XMLFilter restoreNSFilter = new RestoreNamepaceFilter(this.namespaces, originalNamespaces);

        // constructs and returns an aggregate filter.
        return new AggregateFilter(saveNSFilter, transformer, restoreNSFilter);

    }

    /**
     * The aggregator filter aggregate 3 filters as if there were one filter.
     * This allows us to work with one filter, whereas it internally manage the
     * SaveNamespaceFilter as prefilter, a Transformer as a mainfilter, and a
     * RestoreNamepaceFilter as a postfilter.
     */
    protected class AggregateFilter implements XMLFilter {

        /**
         * the preFilter, usually an instance of SaveNamespaceFilter
         */
        protected XMLFilter preFilter;

        /**
         * the mainFilter, usually an instance of Transformer
         */
        protected XMLFilter mainFilter;

        /**
         * the postFilter, usually an instance of RestoreNamepaceFilter
         */
        protected XMLFilter postFilter;

        private XMLReader parent;

        /**
         * The constructor chaina the prefilter, the mainfilter and the
         * postfilter in the respective order.
         */
        public AggregateFilter (XMLFilter preFilter, XMLFilter mainFilter, XMLFilter postFilter) {
            this.preFilter = preFilter;
            this.mainFilter = mainFilter;
            this.postFilter = postFilter;
        }

        //
        // Implements XMLFilter interface
        //
        public void setParent (XMLReader parent) {
        	this.parent = parent;
            preFilter.setParent(parent);
            mainFilter.setParent(preFilter);
            postFilter.setParent(mainFilter);
        }

        public XMLReader getParent () {
	        return parent;
        }

        //
        // Implements XMLReader interface
        //
        public boolean getFeature (String name)
            throws SAXNotRecognizedException, SAXNotSupportedException {
            return parent.getFeature(name);
        }

        public void setFeature (String name, boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
            parent.setFeature(name, value);
        }

        public Object getProperty (String name)
            throws SAXNotRecognizedException, SAXNotSupportedException {
            return parent.getProperty(name);
        }

        public void setProperty (String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
            parent.setProperty(name, value);
        }

        public void setEntityResolver (EntityResolver resolver) { }

        public EntityResolver getEntityResolver () { return null; }

        public void setDTDHandler (DTDHandler handler) { }

        public DTDHandler getDTDHandler () { return null; }

        public void setContentHandler (ContentHandler handler) {
            this.postFilter.setContentHandler(handler);
        }

        public ContentHandler getContentHandler () {
            return this.postFilter.getContentHandler();
        }

        public void setErrorHandler (ErrorHandler handler) { }

        public ErrorHandler getErrorHandler () { return null; }

        public void parse (InputSource input)
            throws SAXException, IOException  {
            this.getParent().parse(input);
        }

        public void parse (String systemId)
            throws SAXException, IOException {
            this.parse(new InputSource(systemId));
        }

    }

    /**
     * This filter listen for source SAX events, and register the declared
     * namespaces into a <code>Map</code> object.
     *
     */
    protected class SaveNamespaceFilter extends XMLFilterImpl {

        private boolean isRootElem;

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
            isRootElem=true;
            super.startDocument();
        }

        /**
         * @see ContentHandler
         */
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            originalNamepaces.put(prefix, uri);
            super.startPrefixMapping(prefix, uri);
        }

        public void startElement (String namespaceURI, String localName,
        			      String qName, Attributes atts) throws SAXException {
            super.startElement(namespaceURI, localName, qName, atts);
        }

    }

    /**
     * This filter listen for SAX events generated by the transformer filter,
     * and store back the registered namespaces to the output SAX stream.
     *
     */
    protected class RestoreNamepaceFilter extends XMLFilterImpl {

        private boolean isRootElem;

        // FIXME (SSA) Xalan2 workaround, should be removed
        private boolean hasStarted;

        private Map originalNamespaces;

        /**
         * Contructor needs a <code>Map</code> object where the stylesheet namespace
         * have been stored, plus another <code>Map</code> object where the original
         * namespaces from input document have been stored.
         */
        public RestoreNamepaceFilter(Map stylesheetNamespaces, Map originalNamespaces) {
            this.originalNamespaces = originalNamespaces;
            this.originalNamespaces.putAll(stylesheetNamespaces);
        }

        /**
         * @see XMLFilter
         */
        public void setParent(XMLReader reader) {
            super.setParent(reader);
            reader.setContentHandler(this);
        }

        public void startDocument () throws SAXException {
            super.startDocument();
            isRootElem=true;
            // FIXME (SSA) Xalan2 workaround, should be removed
            hasStarted= true;
        }

        public void endDocument () throws SAXException {
            // endMappingPrefix for orginal namespace
            // FIXME (SSA) Xalan2j workaround, should be removed
            Set prefixes = originalNamespaces.keySet();
            Object[] keys = prefixes.toArray();
            for (int i=(keys.length-1); i>=0; i--) {
                super.endPrefixMapping((String) keys[i]);
            }
            // FIXME (SSA) Xalan2j workaround, should be replaced
    //      Iterator iter = prefixes.iterator();
    //      String prefix;
    //      while(iter.hasNext()) {
    //          prefix = (String) iter.next();
    //          super.endPrefixMapping(prefix);
    //      }
            // Forward endDocument event
            super.endDocument();

            // FIXME (SSA) Xalan2 workaround, should be removed
            hasStarted= false;
        }


        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            // FIXME (SSA) Xalan2 workaround, should be put back
    //      if(isRootElem ) {
    //          if (originalNamespaces.containsKey(prefix)) {
    //              originalNamespaces.remove(prefix);
    //          }
    //      }
            // FIXME (SSA) Xalan2 workaround, should be removed
            if (hasStarted)
                super.startPrefixMapping(prefix, uri);

        }

        public void endPrefixMapping(String prefix) throws SAXException {
            super.endPrefixMapping(prefix);
        }



        public void startElement (String namespaceURI, String localName,
        			      String qName, Attributes atts) throws SAXException {
            if(isRootElem) {
                isRootElem=false;
                // We are in the root element so send the registered namespaces
                Set prefixes = originalNamespaces.keySet();
                Iterator iter = prefixes.iterator();
                String prefix;
                while(iter.hasNext()) {
                    prefix = (String) iter.next();
                    super.startPrefixMapping(prefix, (String) originalNamespaces.get(prefix));
                }
                // FIXME (SSA) Xalan2 workaround, should be removed
                AttributesImpl newAtts = new AttributesImpl(atts);
                int attsLen = newAtts.getLength();
                for (int i=0; i<attsLen; i++) {
                    // force the URI to empty string, otherwise Xalan2J doesn't
                    // interpret correctly the 'namespace::' axis (XPath axis)
                    newAtts.setURI(i, "");
                }
                atts = newAtts;
            }
            super.startElement(namespaceURI, localName, qName, atts);
        }

    }

}

