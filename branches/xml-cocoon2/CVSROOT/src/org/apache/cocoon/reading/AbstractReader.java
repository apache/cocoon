/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.reading;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.apache.avalon.framework.logger.AbstractLoggable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.avalon.excalibur.pool.Recyclable;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.19 $ $Date: 2001-04-30 14:17:33 $
 */
public abstract class AbstractReader extends AbstractLoggable implements Reader, Recyclable {
    /** The current <code>EntityResolver</code>. */
    protected EntityResolver resolver=null;
    /** The current <code>Map</code> of the object model. */
    protected Map objectModel=null;
    /** The current <code>Parameters</code>. */
    protected Parameters parameters=null;
    /** The source URI associated with the request or <b>null</b>. */
    protected String source=null;
    /** The <code>OutputStream</code> to write on. */
    protected OutputStream out=null;

    /**
     * Set the <code>EntityResolver</code> the object model <code>Map</code>,
     * the source and sitemap <code>Parameters</code> used to process the request.
     */
    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par)
        throws ProcessingException, SAXException, IOException {
        this.resolver=resolver;
        this.objectModel=objectModel;
        this.source=src;
        this.parameters=par;
    }

    /**
     * Set the <code>OutputStream</code>
     */
    public void setOutputStream(OutputStream out) {
          this.out = new BufferedOutputStream(out);
    }

    /**
     * Get the mime-type of the output of this <code>Serializer</code>
     * This default implementation returns null to indicate that the
     * mime-type specified in the sitemap is to be used
     */
    public String getMimeType() {
        return null;
    }

    /**
     * @return the time the read source was last modified or 0 if it is not
     *         possible to detect
     */
    public long getLastModified() {
        return 0;
    }

    /**
     * Recycle the component
     */
    public void recycle() {
        this.out = null;
        this.resolver = null;
        this.source = null;
        this.parameters = null;
        this.objectModel = null;
    }
}
