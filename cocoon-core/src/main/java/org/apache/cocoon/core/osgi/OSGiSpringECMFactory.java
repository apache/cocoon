/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.core.osgi;

import java.beans.PropertyEditor;
import java.net.URL;

import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.core.CoreUtil;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.core.container.spring.AvalonEnvironment;
import org.apache.cocoon.core.container.spring.BeanFactoryUtil;
import org.osgi.service.component.ComponentContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * @version $Id$
 */
public class OSGiSpringECMFactory implements ConfigurableBeanFactory {
	
	private static final String MANIFEST_FILE = "/META-INF/MANIFEST.MF";

	private static final Object CONFIG_FILE = "configFile";
	
    private Logger logger;
	private Settings settings;
	private ConfigurableBeanFactory beanFactory;

	protected Settings getSettings() {
		return this.settings;
	}

	protected void setSettings(final Settings settings) {
		this.settings = settings;
	}	
	
	protected Logger getLogger() {
		return this.logger;
	}

	protected void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	protected void activate(ComponentContext componentContext) throws Exception {
		URL manifestUrl = componentContext.getBundleContext().getBundle().getResource(MANIFEST_FILE);
		String contextPath = manifestUrl.toString();
		contextPath = manifestUrl.toString().substring(0, contextPath.length() - MANIFEST_FILE.length());
		
		DefaultContext avalonContext = CoreUtil.createContext(this.settings, null, contextPath, null, null);
		
		AvalonEnvironment avalonEnvironment = new AvalonEnvironment();
		avalonEnvironment.context = avalonContext;
		avalonEnvironment.logger = this.logger;		
		
		// get the configuration file property
		String configFile= (String) componentContext.getProperties().get(CONFIG_FILE);
		if(configFile == null) {
			throw new ECMConfigurationFileNotSetException("You have to provide a ECM configurationf file!");
		}
		
		this.beanFactory = BeanFactoryUtil.createRootBeanFactory(avalonEnvironment);
//		ConfigurationInfo springBeanConfiguration = ConfigReader.readConfiguration(source, env)
    }

	// ~~~~~~~~~~~~~~~ delegating ... ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
		this.beanFactory.addBeanPostProcessor(beanPostProcessor);
	}

	public boolean containsSingleton(String arg0) {
		return this.beanFactory.containsSingleton(arg0);
	}

	public void destroySingletons() {
		this.beanFactory.destroySingletons();
	}

	public int getBeanPostProcessorCount() {
		return this.beanFactory.getBeanPostProcessorCount();
	}

	public void registerAlias(String arg0, String arg1) throws BeansException {
		this.beanFactory.registerAlias(arg0, arg1);
	}

	public void registerCustomEditor(Class arg0, PropertyEditor arg1) {
		this.beanFactory.registerCustomEditor(arg0, arg1);
	}

	public void registerSingleton(String arg0, Object arg1) throws BeansException {
		this.beanFactory.registerSingleton(arg0, arg1);
	}

	public void setParentBeanFactory(BeanFactory arg0) {
		this.beanFactory.setParentBeanFactory(arg0);
	}

	public BeanFactory getParentBeanFactory() {
		return this.beanFactory.getParentBeanFactory();
	}

	public boolean containsBean(String arg0) {
		return this.beanFactory.containsBean(arg0);
	}

	public String[] getAliases(String arg0) throws NoSuchBeanDefinitionException {
		return this.beanFactory.getAliases(arg0);
	}

	public Object getBean(String arg0) throws BeansException {
		return this.beanFactory.getBean(arg0);
	}

	public Object getBean(String arg0, Class arg1) throws BeansException {
		return this.beanFactory.getBean(arg0, arg1);
	}

	public Class getType(String arg0) throws NoSuchBeanDefinitionException {
		return this.beanFactory.getType(arg0);
	}

	public boolean isSingleton(String arg0) throws NoSuchBeanDefinitionException {
		return this.beanFactory.isSingleton(arg0);
	}
	

}
