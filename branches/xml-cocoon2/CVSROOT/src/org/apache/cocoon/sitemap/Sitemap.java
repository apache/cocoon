/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import org.apache.avalon.Composer; 
import org.apache.avalon.Modifiable; 
import org.apache.avalon.Configurable; 
import org.apache.avalon.Configuration; 
import org.apache.cocoon.Processor;

/**
 * Base interface for generated <code>Sitemap</code> classes
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.4.8 $ $Date: 2000-07-22 20:41:57 $
 */
public interface Sitemap
         extends Composer, Configurable, Processor, Modifiable { 

    /** set the base path of a sitemap */
    public void setBasePath (String basePath);
} 
