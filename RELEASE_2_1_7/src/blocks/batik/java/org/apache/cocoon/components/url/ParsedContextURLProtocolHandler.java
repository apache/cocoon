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
package org.apache.cocoon.components.url;

import org.apache.batik.util.AbstractParsedURLProtocolHandler;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.ParsedURLData;
import org.apache.cocoon.environment.Context;

import java.net.MalformedURLException;


/**
 * Provide an extension to Batik to handle the "context:" protocol.  This class
 * assumes it will live in a separate classloader as the Context is set statically.
 * Batik uses the Jar file Services extension, so the class is instantiated in
 * an uncontrolled manner (as far as Cocoon is concerned).
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Id: ParsedContextURLProtocolHandler.java,v 1.4 2004/03/05 13:01:45 bdelacretaz Exp $
 */
public class ParsedContextURLProtocolHandler extends AbstractParsedURLProtocolHandler {
    private static Context context = null;

    /**
     * Set the ServletContext for this protocol.  If it does not exist, you will
     * get NullPointerExceptions!
     */
    public static final void setContext(final Context newContext) {
        if (ParsedContextURLProtocolHandler.context == null) {
            ParsedContextURLProtocolHandler.context = newContext;
        }
    }

    /**
     * Create a new instance, this doesn't do much beyond register the type of
     * protocol we handle.
     */
    public ParsedContextURLProtocolHandler() {
        super("context");
    }

    /**
     * Getbase.getPath() the ParsedURLData for the context.  Absolute URIs are specified like
     * "context://".
     */
    public ParsedURLData parseURL(String uri) {
        ParsedURLData urldata = null;
        try {
            String path = uri.substring("context:/".length());
            urldata = new ParsedURLData(ParsedContextURLProtocolHandler.context.getResource(path));
        } catch (MalformedURLException mue) {
            StringBuffer baseFile = new StringBuffer(ParsedContextURLProtocolHandler
                                                     .context.getRealPath("/"));

            if (!baseFile.toString().endsWith("/")) {
                baseFile.append("/");
            }

            baseFile.append(baseFile);
            baseFile.append(uri.substring("context://".length()));

            urldata = new ParsedURLData();
            urldata.protocol = "file";
            urldata.path = baseFile.toString();
        }

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
     * The build the relative URL.  Relative URIs are specified like "context:".
     */
    public ParsedURLData parseURL(ParsedURL base, String uri) {
        StringBuffer newURI = new StringBuffer("context://");
        newURI.append(base.getPath());

        if (!newURI.toString().endsWith("/")) {
            newURI.append("/");
        }

        newURI.append(uri.substring("context:".length()));

        return this.parseURL(newURI.toString());
    }
}
