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
package org.apache.cocoon.components.source.impl;

import java.net.MalformedURLException;

import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Environment;

/**
 * This is a helper class for the cocoon protocol.
 *
 * @version $Id$
 */
public final class SitemapSourceInfo {
    
    /**
     * <ul>
     * <li><code>true</code> if the sitemap URI uses the <code>raw:</code> subprotocol,
     * which means that request parameters of the original request are not forwarded,</li>
     * <li><code>false</code> otherwise.
     * </ul>
     */
    public boolean rawMode;
    
    /** The protocol used in the sitemap URI, up to and excluding the colon. */
    public String protocol;
    
    /** The request URI, relative to the context. */
    public String requestURI;
    
    /** The system ID: &lt;protocol&gt;&lt;request-uri&gt;[?&lt;query-string&gt;]. */
    public String systemId;
    
    /** The Cocoon view used in the sitemap URI or <code>null</code> if no view is used.  */
    public String view;
    
    /**
     * The prefix of the URI in progress for <code>cocoon:/</code> requests,
     * or an empty string for <code>cocoon://</code> requests.
     */
    public String prefix;
    
    /** The query string of the sitemap URI. */
    public String queryString;
    
    /** The sitemap URI without protocol identifier and query string. */
    public String uri;

    /**
     * Determine the initial processor for the cocoon protocol request.
     * <ul>
     * <li><code>true</code> - start in the root sitemap (<code>cocoon://</code>)</li>
     * <li><code>false</code> - start in the current sitemap (<code>cocoon:/</code>)</li>
     * </ul>
     */
    public boolean processFromRoot;

    public static SitemapSourceInfo parseURI(Environment env, String sitemapURI) 
    throws MalformedURLException {
        SitemapSourceInfo info = new SitemapSourceInfo();
        info.rawMode = false;

        // remove the protocol
        int position = sitemapURI.indexOf(':') + 1;
        if (position != 0) {
            info.protocol = sitemapURI.substring(0, position-1);
            // check for subprotocol
            if (sitemapURI.startsWith("raw:", position)) {
                position += 4;
                info.rawMode = true;
            }
        } else {
            throw new MalformedURLException("No protocol found for sitemap source in " + sitemapURI);
        }

        // does the uri point to this sitemap or to the root sitemap?
        if (sitemapURI.startsWith("//", position)) {
            position += 2;
            info.prefix = "";
            info.processFromRoot = true;
        } else if (sitemapURI.startsWith("/", position)) {
            position ++;
            info.prefix = env.getURIPrefix();
            info.processFromRoot = false;
        } else {
            throw new MalformedURLException("Malformed cocoon URI: " + sitemapURI);
        }

        // create the queryString (if available)
        int queryStringPos = sitemapURI.indexOf('?', position);
        if (queryStringPos != -1) {
            info.queryString = sitemapURI.substring(queryStringPos + 1);
            info.uri = sitemapURI.substring(position, queryStringPos);
        } else if (position > 0) {
            info.uri = sitemapURI.substring(position);
        }

        
        // determine if the queryString specifies a cocoon-view
        info.view = getView(info.queryString, env);

        // build the request uri which is relative to the context
        info.requestURI = info.prefix + info.uri;

        // create system ID
        final StringBuffer buffer = new StringBuffer(info.protocol);
        buffer.append("://").append(info.requestURI);
        if (info.queryString != null ) {
            buffer.append('?').append(info.queryString);
        }
        info.systemId = buffer.toString();

        return info;
    }

    public static String getView(String query, Environment env) {
        if (query != null) {
            int index = query.indexOf(Constants.VIEW_PARAM);
            if (index != -1 
                    && (index == 0 || query.charAt(index-1) == '&')
                    && query.length() > index + Constants.VIEW_PARAM.length() 
                    && query.charAt(index+Constants.VIEW_PARAM.length()) == '=') {
                
                String tmp = query.substring(index+Constants.VIEW_PARAM.length()+1);
                index = tmp.indexOf('&');
                if (index != -1) {
                    return tmp.substring(0,index);
                } else {
                    return tmp;
                }
            } else {
                return env.getView();
            }
        } else {
            return env.getView();
        }
    }
}
