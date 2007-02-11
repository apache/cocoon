/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.transformation;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.xml.dom.DOMParser;
import org.apache.excalibur.xml.sax.SAXParser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Stack;

/**
 * My first pass at an XInclude transformation. Currently it should set the base URI
 * from the SAX Locator's system id but allow it to be overridden by xml:base
 * elements as the XInclude spec mandates. It's also got some untested code
 * that should handle inclusion of text includes, but that method isn't called
 * by the SAX event FSM yet.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version CVS $Id: XIncludeTransformer.java,v 1.1 2003/03/09 00:09:40 pier Exp $
 */
public class XIncludeTransformer extends AbstractTransformer implements Composable, Disposable {

    private SourceResolver resolver;

    /** XPath Processor */
    private XPathProcessor processor = null;

    protected ComponentManager manager = null;

    public static final String XMLBASE_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
    public static final String XMLBASE_ATTRIBUTE = "base";

    public static final String XINCLUDE_NAMESPACE_URI = "http://www.w3.org/2001/XInclude";
    public static final String XINCLUDE_INCLUDE_ELEMENT = "include";
    public static final String XINCLUDE_INCLUDE_ELEMENT_HREF_ATTRIBUTE = "href";
    public static final String XINCLUDE_INCLUDE_ELEMENT_PARSE_ATTRIBUTE = "parse";

    protected Source base_xmlbase_uri = null;

    /** The current XMLBase URI. We start with an empty "dummy" URL. **/
    protected Source current_xmlbase_uri = null;

    /** This is a stack of xml:base attributes which belong to our ancestors **/
    protected Stack xmlbase_stack = new Stack();

    /** namespace uri of the last element which had an xml:base attribute **/
    protected String last_xmlbase_element_uri = "";

    protected Stack xmlbase_element_uri_stack = new Stack();

    /** name of the last element which had an xml:base attribute **/
    protected String last_xmlbase_element_name = "";

    protected Stack xmlbase_element_name_stack = new Stack();

    public void setup(SourceResolver resolver, Map objectModel,
                      String source, Parameters parameters)
            throws ProcessingException, SAXException, IOException {
        this.resolver = resolver;
    }

    public void compose(ComponentManager manager) {
        this.manager = manager;
        try {
            this.processor = (XPathProcessor)this.manager.lookup(XPathProcessor.ROLE);
        } catch (Exception e) {
            getLogger().error("cannot obtain XPathProcessor", e);
        }
    }

    public void startElement(String uri, String name, String raw, Attributes attr) throws SAXException {
        String value;
        if ((value = attr.getValue(XMLBASE_NAMESPACE_URI,XMLBASE_ATTRIBUTE)) != null) {
            try {
                startXMLBaseAttribute(uri,name,value);
            } catch (ProcessingException e) {
                getLogger().debug("Rethrowing exception", e);
                throw new SAXException(e);
            }
        }
        if (XINCLUDE_NAMESPACE_URI.equals(uri) && XINCLUDE_INCLUDE_ELEMENT.equals(name)) {
            String href = attr.getValue("",XINCLUDE_INCLUDE_ELEMENT_HREF_ATTRIBUTE);
            String parse = attr.getValue("",XINCLUDE_INCLUDE_ELEMENT_PARSE_ATTRIBUTE);

            if (null == parse) parse="xml";

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
        super.startElement(uri,name,raw,attr);
    }

    public void endElement(String uri, String name, String raw) throws SAXException {
        if (last_xmlbase_element_uri.equals(uri) && last_xmlbase_element_name.equals(name)) {
            endXMLBaseAttribute();
        }
        if (uri != null && name != null && uri.equals(XINCLUDE_NAMESPACE_URI) && name.equals(XINCLUDE_INCLUDE_ELEMENT)) {
            return;
        }
        super.endElement(uri,name,raw);
    }

    public void setDocumentLocator(Locator locator) {
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("setDocumentLocator called " + locator.getSystemId());
            }

            base_xmlbase_uri = this.resolver.resolveURI(locator.getSystemId());

            // If url ends with .xxx then truncate to dir
            if (base_xmlbase_uri.getURI().lastIndexOf('.') > base_xmlbase_uri.getURI().lastIndexOf('/')) {
               String uri = base_xmlbase_uri.getURI().substring(0,base_xmlbase_uri.getURI().lastIndexOf('/')+1);
               this.resolver.release(base_xmlbase_uri);
               base_xmlbase_uri = null;
               base_xmlbase_uri = this.resolver.resolveURI(uri);
            }

            if (current_xmlbase_uri == null) {
               current_xmlbase_uri = base_xmlbase_uri;
            }

        } catch (Exception e) {
            getLogger().debug("Exception ignored", e);
        }
        super.setDocumentLocator(locator);
    }

    protected void startXMLBaseAttribute(String uri, String name, String value) throws ProcessingException {
        String urlLoc = value;

        if (! urlLoc.endsWith("/")) {
            urlLoc += "/";
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("XMLBase = " + urlLoc);
        }

        if (current_xmlbase_uri != null) {
            xmlbase_stack.push(current_xmlbase_uri);
        }

        try {
            current_xmlbase_uri = this.resolver.resolveURI(urlLoc);

            xmlbase_element_uri_stack.push(last_xmlbase_element_uri);
            last_xmlbase_element_uri = uri;

            xmlbase_element_name_stack.push(last_xmlbase_element_name);
            last_xmlbase_element_name = name;
        } catch (SourceException e) {
            throw SourceUtil.handle(e);
        } catch (Exception e) {
            throw new ProcessingException("Could not resolve '" + urlLoc + "'", e);
        }
    }

    protected void endXMLBaseAttribute() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("XMLBase ended");
        }

        if (xmlbase_stack.size() > 0) {
            current_xmlbase_uri = (Source)xmlbase_stack.pop();
        } else {
            current_xmlbase_uri = base_xmlbase_uri;
        }
        last_xmlbase_element_uri = (String)xmlbase_element_uri_stack.pop();
        last_xmlbase_element_name = (String)xmlbase_element_name_stack.pop();
    }

    protected void processXIncludeElement(String href, String parse)
    throws SAXException,ProcessingException,IOException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Processing XInclude element: href="+href+", parse="+parse);
            if(current_xmlbase_uri == null)
                getLogger().debug("Base URI: null");
            else
                getLogger().debug("Base URI: " + current_xmlbase_uri.getURI());
        }

        Source url = null;
        String suffix;
        try {
            int index = href.indexOf('#');
            if (index < 0) {
                if(current_xmlbase_uri == null)
                    url = this.resolver.resolveURI(href);
                else
                    url = this.resolver.resolveURI(current_xmlbase_uri.getURI() + href);
                suffix = "";
            } else {
                if(current_xmlbase_uri == null)
                    url = this.resolver.resolveURI(href.substring(0,index));
                else
                    url = this.resolver.resolveURI(current_xmlbase_uri.getURI() + href.substring(0,index));
                suffix = href.substring(index+1);
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("URL: "+url+"\nSuffix: "+suffix);
            }

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
                getLogger().debug("Parse type is XML");
                try {

                    InputSource input = SourceUtil.getInputSource(url);

                    if (suffix.startsWith("xpointer(") && suffix.endsWith(")")) {
                        DOMParser parser = null;
                        try {
                            parser = (DOMParser)manager.lookup(DOMParser.ROLE);
                            String xpath = suffix.substring(9,suffix.length()-1);
                            getLogger().debug("XPath is "+xpath);
                            Document document = parser.parseDocument(input);
                            NodeList list = processor.selectNodeList(document,xpath);
                            DOMStreamer streamer = new DOMStreamer(super.contentHandler,super.lexicalHandler);
                            int length = list.getLength();
                            for (int i=0; i<length; i++) {
                                streamer.stream(list.item(i));
                            }
                        } finally {
                            this.manager.release((Component)parser);
                        }
                    } else {
                        SAXParser parser = null;
                        try {
                            parser = (SAXParser)manager.lookup(SAXParser.ROLE);
                            IncludeXMLConsumer xinclude_handler = new IncludeXMLConsumer(super.contentHandler,super.lexicalHandler);
                            parser.parse(input, xinclude_handler);
                        } finally {
                            this.manager.release((Component)parser);
                        }
                    }
                } catch(SAXException e) {
                    getLogger().error("Error in processXIncludeElement", e);
                    throw e;
                } catch(ProcessingException e) {
                    getLogger().error("Error in processXIncludeElement", e);
                    throw e;
                } catch(MalformedURLException e) {
                    getLogger().error("Error in processXIncludeElement", e);
                    throw e;
                } catch(IOException e) {
                    getLogger().error("Error in processXIncludeElement", e);
                    throw e;
                } catch(ComponentException e) {
                    getLogger().error("Error in processXIncludeElement", e);
                    throw new SAXException(e);
                }
            }
        } catch (SourceException se) {
            throw SourceUtil.handle(se);
        } finally {
            this.resolver.release(url);
        }
    }

    public void recycle()
    {
        // Reset all variables to initial state.
        if (base_xmlbase_uri != null) this.resolver.release(base_xmlbase_uri);
        base_xmlbase_uri = null;
        if (current_xmlbase_uri != null) this.resolver.release(current_xmlbase_uri);
        current_xmlbase_uri = null;
        this.resolver = null;
        xmlbase_stack = new Stack();
        last_xmlbase_element_uri = "";
        xmlbase_element_uri_stack = new Stack();
        last_xmlbase_element_name = "";
        xmlbase_element_name_stack = new Stack();
        super.recycle();
    }

    public void dispose()
    {
        if (this.processor instanceof Component)
            this.manager.release((Component)this.processor);
    }
}
