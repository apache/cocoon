/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.sitemap;

import org.apache.avalon.ComponentManager;
import org.apache.avalon.Configuration;

import org.apache.avalon.Poolable;
import org.apache.avalon.ThreadSafe;
import org.apache.avalon.SingleThreaded;
import org.apache.log.Logger;

import org.apache.cocoon.util.ClassUtils;
/**
 * This factory instantiate the corresponding ComponentHolder according to the
 * interfaces the passed component implements.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.5 $ $Date: 2001-01-22 21:56:48 $
 */
public class ComponentHolderFactory {

    public static ComponentHolder getComponentHolder (Logger logger, String componentName, Configuration configuration, ComponentManager manager)
    throws Exception {
        return (getComponentHolder(logger, componentName, configuration, manager, null));
    }

    public static ComponentHolder getComponentHolder (Logger logger, String componentName, Configuration configuration, ComponentManager manager, String mime_type)
    throws Exception {
        if (ClassUtils.implementsInterface (componentName, Poolable.class.getName())) {
            return new PoolableComponentHolder (logger, componentName, configuration, manager, mime_type);
        } else if (ClassUtils.implementsInterface (componentName, SingleThreaded.class.getName())) {
            return new DefaultComponentHolder (logger, componentName, configuration, manager, mime_type);
        } else if (ClassUtils.implementsInterface (componentName, ThreadSafe.class.getName())) {
            return new ThreadSafeComponentHolder (logger, componentName, configuration, manager, mime_type);
        } else  {
            return new DefaultComponentHolder (logger, componentName, configuration, manager, mime_type);
        }
    }
}
