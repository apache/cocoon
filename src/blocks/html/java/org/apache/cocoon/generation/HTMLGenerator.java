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
package org.apache.cocoon.generation;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.util.PostInputStream;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:barozzi@nicolaken.com">Nicola Ken Barozzi</a>
 * @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 *
 * @version CVS $Id: HTMLGenerator.java,v 1.8 2003/09/04 09:38:39 cziegeler Exp $
 */
public class HTMLGenerator extends ServiceableGenerator
implements Configurable, CacheableProcessingComponent, Disposable {

    /** The parameter that specifies what request attribute to use, if any */
    public static final String FORM_NAME = "form-name";

    /** The  source, if coming from a file */
    private Source inputSource;

    /** The source, if coming from the request */
    private InputStream requestStream;

    /** XPATH expression */
    private String xpath = null;

    /** XPath Processor */
    private XPathProcessor processor = null;

    /** JTidy properties */
    private Properties properties;

    public void service(ServiceManager manager)
    throws ServiceException {
        super.service( manager );
        this.processor = (XPathProcessor)this.manager.lookup(XPathProcessor.ROLE);
    }

    public void configure(Configuration config) throws ConfigurationException {

        String configUrl = config.getChild("jtidy-config").getValue(null);

        if(configUrl != null) {
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
        if (this.inputSource != null) {
            this.resolver.release( this.inputSource );
            this.inputSource = null;
            this.requestStream = null;
        }
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
        
        if (src == null) {
            // Handle this request as the StreamGenerator does (from the POST
            // request or from a request parameter), but try to make sure
            // that the output will be well-formed

            String contentType = request.getContentType();

            if (contentType == null ) {
                throw new IOException("Content-type was not specified for this request");
            } else if (contentType.startsWith("application/x-www-form-urlencoded") ||
                contentType.startsWith("multipart/form-data")) {
                String requested = parameters.getParameter(FORM_NAME, null);
                if (requested == null) {
                    throw new ProcessingException(
                        "HtmlGenerator with no \"src\" parameter expects a sitemap parameter called '" +
                        FORM_NAME + "' for handling form data"
                    );
                }

                String sXml = request.getParameter(requested);

                requestStream = new ByteArrayInputStream(sXml.getBytes());

            } else if (contentType.startsWith("text/plain") ||
                contentType.startsWith("text/xml") ||
                contentType.startsWith("application/xml")) {

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


        }

        xpath = request.getParameter("xpath");
        if(xpath == null)
            xpath = par.getParameter("xpath",null);

        // append the request parameter to the URL if necessary
        if (par.getParameterAsBoolean("copy-parameters", false)
                && request.getQueryString() != null) {
            StringBuffer query = new StringBuffer(super.source);
            query.append(super.source.indexOf("?") == -1 ? '?' : '&');
            query.append(request.getQueryString());
            super.source = query.toString();
        }

        try {
            if (source != null)
                this.inputSource = resolver.resolveURI(super.source);
        } catch (SourceException se) {
            throw SourceUtil.handle("Unable to resolve " + super.source, se);
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
            // Setup an instance of Tidy.
            Tidy tidy = new Tidy();
            tidy.setXmlOut(true);

            if (this.properties == null) {
                tidy.setXHTML(true);
            } else {
                tidy.setConfigurationFromProps(this.properties);
            }

            //Set Jtidy warnings on-off
            tidy.setShowWarnings(getLogger().isWarnEnabled());
            //Set Jtidy final result summary on-off
            tidy.setQuiet(!getLogger().isInfoEnabled());
            //Set Jtidy infos to a String (will be logged) instead of System.out
            StringWriter stringWriter = new StringWriter();
            PrintWriter errorWriter = new PrintWriter(stringWriter);
            tidy.setErrout(errorWriter);

            // Extract the document using JTidy and stream it.

            if (inputSource != null)
                requestStream = this.inputSource.getInputStream();

            org.w3c.dom.Document doc = tidy.parseDOM(new BufferedInputStream(requestStream), null);

            // FIXME: Jtidy doesn't warn or strip duplicate attributes in same
            // tag; stripping.
            XMLUtils.stripDuplicateAttributes(doc, null);

            errorWriter.flush();
            errorWriter.close();
            if(getLogger().isWarnEnabled()) {
               getLogger().warn(stringWriter.toString());
            }


            if(xpath != null) {
                DOMStreamer domStreamer = new DOMStreamer(this.contentHandler,this.lexicalHandler);

                contentHandler.startDocument();
                NodeList nl = processor.selectNodeList(doc, xpath);
                int length = nl.getLength();
                for(int i=0;i<length;i++) {
                    domStreamer.stream(nl.item(i));
                }
                contentHandler.endDocument();
            } else {
                DOMStreamer streamer = new DOMStreamer(this.contentHandler,this.lexicalHandler);
                streamer.stream(doc);
            }
        } catch (IOException e){
            throw new ResourceNotFoundException("Could not get resource "
                + this.inputSource.getURI(), e);
        } catch (SAXException e){
            throw e;
        } catch (Exception e){
            throw new ProcessingException("Exception in HTMLGenerator.generate()",e);
        }
    }


    public void dispose() {
        if (this.manager != null) {
            this.manager.release(this.processor);
            this.manager = null;
        }
        this.processor = null;
        super.dispose();
    }
}
