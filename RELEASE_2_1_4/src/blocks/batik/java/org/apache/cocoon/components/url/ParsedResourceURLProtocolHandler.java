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
package org.apache.cocoon.components.url;

import org.apache.batik.util.AbstractParsedURLProtocolHandler;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.ParsedURLData;

/**
 * Provide an extension to Batik to handle the "resource:" protocol.  This class
 * uses the <code>Thread.getContextClassLoader()</code> classloader to get resources.
 * It is safe to use this URL with multiple Cocoon webapps running.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Id: ParsedResourceURLProtocolHandler.java,v 1.2 2003/03/11 17:44:19 vgritsenko Exp $
 */
public class ParsedResourceURLProtocolHandler extends AbstractParsedURLProtocolHandler {

    /**
     * Create a new instance, this doesn't do much beyond register the type of
     * protocol we handle.
     */
    public ParsedResourceURLProtocolHandler() {
        super("resource");
    }

    /**
     * Getbase.getPath() the ParsedURLData for the context.  Absolute URIs are specified like
     * "resource://".
     */
    public ParsedURLData parseURL(String uri) {
        ParsedURLData urldata = null;
        String path = uri.substring("resource:/".length());
        urldata = new ParsedURLData(Thread.currentThread().getContextClassLoader().getResource(path));

        if ("file".equals(urldata.protocol)) {
            urldata.host = null;
            urldata.port = -1;
        } else if (null == urldata.host) {
            urldata.port = -1;
        } else if (urldata.port < 0) {
            urldata.host = null;
        }

        return urldata;
    }

    /**
     * The build the relative URL.  Relative URIs are specified like "resource:".
     */
    public ParsedURLData parseURL(ParsedURL base, String uri) {
        StringBuffer newURI = new StringBuffer("resource://");
        newURI.append(base.getPath());

        if ( !newURI.toString().endsWith("/") ) {
            newURI.append("/");
        }

        newURI.append(uri.substring("resource:".length()));

        return this.parseURL(newURI.toString());
    }
}
