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
import java.util.Map;

import org.apache.cocoon.caching.Cacheable;
import org.apache.cocoon.caching.CacheValidity;
import org.apache.cocoon.caching.TimeStampCacheValidity;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.Roles;
import org.apache.cocoon.components.url.URLFactory;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.util.HashUtil;

import org.apache.avalon.component.ComponentManager;
import org.apache.avalon.component.Composable;
import org.apache.avalon.component.Component;
import org.apache.avalon.component.ComponentException;
import org.apache.avalon.parameters.Parameters;

import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.30 $ $Date: 2001-04-20 20:50:10 $
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
public class ResourceReader extends AbstractReader
            implements Composable, Cacheable {

    private ComponentManager manager;

    /** The system ID of the input source */
    private String      systemID;


    private InputStream inputStream;
    private long inputLength;
    private long lastModified;

    /**
     * Setup the reader.
     * The resource is opened to get an <code>InputStream</code>,
     * the length and the last modification date
     */
    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par)
        throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        this.systemID = resolver.resolveEntity(null, super.source).getSystemId();

        URLFactory urlFactory = null;
        try {
            try {
                urlFactory = (URLFactory) this.manager.lookup(Roles.URL_FACTORY);
            } catch (Exception e) {
                getLogger().error("cannot obtain the URLFactory", e);
                throw new ProcessingException ("cannot obtain the URLFactory", e);
            }
            try {
                if (this.source.indexOf(":/") != -1) {
                    URLConnection conn = urlFactory.getURL(this.source).openConnection();
                    this.lastModified = conn.getLastModified();
                    this.inputLength = conn.getContentLength();
                    this.inputStream = conn.getInputStream();
                } else {
                    File file = new File(urlFactory.getURL(this.systemID).getFile());
                    this.lastModified = file.lastModified();
                    this.inputLength = file.length();
                    this.inputStream = new BufferedInputStream(new FileInputStream (file));
                }
            } catch (MalformedURLException mue) {
                getLogger().error("ResourceReader: malformed source \"" + this.source + "\"", mue);
                throw new ResourceNotFoundException ("ResourceReader: malformed source \""
                    +this.source+"\". ", mue);
            }
        } finally {
            if (urlFactory != null) {
                this.manager.release((Component)urlFactory);
            }
        }
    }

    public void compose (ComponentManager manager) {
        this.manager = manager;
    }

    public void recycle() {
        super.recycle();
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            } catch (IOException ioe) {
                getLogger().debug("Received an IOException, assuming client severed connection on purpose");
            }
            this.inputStream = null;
        }
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public long generateKey() {
        return HashUtil.hash(this.systemID);
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public CacheValidity generateValidity() {
        return new TimeStampCacheValidity(this.lastModified);
    }

    /**
     * @return the time the read source was last modified or 0 if it is not
     *         possible to detect
     */
    public long getLastModified() {
        return this.lastModified;
    }

    /**
     * Generates the requested resource.
     */
    public int generate() throws IOException, ProcessingException {
        Request request = (Request) objectModel.get(Constants.REQUEST_OBJECT);
        Response response = (Response) objectModel.get(Constants.RESPONSE_OBJECT);

        try {
            long expires = parameters.getParameterAsInteger("expires", -1);

            if (expires > 0) {
                response.setDateHeader("Expires", new Date().getTime() + expires);
            }

            response.setHeader("Accept-Ranges", "bytes");

            byte[] buffer = new byte[8192];
            int length = -1;

            while ((length = this.inputStream.read(buffer)) > -1) {
                out.write(buffer, 0, length);
            }
            this.inputStream.close();
            this.inputStream = null;
            out.flush();
        } catch (IOException ioe) {
            getLogger().debug("Received an IOException, assuming client severed connection on purpose");
        }
        return (int)this.inputLength;
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
