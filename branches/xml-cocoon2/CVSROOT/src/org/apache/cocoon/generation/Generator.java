/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.generation;

import java.io.IOException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.sitemap.SitemapComponent;
import org.apache.cocoon.xml.XMLProducer;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-07-27 21:49:00 $
 */
public interface Generator extends XMLProducer, SitemapComponent {

    public void generate()
    throws IOException, SAXException, ProcessingException;
}
