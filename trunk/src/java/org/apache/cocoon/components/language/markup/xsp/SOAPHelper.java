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
import org.xml.sax.InputSource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Helper for the SOAP logicsheet.
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: SOAPHelper.java,v 1.4 2004/03/08 13:58:31 cziegeler Exp $
 * @since July 16, 2001
 */
public class SOAPHelper {
    XScriptManager xscriptManager;
    URL url;
    String action = "";
    XScriptObject xscriptObject;

    public SOAPHelper(ServiceManager manager, String urlContext, String url,
                      String action, XScriptObject xscriptObject)
            throws MalformedURLException, ServiceException
    {
        this.xscriptManager = (XScriptManager) manager.lookup(XScriptManager.ROLE);
        URL context = new URL(urlContext);
        this.url = new URL(context, url);
        this.action = action;
        this.xscriptObject = xscriptObject;
    }

    public XScriptObject invoke() throws ProcessingException
    {
        HttpConnection conn = null;

        try {
            if (action == null || action.equals("")) {
                action = "\"\"";
            }

            String host = url.getHost();
            int port = url.getPort();

            if (System.getProperty("http.proxyHost") != null) {
                String proxyHost = System.getProperty("http.proxyHost");
                int proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
                conn = new HttpConnection(proxyHost, proxyPort, host, port);
            } else {
                conn = new HttpConnection(host, port);
            }

            PostMethod method = new PostMethod(url.getFile());
            String request;

            try {
                // Write the SOAP request body
                if (xscriptObject instanceof XScriptObjectInlineXML) {
                    // Skip overhead
                    request = ((XScriptObjectInlineXML) xscriptObject).getContent();
                } else {
                    StringBuffer bodyBuffer = new StringBuffer();
                    InputSource saxSource = xscriptObject.getInputSource();

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
                        while ((len = r.read(buffer)) > 0)
                            bodyBuffer.append(buffer, 0, len);
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
            method.setRequestHeader(new Header("SOAPAction", action));
            method.setRequestBody(request);

            method.execute(new HttpState(), conn);

            String ret = method.getResponseBodyAsString();
            int startOfXML = ret.indexOf("<?xml");
            if (startOfXML == -1) { // No xml?!
                throw new ProcessingException("Invalid response - no xml");
            }

            return new XScriptObjectInlineXML(
                    xscriptManager,
                    ret.substring(startOfXML));
        } catch (Exception ex) {
            throw new ProcessingException("Error invoking remote service: " + ex,
                    ex);
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception ex) {
            }
        }
    }
}
