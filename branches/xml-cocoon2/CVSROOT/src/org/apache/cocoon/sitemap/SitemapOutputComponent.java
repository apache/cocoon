/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.avalon.component.Component;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2001-04-20 20:50:14 $
 */
public interface SitemapOutputComponent extends Component {

    /**
     * Set the <code>OutputStream</code> where the requested resource should
     * be serialized.
     */
    void setOutputStream(OutputStream out) throws IOException;

    /**
     * Get the mime-type of the output of this <code>Component</code>.
     */
    String getMimeType();
}
