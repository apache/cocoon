/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
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
 * @version CVS $Id: ProxyTransformer.java,v 1.3 2003/09/24 21:22:33 cziegeler Exp $
 */
public class ProxyTransformer
    extends AbstractTransformer
    implements Composable, Parameterizable {

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
    protected ComponentManager componentManager;

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

    /**
     * @see org.apache.avalon.framework.component.Composable#compose(ComponentManager)
     */
    public void compose(ComponentManager componentManager)
        throws ComponentException {
        this.componentManager = componentManager;
    }

    /**
     * Recycle this component.
     * All instance variables are set to <code>null</code>.
     */
    public void recycle() {
        super.recycle();
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
            getInstanceData(this.componentManager, objectModel, parameters);

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
    protected void processRequest() throws SAXException {
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
    protected Document readXML(HttpURLConnection connection)
        throws IOException {
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
    * @param the uri to resolve
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

    public static CopletInstanceData getInstanceData(
        ComponentManager manager,
        Map objectModel,
        String copletID,
        String portalName)
        throws ProcessingException, IOException, SAXException {
        ProfileManager profileManager = null;
        try {
            profileManager =
                (ProfileManager) manager.lookup(ProfileManager.ROLE);

            // set portal name
            PortalService portalService = null;
            try {
                portalService =
                    (PortalService) manager.lookup(PortalService.ROLE);
                portalService.setPortalName(portalName);
            }
            finally {
                manager.release(portalService);
            }

            CopletInstanceData data =
                profileManager.getCopletInstanceData(copletID);
            return data;
        }
        catch (ComponentException e) {
            throw new ProcessingException("Error getting profile manager.", e);
        }
        finally {
            manager.release(profileManager);
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
    public static CopletInstanceData getInstanceData(
        ComponentManager manager,
        Map objectModel,
        Parameters parameters)
        throws ProcessingException, IOException, SAXException {

        ProfileManager profileManager = null;
        try {
            profileManager =
                (ProfileManager) manager.lookup(ProfileManager.ROLE);
            // determine coplet id
            String copletId = null;
            Map context =
                (Map) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
            if (context != null) {
                copletId = (String) context.get(Constants.COPLET_ID_KEY);
                if (copletId == null) {
                    throw new ProcessingException("copletId must be passed as parameter or in the object model within the parent context.");
                }
            }
            else {
                try {
                    copletId = parameters.getParameter(COPLET_ID_PARAM);

                    // set portal name
                    PortalService portalService = null;
                    try {
                        portalService =
                            (PortalService) manager.lookup(PortalService.ROLE);
                        portalService.setPortalName(
                            parameters.getParameter(PORTAL_NAME_PARAM));
                    }
                    finally {
                        manager.release(portalService);
                    }
                }
                catch (ParameterException e) {
                    throw new ProcessingException("copletId and portalName must be passed as parameter or in the object model within the parent context.");
                }
            }
            return profileManager.getCopletInstanceData(copletId);
        }
        catch (ComponentException e) {
            throw new ProcessingException("Error getting profile manager.", e);
        }
        finally {
            manager.release(profileManager);
        }
    }

}