/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.generation;

import java.util.Map;

import org.apache.avalon.utils.Parameters;

import org.apache.cocoon.xml.AbstractXMLProducer;

import org.xml.sax.EntityResolver;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.5 $ $Date: 2000-09-02 21:12:36 $
 */
public abstract class AbstractGenerator extends AbstractXMLProducer
implements Generator {

    /** The current <code>EntityResolver</code>. */
    protected EntityResolver resolver=null;
    /** The current <code>Map</code> objectModel. */
    protected Map objectModel=null;
    /** The current <code>Parameters</code>. */
    protected Parameters parameters=null;
    /** The source URI associated with the request or <b>null</b>. */
    protected String source=null;

    /**
     * Set the <code>EntityResolver</code>, object model <code>Map</code>,
     * the source and sitemap <code>Parameters</code> used to process the request.
     */
    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par) {
        this.resolver=resolver;
        this.objectModel=objectModel;
        this.source=src;
        this.parameters=par;
    }
}
