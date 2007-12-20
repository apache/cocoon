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
package org.apache.cocoon.portal.transformation;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.om.CopletDefinition;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.services.LinkService;
import org.apache.cocoon.portal.sitemap.Constants;
import org.apache.cocoon.portal.sitemap.InputModuleHelper;
import org.apache.cocoon.portal.util.HtmlDomParser;
import org.apache.cocoon.transformation.AbstractTransformer;
import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This transformer is used to insert the XHTML data from an request
 * to an external application at the specified element ("envelope-tag" parameter).
 * Nesessary connection data for the external request like sessionid, cookies,
 * documentbase, the uri, etc. will be taken from the application coplet instance
 * data.
 *
 * @version $Id$
 */
public class ProxyTransformer
    extends AbstractTransformer
    implements Serviceable, Parameterizable, Disposable {

    /**
     * Parameter for specifying the envelope tag
     */
    public static final String ENVELOPE_TAG_PARAMETER = "envelope-tag";

    public static final String COPLETID = "cocoon-portal-copletid";
    public static final String PROXY_PREFIX = "proxy-";

    public static final String COPLET_ID_PARAM = "copletId";

    // Coplet instance data keys
    public static final String SESSIONTOKEN = "sessiontoken";
    public static final String COOKIE = "cookie";
    public static final String START_URI = "start-uri";
    public static final String LINK = "link";
    public static final String DOCUMENT_BASE = "documentbase";

    /** The document base uri. */
    protected String documentBase;

    /** The current link to the external application. */
    protected String link;

    /** The default value for the envelope Tag. */
    protected String defaultEnvelopeTag;

    /** This tag will include the external XHMTL. */
    protected String envelopeTag;

    /** The Avalon service manager. */
    protected ServiceManager manager;

    /** The coplet instance data. */
    protected CopletInstance copletInstanceData;

    /** The original request to the portal. */
    protected Request request;

    /** The encoding if configured. */
    protected String configuredEncoding;

    /** The user agent identification string if confiugured. */
    protected String userAgent;

    /** The sitemap parameters. */
    protected Parameters parameters;

    /** The portal service. */
    protected PortalService portalService;

    /** Helper for resolving input modules. */
    protected InputModuleHelper imHelper;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager aManager) throws ServiceException {
        this.manager = aManager;
        this.portalService = (PortalService)this.manager.lookup(PortalService.class.getName());
        this.imHelper = new InputModuleHelper(manager);
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.portalService);
            this.portalService = null;
            this.manager = null;
        }
        if ( this.imHelper != null ) {
            this.imHelper.dispose();
            this.imHelper = null;
        }
    }

    /**
     * For the proxy transformer the envelope-tag parameter can be specified.
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(Parameters)
     */
    public void parameterize(Parameters parameters) {
        this.defaultEnvelopeTag = parameters.getParameter(ENVELOPE_TAG_PARAMETER, null);
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(SourceResolver, Map, String, Parameters)
     */
    public void setup(SourceResolver resolver,
                      Map objectModel,
                      String src,
                      Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        this.parameters = parameters;
        this.request = ObjectModelHelper.getRequest(objectModel);

        this.copletInstanceData = getInstanceData(this.manager, objectModel, parameters);

        final CopletDefinition copletData = this.copletInstanceData.getCopletDefinition();

        this.link = (String) this.copletInstanceData.getTemporaryAttribute(LINK);

        this.documentBase = (String) this.copletInstanceData.getAttribute(DOCUMENT_BASE);

        if (this.link == null) {
            final String startURI = (String)copletData.getAttribute(START_URI);
            this.link = this.imHelper.resolve(startURI);
        }

        if (documentBase == null) {
            this.documentBase = this.link.substring(0, this.link.lastIndexOf('/') + 1);
            copletInstanceData.setAttribute(DOCUMENT_BASE, this.documentBase);
        }

        this.configuredEncoding = (String)copletData.getAttribute("encoding");
        this.userAgent = (String)copletData.getAttribute("user-agent");
        this.envelopeTag = parameters.getParameter(ENVELOPE_TAG_PARAMETER, this.defaultEnvelopeTag);

        if (envelopeTag == null) {
            throw new ProcessingException("Can not initialize ProxyTransformer - sitemap parameter 'envelope-tag' missing");
        }
    }

    /**
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        this.envelopeTag = null;
        this.userAgent = null;
        this.documentBase = null;
        this.link = null;
        this.request = null;
        this.parameters = null;
        this.copletInstanceData = null;
        super.recycle();
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String uri,
                             String name,
                             String raw,
                             Attributes attributes)
    throws SAXException {
        super.startElement(uri, name, raw, attributes);

        if (name.equalsIgnoreCase(this.envelopeTag)) {
            //super.startElement(uri, name, raw, attributes);
            processRequest();
            //super.endElement(uri, name, raw);
        }
    }

    /**
     * Processes the request to the external application
     */
    protected void processRequest() throws SAXException {
        try {
            String remoteURI = null;
            try {
                remoteURI = resolveURI(link, documentBase);
            } catch (MalformedURLException ex) {
                throw new SAXException(ex);
            }
            boolean firstparameter = true;
            boolean post = ("POST".equals(request.getMethod()));
            int pos = remoteURI.indexOf('?');
            final StringBuffer query = new StringBuffer();
            if ( pos != -1 ) {
                if ( !post ) {
                    query.append('?');
                }
                query.append(remoteURI.substring(pos+1));
                firstparameter = true;
                remoteURI = remoteURI.substring(0, pos);
            }

            // append all parameters of the current request, except internal ones
            final Enumeration enumeration = request.getParameterNames();
            final LinkService linkService = this.portalService.getLinkService();
            while (enumeration.hasMoreElements()) {
                String paramName = (String) enumeration.nextElement();

                if (!linkService.isInternalParameterName(paramName)) {
                    String[] paramValues = request.getParameterValues(paramName);
                    for (int i = 0; i < paramValues.length; i++) {
                        firstparameter = this.appendParameter(query, firstparameter, post, paramName, paramValues[i]);
                    }
                }
            }

            // now append parameters from the sitemap - if any
            final String[] names = this.parameters.getNames();
            for(int i=0; i<names.length; i++) {
                if ( names[i].startsWith("add:") ) {
                    final String value = this.parameters.getParameter(names[i]);
                    if ( value != null && value.trim().length() > 0 ) {
                        final String pName = names[i].substring(4);
                        firstparameter = this.appendParameter(query, firstparameter, post, pName, value.trim());
                    }
                }

            }

            Document result = null;
            try {
                do {
                    if ( this.getLogger().isDebugEnabled() ) {
                        this.getLogger().debug("Invoking '" + remoteURI + query.toString() +"', post="+post);
                    }
                    HttpURLConnection connection =
                        connect(request, remoteURI, query.toString(), post);
                    remoteURI = checkForRedirect(connection, documentBase);

                    if (remoteURI == null) {
                        result = readXML(connection);
                        remoteURI = checkForRedirect(result, documentBase);
                    }

                } while (remoteURI != null);
            } catch (IOException ex) {
                throw new SAXException(
                    "Failed to retrieve remoteURI " + remoteURI,
                    ex);
            }

            XMLUtils.stripDuplicateAttributes(result, null);

            DOMStreamer streamer = new DOMStreamer();
            streamer.setContentHandler(contentHandler);
            streamer.stream(result.getDocumentElement());
        } catch (SAXException se) {
            throw se;
        } catch (Exception ex) {
            throw new SAXException(ex);
        }
    }

    protected boolean appendParameter(StringBuffer buffer,
                                      boolean firstparameter,
                                      boolean post,
                                      String name,
                                      String value)
    throws UnsupportedEncodingException {
        if (firstparameter) {
            if (!post) {
                buffer.append('?');
            }
            firstparameter = false;
        } else {
            buffer.append('&');
        }

        buffer.append(NetUtils.encode(name, "utf-8"));
        buffer.append('=');
        buffer.append(NetUtils.encode(value, "utf-8"));

        return firstparameter;
    }

    /**
     * Check the http status code of the http response to detect any redirects.
     * @param connection The HttpURLConnection
     * @param documentBase The current documentBase (needed for relative redirects)
     * @return The redirected URL or null if no redirects are detected.
     * @throws IOException if exceptions occure while analysing the response
     */
    protected String checkForRedirect(HttpURLConnection connection,
                                      String documentBase)
    throws IOException {

        if (connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM
            || connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {

            String newURI = (connection.getHeaderField("location"));

            int index_semikolon = newURI.indexOf(";");
            int index_question = newURI.indexOf("?");

            if ((index_semikolon > -1)) {
                String sessionToken =
                    newURI.substring(
                        index_semikolon + 1,
                        (index_question == -1
                            ? newURI.length()
                            : index_question));
                this.copletInstanceData.setTemporaryAttribute(
                    SESSIONTOKEN,
                    sessionToken);
            }
            newURI = resolveURI(newURI, documentBase);
            return newURI;
        }
        return null;
    }

    /**
     * Analyses the XHTML response document for redirects in &lt;meta http-equiv="refresh"&gt; elements.
     * @param doc The W3C DOM document containing the XHTML response
     * @param documentBase The current document base (needed for relative redirects)
     * @return String the redirected URL or null if no redirects are detected.
     * @throws MalformedURLException if the redirect uri is malformed.
     */
    protected String checkForRedirect(Document doc, String documentBase)
        throws MalformedURLException {
        Element htmlElement = doc.getDocumentElement();
        NodeList headList = htmlElement.getElementsByTagName("head");
        if (headList.getLength() <= 0) {
            return null;
        }

        Element headElement = (Element) headList.item(0);
        NodeList metaList = headElement.getElementsByTagName("meta");
        for (int i = 0; i < metaList.getLength(); i++) {
            Element metaElement = (Element) metaList.item(i);
            String httpEquiv = metaElement.getAttribute("http-equiv");
            if ("refresh".equalsIgnoreCase(httpEquiv)) {
                String content = metaElement.getAttribute("content");
                if (content != null) {
                    String time =
                        content.substring(0, content.indexOf(';'));
                    try {
                        if (Integer.parseInt(time) > 10) {
                            getLogger().warn(
                                "Redirects with refresh time longer than 10 seconds ("
                                    + time
                                    + " seconds) will be ignored!");
                            return null;
                        }
                    }
                    catch (NumberFormatException ex) {
                        getLogger().warn(
                            "Failed to convert refresh time from redirect to integer: "
                                + time);
                        return null;
                    }

                    String newURI =
                        content.substring(content.indexOf('=') + 1);

                    int index_semikolon = newURI.indexOf(";");
                    int index_question = newURI.indexOf("?");

                    if ((index_semikolon > -1)) {
                        String sessionToken =
                            newURI.substring(
                                index_semikolon + 1,
                                (index_question == -1
                                    ? newURI.length()
                                    : index_question));
                        this.copletInstanceData.setTemporaryAttribute(
                            SESSIONTOKEN,
                            sessionToken);
                    }
                    newURI = resolveURI(newURI, documentBase);
                    return newURI;
                }
            }
        }
        return null;
    }

    /**
     * Reads the HTML document from given connection and returns a correct W3C DOM XHTML document
     * @param connection hte HttpURLConnection to read from
     * @return the result as valid W3C DOM XHTML document
     */
    protected Document readXML(HttpURLConnection connection)
    throws SAXException {
        try {
            String encoding = configuredEncoding;

            String contentType = connection.getHeaderField("Content-Type");
            int begin = contentType.indexOf("charset=");
            int end = -1;
            if (begin > -1) {
                begin += "charset=".length();
                end = contentType.indexOf(';', begin);
                if (end == -1) {
                    end = contentType.length();
                }
                encoding = contentType.substring(begin, end);
            }

            return HtmlDomParser.parse(connection.getURL().toExternalForm(), connection.getInputStream(), encoding);

        } catch (Exception ex) {
            throw new SAXException(ex);
        }
    }

    /**
     * Establish the HttpURLConnection to the given uri.
     * User-Agent, Accept-Language and Encoding headers will be copied from the original
     * request, if no other headers are specified.
     * @param request the original request
     * @param uri the remote uri
     * @param query the remote query string
     * @param post true if request method was POST
     * @return the established HttpURLConnection
     * @throws IOException on any exception
     */
    protected HttpURLConnection connect(
        Request request,
        String uri,
        String query,
        boolean post)
        throws IOException {

        String cookie = (String) copletInstanceData.getTemporaryAttribute(COOKIE);

        if (!post) {
            uri = uri + query;
        }

        URL url = new URL(uri);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setInstanceFollowRedirects(false);

        connection.setRequestMethod(request.getMethod());
        connection.setRequestProperty(
            "User-Agent",
            (userAgent != null) ? userAgent : request.getHeader("User-Agent"));

        connection.setRequestProperty(
            "Accept-Language",
            request.getHeader("Accept-Language"));

        if (cookie != null) {
            connection.setRequestProperty(COOKIE, cookie);
        }

        if (post) {
            connection.setDoOutput(true);
            connection.setRequestProperty(
                "Content-Type",
                "application/x-www-form-urlencoded");
            connection.setRequestProperty(
                "Content-Length",
                String.valueOf(query.length()));
        }

        connection.connect();

        if (post) {
            PrintWriter out = new PrintWriter(connection.getOutputStream());
            out.print(query);
            out.close();
        }

        copletInstanceData.setTemporaryAttribute(
            COOKIE,
            connection.getHeaderField(COOKIE));
        documentBase = uri.substring(0, uri.lastIndexOf('/') + 1);
        copletInstanceData.setAttribute(DOCUMENT_BASE, documentBase);
        return connection;
    }

    /**
    * Resolve the possibly relative uri to an absolue uri based on given document base.
    * @param uri the uri to resolve
    * @param documentBase the current document base
    * @return returns an absolute URI based on document base (e.g. http://mydomain.com/some/file.html)
    * @throws MalformedURLException if uri or document base is malformed.
    */
    public static String resolveURI(String uri, String documentBase)
    throws MalformedURLException {

        if (uri == null) {
            throw new IllegalArgumentException("URI to be resolved must not be null!");
        }

        if (uri.indexOf("://") > -1) {
            return uri;
        }

        if (documentBase == null) {
            throw new IllegalArgumentException("Documentbase String must not be null!");
        }

        //cut ./ from uri
        if (uri.startsWith("./")) {
            uri = uri.substring(2);
        }

        URL documentBaseURL = new URL(documentBase);

        //absolute uri
        if (uri.startsWith("/")) {
            return documentBaseURL.getProtocol()
                + "://"
                + documentBaseURL.getAuthority()
                + uri;
        }
        return documentBaseURL.toExternalForm() + uri;
    }

    /**
    * Method getInstanceData.
    * @param manager
    * @param objectModel
    * @param parameters
    * @return CopletInstanceData
    * @throws ProcessingException
    */
    public static CopletInstance getInstanceData(ServiceManager manager,
                                                     Map objectModel,
                                                     Parameters parameters)
    throws ProcessingException {

        PortalService portalService = null;
        try {
            portalService = (PortalService) manager.lookup(PortalService.class.getName());

            // determine coplet id
            String copletId = null;
            Map context = (Map) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
            if (context != null) {
                copletId = (String) context.get(Constants.COPLET_ID_KEY);
                if (copletId == null) {
                    throw new ProcessingException("copletId must be passed as parameter or in the object model within the parent context.");
                }
            } else {
                try {
                    copletId = parameters.getParameter(COPLET_ID_PARAM);

                } catch (ParameterException e) {
                    throw new ProcessingException("copletId and portalName must be passed as parameter or in the object model within the parent context.");
                }
            }
            return portalService.getProfileManager().getCopletInstance(copletId);
        } catch (ServiceException e) {
            throw new ProcessingException("Error getting portal service.", e);
        } finally {
            manager.release(portalService);
        }
    }

}
