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
 * @version CVS $Id: SOAPHelper.java,v 1.3 2004/02/06 23:34:32 joerg Exp $
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
