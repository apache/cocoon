/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.transformation;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Map;

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
import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.application.PortalApplicationConfig;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.cocoon.transformation.AbstractTransformer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This transformer is used to insert the XHTML data from an request
 * to an external application at the specified element ("envelope-tag" parameter).
 * Nesessary connection data for the external request like sessionid, cookies,
 * documentbase, the uri, etc. will be taken from the application coplet instance
 * data.
 * @author <a href="mailto:friedrich.klenner@rzb.at">Friedrich Klenner</a>  
 * @author <a href="mailto:gernot.koller@rizit.at">Gernot Koller</a>
 * 
 * @version CVS $Id: ProxyTransformer.java,v 1.8 2004/03/19 14:21:06 cziegeler Exp $
 */
public class ProxyTransformer
    extends AbstractTransformer
    implements Serviceable, Parameterizable {

    /**
     * Parameter for specifying the envelope tag
     */
    public static String ENVELOPE_TAG_PARAMETER = "envelope-tag";

    public static final String PORTALNAME = "cocoon-portal-portalname";
    public static final String COPLETID = "cocoon-portal-copletid";
    public static final String PROXY_PREFIX = "proxy-";

    public static final String COPLET_ID_PARAM = "copletId";
    public static final String PORTAL_NAME_PARAM = "portalName";

    // Coplet instance data keys
    public static final String SESSIONTOKEN = "sessiontoken";
    public static final String COOKIE = "cookie";
    public static final String START_URI = "start-uri";
    public static final String LINK = "link";
    public static final String CONFIG = "config";
    public static final String DOCUMENT_BASE = "documentbase";

    /**
     * Parameter for specifying the java protocol handler (used for https)
     */
    public static String PROTOCOL_HANDLER_PARAMETER = "protocol-handler";

    /**
     * The document base uri
     */
    protected String documentBase;

    /**
     * The current link to the external application
     */
    protected String link;

    /** 
     * This tag will include the external XHMTL 
     */
    protected String envelopeTag;

    /**
     * The Avalon component manager
     */
    protected ServiceManager manager;

    /**
     * The coplet instance data
     */
    protected CopletInstanceData copletInstanceData;

    /**
     * The original request to the portal
     */
    protected Request request;

    /**
     * The encoding (JTidy constant) if configured
     */
    protected int configuredEncoding;

    /**
     * The user agent identification string if confiugured
     */
    protected String userAgent = null;

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;

    }

    /**
     * For the proxy transformer the envelope-tag and the protocol-handler (for https) parameter can be specified.
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(Parameters)
     */
    public void parameterize(Parameters parameters) {
        if (parameters != null) {
            envelopeTag = parameters.getParameter(ENVELOPE_TAG_PARAMETER, null);
            String protocolHandler =
                parameters.getParameter(PROTOCOL_HANDLER_PARAMETER, null);
            if (protocolHandler != null) {
                if (System.getProperty("java.protocol.handler.pkgs") == null) {
                    System.setProperty(
                        "java.protocol.handler.pkgs",
                        protocolHandler);
                }
            }
        }
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(SourceResolver, Map, String, Parameters)
     */
    public void setup(
        SourceResolver resolver,
        Map objectModel,
        String src,
        Parameters parameters)
        throws ProcessingException, SAXException, IOException {

        request = ObjectModelHelper.getRequest(objectModel);

        copletInstanceData =
            getInstanceData(this.manager, objectModel, parameters);

        PortalApplicationConfig pac =
            (PortalApplicationConfig) copletInstanceData.getAttribute(CONFIG);

        String startURI = pac.getAttribute(START_URI);

        link = (String) copletInstanceData.getAttribute(LINK);

        documentBase = (String) copletInstanceData.getAttribute(DOCUMENT_BASE);

        if (link == null) {
            link = startURI;
        }

        if (documentBase == null) {
            documentBase = link.substring(0, link.lastIndexOf('/') + 1);
            copletInstanceData.setAttribute(DOCUMENT_BASE, documentBase);
        }

        String encodingString = pac.getAttribute("encoding");
        configuredEncoding = encodingConstantFromString(encodingString);
        userAgent = pac.getAttribute("user-agent");
        envelopeTag = parameters.getParameter("envelope-tag", envelopeTag);

        if (envelopeTag == null) {
            throw new ProcessingException("Can not initialize RSFHtmlTransformer - sitemap parameter envelope-tag missing");
        }

        String protocolHandler =
            parameters.getParameter(PROTOCOL_HANDLER_PARAMETER, null);
        if (protocolHandler != null) {
            System.setProperty("java.protocol.handler.pkgs", protocolHandler);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(
        String uri,
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
     * @throws SAXException on any exceptions while sending the request.
     */
    protected void processRequest() {
        try {
            String remoteURI = null;
            try {
                remoteURI = resolveURI(link, documentBase);
            }
            catch (MalformedURLException ex) {
                throw new SAXException(ex);
            }

            StringBuffer query = new StringBuffer();

            boolean firstparameter = true;
            Enumeration enum = request.getParameterNames();

            boolean post = ("POST".equals(request.getMethod()));

            while (enum.hasMoreElements()) {
                String paramName = (String) enum.nextElement();

                if (!paramName.startsWith("cocoon-portal-")) {
                    String[] paramValues =
                        request.getParameterValues(paramName);
                    for (int i = 0; i < paramValues.length; i++) {
                        if (firstparameter) {
                            if (!post) {
                                query.append('?');
                            }
                            firstparameter = false;
                        }
                        else {
                            query.append('&');
                        }

                        query.append(URLEncoder.encode(paramName));
                        query.append('=');
                        query.append(URLEncoder.encode(paramValues[i]));
                    }
                }
            }

            Document result = null;
            try {
                do {
                    HttpURLConnection connection =
                        connect(request, remoteURI, query.toString(), post);
                    remoteURI = checkForRedirect(connection, documentBase);

                    if (remoteURI == null) {
                        result = readXML(connection);
                        remoteURI = checkForRedirect(result, documentBase);
                    }

                }
                while (remoteURI != null);
            }
            catch (IOException ex) {
                throw new SAXException(
                    "Failed to retrieve remoteURI " + remoteURI,
                    ex);
            }

            XMLUtils.stripDuplicateAttributes(result, null);

            DOMStreamer streamer = new DOMStreamer();
            streamer.setContentHandler(contentHandler);
            streamer.stream(result.getDocumentElement());
        }
        catch (Exception ex) {
            System.err.println("Unexpected Exception occured: " + ex);
            ex.printStackTrace();
            throw new RuntimeException(ex.toString());
        }
    }

    /**
     * Check the http status code of the http response to detect any redirects.
     * @param connection The HttpURLConnection
     * @param documentBase The current documentBase (needed for relative redirects)
     * @return the redirected URL or null if no redirects are detected.
     * @throws IOException if exceptions occure while analysing the response
     */
    protected String checkForRedirect(
        HttpURLConnection connection,
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
                this.copletInstanceData.getPersistentAspectData().put(
                    SESSIONTOKEN,
                    sessionToken);
            }

            if (newURI != null) {
                newURI = resolveURI(newURI, documentBase);
            }
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
                        this.copletInstanceData.getPersistentAspectData().put(
                            SESSIONTOKEN,
                            sessionToken);
                    }

                    if (newURI != null) {
                        newURI = resolveURI(newURI, documentBase);
                    }
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
     * @throws IOException if any exceptions occure while reading from the url connection.
     */
    protected Document readXML(HttpURLConnection connection) {
        try {
            int charEncoding = configuredEncoding;

            String contentType = connection.getHeaderField("Content-Type");
            int begin = contentType.indexOf("charset=");
            int end = -1;
            if (begin > -1) {
                begin += "charset=".length();
                end = contentType.indexOf(';', begin);
                if (end == -1) {
                    end = contentType.length();
                }
                String charset = contentType.substring(begin, end);
                charEncoding = encodingConstantFromString(charset);
            }

            InputStream stream = connection.getInputStream();
            // Setup an instance of Tidy.
            Tidy tidy = new Tidy();
            tidy.setXmlOut(true);

            tidy.setCharEncoding(charEncoding);
            tidy.setXHTML(true);

            //Set Jtidy warnings on-off
            tidy.setShowWarnings(this.getLogger().isWarnEnabled());
            //Set Jtidy final result summary on-off
            tidy.setQuiet(!this.getLogger().isInfoEnabled());
            //Set Jtidy infos to a String (will be logged) instead of System.out
            StringWriter stringWriter = new StringWriter();
            //FIXME ??
            PrintWriter errorWriter = new PrintWriter(stringWriter);
            tidy.setErrout(errorWriter);
            // Extract the document using JTidy and stream it.
            Document doc = tidy.parseDOM(new BufferedInputStream(stream), null);
            errorWriter.flush();
            errorWriter.close();
            return doc;
        }
        catch (Exception ex) {
            System.err.println("unexpected exeption: " + ex);
            ex.printStackTrace();
            throw new RuntimeException(ex.toString());
        }
    }

    /**
     * Helper method to convert the HTTP encoding String to JTidy encoding constants.
     * @param encoding the HTTP encoding String
     * @return the corresponding JTidy constant.
     */
    private int encodingConstantFromString(String encoding) {
        if ("ISO8859_1".equalsIgnoreCase(encoding)) {
            return Configuration.LATIN1;
        }
        else if ("UTF-8".equalsIgnoreCase(encoding)) {
            return Configuration.UTF8;
        }
        else {
            return Configuration.LATIN1;
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

        String cookie = (String) copletInstanceData.getAttribute(COOKIE);

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

        copletInstanceData.setAttribute(
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

        if (uri.indexOf("://") > -1) {
            return uri;
        }

        if (uri == null) {
            throw new IllegalArgumentException("URI to be resolved must not be null!");
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
        else {
            return documentBaseURL.toExternalForm() + uri;
        }
    }

    public static CopletInstanceData getInstanceData(ServiceManager manager,
                                                     String copletID,
                                                     String portalName)
    throws ProcessingException {
        // set portal name
        PortalService portalService = null;
        try {
            portalService = (PortalService) manager.lookup(PortalService.ROLE);
            portalService.setPortalName(portalName);
                
            ProfileManager profileManager = portalService.getComponentManager().getProfileManager();
            CopletInstanceData data = profileManager.getCopletInstanceData(copletID);
            return data;
        } catch (ServiceException e) {
            throw new ProcessingException("Error getting portal service.", e);
        } finally {
            manager.release(portalService);
        }
    }

    /**
    * Method getInstanceData.
    * @param manager
    * @param objectModel
    * @param parameters
    * @return CopletInstanceData
    * @throws ProcessingException
    * @throws IOException
    * @throws SAXException
    */
    public static CopletInstanceData getInstanceData(ServiceManager manager,
                                                     Map objectModel,
                                                     Parameters parameters)
    throws ProcessingException {

        PortalService portalService = null;
        try {
            portalService = (PortalService) manager.lookup(PortalService.ROLE);

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

                    portalService.setPortalName(parameters.getParameter(PORTAL_NAME_PARAM));
                } catch (ParameterException e) {
                    throw new ProcessingException("copletId and portalName must be passed as parameter or in the object model within the parent context.");
                }
            }
            return portalService.getComponentManager().getProfileManager().getCopletInstanceData(copletId);
        } catch (ServiceException e) {
            throw new ProcessingException("Error getting portal service.", e);
        } finally {
            manager.release(portalService);
        }
    }

}