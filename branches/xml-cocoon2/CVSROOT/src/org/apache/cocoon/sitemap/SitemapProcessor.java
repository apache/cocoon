/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import org.apache.arch.Composer; 
import org.apache.arch.Modifiable; 
import org.apache.arch.config.Configurable;
import org.apache.cocoon.Processor;

/**
 * Base class for XSP-generated <code>SitemapProcessor</code> classes
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-07-06 04:57:50 $
 */
public interface SitemapProcessor 
        extends Composer, Configurable, Processor, LinkResolver, Modifiable {
}
