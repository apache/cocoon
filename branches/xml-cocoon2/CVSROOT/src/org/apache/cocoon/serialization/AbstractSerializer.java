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
import java.util.Map;
import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.ProcessingException;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.16 $ $Date: 2001-05-03 14:09:28 $
 */

public abstract class AbstractSerializer extends AbstractXMLPipe implements Serializer, Recyclable {

    /**
     * The <code>OutputStream</code> used by this serializer.
     */
    protected OutputStream output;

    /**
     * Set the <code>EntityResolver</code> the object model <code>Map</code>,
     * the source and sitemap <code>Parameters</code> used to process the request.
     */
    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {}

    /**
     * Set the <code>OutputStream</code> where the XML should be serialized.
     */
    public void setOutputStream(OutputStream out) {
        this.output = new BufferedOutputStream(out);
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
     * Recycle serializer by removing references
     */
    public void recycle() {
        super.recycle();
        this.output = null;
    }
}
