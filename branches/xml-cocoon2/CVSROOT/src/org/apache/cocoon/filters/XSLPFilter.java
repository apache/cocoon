/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.filters;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import com.kvisco.util.ErrorObserver;
import com.kvisco.xsl.DOMFormatter;
import com.kvisco.xsl.RuleProcessor;
import com.kvisco.xsl.XSLStylesheet;
import com.kvisco.xsl.util.StylesheetHandler;
import org.apache.arch.Component;
import org.apache.arch.ComponentManager;
import org.apache.arch.Composer;
import org.apache.cocoon.Cocoon;
import org.apache.cocoon.Parameters;
import org.apache.cocoon.Request;
import org.apache.cocoon.Response;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.util.DocumentHandlerAdapter;
import org.apache.cocoon.xml.util.DocumentHandlerWrapper;
import org.apache.cocoon.xml.util.DOMBuilder;
import org.apache.cocoon.xml.util.DOMStreamer;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.w3c.dom.Document;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.4.1 $ $Date: 2000-02-27 17:47:59 $
 */
public class XSLPFilter extends DOMBuilder implements Filter, Composer {
    
    /** The <code>ContentHandler</code> receiving SAX events. */
    private ContentHandler contentHandler=null;
    /** The <code>LexicalHandler</code> receiving SAX events. */
    private LexicalHandler lexicalHandler=null;
    /** The component manager instance */
    private ComponentManager manager=null;
    /** The current <code>Request</code>. */
    private Request request=null;
    /** The current <code>Response</code>. */
    private Response response=null;
    /** The current <code>Parameters</code>. */
    private Parameters parameters=null;
    /** The source URI associated with the request or <b>null</b>. */
    private String source=null;

    /**
     * Set the <code>Request</code>, <code>Response</code> and sitemap
     * <code>Parameters</code> used to process the request.
     */
    public void setup(Request req, Response res, String src, Parameters par) {
        this.request=req;
        this.response=res;
        this.source=src;
        this.parameters=par;
        super.factory=(Parser)this.manager.getComponent("parser");
    }

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     * <br>
     * This method will simply call <code>setContentHandler(consumer)</code>
     * and <code>setLexicalHandler(consumer)</code>.
     */
    public void setConsumer(XMLConsumer consumer) {
        this.contentHandler=consumer;
        this.lexicalHandler=consumer;
    }

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composer</code>.
     */
    public void setComponentManager(ComponentManager manager) {
        this.manager=manager;
    }

    /**
     * Set the <code>ContentHandler</code> that will receive XML data.
     * <br>
     * Subclasses may retrieve this <code>ContentHandler</code> instance
     * accessing the protected <code>super.contentHandler</code> field.
     */
    public void setContentHandler(ContentHandler content) {
        this.contentHandler=content;
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
        this.lexicalHandler=lexical;
    }

    /**
     * Receive notification of a successfully completed DOM tree generation.
     */
    public void notify(Document doc)
    throws SAXException {
        if (doc==null) throw new SAXException("Null document");

        String xsluri=this.parameters.getParameter("stylesheet",null);
        if (xsluri==null) {
            DOMStreamer s=new DOMStreamer(contentHandler,lexicalHandler);
            s.stream(doc);
            return;
        }

        StylesheetHandler sh=new StylesheetHandler();
        DocumentHandlerWrapper wr=new DocumentHandlerWrapper(sh);
        Cocoon co=(Cocoon)this.manager.getComponent("cocoon");
        Parser pa=(Parser)this.manager.getComponent("parser");

        pa.setConsumer(wr);

        try {
            InputSource in=co.resolveEntity(null,xsluri);
            /* ROUTINE TO CHECK STYLESHEET FILE DATE                
            URL u=new URL(in.getSystemId());
            if (u.getProtocol().equals("file")) {
                File f=new File(u.getFile()).getCanonicalFile();
                if (f.isFile()) {
                    long mod=f.lastModified();
                }
            } */
            pa.parse(in);
        } catch (IOException e) {
            throw new SAXException("IOException parsing stylesheet");
        }
        XSLStylesheet stylesheet=sh.getStylesheet();
        // TODO: Cache the stylesheet here
        RuleProcessor proc=new RuleProcessor(stylesheet);

        Document result=pa.newDocument();
        DOMFormatter form=new DOMFormatter(result);
        // Why the hell is not working today????
        //Formatter form=new Formatter(this.contentHandler,this.lexicalHandler);
        proc.process(doc, form);
        //if (form.exception!=null) throw form.exception;

        DOMStreamer s=new DOMStreamer(this.contentHandler,this.lexicalHandler);
        s.stream(result);
    }

    private class Formatter extends DocumentHandlerAdapter
    implements com.kvisco.xsl.Formatter {
        public SAXException exception;
        
        public Formatter(ContentHandler c, LexicalHandler l) {
            super(c);
            this.lexicalHandler=l;
        }

        public void cdata(char ch[], int start, int len) {
            if (exception==null) try {
                if (super.lexicalHandler!=null)
                    super.lexicalHandler.startCDATA();
                if (super.contentHandler!=null)
                    super.contentHandler.characters(ch,start,len);
                if (super.lexicalHandler!=null)
                    super.lexicalHandler.endCDATA();
            } catch (SAXException e) {
                this.exception=e;
            }
        }

        public void comment(java.lang.String data) {
            if (exception==null) try {
                if (super.lexicalHandler!=null) {
                    char ch[]=data.toCharArray();
                    super.lexicalHandler.comment(ch,0,ch.length);
                }
            } catch (SAXException e) {
                this.exception=e;
            }
        }

        public void entityReference(java.lang.String name) {
            if (exception==null) try {
                if (super.contentHandler!=null)
                    super.contentHandler.skippedEntity(name);
            } catch (SAXException e) {
                this.exception=e;
            }
        }

        public void setIndentSize(short indentSize) {
        }

        public void setOutputFormat(com.kvisco.xsl.OutputFormat f) {
        }
    }
}
