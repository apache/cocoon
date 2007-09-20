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
package org.apache.cocoon.components.language.markup.xsp;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.xscript.XScriptManager;
import org.apache.cocoon.components.xscript.XScriptObject;
import org.apache.cocoon.components.xscript.XScriptObjectInlineXML;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.excalibur.source.SourceUtil;
import org.xml.sax.InputSource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Helper for the SOAP logicsheet.
 *
 * @version $Id$
 * @since July 16, 2001
 */
public class SOAPHelper {
    XScriptManager xscriptManager;
    URL url;
    String action = "";
    XScriptObject xscriptObject;
    String authorization = "";

    public SOAPHelper(ServiceManager manager, String urlContext, String url,
                      String action, String authorization, XScriptObject xscriptObject)
            throws MalformedURLException, ServiceException
    {
        this.xscriptManager = (XScriptManager) manager.lookup(XScriptManager.ROLE);
        URL context = new URL(urlContext);
        this.url = new URL(context, url);
        this.action = action;
        this.authorization = authorization;
        this.xscriptObject = xscriptObject;
    }

    public XScriptObject invoke() throws ProcessingException
    {
        HttpConnection conn = null;

        try {
            if (this.action == null || this.action.equals("")) {
                this.action = "\"\"";
            }

            String host = this.url.getHost();
            int port = this.url.getPort();

            if (System.getProperty("http.proxyHost") != null) {
                String proxyHost = System.getProperty("http.proxyHost");
                int proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
                conn = new HttpConnection(proxyHost, proxyPort, host, port);
            } else {
                conn = new HttpConnection(host, port);
            }

            PostMethod method = new PostMethod(this.url.getFile());
            String request;

            try {
                // Write the SOAP request body
                if (this.xscriptObject instanceof XScriptObjectInlineXML) {
                    // Skip overhead
                    request = ((XScriptObjectInlineXML)this.xscriptObject).getContent();
                } else {
                    StringBuffer bodyBuffer = new StringBuffer();
                    InputSource saxSource = this.xscriptObject.getInputSource();

                    Reader r = null;
                    // Byte stream or character stream?
                    if (saxSource.getByteStream() != null) {
                        r = new InputStreamReader(saxSource.getByteStream());
                    } else {
                        r = saxSource.getCharacterStream();
                    }

                    try {
                        char[] buffer = new char[1024];
                        int len;
                        while ((len = r.read(buffer)) > 0) {
                            bodyBuffer.append(buffer, 0, len);
                        }
                    } finally {
                        if (r != null) {
                            r.close();
                        }
                    }

                    request = bodyBuffer.toString();
                }

            } catch (Exception ex) {
                throw new ProcessingException("Error assembling request", ex);
            }

            method.setRequestHeader(
                    new Header("Content-type", "text/xml; charset=\"utf-8\""));
            method.setRequestHeader(new Header("SOAPAction", this.action));
            method.setRequestBody(request);

            if (this.authorization != null && !this.authorization.equals("")) {
               method.setRequestHeader(
                       new Header("Authorization",
                                  "Basic " + SourceUtil.encodeBASE64(this.authorization)));
            }

            method.execute(new HttpState(), conn);

            String ret = method.getResponseBodyAsString();
            return new XScriptObjectInlineXML(this.xscriptManager, ret);
        } catch (ProcessingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ProcessingException("Error invoking remote service: " + ex, ex);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ex) {
            }
        }
    }
}
