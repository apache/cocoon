/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.reading;

import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.Roles;
import org.apache.cocoon.components.url.URLFactory;

import org.apache.avalon.ComponentManager;
import org.apache.avalon.Composer;
import org.apache.avalon.Component;

import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.18 $ $Date: 2001-02-22 19:08:07 $
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
    public void generate() throws IOException, ProcessingException {
        HttpServletRequest req = (HttpServletRequest) objectModel.get(Constants.REQUEST_OBJECT);
        HttpServletResponse res = (HttpServletResponse) objectModel.get(Constants.RESPONSE_OBJECT);
        URLFactory urlFactory = null;

        try {
            urlFactory = (URLFactory) this.manager.lookup(Roles.URL_FACTORY);
        } catch (Exception e) {
            getLogger().error("cannot obtain the URLFactory", e);
            throw new ProcessingException ("cannot obtain the URLFactory");
        }

        if (res == null) {
           this.manager.release((Component) urlFactory);
           throw new ProcessingException ("Missing a Response object in the objectModel");
        }
        if (req == null) {
           this.manager.release((Component) urlFactory);
           throw new ProcessingException ("Missing a Request object in the objectModel");
        }
        String src = null;
        File file = null;
        URL url = null;
        URLConnection conn = null;
        InputStream is = null;
        long len = 0;
        try {
            if(this.source.indexOf(":/") != -1) {
                src = this.source;
                url = urlFactory.getURL (src);
                conn = url.openConnection();
                if (!modified (conn.getLastModified(), req, res)) {
                    return;
                }
                len = conn.getContentLength();
                is = conn.getInputStream();
            } else {
                src = this.resolver.resolveEntity (null,this.source).getSystemId();
                url = urlFactory.getURL (src);
                file = new File (url.getFile());
                if (!modified (file.lastModified(), req, res)) {
                    return;
                }
                len = file.length();
                is = new FileInputStream (file);
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
            this.manager.release((Component) urlFactory);
        }
        byte[] buffer = new byte[(int)len];
        is.read(buffer);
        is.close();
        res.setContentLength(buffer.length);
        long expires = parameters.getParameterAsInteger("expires", -1);
        if (expires > 0) {
            res.setDateHeader("Expires", System.currentTimeMillis()+expires);
        }
        res.setHeader("Accept-Ranges", "bytes");
        out.write ( buffer );
    }

    /**
     * Checks if the file has been modified
     */
    private boolean modified (long lastModified, HttpServletRequest req, HttpServletResponse res) {
        res.setDateHeader("Last-Modified", lastModified);
        long if_modified_since = req.getDateHeader("if-modified-since");
        if (if_modified_since >= lastModified) {
            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }
        getLogger().debug("ResourceReader: resource has " + ((if_modified_since < lastModified) ? "" : "not ") + "been modified");
        return (if_modified_since < lastModified);
    }

    /**
     * Returns the mime-type of the resource in process.
     */
    public String getMimeType () {
        ServletContext ctx = (ServletContext) objectModel.get("context");
        if (ctx != null) {
           return ctx.getMimeType(this.source);
        } else {
           return null;
        }
    }
}
