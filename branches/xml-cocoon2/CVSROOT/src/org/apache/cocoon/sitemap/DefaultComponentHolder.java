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

import org.apache.cocoon.util.ClassUtils;

/** 
 * This class holds a sitemap component which is not specially marked as having 
 * a spezial behaviour or treatment.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-10-13 04:14:42 $
 */
public class DefaultComponentHolder implements ComponentHolder {

    protected String className;
    protected Configuration configuration;
    protected ComponentManager manager;
    protected String mime_type;

    /** Creates a DefaultComponentHolder
     * @param className The component class name
     * @param configuration The </CODE>Configuration</CODE> for the component
     * @param manager A <CODE>ComponentManager</CODE> for the component
     */
    public DefaultComponentHolder(String className, Configuration configuration, ComponentManager manager, String mime_type) {
        this.className = className;
        this.configuration = configuration;
        this.manager = manager;
        this.mime_type = mime_type;
    }

    /** Creates a new instance of the <CODE>Component</CODE>
     * @return A <CODE>Component</CODE>
     */
    public Component get() throws Exception {
        Component comp = (Component) ClassUtils.newInstance (this.className);
        if (comp instanceof Composer) {
            ((Composer) comp).setComponentManager (this.manager);
        } 
        if (comp instanceof Configurable) {
            ((Configurable) comp).setConfiguration (this.configuration);
        }
        return comp;
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
        return className;
    }

    /**
     * This method returns the mime-type of the component or null
     */
    public String getMimeType() {
        return mime_type;
    }
}