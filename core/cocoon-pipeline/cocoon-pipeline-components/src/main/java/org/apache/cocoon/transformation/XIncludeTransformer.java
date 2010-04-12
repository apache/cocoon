/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.transformation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.util.SourceUtil;
import org.apache.cocoon.components.source.impl.MultiSourceValidity;
import org.apache.cocoon.components.xpointer.XPointer;
import org.apache.cocoon.components.xpointer.XPointerContext;
import org.apache.cocoon.components.xpointer.parser.ParseException;
import org.apache.cocoon.components.xpointer.parser.XPointerFrameworkParser;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLBaseSupport;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Implementation of an XInclude transformer. It supports xml:base attributes,
 * XPointer fragment identifiers (see the xpointer package to see what exactly is
 * supported), fallback elements, and does xinclude processing on the included content
 * and on the content of fallback elements (with loop inclusion detection).
 *
 * @cocoon.sitemap.component.documentation
 * Implementation of an XInclude transformer.
 * @cocoon.sitemap.component.name   xinclude
 * @cocoon.sitemap.component.documentation.caching Yes
 * @cocoon.sitemap.component.pooling.max  16
 *
 * @version $Id$
 */
public class XIncludeTransformer extends AbstractTransformer
                                 implements Serviceable, CacheableProcessingComponent {

    protected SourceResolver resolver;
    protected ServiceManager manager;
    private XIncludePipe xIncludePipe;

    public static final String XMLBASE_NAMESPACE_URI = javax.xml.XMLConstants.XML_NS_URI;
    public static final String XMLBASE_NAMESPACE_PREFIX = javax.xml.XMLConstants.XML_NS_PREFIX;
    public static final String XMLBASE_ATTRIBUTE = "base";
    public static final String XMLBASE_ATTRIBUTE_TYPE = "CDATA";

    public static final String XINCLUDE_NAMESPACE_URI = "http://www.w3.org/2001/XInclude";
    public static final String XINCLUDE_INCLUDE_ELEMENT = "include";
    public static final String XINCLUDE_FALLBACK_ELEMENT = "fallback";
    public static final String XINCLUDE_INCLUDE_ELEMENT_HREF_ATTRIBUTE = "href";
    public static final String XINCLUDE_INCLUDE_ELEMENT_XPOINTER_ATTRIBUTE = "xpointer";
    public static final String XINCLUDE_INCLUDE_ELEMENT_PARSE_ATTRIBUTE = "parse";

    private static final String XINCLUDE_CACHE_KEY = "XInclude"; 

    /** The {@link SourceValidity} instance associated with this request. */ 
    protected MultiSourceValidity validity; 


    public void setup(SourceResolver resolver, Map objectModel, String source, Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        this.resolver = resolver;
        this.validity = new MultiSourceValidity(resolver, MultiSourceValidity.CHECK_ALWAYS); 
        this.xIncludePipe = new XIncludePipe(); 
        this.xIncludePipe.init(null, null);
        super.setContentHandler(xIncludePipe);
        super.setLexicalHandler(xIncludePipe);
    }

    public void setConsumer(XMLConsumer consumer) {
        xIncludePipe.setConsumer(consumer);
    }

    public void setContentHandler(ContentHandler handler) {
        xIncludePipe.setContentHandler(handler);
    }

    public void setLexicalHandler(LexicalHandler handler) {
        xIncludePipe.setLexicalHandler(handler);
    }

    public void service(ServiceManager manager) {
        this.manager = manager;
    }

    /** Key to be used for caching */ 
    public Serializable getKey() { 
        return XINCLUDE_CACHE_KEY; 
    } 

    /** Get the validity for this transform */ 
    public SourceValidity getValidity() { 
        return this.validity; 
    } 

    public void recycle()
    {
        // Reset all variables to initial state.
        this.resolver = null;
        this.validity = null; 
        this.xIncludePipe = null;
        super.recycle();
    }

    /**
     * XMLPipe that processes XInclude elements. To perform XInclude processing on included content,
     * this class is instantiated recursively.
     */
    private class XIncludePipe extends AbstractXMLPipe {
        /** Helper class to keep track of xml:base attributes */
        private XMLBaseSupport xmlBaseSupport;
        /** The nesting level of xi:include elements that have been encountered. */
        private int xIncludeElementLevel = 0;

        /** The nesting level of fallback that should be used */
        private int useFallbackLevel = 0;

        /** The nesting level of xi:fallback elements that have been encountered. */
        private int fallbackElementLevel;
        
        /** 
         * Keep a map of namespaces prefix in the source document to pass it
         * to the XPointerContext for correct namespace identification. 
         */
        private Map namespaces = new HashMap();

        /**
         * In case {@link #useFallbackLevel} > 0, then this should contain the 
         * exception that caused fallback to be needed. In the case of nested
         * include elements it will contain only the deepest exception.
         */
        private Exception fallBackException;

        /**
         * Locator of the current stream, stored here so that it can be restored after
         * another document send its content to the consumer.
         */
        private Locator locator;

        /**
         * Value of the href attribute of the xi:include element that caused the creation of the this
         * XIncludePipe. Used to detect loop inclusions.
         */
        private String href;

        /**
         * Value of the xpointer attribute of the xi:include element that caused the creation of this
         * XIncludePipe. Used to detect loop inclusions.
         */
        private String xpointer;
        
        /**
         * Value of the current element level. Used to determine when to insert
         * xml:base attributes for base URI fixup.
         */
        private int level = 0;

        /**
         * Base URI of the parent of the current element. Used to determine
         * if base URI fixup is necessary.
         */
        private String parentBaseURI = null;

        private XIncludePipe parent;

        public void init(String uri, String xpointer) {
            this.href = uri;
            this.xpointer = xpointer;
            this.xmlBaseSupport = new XMLBaseSupport(resolver, getLogger());
        }

        public void setParent(XIncludePipe parent) {
            this.parent = parent;
        }

        public XIncludePipe getParent() {
            return parent;
        }

        public String getHref() {
            return href;
        }

        public String getXpointer() {
            return xpointer;
        }

        /**
         * Determine whether the pipe is currently in a state where contents
         * should be evaluated, i.e. xi:include elements should be resolved
         * and elements in other namespaces should be copied through. Will
         * return false for fallback contents within a successful xi:include,
         * and true for contents outside any xi:include or within an xi:fallback
         * for an unsuccessful xi:include.
         */
        private boolean isEvaluatingContent() {
            return xIncludeElementLevel == 0 || (fallbackElementLevel > 0 && fallbackElementLevel == useFallbackLevel);
        }

        public void endDocument() throws SAXException { 
            // We won't be getting any more sources so mark the MultiSourceValidity as finished. 
            validity.close(); 
            super.endDocument(); 
        } 

        public void startElement(String uri, String name, String raw, Attributes attr) throws SAXException {
            // Track xml:base context:
            parentBaseURI = xmlBaseSupport.getCurrentBase();
            xmlBaseSupport.startElement(uri, name, raw, attr);
            this.level++;
            // Handle elements in xinclude namespace:
            if (XINCLUDE_NAMESPACE_URI.equals(uri)) {
                // Handle xi:include:
                if (XINCLUDE_INCLUDE_ELEMENT.equals(name)) {
                    // Process the include, unless in an ignored fallback:
                    if (isEvaluatingContent()) {
                        String href = attr.getValue("", XINCLUDE_INCLUDE_ELEMENT_HREF_ATTRIBUTE);
                        String parse = attr.getValue("", XINCLUDE_INCLUDE_ELEMENT_PARSE_ATTRIBUTE);
                        String xpointer = attr.getValue("", XINCLUDE_INCLUDE_ELEMENT_XPOINTER_ATTRIBUTE);

                        try {
                            processXIncludeElement(href, parse, xpointer);
                        } catch (ProcessingException e) {
                            getLogger().debug("Rethrowing exception", e);
                            throw new SAXException(e);
                        } catch (IOException e) {
                            getLogger().debug("Rethrowing exception", e);
                            throw new SAXException(e);
                        }
                    }
                    xIncludeElementLevel++;
                } else if (XINCLUDE_FALLBACK_ELEMENT.equals(name)) {
                    // Handle xi:fallback
                    fallbackElementLevel++;
                } else {
                    // Unknown element:
                    throw new SAXException("Unknown XInclude element " + raw + " at " + getLocation());
                }
            } else if (isEvaluatingContent()) {
                // Copy other elements through when appropriate,
                // performing base URI fixup when necessary.
                if(mustAddBaseAttr())
                    super.startElement(uri, name, raw, addBaseURI(attr));
                else
                    super.startElement(uri, name, raw, attr);
            }
        }
       
        private boolean mustAddBaseAttr(){
            if(level != 1)
                return false;
            if(this.parent == null)
                return false;
            String parentBase = this.parent.parentBaseURI;
            String currentBase = xmlBaseSupport.getCurrentBase();
            if(currentBase == null)
                return false;
            if(parentBase == null || !parentBase.equals(currentBase))
                return true;
            return false;
        }

        /**
         * Adds xml:base attribute as per the XInclude spec.
         */
        private Attributes addBaseURI(Attributes oldAttr) throws SAXException {          
            String currentBaseURI = xmlBaseSupport.getCurrentBase();

            AttributesImpl fixedAttr = new AttributesImpl(oldAttr);
                
            // Old xml:base attributes are removed.
            int xmlBaseAttrIdx = fixedAttr.getIndex(XMLBASE_NAMESPACE_URI, XMLBASE_ATTRIBUTE);
            if(xmlBaseAttrIdx != -1)
                fixedAttr.removeAttribute(xmlBaseAttrIdx);
            
            fixedAttr.addAttribute(
                    XMLBASE_NAMESPACE_URI, XMLBASE_ATTRIBUTE,
                    XMLBASE_NAMESPACE_PREFIX + ":" + XMLBASE_ATTRIBUTE,
                    XMLBASE_ATTRIBUTE_TYPE,
                    currentBaseURI
            );
            return fixedAttr;
        }

        public void endElement(String uri, String name, String raw) throws SAXException {
            // Track xml:base context:
            xmlBaseSupport.endElement(uri, name, raw);
            this.level--;
            
            // Handle elements in xinclude namespace:
            if (XINCLUDE_NAMESPACE_URI.equals(uri)) {
                // Handle xi:include:
                if (XINCLUDE_INCLUDE_ELEMENT.equals(name)) {
                    xIncludeElementLevel--;
                    if (useFallbackLevel > xIncludeElementLevel) {
                        useFallbackLevel = xIncludeElementLevel;
                    }
                } else if (XINCLUDE_FALLBACK_ELEMENT.equals(name)) {
                    // Handle xi:fallback:
                    fallbackElementLevel--;
                }
            } else if (isEvaluatingContent()) {
                // Copy other elements through when appropriate:
                super.endElement(uri, name, raw);
            }
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            if (isEvaluatingContent()) {
                super.startPrefixMapping(prefix, uri);
                namespaces.put(prefix, uri);
            }
        }

        public void endPrefixMapping(String prefix) throws SAXException {
            if (isEvaluatingContent()) {
                super.endPrefixMapping(prefix);
                namespaces.remove(prefix);
            }
        }

        public void characters(char c[], int start, int len) throws SAXException {
            if (isEvaluatingContent()) {
                super.characters(c, start, len);
            }
        }

        public void ignorableWhitespace(char c[], int start, int len) throws SAXException {
            if (isEvaluatingContent()) {
                super.ignorableWhitespace(c, start, len);
            }
        }

        public void processingInstruction(String target, String data) throws SAXException {
            if (isEvaluatingContent()) {
                super.processingInstruction(target, data);
            }
        }

        public void skippedEntity(String name) throws SAXException {
            if (isEvaluatingContent()) {
                super.skippedEntity(name);
            }
        }

        public void startEntity(String name) throws SAXException {
            if (isEvaluatingContent()) {
                super.startEntity(name);
            }
        }

        public void endEntity(String name) throws SAXException {
            if (isEvaluatingContent()) {
                super.endEntity(name);
            }
        }

        public void startCDATA() throws SAXException {
            if (isEvaluatingContent()) {
                super.startCDATA();
            }
        }

        public void endCDATA() throws SAXException {
            if (isEvaluatingContent()) {
                super.endCDATA();
            }
        }

        public void comment(char ch[], int start, int len) throws SAXException {
            if (isEvaluatingContent()) {
                super.comment(ch, start, len);
            }
        }

        public void setDocumentLocator(Locator locator) {
            try {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("setDocumentLocator called " + locator.getSystemId());
                }

                // When using SAXON to serialize a DOM tree to SAX, a locator is passed with a "null" system id
                if (locator.getSystemId() != null) {
                    Source source = resolver.resolveURI(locator.getSystemId());
                    try {
                        xmlBaseSupport.setDocumentLocation(source.getURI());
                        // only for the "root" XIncludePipe, we'll have to set the href here, in the other cases
                        // the href is taken from the xi:include href attribute
                        if (href == null)
                            href = source.getURI();
                    } finally {
                        resolver.release(source);
                    }
                }
            } catch (Exception e) {
                throw new CascadingRuntimeException("Error in XIncludeTransformer while trying to resolve base URL for document", e);
            }
            this.locator = locator;
            super.setDocumentLocator(locator);
        }

        protected void processXIncludeElement(String href, String parse, String xpointer)
        throws SAXException,ProcessingException,IOException {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Processing XInclude element: href="+href+", parse="+parse+", xpointer="+xpointer);
            }

            // Default for @parse is "xml"
            if (parse == null) {
                parse = "xml";
            }
            Source url = null;

            try {
                int fragmentIdentifierPos = href.indexOf('#');
                if (fragmentIdentifierPos != -1) {
                    getLogger().warn("Fragment identifer found in 'href' attribute: " + href + 
                            "\nFragment identifiers are forbidden by the XInclude specification. " +
                            "They are still handled by XIncludeTransformer for backward " +
                            "compatibility, but their use is deprecated and will be prohibited " +
                            "in a future release.  Use the 'xpointer' attribute instead.");
                    if (xpointer == null) {
                        xpointer = href.substring(fragmentIdentifierPos + 1);
                    }
                    href = href.substring(0, fragmentIdentifierPos);
                }

                // An empty or absent href is a reference to the current document -- this can be different than the current base
                if (href == null || href.length() == 0) {
                    if (this.href == null) {
                        throw new SAXException("XIncludeTransformer: encountered empty href (= href pointing to the current document) but the location of the current document is unknown.");
                    }
                    // The following can be simplified once fragment identifiers are prohibited
                    int fragmentIdentifierPos2 = this.href.indexOf('#');
                    if (fragmentIdentifierPos2 != -1)
                        href = this.href.substring(0, fragmentIdentifierPos2);
                    else
                        href = this.href;
                }

                url = xmlBaseSupport.makeAbsolute(href);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("URL: " + url.getURI() + "\nXPointer: " + xpointer);
                }

                // add the source to the SourceValidity 
                validity.addSource(url); 

                if (parse.equals("text")) {
                    getLogger().debug("Parse type is text");
                    if (xpointer != null) {
                        throw new SAXException("xpointer attribute must not be present when parse='text': " + getLocation());
                    }
                    InputStream is = null;
                    InputStreamReader isr = null;
                    Reader reader = null;
                    try {
                        is = url.getInputStream();
                        isr = new InputStreamReader(is);
                        reader = new BufferedReader(isr);
                        int read;
                        char ary[] = new char[1024 * 4];
                        while ((read = reader.read(ary)) != -1) {
                            super.characters(ary,0,read);
                        }
                    } catch (SourceNotFoundException e) {
                        useFallbackLevel++;
                        fallBackException = new CascadingException("Resource not found: " + url.getURI());
                        getLogger().error("xIncluded resource not found: " + url.getURI(), e);
                    } finally {
                        if (reader != null) reader.close();
                        if (isr != null) isr.close();
                        if (is != null) is.close();
                    }
                } else if (parse.equals("xml")) {
                    getLogger().debug("Parse type is XML");

                    // Check loop inclusion
                    if (isLoopInclusion(url.getURI(), xpointer)) {
                        throw new ProcessingException("Detected loop inclusion of href=" + url.getURI() + ", xpointer=" + xpointer);
                    }

                    XIncludePipe subPipe = new XIncludePipe();
                    subPipe.init(url.getURI(), xpointer);
                    subPipe.setConsumer(xmlConsumer);
                    subPipe.setParent(this);

                    try {
                        if (xpointer != null && xpointer.length() > 0) {
                            XPointer xptr;
                            xptr = XPointerFrameworkParser.parse(NetUtils.decodePath(xpointer));
                            XPointerContext context = new XPointerContext(xpointer, url, subPipe, manager);
                            for (Iterator iter = namespaces.keySet().iterator(); iter.hasNext();) {
                                String prefix = (String) iter.next();
                                context.addPrefix(prefix, (String) namespaces.get(prefix));
                            }
                            xptr.process(context);
                        } else {
                            SourceUtil.toSAX(manager, url, new IncludeXMLConsumer(subPipe));
                        }
                        // restore locator on the consumer
                        if (locator != null)
                            xmlConsumer.setDocumentLocator(locator);
                    } catch (ResourceNotFoundException e) {
                        useFallbackLevel++;
                        fallBackException = new CascadingException("Resource not found: " + url.getURI());
                        getLogger().error("xIncluded resource not found: " + url.getURI(), e);
                    } catch (ParseException e) {
                        // this exception is thrown in case of an invalid xpointer expression
                        useFallbackLevel++;
                        fallBackException = new CascadingException("Error parsing xPointer expression", e);
                        fallBackException.fillInStackTrace();
                        getLogger().error("Error parsing XPointer expression, will try to use fallback.", e);
                    } catch(SAXException e) {
                        getLogger().error("Error in processXIncludeElement", e);
                        throw e;
                    } catch(ProcessingException e) {
                        getLogger().error("Error in processXIncludeElement", e);
                        throw e;
                    } catch(MalformedURLException e) {
                        useFallbackLevel++;
                        fallBackException = e;
                        getLogger().error("Error processing an xInclude, will try to use fallback.", e);
                    } catch(IOException e) {
                        useFallbackLevel++;
                        fallBackException = e;
                        getLogger().error("Error processing an xInclude, will try to use fallback.", e);
                    }
                } else {
                    throw new SAXException("Found 'parse' attribute with unknown value " + parse + " at " + getLocation());
                }
            } catch (SourceException se) {
                throw SourceUtil.handle(se);
            } finally {
                if (url != null) {
                    resolver.release(url);
                }
            }
        }

        public boolean isLoopInclusion(String uri, String xpointer) {
            if (xpointer == null) {
                xpointer = "";
            }

            if (uri.equals(this.href) && xpointer.equals(this.xpointer == null ? "" : this.xpointer)) {
                return true;
            }

            XIncludePipe parent = getParent();
            while (parent != null) {
                if (uri.equals(parent.getHref()) && xpointer.equals(parent.getXpointer() == null ? "" : parent.getXpointer())) {
                    return true;
                }
                parent = parent.getParent();
            }
            return false;
        }

        private String getLocation() {
            if (this.locator == null) {
                return "unknown location";
            } else {
                return this.locator.getSystemId() + ":" + this.locator.getColumnNumber() + ":" + this.locator.getLineNumber();
            }
        }
    }
}
