/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.sitemap;

import java.util.HashMap;

import org.apache.cocoon.CodeFactory;
import org.apache.cocoon.util.ClassUtils;

import org.w3c.dom.traversal.NodeIterator;

import org.apache.log.Logger;
import org.apache.avalon.Loggable;

/**
 * This class is used as a XSLT extension class. It is used by the sitemap
 * generation stylesheet to load <code>MatcherFactory</code>s or
 * <code>SelectorFactory</code>s to get the generated source code.
 *
 * <strong>Note:</strong> This class uses a static log instance to
 * set up the instances it creates. This is suboptimal.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.9 $ $Date: 2001-02-20 12:47:46 $
 */

public class XSLTFactoryLoader {
    protected static Logger log;

    private HashMap obj = new HashMap();

    public String getClassSource(String className, String prefix, String pattern, NodeIterator conf)
    throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {
        Object factory = obj.get(className);
        if (factory == null) factory = ClassUtils.newInstance(className);
        obj.put(className, factory);

        if (factory instanceof Loggable) {
            ((Loggable)factory).setLogger(this.log);
        }
        if (factory instanceof CodeFactory) {
            return ((CodeFactory) factory).generateClassSource(prefix, pattern, conf);
        }

        throw new Exception ("Wrong class \"" + factory.getClass().getName()
                            + "\". Should implement the CodeFactory interface");
    }

    public static void setLogger(Logger logger) {
        if (log == null) {
            log = logger;
        }
    }

    public String getParameterSource(String className, NodeIterator conf)
    throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {
        Object factory = obj.get(className);
        if (factory == null) factory = ClassUtils.newInstance(className);
        obj.put (className, factory);

        if (factory instanceof Loggable) {
            ((Loggable)factory).setLogger(this.log);
        }
        if (factory instanceof CodeFactory) {
            return ((CodeFactory) factory).generateParameterSource(conf);
        }
    
        throw new Exception ("Wrong class \"" + factory.getClass().getName()
                             + "\". Should implement the CodeFactory interface");
    }

    public String getMethodSource(String className, NodeIterator conf)
    throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {
        Object factory = obj.get(className);
        if (factory == null) factory = ClassUtils.newInstance(className);
        obj.put (className, factory);

        if (factory instanceof Loggable) {
            ((Loggable)factory).setLogger(this.log);
        }
        if (factory instanceof CodeFactory) {
            return ((CodeFactory) factory).generateMethodSource(conf);
        }

        throw new Exception ("Wrong class \"" + factory.getClass().getName()
                            + "\". Should implement the CodeFactory interface");
    }

    public boolean isFactory (String className) {
        boolean result = false;
        try {
            result = ClassUtils.implementsInterface (className, CodeFactory.class.getName());
        } catch (Exception e) {log.debug("XSLTFactoryLoader.isFactory", e);}
        return result;
    }
}
