/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Hashtable;
import java.text.StringCharacterIterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.avalon.Component;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Composer;
import org.apache.avalon.Poolable;
import org.apache.avalon.Parameters;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.Roles;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.store.Store;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.ContentHandlerWrapper;

import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.14 $ $Date: 2000-10-19 14:44:37 $
 */
public class XalanTransformer extends ContentHandlerWrapper
implements Transformer, Composer, Poolable {

    /** The store service instance */
    private Store store = null;

    /** The XALAN Transformer */
	trax.Transformer transformer = null;

    /** Hash table for Templates */
    private static Hashtable templatesCache = new Hashtable();

    private static trax.Transformer getTransformer(EntityResolver resolver, String xsluri)
      throws SAXException, ProcessingException, IOException
    {
        trax.Templates templates = (trax.Templates)templatesCache.get(xsluri);
        if(templates == null)
        {
    	    trax.Processor processor = 
                org.apache.cocoon.util.DOMUtils.getXSLTProcessor();
    	    XMLReader reader =
        	    XMLReaderFactory.createXMLReader();
            reader.setFeature("http://xml.org/sax/features/namespaces", true);
    	    trax.TemplatesBuilder templatesBuilder =
        	    processor.getTemplatesBuilder();
    	    reader.setContentHandler (templatesBuilder);
    	    reader.parse(resolver.resolveEntity(null,xsluri));
    	    templates = templatesBuilder.getTemplates();
            templatesCache.put(xsluri,templates);
        }
        return templates.newTransformer();
    }

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composer</code>.
     */
    public void compose(ComponentManager manager) {
        this.store = (Store) manager.lookup(Roles.STORE);
    }

    /**
     * Set the <code>EntityResolver</code>, the <code>Map</code> with
     * the object model, the source and sitemap
     * <code>Parameters</code> used to process the request.
     */
    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par)
    throws SAXException, ProcessingException, IOException {

        /** The Request object */
        HttpServletRequest request = (HttpServletRequest) objectModel.get(Cocoon.REQUEST_OBJECT);        

        // Check the stylesheet uri
        String xsluri = src;
        if (xsluri == null) {
            throw new ProcessingException("Stylesheet URI can't be null");
        }

        /** get a transformer */
    	transformer = getTransformer(resolver,xsluri);

        if (request != null) {
            Enumeration parameters = request.getParameterNames();
            while (parameters.hasMoreElements()) {
                String name = (String) parameters.nextElement();
                if (isValidXSLTParameterName(name)) {
                    String value = request.getParameter(name);
                    transformer.setParameter(name, null /* namespace */,value);
                }
            }
        }

        ContentHandler chandler = transformer.getInputContentHandler();
        super.setContentHandler(chandler);
        if(chandler instanceof org.xml.sax.ext.LexicalHandler)
            this.setLexicalHandler((org.xml.sax.ext.LexicalHandler)chandler);
    }

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     * <br>
     * This method will simply call <code>setContentHandler(consumer)</code>
     * and <code>setLexicalHandler(consumer)</code>.
     */
    public void setConsumer(XMLConsumer consumer) {
        this.setContentHandler(consumer);
    }

    /**
     * Set the <code>ContentHandler</code> that will receive XML data.
     * <br>
     * Subclasses may retrieve this <code>ContentHandler</code> instance
     * accessing the protected <code>super.contentHandler</code> field.
     */
    public void setContentHandler(ContentHandler content) {
        this.transformer.setContentHandler(content);
    }

    /**
     * Set the <code>LexicalHandler</code> that will receive XML data.
     * <br>
     * Subclasses may retrieve this <code>LexicalHandler</code> instance
     * accessing the protected <code>super.lexicalHandler</code> field.
     *
     * @exception IllegalStateException If the <code>LexicalHandler</code> or
     *                                  the <code>XMLConsumer</code> were
     *                                  already set.
     */
    public void setLexicalHandler(LexicalHandler lexical) {
    }

    // FIXME (SM): this method may be a hotspot for requests with many
    //             parameters we should try to optimize it further
    static boolean isValidXSLTParameterName(String name) {
        StringCharacterIterator iter = new StringCharacterIterator(name);
        char c = iter.first();
        if (!(Character.isLetter(c) || c == '_')) {
            return false;
        } else {
            c = iter.next();
        }
        while (c != iter.DONE) {
            if (!(Character.isLetterOrDigit(c) ||
                c == '-' ||
                c == '_' ||
                c == '.')) {
                return false;
            } else {
                c = iter.next();
            }
        }
        return true;
    }
}
