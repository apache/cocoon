/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import org.apache.avalon.Configurable;
import org.apache.avalon.ComponentManager;

import org.apache.cocoon.components.language.generator.CompiledComponent;
import org.apache.cocoon.Processor;

/**
 * Base interface for generated <code>Sitemap</code> classes
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.4.12 $ $Date: 2001-02-16 15:38:33 $
 */
public interface Sitemap extends CompiledComponent, Configurable, Processor {
    void setParentSitemapComponentManager (ComponentManager sitemapComponentManager);
}
