/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.serialization;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.avalon.utils.Parameters;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.SitemapComponent;
import org.apache.cocoon.xml.AbstractXMLConsumer;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-07-29 18:30:37 $
 */
public abstract class AbstractSerializer extends AbstractXMLConsumer 
implements Serializer {
    /** The current <code>OutputStream</code>. */
    protected OutputStream output=null;
    /** The current <code>Environment</code>. */
    protected Environment environment=null;
    /** The current <code>Parameters</code>. */
    protected Parameters parameters=null;
    /** The source URI associated with the request or <b>null</b>. */
    protected String source=null;

    /**
     * Set the <code>Environment</code> and sitemap
     * <code>Parameters</code> used to process the request.
     */
    public void setup(Environment environment, String src, Parameters par) {
        this.environment=environment;
        this.source=src;
        this.parameters=par;
    }

    /**
     * Set the <code>OutputStream</code> where the XML should be serialized.
     */
    public void setOutputStream(OutputStream out) {
        this.output=new BufferedOutputStream(out);
    }
}
