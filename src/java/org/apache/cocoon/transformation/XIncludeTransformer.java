/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.source.SourceUtil;
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
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * Implementation of an XInclude transformer. It supports xml:base attributes,
 * XPointer fragment identifiers (see the xpointer package to see what exactly is
 * supported), fallback elements, and does xinclude processing on the included content
 * and on the content of fallback elements (with loop inclusion detection).
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a> (wrote the original version)
 * @version CVS $Id: XIncludeTransformer.java,v 1.20 2004/05/28 22:07:55 mpo Exp $
 */
public class XIncludeTransformer extends AbstractTransformer implements Serviceable {
    protected SourceResolver resolver;
    protected ServiceManager manager;
    private XIncludePipe xIncludePipe;

    public static final String XMLBASE_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
    public static final String XMLBASE_ATTRIBUTE = "base";

    public static final String XINCLUDE_NAMESPACE_URI = "http://www.w3.org/2001/XInclude";
    public static final String XINCLUDE_INCLUDE_ELEMENT = "include";
    public static final String XINCLUDE_FALLBACK_ELEMENT = "fallback";
    public static final String XINCLUDE_INCLUDE_ELEMENT_HREF_ATTRIBUTE = "href";
    public static final String XINCLUDE_INCLUDE_ELEMENT_PARSE_ATTRIBUTE = "parse";


    public void setup(SourceResolver resolver, Map objectModel, String source, Parameters parameters)
            throws ProcessingException, SAXException, IOException {
        this.resolver = resolver;
        this.xIncludePipe = new XIncludePipe();
        this.xIncludePipe.enableLogging(getLogger());
        this.xIncludePipe.init(null);
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

    public void recycle()
    {
        // Reset all variables to initial state.
        this.resolver = null;
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
        /** Element nesting level when inside an xi:include element. */
        private int xIncludeLevel = 0;
        /** Should the content of the fallback element be inserted when it is encountered? */
        private boolean useFallback = false;
        /** Element nesting level when inside the fallback element. */
        private int fallbackLevel;
        /** In case {@link #useFallback} = true, then this should contain the exception that caused fallback to be needed. */
        private Exception fallBackException;
        /**
         * Locator of the current stream, stored here so that it can be restored after
         * another document send its content to the consumer.
         */
        private Locator locator;
        /**
         * Value of the href attribute of the xi:include element that caused the creation of the this
         * XIncludePipe. Used to detect loop inclusions.
         * */
        private String href;
        private XIncludePipe parent;

        public void init(String uri) {
            this.href = uri;
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

        public void startElement(String uri, String name, String raw, Attributes attr) throws SAXException {
            if (xIncludeLevel == 1 && useFallback && XINCLUDE_NAMESPACE_URI.equals(uri) && XINCLUDE_FALLBACK_ELEMENT.equals(name)) {
                fallbackLevel++;

                // don't need these anymore
                useFallback = false;
                fallBackException = null;

                return;
            } else if (xIncludeLevel > 0 && fallbackLevel < 1) {
                xIncludeLevel++;
                return;
            }

            xmlBaseSupport.startElement(uri, name, raw, attr);
            if (XINCLUDE_NAMESPACE_URI.equals(uri)) {
                if (XINCLUDE_INCLUDE_ELEMENT.equals(name)) {
                    String href = attr.getValue("",XINCLUDE_INCLUDE_ELEMENT_HREF_ATTRIBUTE);
                    if (href == null) {
                        throw new SAXException(raw + " must have a 'href' attribute at " + getLocation());
                    }

                    String parse = attr.getValue("",XINCLUDE_INCLUDE_ELEMENT_PARSE_ATTRIBUTE);

                    if (null == parse) parse="xml";
                    xIncludeLevel++;

                    try {
                        processXIncludeElement(href, parse);
                    } catch (ProcessingException e) {
                        getLogger().debug("Rethrowing exception", e);
                        throw new SAXException(e);
                    } catch (IOException e) {
                        getLogger().debug("Rethrowing exception", e);
                        throw new SAXException(e);
                    }
                    return;
                }

                throw new SAXException("Unknown XInclude element " + raw + " at " + getLocation());

            } else {
                super.startElement(uri,name,raw,attr);
            }
        }

        public void endElement(String uri, String name, String raw) throws SAXException {
            if (xIncludeLevel > 0 && fallbackLevel < 1) {
                xIncludeLevel--;
                if (xIncludeLevel == 0)
                    xmlBaseSupport.endElement(uri, name, raw);
                if (xIncludeLevel == 0 && useFallback) {
                    // an error was encountered but a fallback element was not found: throw the error now
                    useFallback = false;
                    Exception localFallBackException = fallBackException;
                    fallBackException = null;
                    fallbackLevel = 0;
                    getLogger().error("Exception occured during xinclude processing, and did not find a fallback element.", localFallBackException);
                    throw new SAXException("Exception occured during xinclude processing, and did not find a fallback element.", localFallBackException);
                }
                return;
            }

            if (fallbackLevel > 0) {
                fallbackLevel--;
                if (fallbackLevel == 0)
                    return;
            }

            xmlBaseSupport.endElement(uri, name, raw);
            super.endElement(uri,name,raw);
        }

        public void startPrefixMapping(String prefix, String uri)
                throws SAXException {
            if (xIncludeLevel > 0 && fallbackLevel < 1)
                return;
            super.startPrefixMapping(prefix, uri);
        }

        public void endPrefixMapping(String prefix)
                throws SAXException {
            if (xIncludeLevel > 0 && fallbackLevel < 1)
                return;
            super.endPrefixMapping(prefix);
        }

        public void characters(char c[], int start, int len)
                throws SAXException {
            if (xIncludeLevel > 0 && fallbackLevel < 1)
                return;
            super.characters(c, start, len);
        }

        public void ignorableWhitespace(char c[], int start, int len)
                throws SAXException {
            if (xIncludeLevel > 0 && fallbackLevel < 1)
                return;
            super.ignorableWhitespace(c, start, len);
        }

        public void processingInstruction(String target, String data)
                throws SAXException {
            if (xIncludeLevel > 0 && fallbackLevel < 1)
                return;
            super.processingInstruction(target, data);
        }

        public void skippedEntity(String name)
                throws SAXException {
            if (xIncludeLevel > 0 && fallbackLevel < 1)
                return;
            super.skippedEntity(name);
        }

        public void startEntity(String name)
                throws SAXException {
            if (xIncludeLevel > 0 && fallbackLevel < 1)
                return;
            super.startEntity(name);
        }

        public void endEntity(String name)
                throws SAXException {
            if (xIncludeLevel > 0 && fallbackLevel < 1)
                return;
            super.endEntity(name);
        }

        public void startCDATA()
                throws SAXException {
            if (xIncludeLevel > 0 && fallbackLevel < 1)
                return;
            super.startCDATA();
        }

        public void endCDATA()
                throws SAXException {
            if (xIncludeLevel > 0 && fallbackLevel < 1)
                return;
            super.endCDATA();
        }

        public void comment(char ch[], int start, int len)
                throws SAXException {
            if (xIncludeLevel > 0 && fallbackLevel < 1)
                return;
            super.comment(ch, start, len);
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

        protected void processXIncludeElement(String href, String parse)
        throws SAXException,ProcessingException,IOException {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Processing XInclude element: href="+href+", parse="+parse);
            }

            Source url = null;
            String suffix = "";
            try {
                int fragmentIdentifierPos = href.indexOf('#');
                if (fragmentIdentifierPos != -1) {
                    suffix = href.substring(fragmentIdentifierPos + 1);
                    href = href.substring(0, fragmentIdentifierPos);
                }

                // an empty href is a reference to the current document -- this can be different than the current base
                if (href.equals("")) {
                    if (this.href == null)
                        throw new SAXException("XIncludeTransformer: encountered empty href (= href pointing to the current document) but the location of the current document is unknown.");
                    int fragmentIdentifierPos2 = this.href.indexOf('#');
                    if (fragmentIdentifierPos2 != -1)
                        href = this.href.substring(0, fragmentIdentifierPos2);
                    else
                        href = this.href;
                }

                url = xmlBaseSupport.makeAbsolute(href);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("URL: " + url.getURI() + "\nSuffix: " + suffix);
                }

                // check loop inclusion
                String canonicURI = url.getURI() + (suffix.length() > 0 ? "#" + suffix: "");
                if (isLoopInclusion(canonicURI))
                    throw new ProcessingException("Detected loop inclusion of " + canonicURI);

                if (parse.equals("text")) {
                    getLogger().debug("Parse type is text");
                    InputStream input = url.getInputStream();
                    Reader reader = new BufferedReader(new InputStreamReader(input));
                    int read;
                    char ary[] = new char[1024];
                    if (reader != null) {
                        while ((read = reader.read(ary)) != -1) {
                            super.characters(ary,0,read);
                        }
                        reader.close();
                    }
                } else if (parse.equals("xml")) {
                    XIncludePipe subPipe = new XIncludePipe();
                    subPipe.enableLogging(getLogger());
                    subPipe.init(canonicURI);
                    subPipe.setConsumer(xmlConsumer);
                    subPipe.setParent(this);

                    getLogger().debug("Parse type is XML");
                    try {
                        if (suffix.length() > 0) {
                            XPointer xpointer;
                            xpointer = XPointerFrameworkParser.parse(NetUtils.decodePath(suffix));
                            XPointerContext context = new XPointerContext(suffix, url, subPipe, getLogger(), manager);
                            xpointer.process(context);
                        } else {
                            SourceUtil.toSAX(url, new IncludeXMLConsumer(subPipe));
                        }
                        // restore locator on the consumer
                        if (locator != null)
                            xmlConsumer.setDocumentLocator(locator);
                    } catch (ResourceNotFoundException e) {
                        useFallback = true;
                        fallBackException = new CascadingException("Resource not found: " + url.getURI());
                        getLogger().error("xIncluded resource not found: " + url.getURI(), e);
                    } catch (ParseException e) {
                        // this exception is thrown in case of an invalid xpointer expression
                        useFallback = true;
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
                        useFallback = true;
                        fallBackException = e;
                        getLogger().error("Error processing an xInclude, will try to use fallback.", e);
                    } catch(IOException e) {
                        useFallback = true;
                        fallBackException = e;
                        getLogger().error("Error processing an xInclude, will try to use fallback.", e);
                    }
                }
            } catch (SourceException se) {
                throw SourceUtil.handle(se);
            } finally {
                if (url != null) {
                    resolver.release(url);
                }
            }
        }

        public boolean isLoopInclusion(String uri) {
            if (uri.equals(this.href)) {
                return true;
            }

            XIncludePipe parent = getParent();
            while (parent != null) {
                if (uri.equals(parent.getHref())) {
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
