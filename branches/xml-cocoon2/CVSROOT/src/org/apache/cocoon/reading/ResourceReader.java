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
import java.util.Hashtable;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.ProcessingException;

import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.10 $ $Date: 2001-01-12 09:53:09 $
 *
 * The <code>ResourceReader</code> component is used to serve binary data
 * in a sitemap pipeline.
 *
 * Parameters:
 *   <dl>
 *     <dt>&lt;expires&gt;</dt>
 *       <dd>This parameter is optional. When specified it determines how long
 *           in miliseconds the resources can be cached by any proxy between
 *           Cocoon2 and the requesting browser.
 *       </dd>
 *   </dl>
 */
public class ResourceReader extends AbstractReader {

    /**
     * Generates the requested resource.
     */
    public void generate() throws IOException, ProcessingException {
        HttpServletResponse res = (HttpServletResponse) objectModel.get(Cocoon.RESPONSE_OBJECT);
        if (res == null) {
           throw new ProcessingException ("Missing a Response object in the objectModel");
        }
        String src = null;
        File file = null;
        URL url = null;
        URLConnection conn = null;
        InputStream is = null;
        long len = 0;
        long lastModified = 0;
        try {
            if(this.source.indexOf(":/")!=-1) {
                src = this.source;
                url = new URL (src);
                conn = url.openConnection();
                len = conn.getContentLength();
                is = conn.getInputStream();
                lastModified = conn.getLastModified();
            } else {
                src = this.resolver.resolveEntity (null,this.source).getSystemId();
                url = new URL (src);
                file = new File (url.getFile());
                len = file.length();
                is = new FileInputStream (file);
                lastModified = file.lastModified();
            }
        } catch (SAXException se) {
            log.error("ResourceReader: error resolving source \"" + source + "\"", se);
            throw new IOException ("ResourceReader: error resolving source \""
                +source+"\". "+se.toString());
        } catch (MalformedURLException mue) {
            log.error("ResourceReader: malformed source \"" + source + "\"", mue);
            throw new IOException ("ResourceReader: malformed source \""
                +src+"\". "+mue.toString());
        }
        byte[] buffer = new byte[(int)len];
        is.read(buffer);
        is.close();
        res.setContentLength(buffer.length);
        res.setDateHeader("Last-Modified", lastModified);
        long expires = parameters.getParameterAsInteger("expires", -1);
        if (expires > 0) {
            res.setDateHeader("Expires", System.currentTimeMillis()+expires);
        }
        res.setHeader("Accept-Ranges", "bytes");
        out.write ( buffer );
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
