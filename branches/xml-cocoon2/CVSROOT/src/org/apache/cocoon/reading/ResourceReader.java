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
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Hashtable;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.ProcessingException;

import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.5 $ $Date: 2000-10-06 21:25:30 $
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
        try {
            src = this.resolver.resolveEntity (null,this.source).getSystemId();
            url = new URL (src);
        } catch (SAXException se) {
            throw new IOException ("ResourceReader: error resolving source \""
                +source+"\". "+se.toString());
        } catch (MalformedURLException mue) {
            throw new IOException ("ResourceReader: malformed source \""
                +src+"\". "+mue.toString());
        }
        file = new File (url.getFile());
        FileInputStream fis = new FileInputStream (file);
        byte[] buffer = new byte[(int) file.length()];
        fis.read(buffer);
        fis.close();
        res.setContentLength(buffer.length);
        res.setDateHeader("Last-Modified", file.lastModified());
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
