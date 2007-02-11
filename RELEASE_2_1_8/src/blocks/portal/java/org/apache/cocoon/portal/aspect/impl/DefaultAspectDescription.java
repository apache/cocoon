/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.aspect.impl;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.portal.aspect.AspectDescription;



/**
 * A configured aspect
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: DefaultAspectDescription.java,v 1.6 2004/03/05 13:02:10 bdelacretaz Exp $
 */
public class DefaultAspectDescription 
    implements AspectDescription {

    protected String name;
    
    protected String className;
    
    protected String persistence;

    protected boolean autoCreate;
    
    protected String defaultValue;
    
    /**
     * Create a new description from a {@link Configuration} object.
     * All values must be stored as attributes
     */
    public static AspectDescription newInstance(Configuration conf)
    throws ConfigurationException {
        DefaultAspectDescription adesc = new DefaultAspectDescription();
        adesc.setClassName(conf.getAttribute("class"));
        adesc.setName(conf.getAttribute("name"));
        adesc.setPersistence(conf.getAttribute("store"));
        adesc.setAutoCreate(conf.getAttributeAsBoolean("auto-create", false));
        adesc.setDefaultValue(conf.getAttribute("value", null));
        
        return adesc;
    }
    
    /**
     * @return The class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return The configred name
     */
    public String getName() {
        return name;
    }

    /**
     * @param string
     */
    public void setClassName(String string) {
        className = string;
    }

    /**
     * @param string
     */
    public void setName(String string) {
        name = string;
    }

    /**
     * @return The role of the store
     */
    public String getStoreName() {
        return persistence;
    }

    /**
     * @param string
     */
    public void setPersistence(String string) {
        persistence = string;
    }

    /**
     * If the data is not available, create it automatically (or not)
     */
    public boolean isAutoCreate() {
        return autoCreate;
    }

    /**
     * Set auto create
     */
    public void setAutoCreate(boolean b) {
        autoCreate = b;
    }

    /**
     * Default value
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String value) {
        this.defaultValue = value;
    }
    
    public String toString() {
        return ("AspectDescription name=" + this.name + 
                 ", class=" + this.className +
                 ", persistence=" + this.persistence +
                 ", autoCreate=" + this.autoCreate +
                 ", defaultValue=" + this.defaultValue);
    }
}
