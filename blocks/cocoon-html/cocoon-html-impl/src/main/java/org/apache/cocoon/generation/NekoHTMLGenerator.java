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
package org.apache.cocoon.generation;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.NekoHtmlSaxParser;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.util.PostInputStream;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The Neko HTML generator reads HTML from a source, converts it to XHTML
 * and generates SAX Events. It uses the NekoHTML library to do this.
 *
 * @cocoon.sitemap.component.documentation
 * The Neko HTML generator reads HTML from a source, converts it to XHTML
 * and generates SAX Events. It uses the NekoHTML library to do this.
 * @cocoon.sitemap.component.name   nekohtml
 * @cocoon.sitemap.component.label  content
 * @cocoon.sitemap.component.documentation.caching Yes.
 * Uses the last modification date of the xml document for validation
 * @cocoon.sitemap.component.pooling.max  32
 *
 * @version $Id$
 */
public class NekoHTMLGenerator extends ServiceableGenerator
                               implements Configurable, CacheableProcessingComponent, Disposable {

    /** The parameter that specifies what request parameter to use, if any */
    public static final String FORM_NAME = "form-name";

    /** The request parameter value, if coming from a request parameter */
    private String requestParameterValue;

    /** The source, if coming from a file */
    private Source inputSource;

    /** The source, if coming from the request */
    private InputStream requestStream;

    /** XPATH expression */
    private String xpath;

    /** XPath Processor */
    private XPathProcessor processor;

    /** Neko properties */
    private Properties properties;

    public void service(ServiceManager manager)
    throws ServiceException {
        super.service(manager);
        this.processor = (XPathProcessor) this.manager.lookup(XPathProcessor.ROLE);
    }

    public void configure(Configuration config) throws ConfigurationException {

        String configUrl = config.getChild("neko-config").getValue(null);
        if (configUrl != null) {
            org.apache.excalibur.source.SourceResolver resolver = null;
            Source configSource = null;
            try {
                resolver = (org.apache.excalibur.source.SourceResolver)this.manager.lookup(org.apache.excalibur.source.SourceResolver.ROLE);
                configSource = resolver.resolveURI(configUrl);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Loading configuration from " + configSource.getURI());
                }

                this.properties = new Properties();
                this.properties.load(configSource.getInputStream());

            } catch (Exception e) {
                getLogger().warn("Cannot load configuration from " + configUrl);
                throw new ConfigurationException("Cannot load configuration from " + configUrl, e);
            } finally {
                if ( null != resolver ) {
                    this.manager.release(resolver);
                    resolver.release(configSource);
                }
            }
        }
    }

    /**
     * Recycle this component.
     * All instance variables are set to <code>null</code>.
     */
    public void recycle() {
        if (this.requestStream != null) {
            try {
                this.requestStream.close();
            } catch (IOException e) {
                // ignore
            }
            this.requestStream = null;
        }
        if (this.inputSource != null) {
            this.resolver.release(this.inputSource);
            this.inputSource = null;
        }
        this.requestParameterValue = null;
        this.xpath = null;
        super.recycle();
    }

    /**
     * Setup the html generator.
     * Try to get the last modification date of the source for caching.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        Request request = ObjectModelHelper.getRequest(objectModel);
        
        if (this.source == null) {
            // Handle this request as the StreamGenerator does (from the POST
            // request or from a request parameter), but try to make sure
            // that the output will be well-formed

            String contentType = request.getContentType();

            if (contentType == null ) {
                throw new IOException("Content-type was not specified for this request");
            } else if (contentType.startsWith("application/x-www-form-urlencoded")
                       || contentType.startsWith("multipart/form-data")) {
                String requested = parameters.getParameter(FORM_NAME, null);
                if (requested == null) {
                    throw new ProcessingException(
                        "NekoHtmlGenerator with no \"src\" parameter expects a sitemap parameter called '" +
                        FORM_NAME + "' for handling form data"
                    );
                }

                requestParameterValue = request.getParameter(requested);
            } else if (contentType.startsWith("text/plain")
                       || contentType.startsWith("text/xml")
                       || contentType.startsWith("application/xml")) {

                HttpServletRequest httpRequest = (HttpServletRequest) objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT);
                if ( httpRequest == null ) {
                    throw new ProcessingException("This functionality only works in an http environment.");
                }
                int len = request.getContentLength();
                if (len > 0) {
                    requestStream = new PostInputStream(httpRequest.getInputStream(), len);
                } else {
                    throw new IOException("getContentLen() == 0");
                }
            } else {
                throw new IOException("Unexpected getContentType(): " + request.getContentType());
            }
        } else {
            // append the request parameter to the URL if necessary
            if (parameters.getParameterAsBoolean("copy-parameters", false)
                    && request.getQueryString() != null) {
                StringBuffer query = new StringBuffer(this.source);
                query.append(this.source.indexOf("?") == -1 ? '?' : '&');
                query.append(request.getQueryString());
                this.source = query.toString();
            }

            try {
                this.inputSource = resolver.resolveURI(this.source);
            } catch (SourceException se) {
                throw SourceUtil.handle("Unable to resolve " + this.source, se);
            }
        }

        xpath = request.getParameter("xpath");
        if (xpath == null) {
            xpath = par.getParameter("xpath",null);
        }
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     * This method must be invoked before the generateValidity() method.
     *
     * @return The generated key or <code>0</code> if the component
     *              is currently not cacheable.
     */
    public java.io.Serializable getKey() {
        if (this.inputSource == null)
            return null;

        if (this.xpath != null) {
            StringBuffer buffer = new StringBuffer(this.inputSource.getURI());
            buffer.append(':').append(this.xpath);
            return buffer.toString();
        } else {
            return this.inputSource.getURI();
        }
    }

    /**
     * Generate the validity object.
     * Before this method can be invoked the generateKey() method
     * must be invoked.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        if (this.inputSource == null)
            return null;
        return this.inputSource.getValidity();
    }

    /**
     * Generate XML data.
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
        try {
            NekoHtmlSaxParser parser = new NekoHtmlSaxParser(this.properties);
            
            InputSource saxSource;
            if (this.requestParameterValue != null) {
                saxSource = new InputSource(new StringReader(this.requestParameterValue));
            }
            else {
                if (inputSource != null) {
                    requestStream = this.inputSource.getInputStream();
                }
                saxSource = new InputSource(requestStream);
            }
            
            if (xpath != null) {
                DOMBuilder builder = new DOMBuilder();
                parser.setContentHandler(builder);
                parser.parse(saxSource);
                Document doc = builder.getDocument();

                DOMStreamer domStreamer = new DOMStreamer(this.contentHandler,
                                                          this.lexicalHandler);
                this.contentHandler.startDocument();
                NodeList nl = processor.selectNodeList(doc, xpath);
                int length = nl.getLength();
                for(int i=0; i < length; i++) {
                    domStreamer.stream(nl.item(i));
                }
                this.contentHandler.endDocument();
            } else {
                parser.setContentHandler(this.contentHandler);
                parser.parse(saxSource);
            }
        } catch (IOException e){
            throw new ResourceNotFoundException("Could not get resource "
                + this.inputSource.getURI(), e);
        } catch (SAXException e){
            throw e;
        } catch (Exception e){
            throw new ProcessingException("Exception in NekoHTMLGenerator.generate()",e);
        }
    }

    public void dispose() {
        if (this.processor != null) {
            this.manager.release(this.processor);
            this.processor = null;
        }
        this.manager = null;
        super.dispose();
    }

}
