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

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.regexp.RE;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 *  The WebServiceProxyGenerator is intended to:
 *
 * 1) Allow easy syndication of dynamic interactive content as a natural extension of the currently popular static content syndication with RSS.
 *
 * 2) Allow transparent routing of web service request through GET, POST, SOAP-RPC and SOAP-DOC binding methods.
 *
 * 3) Allow almost full control through sitemap configuration.
 *
 * 4) Allow use of Cocoon components for content formatting, aggregation and styling through a tight integration with the Cocoon sitemap.
 *
 * 5) Require 0 (zero) lines of Java or other business logic code in most cases.
 *
 * 6) Be generic and flexible enough to allow custom extensions for advanced and non-typical uses.
 *
 * 7) Support sessions, authentication, http 1.1, https,  request manipulation, redirects following, connection pooling, and others.
 *
 * 8) Use the Jakarta HttpClient library which provides many sophisticated features for HTTP connections.
 *
 * 9) (TBD) Use Axis for SOAP-RPC and SOAP-DOC bindings.
 *
 *
 * @author <a href="mailto:ivelin@apache.org">Ivelin Ivanov</a>, June 30, 2002
 * @author <a href="mailto:tony@apache.org">Tony Collen</a>, December 2, 2002
 * @version CVS $Id: WebServiceProxyGenerator.java,v 1.5 2003/12/15 07:47:42 tony Exp $
 */
public class WebServiceProxyGenerator extends ServiceableGenerator {

    private static final String HTTP_CLIENT = "HTTP_CLIENT";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";

    private HttpClient httpClient = null;
    private String configuredHttpMethod = null;

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        try {
            Source inputSource = resolver.resolveURI(super.source);
            this.source = inputSource.getURI();
        } catch (SourceException se) {
            throw SourceUtil.handle("Unable to resolve " + super.source, se);
        }

        this.configuredHttpMethod = par.getParameter("wsproxy-method", METHOD_GET);
        this.httpClient = this.getHttpClient();
    }

    /**
     * Generate XML data.
     */
    public void generate() throws IOException, SAXException, ProcessingException {
        SAXParser parser = null;
        try {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("processing Web Service request: " + this.source);
            }

            // forward request and bring response back
            byte[] response = this.fetch();
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("response: " + new String(response));
            }

            /* TODO: Though I avoided the getResponseBodyAsString(), the content
             *       seems not to be parsed correctly. Who cares about the encoding
             *       in the XML declaration?
             * {@link http://jakarta.apache.org/commons/httpclient/apidocs/org/apache/commons/httpclient/HttpMethodBase.html#getResponseBodyAsString()}
             */
            ByteArrayInputStream responseStream = new ByteArrayInputStream(response);
            InputSource inputSource = new InputSource(responseStream);
            parser = (SAXParser)this.manager.lookup(SAXParser.ROLE);
            parser.parse(inputSource, super.xmlConsumer);

        } catch (ServiceException ex) {
            throw new ProcessingException("WebServiceProxyGenerator.generate() error", ex);
        } finally {
            this.manager.release(parser);
        }

    } // generate

    /**
     * Recycle this component.
     * All instance variables are set to <code>null</code>.
     */
    public void recycle() {
        this.httpClient = null;
        this.configuredHttpMethod = null;
        super.recycle();
    }

    /**
     * Forwards the request and returns the response.
     * 
     * The rest is probably out of date:
     * Will use a UrlGetMethod to benefit the cacheing mechanism
     * and intermediate proxy servers.
     * It is potentially possible that the size of the request
     * may grow beyond a certain limit for GET and it will require POST instead.
     *
     * @return byte[] XML response
     */
    public byte[] fetch() throws ProcessingException {
        HttpMethod method = null;

        // check which method (GET or POST) to use.
        if (this.configuredHttpMethod.equalsIgnoreCase(METHOD_POST)) {
            method = new PostMethod(this.source);
        } else {
            method = new GetMethod(this.source);
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("request HTTP method: " + method.getName());
        }

        // this should probably be exposed as a sitemap option
        method.setFollowRedirects(true);

        // copy request parameters and merge with URL parameters
        Request request = ObjectModelHelper.getRequest(objectModel);

        ArrayList paramList = new ArrayList();
        Enumeration enum = request.getParameterNames();
        while (enum.hasMoreElements()) {
            String pname = (String)enum.nextElement();
            String[] paramsForName = request.getParameterValues(pname);
            for (int i = 0; i < paramsForName.length; i++) {
                NameValuePair pair = new NameValuePair(pname, paramsForName[i]);
                paramList.add(pair);
            }
        }

        if (paramList.size() > 0) {
            NameValuePair[] allSubmitParams = new NameValuePair[paramList.size()];
            paramList.toArray(allSubmitParams);

            String urlQryString = method.getQueryString();

            // use HttpClient encoding routines
            method.setQueryString(allSubmitParams);
            String submitQryString = method.getQueryString();

            // set final web service query string
            
            // sometimes the querystring is null here...
            if (null == urlQryString) {
            	method.setQueryString(submitQryString);
            } else {
				method.setQueryString(urlQryString + "&" + submitQryString);	
            }
            
        } // if there are submit parameters

        byte[] response = null;
        try {
            int httpStatus = httpClient.executeMethod(method);
            if (httpStatus < 400) {
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("Return code when accessing the remote Url: " + httpStatus);
                }
            } else {
                throw new ProcessingException("The remote returned error " + httpStatus + " when attempting to access remote URL:" + method.getURI());
            }
        } catch (URIException e) {
            throw new ProcessingException("There is a problem with the URI: " + this.source, e);
        } catch (IOException e) {
            try {
                throw new ProcessingException("Exception when attempting to access the remote URL: " + method.getURI(), e);
            } catch (URIException ue) {
                throw new ProcessingException("There is a problem with the URI: " + this.source, ue);
            }
        } finally {
            /* It is important to always read the entire response and release the
             * connection regardless of whether the server returned an error or not.
             * {@link http://jakarta.apache.org/commons/httpclient/tutorial.html}
             */
            response = method.getResponseBody();
            method.releaseConnection();
        }

        return response;
    } // fetch

    /**
     * Create one per client session. 
     */
    protected HttpClient getHttpClient() throws ProcessingException {
        URI uri = null;
        String host = null;
        Request request = ObjectModelHelper.getRequest(objectModel);
        Session session = request.getSession(true);
        HttpClient httpClient = null;
        if (session != null) {
            httpClient = (HttpClient)session.getAttribute(HTTP_CLIENT);
        }
        if (httpClient == null) {
            httpClient = new HttpClient();
            HostConfiguration config = httpClient.getHostConfiguration();
            if (config == null) {
                config = new HostConfiguration();
            }
            
            
            /* TODO: fixme!
             * When the specified source sent to the wsproxy is not "http" 
             * (e.g. "cocoon:/"), the HttpClient throws an exception.  Does the source
             * here need to be resolved before being set in the HostConfiguration?
             */
            try {
                uri = new URI(this.source);
                host = uri.getHost();
                config.setHost(uri);
            } catch (URIException ex) {
                throw new ProcessingException("URI format error: " + ex, ex);
            }

            // Check the http.nonProxyHosts to see whether or not the current
            // host needs to be served through the proxy server.
            boolean proxiableHost = true;
            String nonProxyHosts = System.getProperty("http.nonProxyHosts");
            if (nonProxyHosts != null)
            {
                StringTokenizer tok = new StringTokenizer(nonProxyHosts, "|");

                while (tok.hasMoreTokens()) {
                    String nonProxiableHost = tok.nextToken().trim();

                    // XXX is there any other characters that need to be
                    // escaped?
                    nonProxiableHost = StringUtils.replace(nonProxiableHost, ".", "\\.");
                    nonProxiableHost = StringUtils.replace(nonProxiableHost, "*", ".*");

                    // XXX do we want .example.com to match
                    // computer.example.com?  it seems to be a very common
                    // idiom for the nonProxyHosts, in that case then we want
                    // to change "^" to "^.*"
                    RE re = null;
                    try {
                        re = new RE("^" + nonProxiableHost + "$");
                    }
                    catch (Exception ex) {
                        throw new ProcessingException("Regex syntax error: " + ex, ex);
                    }

                    if (re.match(host))
                    {
                        proxiableHost = false;
                        break;
                    }
                }
            }

            if (proxiableHost && System.getProperty("http.proxyHost") != null) {
                String proxyHost = System.getProperty("http.proxyHost");
                int proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
                config.setProxy(proxyHost, proxyPort);
            }

            httpClient.setHostConfiguration(config);

            session.setAttribute(HTTP_CLIENT, httpClient);
        }
        return httpClient;
    }


} // class
