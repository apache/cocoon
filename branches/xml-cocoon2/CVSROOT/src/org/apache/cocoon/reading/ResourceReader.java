/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.reading;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Date;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.Roles;
import org.apache.cocoon.components.url.URLFactory;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.Context;

import org.apache.avalon.ComponentManager;
import org.apache.avalon.Composer;
import org.apache.avalon.Component;

import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.27 $ $Date: 2001-04-18 12:06:02 $
 *
 * The <code>ResourceReader</code> component is used to serve binary data
 * in a sitemap pipeline. It makes use of HTTP Headers to determine if
 * the requested resource should be written to the <code>OutputStream</code>
 * or if it can signal that it hasn't changed.
 *
 * Parameters:
 *   <dl>
 *     <dt>&lt;expires&gt;</dt>
 *       <dd>This parameter is optional. When specified it determines how long
 *           in miliseconds the resources can be cached by any proxy or browser
 *           between Cocoon2 and the requesting visitor.
 *       </dd>
 *   </dl>
 */
public class ResourceReader extends AbstractReader implements Composer {

    private ComponentManager manager;

    public void compose (ComponentManager manager) {
        this.manager = manager;
    }

    /**
     * Generates the requested resource.
     */
    public int generate() throws IOException, ProcessingException {
        Request request = (Request) objectModel.get(Constants.REQUEST_OBJECT);
        Response response = (Response) objectModel.get(Constants.RESPONSE_OBJECT);

        if (response == null) {
           throw new ProcessingException ("Missing a Response object in the objectModel");
        }

        if (request == null) {
           throw new ProcessingException ("Missing a Request object in the objectModel");
        }

        String src = null;
        File file = null;
        URL url = null;
        URLConnection conn = null;
        InputStream is = null;
        long len = 0;

        URLFactory urlFactory = null;
        try {
            try {
                urlFactory = (URLFactory) this.manager.lookup(Roles.URL_FACTORY);
            } catch (Exception e) {
                getLogger().error("cannot obtain the URLFactory", e);
                throw new ProcessingException ("cannot obtain the URLFactory", e);
            }
            if(this.source.indexOf(":/") != -1) {
                src = this.source;
                url = urlFactory.getURL (src);
                conn = url.openConnection();

                if (!modified (conn.getLastModified(), request, response)) {
                    return 0;
                }

                len = conn.getContentLength();
                is = conn.getInputStream();
            } else {
                src = this.resolver.resolveEntity (null,this.source).getSystemId();
                url = urlFactory.getURL (src);
                file = new File (url.getFile());

                if (!modified (file.lastModified(), request, response)) {
                    return 0;
                }

                len = file.length();
                is = new BufferedInputStream(new FileInputStream (file));
            }
        } catch (SAXException se) {
            getLogger().error("ResourceReader: error resolving source \"" + source + "\"", se);
            throw new ResourceNotFoundException ("ResourceReader: error resolving source \""
                +source+"\". ", se);
        } catch (MalformedURLException mue) {
            getLogger().error("ResourceReader: malformed source \"" + source + "\"", mue);
            throw new ResourceNotFoundException ("ResourceReader: malformed source \""
                +src+"\". ", mue);
        } finally {
            if(urlFactory != null) this.manager.release((Component) urlFactory);
        }

        try {
            long expires = parameters.getParameterAsInteger("expires", -1);

            if (expires > 0) {
                response.setDateHeader("Expires", new Date().getTime() + expires);
            }

            response.setHeader("Accept-Ranges", "bytes");

            byte[] buffer = new byte[8192];
            int length = -1;

            while ((length = is.read(buffer)) > -1) {
                out.write(buffer, 0, length);
            }
            is.close();
            out.flush();
        } catch (IOException ioe) {
            getLogger().debug("Received an IOException, assuming client severed connection on purpose");
        }
        return (int)len;
    }

    /**
     * Checks if the file has been modified
     */
    private boolean modified (long lastModified, Request request, Response response) {
        response.setDateHeader("Last-Modified", lastModified);
        long if_modified_since = request.getDateHeader("if-modified-since");
        boolean isHttpResponse = response instanceof org.apache.cocoon.environment.http.HttpResponse;

        if (if_modified_since >= lastModified && isHttpResponse == true) {
            ((org.apache.cocoon.environment.http.HttpResponse)response).setStatus(
                javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED);
        }

        getLogger().debug("ResourceReader: resource has " + ((if_modified_since < lastModified) ? "" : "not ") + "been modified");
        return (if_modified_since < lastModified || isHttpResponse == false);
    }

    /**
     * Returns the mime-type of the resource in process.
     */
    public String getMimeType () {
        Context ctx = (Context) objectModel.get(Constants.CONTEXT_OBJECT);

        if (ctx != null) {
           return ctx.getMimeType(this.source);
        } else {
           return null;
        }
    }
}
