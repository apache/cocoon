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
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2001-02-14 11:39:11 $
 */
public class ComponentHolderFactory {

    public static ComponentHolder getComponentHolder (Logger logger, Class component, Configuration configuration, ComponentManager manager)
    throws Exception {
        return (getComponentHolder(logger, component, configuration, manager, null));
    }

    public static ComponentHolder getComponentHolder (Logger logger, Class component, Configuration configuration, ComponentManager manager, String mime_type)
    throws Exception {
        if (Poolable.class.isAssignableFrom(component)) {
            return new PoolableComponentHolder (logger, component, configuration, manager, mime_type);
        } else if (SingleThreaded.class.isAssignableFrom(component)) {
            return new DefaultComponentHolder (logger, component, configuration, manager, mime_type);
        } else if (ThreadSafe.class.isAssignableFrom(component)) {
            return new ThreadSafeComponentHolder (logger, component, configuration, manager, mime_type);
        } else  {
            return new DefaultComponentHolder (logger, component, configuration, manager, mime_type);
        }
    }
}
