/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.generation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.Roles;
import org.apache.cocoon.caching.Cacheable;
import org.apache.cocoon.caching.CacheValidity;
import org.apache.cocoon.caching.TimeStampCacheValidity;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.components.url.URLFactory;
import org.apache.cocoon.util.HashUtil;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.w3c.tidy.Tidy;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.20 $ $Date: 2001-05-09 15:25:08 $
 */
public class HTMLGenerator extends ComposerGenerator implements Cacheable {

    /** The system ID of the input source */
    private String      systemID;
    /** Last modification date of the source */
    private long        lastModificationDate;

    /**
     * Recycle this component.
     * All instance variables are set to <code>null</code>.
     */
    public void recycle() {
        super.recycle();
        this.systemID = null;
    }

    /**
     * Setup the html generator.
     * Try to get the last modification date of the source for caching.
     */
    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        InputSource inputSource = super.resolver.resolveEntity(null, super.source);
        this.systemID = inputSource.getSystemId();
        if (this.systemID.startsWith("file:") == true) {
            File xmlFile = new File(this.systemID.substring("file:".length()));
            this.lastModificationDate = xmlFile.lastModified();
        } else {
            try {
                java.net.URL u= new java.net.URL(this.systemID);
                java.net.URLConnection conn = u.openConnection();
                this.lastModificationDate = u.openConnection().getLastModified();
            } catch (java.net.MalformedURLException local) {
                // we ignore this at this stage
                this.lastModificationDate = 0; // no caching!
            }
        }
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     * This method must be invoked before the generateValidity() method.
     *
     * @return The generated key or <code>0</code> if the component
     *              is currently not cacheable.
     */
    public long generateKey() {
        if (this.lastModificationDate != 0) {
            return HashUtil.hash(this.systemID);
        }
        return 0;
    }

    /**
     * Generate the validity object.
     * Before this method can be invoked the generateKey() method
     * must be invoked.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public CacheValidity generateValidity() {
        if (this.lastModificationDate != 0) {
            return new TimeStampCacheValidity(this.lastModificationDate);
        }
        return null;
    }

    /**
     * Generate XML data.
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
        URLFactory urlFactory = null;
        try
        {
            // Setup an instance of Tidy.
            Tidy tidy = new Tidy();
            tidy.setXmlOut(true);
            tidy.setXHTML(true);

            urlFactory = (URLFactory) this.manager.lookup(Roles.URL_FACTORY);
            URL url = urlFactory.getURL(this.source);

            // Extract the document using JTidy and stream it.
            org.w3c.dom.Document doc = tidy.parseDOM(new BufferedInputStream(url.openStream()), null);
            DOMStreamer streamer = new DOMStreamer(this.contentHandler,this.lexicalHandler);
            streamer.stream(doc);
        } catch (IOException e){
            getLogger().warn("HTMLGenerator.generate()", e);
            throw new ResourceNotFoundException("Could not get Resource for HTMLGenerator", e);
        } catch (SAXException e){
            getLogger().error("HTMLGenerator.generate()", e);
            throw(e);
        } catch (Exception e){
            getLogger().error("Could not get parser", e);
            throw new ProcessingException("Exception in HTMLGenerator.generate()",e);
        } finally {
            this.manager.release((Component)urlFactory);
        }
    }
}
