/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.saxconnector;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.sitemap.Sitemap;
import org.apache.cocoon.sitemap.SitemapModelComponent;
import org.apache.cocoon.xml.XMLPipe;
import org.xml.sax.XMLFilter;

/**
 * Provides a connection between SAX components.
 * @author <a href="mailto:prussell@apache.org">Paul Russell</a>
 * @version CVS $Revision: 1.1.2.9 $ $Date: 2001-04-30 14:17:15 $
 */
public interface SAXConnector extends XMLPipe, Component, SitemapModelComponent {
    /**
     * Pass reference to containing Sitemap.
     * This is a temporary hack until something better
     * comes along.
     */
    void setSitemap(Sitemap sitemap);
}
