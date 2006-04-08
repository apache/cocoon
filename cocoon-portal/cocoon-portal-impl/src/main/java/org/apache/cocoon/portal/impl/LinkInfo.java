/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.wrapper.RequestParameters;
import org.apache.cocoon.util.NetUtils;
import org.apache.commons.lang.BooleanUtils;

/**
 * Helper class containing the information about common parts for each link
 * that will be generated in the portal page.
 *
 * @version $Id$
 */
public class LinkInfo {

    /** Link base contains the base url for the http protocol. */
    protected final String       httpLinkBase;
    protected final String       secureLinkBase;
    protected boolean            hasParameters = false;
    protected final ArrayList    comparableEvents = new ArrayList(5);
    protected final StringBuffer url = new StringBuffer();

    /** Is the page called using https? */
    protected final boolean isSecure;

    public LinkInfo(Request request, int defaultPort, int defaultSecurePort) {
        this.isSecure = request.getScheme().equals("https");
        // create relative url
        String relativeURI = request.getSitemapURI();
        final int pos = relativeURI.lastIndexOf('/');
        if ( pos != -1 ) {
            relativeURI = relativeURI.substring(pos+1);
        }

        // do we need a protocol shift for link base?
        if ( this.isSecure ) {
            this.secureLinkBase = relativeURI;
            this.httpLinkBase = this.getAbsoluteUrl(request, false, defaultPort);
        } else {
            httpLinkBase = relativeURI;
            this.secureLinkBase = this.getAbsoluteUrl(request, true, defaultSecurePort);
        }
    }

    protected String getAbsoluteUrl(Request request, boolean useSecure, int port) {
        final StringBuffer buffer = new StringBuffer();
        if ( useSecure ) {
            buffer.append("https://");
        } else {
            buffer.append("http://");
        }
        buffer.append(request.getServerName());
        if (  (  useSecure && port != 443)
           || ( !useSecure && port != 80 ) ) {
            buffer.append(':');
            buffer.append(port);
        }
        if ( request.getContextPath().length() > 0 ) {
            buffer.append(request.getContextPath());
        }
        buffer.append('/');                        
        if ( request.getSitemapURIPrefix().length() > 0 ) {
            buffer.append(request.getSitemapURIPrefix());
        }
        buffer.append(request.getSitemapURI());
        return buffer.toString();
    }

    public String getBase(Boolean secure) {
        // if no information is provided, we stay with the same protocol
        if ( secure == null ) {
            secure = BooleanUtils.toBooleanObject(this.isSecure);
        }
        if ( secure.booleanValue() ) {
            return this.secureLinkBase + this.url.toString();
        }
        return this.httpLinkBase + this.url.toString();
    }

    public LinkInfo appendToBase(String value) {
        this.url.append(value);
        return this;
    }

    public LinkInfo appendToBase(char c) {
        this.url.append(c);
        return this;
    }

    public void deleteParameterFromBase(String parameterName) {
        if ( this.hasParameters ) {
            final int pos = this.url.toString().indexOf("?");
            final String queryString = this.url.substring(pos + 1);
            final RequestParameters params = new RequestParameters(queryString);
            if ( params.getParameter(parameterName) != null ) {
                // the parameter is available, so remove it
                this.url.delete(pos, this.url.length() + 1);
                this.hasParameters = false;

                Enumeration enumeration = params.getParameterNames();
                while ( enumeration.hasMoreElements() ) {
                    final String paramName = (String)enumeration.nextElement();
                    if ( !paramName.equals(parameterName) ) {
                        String[] values = params.getParameterValues(paramName);
                        for( int i = 0; i < values.length; i++ ) {
                            this.addParameterToBase(paramName, values[i]);
                        }
                    }
                }
            }
        }            
    }

    public void addParameterToBase(String name, String value) {
        if ( this.hasParameters ) {
            this.appendToBase('&');
        } else {
            this.appendToBase('?');
        }
        try {
            this.appendToBase(name).appendToBase('=').appendToBase(NetUtils.encode(value, "utf-8"));
        } catch (UnsupportedEncodingException uee) {
            // ignore this as utf-8 is always supported
        }
        this.hasParameters = true;
    }

    public boolean hasParameters() {
        return this.hasParameters;
    }
}