/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.filters;

import java.io.File;
import com.kvisco.util.ErrorObserver;
import com.kvisco.xsl.DOMFormatter;
import com.kvisco.xsl.RuleProcessor;
import com.kvisco.xsl.XSLStylesheet;
import com.kvisco.xsl.util.StylesheetHandler;
import org.apache.cocoon.Cocoon;
import org.apache.cocoon.dom.EventGenerator;
import org.apache.cocoon.dom.TreeGenerator;
import org.apache.cocoon.parsers.Parser;
import org.apache.cocoon.sitemap.Request;
import org.apache.cocoon.sitemap.Response;
import org.apache.cocoon.sax.XMLConsumer;
import org.apache.cocoon.sax.DocumentHandlerWrapper;
import org.apache.cocoon.sax.DocumentHandlerAdapter;
import org.apache.cocoon.framework.AbstractComponent;
import org.apache.cocoon.framework.Configurations;
import org.apache.cocoon.framework.ConfigurationException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;

/**
 * The <code>XSPFilter</code> implements the <code>Filter</code> interface
 * for XSLT transformations.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-12 00:39:54 $
 * @since Cocoon 2.0
 */
public class XSLPFilter extends AbstractComponent implements Filter {
    /** The current cocoon instance */
    private Cocoon cocoon=null;
    /** The current stylesheet */
    private XSLStylesheet stylesheet=null;
    /** The current stylesheet file */
    private File stylesheetFile=null;
    /** The current stylesheet file */
    private Exception stylesheetException=null;
    /** The time when the stylesheet was loaded */
    private long stylesheetUpdate=0;

    /**
     * Get the <code>XMLConsumer</code> object that will listen to SAX events,
     * modify them, and then forward them to the specified 
     * <code>XMLConsumer</code>.
     *
     * @param q The cocoon <code>Request</code>.
     * @param s The cocoon <code>Response</code>.
     * @param c The <code>XMLConsumer</code> listening to the modified
     *          SAX events.
     */
    public XMLConsumer getXMLConsumer(Request q, Response s, XMLConsumer c) {
        // Check if the stylesheet is valid and, if necessary, reload
        this.checkAndReloadStylesheet();
        // The source document must be passed to XSLT as a DOM (for now)
        // so, set up a DOM TreeGenerator
        TreeGenerator tree=new TreeGenerator();
        tree.setDocumentFactory(this.getCocoonInstance().getDocumentFactory());
        // Create a new TreeGeneratorListener that will handle the constructed
        // DOM Document and set it to the TreeGenerator
        Listener lis=new Listener();
        tree.setListener(lis);
        // Set parameters in the listener (consumer, stylesheet and exception)
        lis.consumer=c;
        lis.stylesheet=this.stylesheet;
        lis.exception=this.stylesheetException;
        // Create a new node, used to generate the result tree, and set it in
        // the listener. NOTE: (PF) Need to fix the bug in XSLP to use SAX.
        lis.document=this.cocoon.getDocumentFactory().newDocument();
        // Return the tree generator
        return(tree);
    }

    /**
     * Configure this <code>XSLPFilter</code> instance.
     * <br>
     * The <code>XSLPFilter</code> requires a <code>stylesheet</code> parameter,
     * specifying the XSLT stylesheet uri.
     */
    public void configure(Configurations conf)
    throws ConfigurationException {
        // Check the current Cocoon instance (just in case!)
        this.cocoon=this.getCocoonInstance();
        if (this.cocoon==null)
            throw new ConfigurationException("Cannot access current 'Cocoon'"+
                                             "instance",this.getClass());
        // Retrieve the stylesheet uri
        String stylesheetUri=conf.getParameter("stylesheet");
        if (stylesheetUri==null)
            throw new ConfigurationException("Parameter 'stylesheet' not "+
                                             "specified",this.getClass());
        // Store the URI as a File (to get timedate modifications)
        this.stylesheetFile=new File(stylesheetUri);
        // Load the stylesheet for the first time
        this.checkAndReloadStylesheet();
        // Check if the stylesheet was correctly parsed.
        if(this.stylesheetException!=null) {
            throw new ConfigurationException("Exception parsing stylesheet '"+
                                             stylesheetUri+"'",
                                             this.stylesheetException,
                                             this.getClass());
        }
    }

    /** Check if the stylesheet expired and, if necessary, reload it */
    private void checkAndReloadStylesheet() {
        // Do a reload only if the current stylesheet is null (there was an
        // error) or if it was modified
        if((this.modifiedSince(this.stylesheetUpdate)) || 
           (this.stylesheet==null)) try {
            // Reset the stylesheet value (in case of exceptions on reloads)
            this.stylesheet=null;
            // Create the stylesheet object
            Parser pf=this.cocoon.getParser();
            StylesheetHandler sh=new StylesheetHandler();
            DocumentHandlerWrapper wr=new DocumentHandlerWrapper(sh);
            String uri=this.stylesheetFile.getPath();
            pf.getXMLProducer(new InputSource(uri)).produce(wr);
            this.stylesheet=sh.getStylesheet();
        // If we get an exception loading the stylesheet set the current
        // exception as the one we catched
        } catch (Exception e) {
            this.stylesheet=null;
            this.stylesheetException=e;
        }
    }

    /**
     * Check for modification.
     */
    public boolean modifiedSince(long date) {
        return(true);
    }
    
    /**
     * The <code>TreeGenerator.Listener</code> implementation that will do
     * all XSLT processing.
     */
    private class Listener implements TreeGenerator.Listener, ErrorObserver {
        /** The consumer listening for the result tree data */
        private XMLConsumer consumer=null;
        /** The stylesheet on wich we need to work */
        private XSLStylesheet stylesheet=null;
        /** The current error message. NOTE: (PF) Fix this in XSLP */
        private String message=null;
        /** The number of errors received. NOTE: (PF) Fix this in XSLP */
        private int errors=0;
        /** The exception got parsing the stylesheet (if any) */
        private Exception exception=null;
        /** The result tree document. NOTE: (PF) Fix this in XSLP */
        private Document document=null;
        
        /**
         * Receive notification of a successful <code>Document</code> creation.
         */
        public void notify(org.w3c.dom.Document doc)
        throws SAXException {
            // Check if the stylesheet is available or last time we parsed it
            // there was an error.
            if (this.stylesheet==null) 
                // Throw the exception we got parsing the stylesheet
                throw new SAXException("Invalid Stylesheet",this.exception);
            // Set up the XSLP processor
            RuleProcessor proc=new RuleProcessor(this.stylesheet);
            proc.addErrorObserver(this);
            // Create a DOMFormatter as an output NOTE: (PF) Fix this in XSLP
            DOMFormatter form=new DOMFormatter(this.document);
            // Process the document and generate output
            proc.process(doc, form);
            // Check if receiveError(...) was called NOTE: (PF) Fix this in XSLP
            if(this.errors>0) {
                throw new SAXException("Error processing stylesheet: "+message);
            }
            // Translate the target DOM into SAX events
            new EventGenerator(this.document).produce(consumer);
        }

        /**
         * Receive notification of an error.
         */
        public void receiveError(String message) {
            this.errors++;
            this.message=message;
        }
        

        /**
         * Receive notification of an error.
         */
        public void receiveError(String message, int level) {
            this.errors++;
            this.message=message;
        }
    }
}
