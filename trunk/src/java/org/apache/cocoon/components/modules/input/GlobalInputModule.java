/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import java.util.Iterator;
import java.util.Map;

/**
 * This simple module allows to define global parameters in a sitemap. The
 * values are inherited from one sitemap to its sub sitemaps and can be
 * extended there.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: GlobalInputModule.java,v 1.3 2004/03/08 13:58:30 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=InputModule
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=global-input
 */
public final class GlobalInputModule 
    extends AbstractLogEnabled
    implements InputModule, Serviceable, ThreadSafe {

    private ServiceManager manager;
    
    /**
     * Serviceable
     * 
     * @avalon.dependency  type=SitemapVariableHolder
     */
    public void service(ServiceManager manager) {
        this.manager = manager;
    }
    
    /**
     * Standard access to an attribute's value. If more than one value
     * exists, the first is returned. If the value does not exist,
     * null is returned. To get all values, use {@link
     * #getAttributeValues getAttributeSet} or {@link
     * #getAttributeNames getAttributeNames} and {@link #getAttribute
     * getAttribute} to get them one by one.
     * @param name a String that specifies what the caller thinks
     * would identify an attribute. This is mainly a fallback if no
     * modeConf is present.
     * @param modeConf column's mode configuration from resource
     * description. This argument is optional.
     * @param objectModel
     */
    public Object getAttribute( String name, Configuration modeConf, Map objectModel ) 
    throws ConfigurationException {
        SitemapVariableHolder holder = null;
        try {
            holder = (SitemapVariableHolder)this.manager.lookup(SitemapVariableHolder.ROLE);
            return holder.get(name); 
        } catch (ServiceException ce) {
            throw new ConfigurationException("Unable to lookup SitemapVariableHolder.", ce);
        } finally {
            this.manager.release(holder);
        }
    }


    /**
     * Returns an Iterator of String objects containing the names
     * of the attributes available. If no attributes are available,
     * the method returns an empty Iterator.
     * @param modeConf column's mode configuration from resource
     * description. This argument is optional.
     * @param objectModel
     */
    public Iterator getAttributeNames( Configuration modeConf, Map objectModel ) 
    throws ConfigurationException {
        SitemapVariableHolder holder = null;
        try {
            holder = (SitemapVariableHolder)this.manager.lookup(SitemapVariableHolder.ROLE);
            return holder.getKeys(); 
        } catch (ServiceException ce) {
            throw new ConfigurationException("Unable to lookup SitemapVariableHolder.", ce);
        } finally {
            this.manager.release(holder);
        }
    }


    /**
     * Returns an array of String objects containing all of the values
     * the given attribute has, or null if the attribute does not
     * exist. As an alternative, {@link #getAttributeNames
     * getAttributeNames} together with {@link #getAttribute
     * getAttribute} can be used to get the values one by one.
     * @param name a String that specifies what the caller thinks
     * would identify an attributes. This is mainly a fallback
     * if no modeConf is present.
     * @param modeConf column's mode configuration from resource
     * description. This argument is optional.
     * @param objectModel
     */
    public Object[] getAttributeValues( String name, Configuration modeConf, Map objectModel ) 
    throws ConfigurationException {
        Object o = this.getAttribute(name, modeConf, objectModel);
        if (o != null) {
            return new Object[] {o};
        }
        return null;
    }

}

