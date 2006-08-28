/* 
 * Copyright 2006 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring;

import java.util.Properties;

import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.configuration.impl.MutableSettings;
import org.apache.cocoon.configuration.impl.PropertyHelper;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;

/**
 * This is a bean factory post processor which sets up a child settings object.
 *
 * @since 2.2
 * @version $Id$
 */
public class SubSettingsBeanFactoryPostProcessor
    extends AbstractSettingsBeanFactoryPostProcessor {

    private static final String DEFAULT_CONFIG_PROPERTIES = "config/properties";

    private static final String DEFAULT_CONFIG_XCONF  = "config/xconf";

    private static final String DEFAULT_CONFIG_SPRING = "config/spring";

    /**
     * Initialize this processor.
     * Setup the settings object.
     * @throws Exception
     */
    public void init()
    throws Exception {
        this.settings = this.createSettings();

        this.doInit();

        // settings can't be changed anymore
        this.settings.makeReadOnly();

    }

    protected MutableSettings createSettings() {
        MutableSettings s;
        Settings parentSettings = null;
        final Properties globalSitemapVariables = null;
        if ( this.beanFactory != null && this.beanFactory instanceof HierarchicalBeanFactory
             && ((HierarchicalBeanFactory)this.beanFactory).getParentBeanFactory() != null ) {
            parentSettings = (Settings)((HierarchicalBeanFactory)this.beanFactory).getParentBeanFactory().getBean(Settings.ROLE);
        }
//        if ( componentConfig != null ) {
//            final String propertyDir = componentConfig.getAttribute("property-dir", null);
//            s = this.createSettings(parentSettings, propertyDir, useDefaultIncludes, factory.getCurrentBeanFactory(itsContext), globalSitemapVariables);
//        } else if ( globalSitemapVariables != null ) {
            s = new MutableSettings(parentSettings);
            PropertyHelper.replaceAll(globalSitemapVariables, parentSettings);
            s.configure(globalSitemapVariables);
//        }
        // if no config we just add an empty settings
        if ( s == null ) {
            s = new MutableSettings(parentSettings);
        }
        return s;
    }

    /**
     * Get the settings for Cocoon.
     * This method reads several property files and merges the result. If there
     * is more than one definition for a property, the last one wins.
     * The property files are read in the following order:
     * 1) PROPERTYDIR/*.properties
     *    Default values for the core and each block - the order in which the files are read is not guaranteed.
     * 2) PROPERTYDIR/[RUNNING_MODE]/*.properties
     *    Default values for the running mode - the order in which the files are read is not guaranteed.
     * 3) Property providers (ToBeDocumented)
     *
     * @return A new Settings object
     */
    protected MutableSettings createSettings(Settings    parent,
                                             String      directory,
                                             boolean     useDefaultIncludes,
                                             BeanFactory parentBeanFactory,
                                             Properties  globalSitemapVariables) {
        // get the running mode
        final String mode = parent.getRunningMode();
        // get properties
        final Properties properties = new Properties();

        // create an empty settings objects
        final MutableSettings s = new MutableSettings(parent);

        // read properties from default includes
        if ( useDefaultIncludes ) {
            this.readProperties(DEFAULT_CONFIG_PROPERTIES, properties);
            // read all properties from the mode dependent directory
            this.readProperties(DEFAULT_CONFIG_PROPERTIES + '/' + mode, properties);    
        }

        if ( directory != null ) {
            // now read all properties from the properties directory
            this.readProperties(directory, properties);
            // read all properties from the mode dependent directory
            this.readProperties(directory + '/' + mode, properties);
        }

        if ( globalSitemapVariables != null ) {
            properties.putAll(globalSitemapVariables);
        }
        PropertyHelper.replaceAll(properties, parent);
        s.configure(properties);

        return s;
    }
}
