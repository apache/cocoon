/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.reading;

import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.sitemap.SitemapModelComponent;
import org.apache.cocoon.sitemap.SitemapOutputComponent;

import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2001-04-20 11:27:21 $
 */
public interface Reader extends SitemapModelComponent, SitemapOutputComponent {

    /**
     * Generate the response.
     * @return The length of the response or <code>0</code> if the length
     * is unknown.
     */
    int generate()
    throws IOException, SAXException, ProcessingException;

    /**
     * @return the time the read source was last modified or 0 if it is not
     *         possible to detect
     */
    long getLastModified();
}
