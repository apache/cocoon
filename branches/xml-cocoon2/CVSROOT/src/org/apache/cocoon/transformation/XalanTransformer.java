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
import java.text.StringCharacterIterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.avalon.Component;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Composer;
import org.apache.avalon.utils.Parameters;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.store.Store;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.dom.DocumentHandlerAdapter;
import org.apache.cocoon.xml.dom.DocumentHandlerWrapper;

import org.apache.xalan.xslt.StylesheetRoot;
import org.apache.xalan.xslt.XSLTInputSource;
import org.apache.xalan.xslt.XSLTProcessor;
import org.apache.xalan.xslt.XSLTProcessorFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2000-09-05 17:26:52 $
 */
public class XalanTransformer extends DocumentHandlerWrapper
implements Transformer, Composer {
    
    /** The store service instance */
    private Store store = null;

    /** The XALAN XSLTProcessor */
    private XSLTProcessor processor = null;

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composer</code>.
     */
    public void setComponentManager(ComponentManager manager) {
        this.store = (Store) manager.getComponent("store");
    }

    /**
     * Set the <code>EntityResolver</code>, the <code>Map</code> with 
     * the object model, the source and sitemap
     * <code>Parameters</code> used to process the request.
     */
    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par)
    throws SAXException, ProcessingException, IOException {

        /** The Request object */
        HttpServletRequest request = (HttpServletRequest)objectModel.get("request");
        if (request == null) {
            throw new ProcessingException ("Missing request object in obejctModel");
        }
        
        // Check the stylesheet uri
        String xsluri = src; 
        if (xsluri == null) {
            throw new ProcessingException("Stylesheet URI can't be null");
        }

        // Load the stylesheet
        XSLTProcessor loaderprocessor = XSLTProcessorFactory.getProcessor();
        InputSource xslsrc = resolver.resolveEntity(null,xsluri);
        XSLTInputSource style = new XSLTInputSource(xslsrc);
        StylesheetRoot stylesheet = loaderprocessor.processStylesheet(style);

        // Create the processor and set it as this documenthandler
        this.processor = XSLTProcessorFactory.getProcessor();
        this.processor.setStylesheet(stylesheet);
		Enumeration enum = request.getParameterNames();
		while (enum.hasMoreElements()) {
			String name = (String) enum.nextElement();
			if (isValidXSLTParameterName(name)) {
				String value = request.getParameter(name);
				processor.setStylesheetParam(name, this.processor.createXString(value));
			}
		}

        this.setDocumentHandler(this.processor);
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
        this.processor.setDocumentHandler(new DocumentHandlerAdapter(content));
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
