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
import java.text.StringCharacterIterator;

import org.apache.avalon.Component;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Composer;
import org.apache.avalon.utils.Parameters;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.util.DocumentHandlerAdapter;
import org.apache.cocoon.xml.util.DocumentHandlerWrapper;

import org.apache.xalan.xslt.StylesheetRoot;
import org.apache.xalan.xslt.XSLTInputSource;
import org.apache.xalan.xslt.XSLTProcessor;
import org.apache.xalan.xslt.XSLTProcessorFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-07-22 20:42:01 $
 */
public class XalanTransformer extends DocumentHandlerWrapper
implements Transformer, Composer {
    
    /** The component manager instance */
    private ComponentManager manager=null;
    /** The XALAN XSLTProcessor */
    private XSLTProcessor processor=null;

    /**
     * Set the <code>Environment</code> and sitemap
     * <code>Parameters</code> used to process the request.
     */
    public void setup(Environment environment, String src, Parameters par)
    throws SAXException, ProcessingException, IOException {


        // Check the stylesheet uri
        // String xsluri=par.getParameter("stylesheet",null); 
        String xsluri=src; 
        if (xsluri==null) throw new ProcessingException("No stylesheet");

        // Load the stylesheet (we should cache it in the STORE!)
        Cocoon cocoon=(Cocoon)this.manager.getComponent("cocoon");
        StylesheetRoot stylesheet=null;
        if (true) {
            XSLTProcessor loaderprocessor=XSLTProcessorFactory.getProcessor();
            InputSource xslsrc=cocoon.resolveEntity(xsluri);
            XSLTInputSource style=new XSLTInputSource(xslsrc);
            stylesheet=loaderprocessor.processStylesheet(style);
        }

        // Create the processor and set it as this documenthandler
        this.processor=XSLTProcessorFactory.getProcessor();
        this.processor.setStylesheet(stylesheet);
		Enumeration enum = ((HttpEnvironment)environment).getRequest().getParameterNames();
		while (enum.hasMoreElements()) {
			String name = (String)enum.nextElement();
			if (isValidXSLTParameterName(name)) {
				String value = ((HttpEnvironment)environment).getRequest().getParameter(name);
				processor.setStylesheetParam(name,this.processor.createXString(value));
			}
		}
        this.setDocumentHandler(this.processor);

    }

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composer</code>.
     */
    public void setComponentManager(ComponentManager manager) {
        this.manager=manager;
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

	public static boolean isValidXSLTParameterName(String name) {
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
