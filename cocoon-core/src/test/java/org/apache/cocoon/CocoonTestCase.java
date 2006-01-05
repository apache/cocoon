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

package org.apache.cocoon;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.core.container.CoreServiceManager;
import org.apache.cocoon.core.container.StandaloneServiceSelector;
import org.apache.cocoon.core.container.ContainerTestCase;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.impl.ResourceSourceFactory;
import org.apache.excalibur.source.impl.SourceResolverImpl;
import org.apache.excalibur.source.impl.URLSourceFactory;

/**
 * Testcase for Cocoon. 
 *
 * @version $Id: SitemapComponentTestCase.java 55427 2004-10-24 11:38:37Z cziegeler $
 */
public abstract class CocoonTestCase extends ContainerTestCase {

    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.ContainerTestCase#addComponents(org.apache.cocoon.core.container.CocoonServiceManager)
     */
    protected void addComponents(CoreServiceManager manager) 
    throws ServiceException, ConfigurationException {
        super.addComponents(manager);
        if ( this.addSourceFactories() ) {
            // Create configuration for source-factories
            final DefaultConfiguration df = new DefaultConfiguration("source-factories");
            DefaultConfiguration factory = new DefaultConfiguration("component-instance");
            factory.setAttribute("class", ResourceSourceFactory.class.getName());
            factory.setAttribute("name", "resource");
            df.addChild(factory);
            factory = new DefaultConfiguration("component-instance");
            factory.setAttribute("class", URLSourceFactory.class.getName());
            factory.setAttribute("name", "*");
            df.addChild(factory);
            manager.addComponent("org.apache.excalibur.source.SourceFactorySelector", 
                                 StandaloneServiceSelector.class.getName(), 
                                 df,
                                 null);
        }
        if ( this.addSourceResolver() ) {
            manager.addComponent(SourceResolver.ROLE, 
                    SourceResolverImpl.class.getName(), 
                    new DefaultConfiguration("", "-"),
                    null);
        }
    }
    
    /**
     * This method should return true if the source factories should
     * be added automatically. Can be overwritten by subclasses. The
     * default is true.
     */
    protected boolean addSourceFactories() {
        return true;
    }
    
    /**
     * This method should return true if the source resolver should
     * be added automatically. Can be overwritten by subclasses. The
     * default is true.
     */
    protected boolean addSourceResolver() {
        return true;
    }
}
