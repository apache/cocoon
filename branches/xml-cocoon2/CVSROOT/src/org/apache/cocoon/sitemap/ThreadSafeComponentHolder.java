/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.sitemap;

import org.apache.avalon.Component;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Composer;
import org.apache.avalon.Configuration;
import org.apache.avalon.Configurable;
import org.apache.log.Logger;

import org.apache.cocoon.util.ClassUtils;

/**
 * This class holds a sitemap component which is specially marked as beeing thread safe
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2001-02-14 11:39:17 $
 */
public class ThreadSafeComponentHolder extends DefaultComponentHolder {

    private Component comp;

    /** Creates a ThreadSafeComponentHolder
     * @param className The component class name
     * @param configuration The </CODE>Configuration</CODE> for the component
     * @param manager A <CODE>ComponentManager</CODE> for the component
     */
    public ThreadSafeComponentHolder(Logger log, Class clazz, Configuration configuration, ComponentManager manager, String mime_type)
    throws Exception {
        super(log, clazz, configuration, manager, mime_type);
        this.comp = super.get();
    }

    /** Returns the instance of the <CODE>Component</CODE>
     * @return A <CODE>Component</CODE>
     */
    public Component get() throws Exception {
        return this.comp;
    }

    /**
     * This method has no sense in this implementation. It is used to return a
     * component to the <CODE>ComponentHolder</CODE>
     * @param component The<CODE>Component</CODE> to return
     */
    public void put(Component component) {
    }

    /**
     * This method returns the name of the component hold by this object
     * @return The name of the class this Holder holds
     */
    public String getName() {
        return this.comp.getClass().getName();
    }
}