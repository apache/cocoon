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
package org.apache.cocoon.components.source.impl;

import java.net.MalformedURLException;

import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Environment;

/**
 * This is a helper class for the cocoon protocol.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: SitemapSourceInfo.java,v 1.1 2004/05/25 07:28:24 cziegeler Exp $
 */
public final class SitemapSourceInfo {
    
    public boolean rawMode;
    public String protocol;
    public String requestURI;
    public String systemId;
    public String view;
    public String prefix;
    public String queryString;
    public String uri;
    
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
            info.prefix = ""; // start at the root
        } else if (sitemapURI.startsWith("/", position)) {
            position ++;
            info.prefix = env.getURIPrefix();
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
        info.view = null;
        if (info.queryString != null) {
            int index = info.queryString.indexOf(Constants.VIEW_PARAM);
            if (index != -1 
                    && (index == 0 || info.queryString.charAt(index-1) == '&')
                    && info.queryString.length() > index + Constants.VIEW_PARAM.length() 
                    && info.queryString.charAt(index+Constants.VIEW_PARAM.length()) == '=') {
                
                String tmp = info.queryString.substring(index+Constants.VIEW_PARAM.length()+1);
                index = tmp.indexOf('&');
                if (index != -1) {
                    info.view = tmp.substring(0,index);
                } else {
                    info.view = tmp;
                }
            } else {
                info.view = env.getView();
            }
        } else {
            info.view = env.getView();
        }

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
    
}
