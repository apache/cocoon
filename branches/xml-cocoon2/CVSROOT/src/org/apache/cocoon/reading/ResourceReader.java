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

import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.environment.http.HttpResponse;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.sitemap.SitemapComponent;

import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-07-28 16:20:32 $
 */
public class ResourceReader extends AbstractReader {

    /** The OutputStream */
    private OutputStream out;

    /**
     * Set the <code>OutputStream</code> where the XML should be serialized.
     */
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    /**
     * Generates the requested resource.
     */
    public void generate() throws IOException, ProcessingException {
        HttpEnvironment env = (HttpEnvironment)environment;
        HttpResponse res = env.getResponse();
        String src = null;
        File file = null;
        URL url = null;
        try {
            src = environment.resolveEntity (null,source).getSystemId();
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
        res.setHeader("Accept-Ranges","bytes");
        out.write ( buffer );
    }
}
