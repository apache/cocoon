/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.transformation;

import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.cocoon.xml.dom.DOMFactory;
import org.apache.cocoon.Roles;
import org.apache.cocoon.transformation.AbstractTransformer;
import org.apache.cocoon.generation.FragmentExtractorGenerator;
import org.apache.cocoon.ProcessingException;
import org.apache.avalon.configuration.Parameters;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Composer;
import org.apache.avalon.ComponentManagerException;
import org.apache.avalon.Component;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.helpers.AttributesImpl;

import org.w3c.dom.Document;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.IOException;


/** The transformation half of the FragmentExtractor.
 * This transformer sieves an incoming stream of xml with embedded SVG images
 * and replaces the images with an xlink locator pointing to the image.
 * Ultimately this could be much more general, but at the moment, I think it's
 * mainly SVG extraction that's likely to go on, and afterall,
 * <a href="http://c2.com/cgi/wiki?YouArentGonnaNeedIt">you aren't gonna need it</a>,
 * so I've just used very simple extraction based on a URI and local name.
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a>
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2001-03-19 21:20:45 $
 */
public class FragmentExtractorTransformer extends AbstractTransformer implements Composer {
    private static String EXTRACT_URI="http://www.w3.org/2000/svg";
    private static String EXTRACT_ELEMENT="svg";

    private static String FE_URI="http://apache.org/cocoon/fragmentextractor";
    private static String XLINK_URI="http://www.w3c.org/1999/xlink";

    private static String generatorClass = "org.apache.cocoon.generation.FragmentExtractorGenerator";

    private DOMBuilder currentBuilder;

    private Map prefixMap;

    private int extractLevel;

    private int imageID;

    protected ComponentManager manager;

    private DOMFactory documentFactory;

    public void compose(ComponentManager manager) {
        this.manager = manager;
    }

    /** Setup the transformer.
     */
    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par)
            throws ProcessingException, SAXException, IOException {
        extractLevel = 0;
        imageID = 0;
        prefixMap = new HashMap();
    }

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument()
    throws SAXException {
        try {
            documentFactory = (DOMFactory) manager.lookup(Roles.PARSER);
        } catch ( ComponentManagerException cme ) {
            documentFactory = null;
            getLogger().error("FragmentExtractorTransformer could not lookup parser.");
            throw new SAXException("Could not get documentFactory", cme);
        }

        super.startDocument();
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument()
    throws SAXException {
        super.endDocument();

        this.manager.release((Component) this.documentFactory);
        this.documentFactory = null;
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     *
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        if ( extractLevel == 0 ) {
            super.startPrefixMapping(prefix,uri);
            prefixMap.put(prefix,uri);
        } else {
            this.currentBuilder.startPrefixMapping(prefix,uri);
        }
    }

    /**
     * End the scope of a prefix-URI mapping.
     *
     * @param prefix The prefix that was being mapping.
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        if ( extractLevel == 0 ) {
            super.endPrefixMapping(prefix);
            prefixMap.remove(prefix);
        } else {
            this.currentBuilder.endPrefixMapping(prefix);
        }
    }

    /**
     * Receive notification of the beginning of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     * @param a The attributes attached to the element. If there are no
     *          attributes, it shall be an empty Attributes object.
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        if ( uri.equals(this.EXTRACT_URI) && loc.equals(this.EXTRACT_ELEMENT) ) {
            extractLevel++;
            imageID++;
            getLogger().debug("FragmentExtractorTransformer extractLevel now " + extractLevel + ".");

            // Start the DOM document
            this.currentBuilder = new DOMBuilder(documentFactory);
            this.currentBuilder.startDocument();
            Iterator itt = prefixMap.entrySet().iterator();
            while ( itt.hasNext() ) {
                Map.Entry entry = (Map.Entry)itt.next();
                this.currentBuilder.startPrefixMapping(
                    (String)entry.getKey(),
                    (String)entry.getValue()
                );
            }
        }

        if ( extractLevel == 0 ) {
            super.startElement(uri,loc,raw,a);
        } else {
            this.currentBuilder.startElement(uri,loc,raw,a);
        }
    }


    /**
     * Receive notification of the end of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     */
    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        if ( extractLevel == 0 ) {
            super.endElement(uri,loc,raw);
        } else {
            this.currentBuilder.endElement(uri,loc,raw);
            if ( uri.equals(this.EXTRACT_URI) && loc.equals(this.EXTRACT_ELEMENT) ) {
                extractLevel--;
                getLogger().debug("FragmentExtractorTransformer extractLevel now " + extractLevel + ".");

                if ( extractLevel == 0 ) {
                    // finish building the DOM. remove existing prefix mappings.
                    Iterator itt = prefixMap.entrySet().iterator();
                    while ( itt.hasNext() ) {
                        Map.Entry entry = (Map.Entry) itt.next();
                        this.currentBuilder.endPrefixMapping(
                            (String)entry.getKey()
                        );
                    }
                    this.currentBuilder.endDocument();

                    Document doc = this.currentBuilder.getDocument();
                    String id = FragmentExtractorGenerator.store(doc);
                    getLogger().debug("FragmentExtractorTransformer stored document " + id + ".");

                    // Insert ref.
                    super.startPrefixMapping("fe",FE_URI);
                    AttributesImpl atts = new AttributesImpl();
                    atts.addAttribute(null,"fragment-id","fragment-id","CDATA",id);
                    super.startElement(FE_URI,"fragment","fe:fragment",atts);
                    super.endElement(FE_URI,"fragment","fe:fragment");
                    super.endPrefixMapping("fe");
                }
            }
        }
    }

    /**
     * Receive notification of character data.
     *
     * @param c The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void characters(char c[], int start, int len)
    throws SAXException {
        if ( extractLevel == 0 ) {
            super.characters(c,start,len);
        } else {
            this.currentBuilder.characters(c,start,len);
        }
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     *
     * @param c The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void ignorableWhitespace(char c[], int start, int len)
    throws SAXException {
        if ( extractLevel == 0 ) {
            super.ignorableWhitespace(c,start,len);
        } else {
            this.currentBuilder.ignorableWhitespace(c,start,len);
        }
    }

    /**
     * Receive notification of a processing instruction.
     *
     * @param target The processing instruction target.
     * @param data The processing instruction data, or null if none was
     *             supplied.
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        if ( extractLevel == 0 ) {
            super.processingInstruction(target,data);
        } else {
            this.currentBuilder.processingInstruction(target,data);
        }
    }

    /**
     * Receive notification of a skipped entity.
     *
     * @param name The name of the skipped entity.  If it is a  parameter
     *             entity, the name will begin with '%'.
     */
    public void skippedEntity(String name)
    throws SAXException {
        if ( extractLevel == 0 ) {
            super.skippedEntity(name);
        } else {
            this.currentBuilder.skippedEntity(name);
        }
    }

    /**
     * Report the start of DTD declarations, if any.
     *
     * @param name The document type name.
     * @param publicId The declared public identifier for the external DTD
     *                 subset, or null if none was declared.
     * @param systemId The declared system identifier for the external DTD
     *                 subset, or null if none was declared.
     */
    public void startDTD(String name, String publicId, String systemId)
    throws SAXException {
        if ( extractLevel == 0 ) {
            super.startDTD(name,publicId,systemId);
        } else {
            throw new SAXException(
                "Recieved startDTD after beginning SVG extraction process."
            );
        }
    }

    /**
     * Report the end of DTD declarations.
     */
    public void endDTD()
    throws SAXException {
        if ( extractLevel == 0 ) {
            super.endDTD();
        } else {
            throw new SAXException(
                "Recieved endDTD after beginning SVG extraction process."
            );
        }
    }

    /**
     * Report the beginning of an entity.
     *
     * @param name The name of the entity. If it is a parameter entity, the
     *             name will begin with '%'.
     */
    public void startEntity(String name)
    throws SAXException {
        if ( extractLevel == 0 ) {
            super.startEntity(name);
        } else {
            this.currentBuilder.startEntity(name);
        }
    }

    /**
     * Report the end of an entity.
     *
     * @param name The name of the entity that is ending.
     */
    public void endEntity(String name)
    throws SAXException {
        if ( extractLevel == 0 ) {
            super.endEntity(name);
        } else {
            this.currentBuilder.endEntity(name);
        }
    }

    /**
     * Report the start of a CDATA section.
     */
    public void startCDATA()
    throws SAXException {
        if ( extractLevel == 0 ) {
            super.startCDATA();
        } else {
            this.currentBuilder.startCDATA();
        }
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA()
    throws SAXException {
        if ( extractLevel == 0 ) {
            super.endCDATA();
        } else {
            this.currentBuilder.endCDATA();
        }
    }

    /**
     * Report an XML comment anywhere in the document.
     *
     * @param ch An array holding the characters in the comment.
     * @param start The starting position in the array.
     * @param len The number of characters to use from the array.
     */
    public void comment(char ch[], int start, int len)
    throws SAXException {
        if ( extractLevel == 0 ) {
            super.comment(ch,start,len);
        } else {
            this.currentBuilder.comment(ch,start,len);
        }
    }
}
