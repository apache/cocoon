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
import org.apache.cocoon.sitemap.SitemapOutputComponent;
import org.apache.cocoon.xml.AbstractXMLConsumer;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.5 $ $Date: 2000-08-16 05:08:17 $
 */
public abstract class AbstractSerializer extends AbstractXMLConsumer 
implements Serializer {
    /** The current <code>OutputStream</code>. */
    protected OutputStream output=null;

    /**
     * Set the <code>OutputStream</code> where the XML should be serialized.
     */
    public void setOutputStream(OutputStream out) {
        this.output=new BufferedOutputStream(out);
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
