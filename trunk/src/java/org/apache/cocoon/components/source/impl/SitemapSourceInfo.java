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
package org.apache.cocoon.components.source.impl;

import java.net.MalformedURLException;

import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Environment;

/**
 * This is a helper class for the cocoon protocol.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: SitemapSourceInfo.java,v 1.1 2004/01/06 12:49:26 cziegeler Exp $
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
