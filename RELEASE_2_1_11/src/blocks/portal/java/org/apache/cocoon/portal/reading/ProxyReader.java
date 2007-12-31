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
package org.apache.cocoon.portal.reading;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.transformation.ProxyTransformer;
import org.apache.cocoon.reading.ServiceableReader;
import org.apache.cocoon.util.NetUtils;
import org.xml.sax.SAXException;

/**
 * This reader is used to retrieve non XML content from external applications
 * routet via the portal site. Requests from external resources are marked with
 * a proxy prefix and have coplet id and portal name attached as request
 * parameters. The portal name and coplet id are used to look up necessary
 * connection data for the external request (like session id, cookies, 
 * document base, etc.) from the application coplet instance data.
 * 
 * @author <a href="mailto:gernot.koller@rizit.at">Gernot Koller</a>
 * @author <a href="mailto:friedrich.klenner@rzb.at">Friedrich Klenner</a> 
 * 
 * @version CVS $Id$
 */
public class ProxyReader extends ServiceableReader {

    /**
     * The coplet instance data
     */
    protected CopletInstanceData copletInstanceData;

    /**
     * The HTTP response
     */
    protected Response response;

    /**
     * The origninal HTTP request
     */
    protected Request request;

    /** The prefix */
    protected String prefix;
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(SourceResolver, Map, String, Parameters)
     */
    public void setup(SourceResolver resolver,
                      Map objectModel,
                      String src,
                      Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        request = ObjectModelHelper.getRequest(objectModel);
        response = ObjectModelHelper.getResponse(objectModel);

        String copletID = request.getParameter(ProxyTransformer.COPLETID);
        String portalName = request.getParameter(ProxyTransformer.PORTALNAME);

        copletInstanceData =
            ProxyTransformer.getInstanceData(
                this.manager,
                copletID,
                portalName);
        this.prefix = par.getParameter("prefix", ProxyTransformer.PROXY_PREFIX);
    }

    /**
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        this.response = null;
        this.request = null;
        this.copletInstanceData = null;
        super.recycle();
    }

    /**
     * Send the request to the external WebServer
     * @throws IOException on any exception that occures
     * @see org.apache.cocoon.reading.Reader#generate()
     */
    public void generate() throws IOException {
        String link = request.getRequestURI();
        link = link.substring(link.indexOf(this.prefix) + this.prefix.length());

        String documentBase =
            (String) copletInstanceData.getAttribute(
                ProxyTransformer.DOCUMENT_BASE);
        String remoteURI = null;

        remoteURI = ProxyTransformer.resolveURI(link, documentBase);

        HttpURLConnection connection = connect(request, remoteURI);
        copyHeaderFields(connection, response);
        sendData(connection.getInputStream());
    }

    /**
     * Copy the data to the original response stream
     * @param in the response from external request to read from
     * @throws IOException on any exception
     */
    protected void sendData(InputStream in) throws IOException {
        int length = -1;
        byte[] buf = new byte[4096];

        while ((length = in.read(buf)) > -1) {
            out.write(buf, 0, length);
        }
        out.flush();
        in.close();
    }

    /**
     * Establish the HttpURLConnection to the given uri.
     * @param request the original request
     * @param uri the remote uri
     * @return the established HttpURLConnection
     * @throws IOException on any exception
     */
    protected HttpURLConnection connect(Request request, String uri)
        throws IOException {
        String cookie =
            (String) copletInstanceData.getAttribute(ProxyTransformer.COOKIE);

        Enumeration enumeration = request.getParameterNames();

        boolean firstattribute = true;
        StringBuffer query = new StringBuffer();

        while (enumeration.hasMoreElements()) {
            String paramName = (String) enumeration.nextElement();

            if (!paramName.startsWith("cocoon-portal-")) {

                String[] paramValues = request.getParameterValues(paramName);

                for (int i = 0; i < paramValues.length; i++) {
                    if (firstattribute) {
                        query.append('?');
                        firstattribute = false;
                    }
                    else {
                        query.append('&');
                    }

                    query.append(NetUtils.encode(paramName, "utf-8"));
                    query.append('=');
                    query.append(NetUtils.encode(paramValues[i], "utf-8"));

                }
            }
        }

        uri = uri + query.toString();

        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(true);

        if (cookie != null) {
            connection.setRequestProperty(ProxyTransformer.COOKIE, cookie);
        }

        connection.connect();

        copletInstanceData.setAttribute(
            ProxyTransformer.COOKIE,
            connection.getHeaderField(ProxyTransformer.COOKIE));

        return connection;
    }

    /**
     * Copy header fields from external response to original response.
     * @param connection the connection to the external resource
     * @param response the original HTTP response.
     */
    private void copyHeaderFields(
        HttpURLConnection connection,
        Response response) {
        String[] fieldNames =
            new String[] {
                "Content-Range",
                "Accept-Ranges",
                "Content-Length",
                "Last-Modified",
                "Content-Type",
                "Expires" };
        for (int i = 0; i < fieldNames.length; i++) {
            String value = connection.getHeaderField(fieldNames[i]);
            if (value != null) {
                response.setHeader(fieldNames[i], value);
            }

        }
    }
}
