/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.reading;

import java.io.OutputStream;
import java.util.Map;

import org.xml.sax.EntityResolver;

import org.apache.avalon.Parameters;
import org.apache.avalon.AbstractLoggable;
import org.apache.avalon.Poolable;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.10 $ $Date: 2001-02-22 17:10:41 $
 */
public abstract class AbstractReader extends AbstractLoggable implements Poolable, Reader {
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
    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par) {
        this.resolver=resolver;
        this.objectModel=objectModel;
        this.source=src;
        this.parameters=par;
    }

    /**
     * Set the <code>OutputStream</code>
     */
    public void setOutputStream(OutputStream out) {
        this.out=out;
    }

    /**
     * Get the mime-type of the output of this <code>Serializer</code>
     * This default implementation returns null to indicate that the
     * mime-type specified in the sitemap is to be used
     */
    public String getMimeType() {
        return null;
    }
}
