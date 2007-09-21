/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.cocoon.core.container.ContainerTestCase;
import org.apache.cocoon.core.container.spring.avalon.ComponentInfo;
import org.apache.cocoon.core.container.spring.avalon.ConfigurationInfo;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.impl.ResourceSourceFactory;
import org.apache.excalibur.source.impl.SourceResolverImpl;
import org.apache.excalibur.source.impl.URLSourceFactory;
import org.apache.excalibur.xml.dom.DOMParser;
import org.apache.excalibur.xml.impl.JaxpParser;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.excalibur.xmlizer.DefaultXMLizer;
import org.apache.excalibur.xmlizer.XMLizer;

/**
 * Testcase for Cocoon. 
 *
 * @version $Id$
 */
public abstract class CocoonTestCase extends ContainerTestCase {

    /**
     * @see org.apache.cocoon.core.container.ContainerTestCase#addComponents(org.apache.cocoon.core.container.spring.avalon.ConfigurationInfo)
     */
    protected void addComponents(ConfigurationInfo info) 
    throws Exception {
        super.addComponents(info);
        if ( this.addSourceFactories() ) {
            ComponentInfo component;
            // Add resource source factory
            component = new ComponentInfo();
            component.setComponentClassName(ResourceSourceFactory.class.getName());
            component.setRole(SourceFactory.ROLE + "/resource");
            component.setConfiguration(new DefaultConfiguration("-"));
            info.addComponent(component);

            // Add url source source factory
            component = new ComponentInfo();
            component.setComponentClassName(URLSourceFactory.class.getName());
            component.setRole(SourceFactory.ROLE + "/*");
            component.setConfiguration(new DefaultConfiguration("-"));
            info.addComponent(component);

            // add source factory selector
            component = new ComponentInfo();
            component.setModel(ComponentInfo.MODEL_SINGLETON);
            component.setComponentClassName("org.apache.cocoon.core.container.DefaultServiceSelector");
            component.setRole(SourceFactory.ROLE + "Selector");
            component.setAlias("source-factories");
            component.setDefaultValue("*");
            component.setConfiguration(new DefaultConfiguration("-"));
            info.addComponent(component);
            info.getShorthands().put("source-factories", SourceFactory.ROLE + "Selector");
        }
        if ( this.addSourceResolver() ) {
            ComponentInfo component = new ComponentInfo();
            component.setComponentClassName(SourceResolverImpl.class.getName());
            component.setRole(SourceResolver.ROLE);
            component.setConfiguration(new DefaultConfiguration("-"));
            info.addComponent(component);
        }
        if ( this.addSAXParser() ) {
            ComponentInfo component = new ComponentInfo();
            component.setComponentClassName(JaxpParser.class.getName());
            component.setRole(SAXParser.ROLE);
            component.setConfiguration(new DefaultConfiguration("-"));
            info.addComponent(component);            
        }
        if ( this.addDOMParser() ) {
            ComponentInfo component = new ComponentInfo();
            component.setComponentClassName(JaxpParser.class.getName());
            component.setRole(DOMParser.ROLE);
            component.setConfiguration(new DefaultConfiguration("-"));
            info.addComponent(component);            
        }
        if ( this.addXMLizer() ) {
            ComponentInfo component = new ComponentInfo();
            component.setComponentClassName(DefaultXMLizer.class.getName());
            component.setRole(XMLizer.ROLE);
            component.setConfiguration(new DefaultConfiguration("-"));
            info.addComponent(component);            
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

    /**
     * This method should return true if the sax parser should
     * be added automatically. Can be overwritten by subclasses. The
     * default is true.
     */
    protected boolean addSAXParser() {
        return true;
    }

    /**
     * This method should return true if the dom parser should
     * be added automatically. Can be overwritten by subclasses. The
     * default is true.
     */
    protected boolean addDOMParser() {
        return true;
    }

    /**
     * This method should return true if the xmlizer should
     * be added automatically. Can be overwritten by subclasses. The
     * default is true.
     */
    protected boolean addXMLizer() {
        return true;
    }
}
