/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.serializers;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.cocoon.sitemap.SitemapComponent;
import org.apache.cocoon.xml.XMLConsumer;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2000-02-27 01:33:07 $
 */
public interface Serializer extends XMLConsumer, SitemapComponent {

    /**
     * Set the <code>OutputStream</code> where the XML should be serialized.
     */
    public void setOutputStream(OutputStream out);
}
