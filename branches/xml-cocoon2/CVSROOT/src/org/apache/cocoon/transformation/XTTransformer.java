/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.transformation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.avalon.Component;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Composer;
import org.apache.avalon.Modifiable;
import org.apache.avalon.Parameters;

import org.apache.cocoon.Constants;
import org.apache.cocoon.Roles;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.DocumentHandlerAdapter;
import org.apache.cocoon.xml.DocumentHandlerWrapper;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.components.store.Store;
import org.apache.cocoon.components.url.URLFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.Parser;
import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.HandlerBase;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.AttributeList;
import org.xml.sax.ext.LexicalHandler;

import com.jclark.xsl.sax.XMLProcessorImpl;
import com.jclark.xsl.sax.XMLProcessorEx;
import com.jclark.xsl.sax.OutputMethodHandler;
import com.jclark.xsl.sax.ResultBase;
import com.jclark.xsl.sax.MultiNamespaceResult;
import com.jclark.xsl.sax.ExtensionHandlerImpl;
import com.jclark.xsl.tr.ParameterSet;
import com.jclark.xsl.tr.Sheet;
import com.jclark.xsl.tr.Engine;
import com.jclark.xsl.tr.EngineImpl;
import com.jclark.xsl.om.Node;
import com.jclark.xsl.om.Name;
import com.jclark.xsl.om.XSLException;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log.Logger;
import org.apache.avalon.Loggable;

/**
 * This Transformer use the XT processor.
 *
 * @author <a href="mailto:ssahuc@imediation.com">Sahuc Sebastien</a>
 * @version CVS $Revision: 1.1.2.11 $ $Date: 2001-02-15 20:30:42 $
 */
public class XTTransformer extends DocumentHandlerWrapper
implements Transformer, Composer, Loggable {
    private Logger log;

    /** The component manager */
    private ComponentManager manager = null;

    /** The store service instance */
    private Store store = null;

    /** The XT Processor */
    private XTProcessor processor = null;

    /**The DocumentHandler */
    private DocumentHandler docHandler = null;

    public void setLogger(Logger logger) {
        if (this.log == null) {
            this.log = logger;
        }
    }

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composer</code>.
     */
    public void compose(ComponentManager manager) {
        this.manager = manager;

        try {
            log.debug("Looking up " + Roles.STORE);
            this.store = (Store) manager.lookup(Roles.STORE);
        } catch (Exception e) {
            log.error("Could not find component", e);
        }
    }

    /**
     * Set the <code>EntityResolver</code>, the <code>Dictionary</code> with
     * the object model, the source and sitemap
     * <code>Parameters</code> used to process the request.
     */
    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par)
    throws SAXException, ProcessingException, IOException {

        /** The Request object */
        HttpServletRequest request = (HttpServletRequest) objectModel.get(Constants.REQUEST_OBJECT);
        if (request == null) {
            throw new ProcessingException ("Missing request object in objectModel");
        }

        // Check the stylesheet uri
        String xsluri = src;
        if (xsluri == null) {
            throw new ProcessingException("Stylesheet URI can't be null");
        }

        // Get the stylesheet from the Store if available,
        // otherwise load it and put it into the store for further request
        XTProcessor loaderprocessor = null;

        if (store != null) {
            loaderprocessor = (XTProcessor) store.get(xsluri);
            loaderprocessor.setLogger(this.log);
            loaderprocessor.compose(this.manager);
        }

        // If not in the store or if style sheet has changed, loads and stores it
        if (loaderprocessor == null || loaderprocessor.hasChanged()) {
            loaderprocessor= new XTProcessor();
            loaderprocessor.setLogger(this.log);
            SAXParser saxParser = null;
            try {
                saxParser = SAXParserFactory.newInstance().newSAXParser();
            } catch (ParserConfigurationException e) {
                log.error("XTTransformer.setup", e);
                new ProcessingException(e.getMessage());
            }
            loaderprocessor.setParser(saxParser.getParser());
            InputSource xslsrc = resolver.resolveEntity(null, xsluri);
            loaderprocessor.loadStylesheet(xslsrc);
            if (store != null) store.store(xsluri, loaderprocessor);
        }

        // Always clone the processor before using it,
        // Indeed 1 instance per thread is allowed
        this.processor = (XTProcessor) loaderprocessor.clone();
        this.processor.setLogger(this.log);

        // Create the processor and set it as this documenthandler
        // FIXME (SS): set the correct SystemId to the XML inputSource
        DocHandler temp = new DocHandler(processor.createBuilder("XTSystemID"));
        temp.setLogger(this.log);
        this.docHandler = temp;
        this.setDocumentHandler(this.docHandler);
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

    /**
    * inner class DocumentHandler that delegates all SAX Events to the XT's builder.
    */
    class DocHandler implements DocumentHandler, DTDHandler, Loggable {
        protected Logger log;

        /**
        * The XT's DocumentHandler instance to which SAX events are forwarded
        */
        private XMLProcessorImpl.Builder builder = null;

        /**
        * The Document Handler delivered.
        */
        public DocHandler(XMLProcessorImpl.Builder builder) {
            this.builder = builder;
        }

    public void setLogger(Logger logger) {
        if (this.log == null) {
            this.log = logger;
        }
    }

        public void setDocumentLocator (Locator locator) {
            builder.setDocumentLocator(locator);
        }

        public void ignorableWhitespace (char[] ch, int start, int length) throws SAXException {
            builder.ignorableWhitespace (ch, start, length);
        }

        public void processingInstruction (String target, String data) throws SAXException {
            builder.processingInstruction (target, data);
        }

        public void startDocument () throws SAXException {
            builder.startDocument();
        }

        public void endDocument () throws SAXException {
            builder.endDocument();

            try {
                // We've finished with the source document.
                // Start processing it by passing the builder
                processor.process(builder.getRootNode());
            } catch (IOException ioe) {
                log.error("XTTransformer", ioe);
                throw new SAXException(ioe);
            }

        }

        public void startElement (String name, AttributeList atts) throws SAXException {
            builder.startElement (name, atts);
        }

        public void endElement (String name) throws SAXException {
            builder.endElement (name);
        }

        public void characters (char[] str, int index, int len) throws SAXException {
            builder.characters ( str, index, len);
        }

        public void notationDecl (String name, String publicId, String systemId) throws SAXException {
            builder.notationDecl (name, publicId, systemId);
        }

        public void unparsedEntityDecl (String name, String publicId, String systemId, String notationName) throws SAXException {
            builder.unparsedEntityDecl (name, publicId, systemId, notationName);
        }
    }
}

 /**
  * The XT processor.
  */

class XTProcessor implements Cloneable, ParameterSet, Modifiable, Loggable, Composer {

    protected Logger log;
    private XMLProcessorEx sheetLoader;
    private Parser sheetParser;
    private Sheet sheet;
    private Engine engine;
    private InputSource sheetSource;
    private ResultBase result;
    private DocumentHandler documentHandler;
    private ErrorHandler errorHandler;
    private HashMap params = new HashMap();
    private File xslFile;
    private long lastModified;
    private ComponentManager manager;

    public void setLogger(Logger logger) {
        if (this.log == null) {
            this.log = logger;
        }
    }

    public void compose(ComponentManager manager) {
        this.manager = manager;
    }

    /**
    * set the Parser that will parse the XSL style sheet
    */
    public void setParser(Parser sheetParser) {
        this.sheetParser = sheetParser;
        sheetLoader = new XMLProcessorImpl(sheetParser);
    }

    /**
    * set the DocumentHandler (Consumer) to which the XT Porcessor will
    * fire the result SAX events.
    */
    public void setDocumentHandler(DocumentHandler handler) {
        documentHandler = handler;
    }

    /**
    * set the ErrorHandler
    */
    public void setErrorHandler(ErrorHandler handler) {
        if (sheetParser != null)
            sheetParser.setErrorHandler(handler);
        if (sheetLoader != null)
            sheetLoader.setErrorHandler(handler);
        this.errorHandler = handler;
    }

    /**
    * Load the style sheet given its <code>InputSource</code>
    * Should be called after the setParser() and before the process()
    */
    public void loadStylesheet(InputSource sheetSource) throws SAXException, IOException {
        // Set the xslFile for the caching mechanism
        URL url = null;
        try {
            url = ((URLFactory)this.manager.lookup(Roles.URL_FACTORY)).getURL(sheetSource.getSystemId());
        } catch (Exception e) {
            log.error("cannot obtain the URLFactory", e);
            throw new SAXException ("cannot obtain the URLFactory", e);
        }
        this.xslFile = new File(url.getFile());
        lastModified = xslFile.lastModified();

        // XT internal
        engine = new EngineImpl(sheetLoader, new ExtensionHandlerImpl());
        try {
            Node node = sheetLoader.load(sheetSource,
                       0,
                       engine.getSheetLoadContext(),
                       engine.getNameTable());
            sheet = engine.createSheet(node);
        } catch (XSLException e) {
            handleXSLException(e);
        }
    }

    /**
    * Create the XT's Builder which implements a DocumentHandler
    */
    public XMLProcessorImpl.Builder createBuilder (String systemId) {
        XMLProcessorImpl.Builder builder
                    = XMLProcessorImpl.createBuilder ( systemId, 0, sheet.getSourceLoadContext(),engine.getNameTable());
        return builder;
    }

    /**
    * Applies the Style sheet to the source root node.
    * the rootNode are taken from the builder retrieved with createBuilder() method,
    * and by calling on the builder object the method getRootNode() after the SAX events
    * have fed the builder.
    */
    public void process(Node root) throws SAXException, IOException {
        try {
            result = new MultiNamespaceResult(documentHandler, errorHandler);
            sheet.process(root, sheetLoader, this, result);
        } catch (XSLException e) {
            handleXSLException(e);
        }
    }

    void handleXSLException(XSLException e) throws SAXException, IOException {
        log.error("XTTransformer", e);
        String systemId = null;
        int lineNumber = -1;
        Node node = e.getNode();
        if (node != null) {
            URL url = node.getURL();
            if (url != null)
                systemId = url.toString();
            lineNumber = node.getLineNumber();
        }
        Exception wrapped = e.getException();
        String message = e.getMessage();
        if (systemId != null || lineNumber != -1)
            throw new SAXParseException(message,
                                        null,
                                        systemId,
                                        lineNumber,
                                        -1,
                                        wrapped);
        if (message == null) {
            if (wrapped instanceof SAXException)
                throw (SAXException)wrapped;
            if (wrapped instanceof IOException)
                throw (IOException)wrapped;
        }
        throw new SAXException(message, wrapped);
    }

    public Object clone() {
        try {
            XTProcessor cloned = (XTProcessor) super.clone();
            cloned.setLogger(this.log);
            cloned.params = (HashMap) cloned.params.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            log.error("unexpected CloneNotSupportedException", e);
            throw new Error("unexpected CloneNotSupportedException");
        }
    }

    public Object getParameter(Name name) {
        String nameString = name.getNamespace();
        if (nameString == null)
            nameString = name.getLocalPart();
        else
            nameString = (nameString
            + OutputMethodHandler.namespaceSeparator
            + name.getLocalPart());
        return params.get(nameString);
    }

   /**
   * Set XSL parameters
   */
    public void setParameter(String name, Object obj) {
        params.put(name, obj);
    }

    /**
    * implements interface <code>Modifiable</code>
    */
    public boolean modifiedSince(long date) {
        return(date < this.xslFile.lastModified());
    }

    /**
    * Checks if the style sheet file have changed since the construction
    * of <code>this</code> instance.
    */
    public boolean hasChanged() {
        if (this.xslFile == null) {
            return false;
        }
        return modifiedSince(this.lastModified);
    }
}


