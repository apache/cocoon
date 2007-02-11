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
package org.apache.cocoon.generation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.SourceResolver;

import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import org.apache.excalibur.xml.sax.SAXParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The <code>HttpProxyGenerator</code> is a Cocoon generator using the
 * <b>Jakarta Commons HTTPClient Library</b> to access an XML stream
 * over HTTP.
 * 
 * @author <a href="mailto:ivelin@apache.org">Ivelin Ivanov</a>, June 2002
 * @author <a href="mailto:tony@apache.org">Tony Collen</a>, December 2002
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: HttpProxyGenerator.java,v 1.7 2004/03/05 13:02:20 bdelacretaz Exp $
 */
public class HttpProxyGenerator extends ServiceableGenerator implements Configurable {

    /** The HTTP method to use at request time. */
    private HttpMethodBase method = null;
    /** The base HTTP URL for requests. */
    private HttpURL url = null;
    /** The list of request parameters for the request */
    private ArrayList reqParams = null;
    /** The list of query parameters for the request */
    private ArrayList qryParams = null;
    /** Wether we want a debug output or not */
    private boolean debug = false;

    /**
     * Default (empty) constructor.
     */
    public HttpProxyGenerator() {
        super();
    }

    /**
     * Set up this <code>Generator</code> instance from its sitemap <code>Configuration</code>
     *
     * @param configuration The base <code>Configuration</code> for this <code>Generator</code>.
     * @throws ConfigurationException If this instance cannot be configured properly.
     * @see #recycle()
     */
    public void configure(Configuration configuration)
    throws ConfigurationException {

        /* Setup the HTTP method to use. */
        String method = configuration.getChild("method").getValue("GET");
        if ("GET".equalsIgnoreCase(method)) {
            this.method = new GetMethod();
        } else if ("POST".equalsIgnoreCase(method)) {
            this.method = new PostMethod();
            /* TODO: Is this still needed? Does it refer to a bug in bugzilla?
             *       At least the handling in httpclient has completely changed.
             * Work around a bug from the HttpClient library */
            ((PostMethod) this.method).setRequestBody("");
        } else {
            throw new ConfigurationException("Invalid method \"" + method + "\" specified"
                    + " at " + configuration.getChild("method").getLocation());
        }

        /* Create the base URL */
        String url = configuration.getChild("url").getValue(null);
        try {
            if (url != null) this.url = new HttpURL(url);
        } catch (URIException e) {
            throw new ConfigurationException("Cannot process URL \"" + url + "\" specified"
                    + " at " + configuration.getChild("url").getLocation());
        }

        /* Prepare the base request and query parameters */
        this.reqParams = this.getParams(configuration.getChildren("param"));
        this.qryParams = this.getParams(configuration.getChildren("query"));
    }

    /**
     * Setup this <code>Generator</code> with its runtime configurations and parameters
     * specified in the sitemap, and prepare it for generation.
     *
     * @param sourceResolver The <code>SourceResolver</code> instance resolving sources by
     *                       system identifiers.
     * @param objectModel The Cocoon "object model" <code>Map</code>
     * @param parameters The runtime <code>Parameters</code> instance.
     * @throws ProcessingException If this instance could not be setup.
     * @throws SAXException If a SAX error occurred during setup.
     * @throws IOException If an I/O error occurred during setup.
     * @see #recycle()
     */
    public void setup(SourceResolver sourceResolver, Map objectModel,
            String source, Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        /* Do the usual stuff */
        super.setup(sourceResolver, objectModel, source, parameters);

        /*
         * Parameter handling: In case the method is a POST method, query
         * parameters and request parameters will be two different arrays
         * (one for the body, one for the query string, otherwise it's going
         * to be the same one, as all parameters are passed on the query string
         */
        ArrayList req = new ArrayList();
        ArrayList qry = req;
        if (this.method instanceof PostMethod) qry = new ArrayList();
        req.addAll(this.reqParams);
        qry.addAll(this.qryParams);

        /*
         * Parameter handling: complete or override the configured parameters with
         * those specified in the pipeline.
         */
        String names[] = parameters.getNames();
        for (int x = 0; x < names.length; x++) {
            String name = names[x];
            String value = parameters.getParameter(name, null);
            if (value == null) continue;

            if (name.startsWith("query:")) {
                name = name.substring("query:".length());
                qry.add(new NameValuePair(name, value));
            } else if (name.startsWith("param:")) {
                name = name.substring("param:".length());
                req.add(new NameValuePair(name, value));
            } else if (name.startsWith("query-override:")) {
                name = name.substring("query-override:".length());
                qry = overrideParams(qry, name, value);
            } else if (name.startsWith("param-override:")) {
                name = name.substring("param-override:".length());
                req = overrideParams(req, name, value);
            }
        }

        /* Process the current source URL in relation to the configured one */
        HttpURL src = (super.source == null ? null : new HttpURL(super.source));
        if (this.url != null) src = (src == null ? this.url : new HttpURL(this.url, src));
        if (src == null) throw new ProcessingException("No URL specified");
        if (src.isRelativeURI()) {
            throw new ProcessingException("Invalid URL \"" + src.toString() + "\"");
        }

        /* Configure the method with the resolved URL */
        HostConfiguration hc = new HostConfiguration();
        hc.setHost(src);
        this.method.setHostConfiguration(hc);
        this.method.setPath(src.getPath());
        this.method.setQueryString(src.getQuery());

        /* And now process the query string (from the parameters above) */
        if (qry.size() > 0) {
            String qs = this.method.getQueryString();
            NameValuePair nvpa[] = new NameValuePair[qry.size()];
            this.method.setQueryString((NameValuePair []) qry.toArray(nvpa));
            if (qs != null) {
                this.method.setQueryString(qs + "&" + this.method.getQueryString());
            }
        }

        /* Finally process the body parameters */
        if ((this.method instanceof PostMethod) && (req.size() > 0)) {
            PostMethod post = (PostMethod) this.method;
            NameValuePair nvpa[] = new NameValuePair[req.size()];
            post.setRequestBody((NameValuePair []) req.toArray(nvpa));
        }

        /* Check the debugging flag */
        this.debug = parameters.getParameterAsBoolean("debug", false);
    }

    /**
     * Recycle this instance, clearing all done during setup and generation, and reverting
     * back to what was configured in the sitemap.
     *
     * @see #configure(Configuration)
     * @see #setup(SourceResolver, Map, String, Parameters)
     * @see #generate()
     */
    public void recycle() {
        /* Recycle the method */
        this.method.recycle();
        /* TODO: Is this still needed? Does it refer to a bug in bugzilla?
         *       At least the handling in httpclient has completely changed.
         *  Work around a bug from the HttpClient library */
        if (this.method instanceof PostMethod) ((PostMethod) this.method).setRequestBody("");

        /* Clean up our parent */
        super.recycle();
    }

    /**
     * Parse the remote <code>InputStream</code> accessed over HTTP.
     *
     * @throws ResourceNotFoundException If the remote HTTP resource could not be found.
     * @throws ProcessingException If an error occurred processing generation.
     * @throws SAXException If an error occurred parsing or processing XML in the pipeline.
     * @throws IOException If an I/O error occurred accessing the HTTP server.
     */
    public void generate()
    throws ResourceNotFoundException, ProcessingException, SAXException, IOException {
        /* Do the boring stuff in case we have to do a debug output (blablabla) */
        if (this.debug) {
            this.generateDebugOutput();
            return;
        }

        /* Call up the remote HTTP server */
        HttpConnection connection = new HttpConnection(this.method.getHostConfiguration());
        HttpState state = new HttpState();
        this.method.setFollowRedirects(true);
        int status = this.method.execute(state, connection);
        if (status == 404) {
            throw new ResourceNotFoundException("Unable to access \"" + this.method.getURI()
                    + "\" (HTTP 404 Error)");
        } else if ((status < 200) || (status > 299)) {
            throw new IOException("Unable to access HTTP resource at \""
                    + this.method.getURI().toString() + "\" (status=" + status + ")");
        }
        InputStream response = this.method.getResponseBodyAsStream();

        /* Let's try to set up our InputSource from the response output stream and to parse it */
        SAXParser parser = null;
        try {
            InputSource inputSource = new InputSource(response);
            parser = (SAXParser) this.manager.lookup(SAXParser.ROLE);
            parser.parse(inputSource, super.xmlConsumer);
        } catch (ServiceException ex) {
            throw new ProcessingException("Unable to get parser", ex);
        } finally {
            this.manager.release(parser);
            this.method.releaseConnection();
            connection.close();
        }
    }

    /**
     * Generate debugging output as XML data from the current configuration.
     *
     * @throws SAXException If an error occurred parsing or processing XML in the pipeline.
     * @throws IOException If an I/O error occurred accessing the HTTP server.
     */
    private void generateDebugOutput()
    throws SAXException, IOException {
        super.xmlConsumer.startDocument();

        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute("", "method", "method", "CDATA", this.method.getName());
        attributes.addAttribute("", "url", "url", "CDATA", this.method.getURI().toString());
        attributes.addAttribute("", "protocol", "protocol", "CDATA",
                (this.method.isHttp11() ? "HTTP/1.1" : "HTTP/1.0"));
        super.xmlConsumer.startElement("", "request", "request", attributes);

        if (this.method instanceof PostMethod) {
            String body = ((PostMethod) this.method).getRequestBodyAsString();

            attributes = new AttributesImpl();
            attributes.addAttribute("", "name", "name", "CDATA", "Content-Type");
            attributes.addAttribute("", "value", "value", "CDATA", "application/x-www-form-urlencoded");
            super.xmlConsumer.startElement("", "header", "header", attributes);
            super.xmlConsumer.endElement("", "header", "header");

            attributes = new AttributesImpl();
            attributes.addAttribute("", "name", "name", "CDATA", "Content-Length");
            attributes.addAttribute("", "value", "value", "CDATA", Integer.toString(body.length()));
            super.xmlConsumer.startElement("", "header", "header", attributes);
            super.xmlConsumer.endElement("", "header", "header");

            attributes = new AttributesImpl();
            super.xmlConsumer.startElement("", "body", "body", attributes);
            super.xmlConsumer.characters(body.toCharArray(), 0, body.length());
            super.xmlConsumer.endElement("", "body", "body");
        }

        super.xmlConsumer.endElement("", "request", "request");

        super.xmlConsumer.endDocument();
        return;
    }

    /**
     * Prepare a map of parameters from an array of <code>Configuration</code>
     * items.
     *
     * @param configurations An array of <code>Configuration</code> elements.
     * @return A <code>List</code> of <code>NameValuePair</code> elements.
     * @throws ConfigurationException If a parameter doesn't specify a name.
     */
    private ArrayList getParams(Configuration configurations[])
    throws ConfigurationException {
        ArrayList list = new ArrayList();

        if (configurations.length < 1) return (list);

        for (int x = 0; x < configurations.length; x++) {
            Configuration configuration = configurations[x];
            String name = configuration.getAttribute("name", null);
            if (name == null) {
                throw new ConfigurationException("No name specified for parameter at "
                        + configuration.getLocation());
            }

            String value = configuration.getAttribute("value", null);
            if (value != null) list.add(new NameValuePair(name, value));

            Configuration subconfigurations[] = configuration.getChildren("value");
            for (int y = 0; y < subconfigurations.length; y++) {
                value = subconfigurations[y].getValue(null);
                if (value != null) list.add(new NameValuePair(name, value));
            }
        }
        
        return (list);
    }

    /**
     * Override the value for a named parameter in a specfied <code>ArrayList</code>
     * or add it if the parameter was not found.
     *
     * @param list The <code>ArrayList</code> where the parameter is stored.
     * @param name The parameter name.
     * @param value The new parameter value.
     * @return The same <code>List</code> of <code>NameValuePair</code> elements.
     */
    private ArrayList overrideParams(ArrayList list, String name, String value) {
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            NameValuePair param = (NameValuePair) iterator.next();
            if (param.getName().equals(name)) {
                iterator.remove();
                break;
            }
        }
        list.add(new NameValuePair(name, value));
        return (list);
    }
}

