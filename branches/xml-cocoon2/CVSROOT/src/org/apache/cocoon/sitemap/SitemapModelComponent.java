/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.Component;
import org.apache.avalon.utils.Parameters;
import org.apache.cocoon.ProcessingException;

import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-09-02 21:12:39 $
 */
public interface SitemapModelComponent extends Component {

    /**
     * Set the <code>EntityResolver</code>, objectModel <code>Map</code>,
     * the source and sitemap <code>Parameters</code> used to process the request.
     */
    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException;
}
