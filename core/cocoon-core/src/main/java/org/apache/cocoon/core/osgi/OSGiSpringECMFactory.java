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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.ProcessingUtil;
import org.apache.cocoon.core.CoreUtil;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.core.container.spring.AvalonEnvironment;
import org.apache.cocoon.core.container.spring.BeanFactoryUtil;
import org.apache.cocoon.core.container.spring.ConfigReader;
import org.apache.cocoon.core.container.spring.ConfigurationInfo;
import org.apache.cocoon.environment.Context;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.config.Scope;

/**
 * The @link {@link OSGiSpringECMFactory} gives access to all Spring beans via the 
 * {@link CocoonSpringConfigurableListableBeanRegistry} interface which extends 
 * the @link {@link ConfigurableListableBeanFactory}. 
 * 
 * Additionally it exposes all beans as OSGi services.
 * 
 * @version $Id$
 */
public class OSGiSpringECMFactory implements CocoonSpringConfigurableListableBeanRegistry {
	
	private static final String MANIFEST_FILE = "/META-INF/MANIFEST.MF";

	private static final Object CONFIG_FILE = "configFile";
	
    private Logger logger;
	private Settings settings;
	private ConfigurableListableBeanFactory beanFactory;
    private ComponentContext componentContext;
    // FIXME there should not be any need for a parentBeanFactory in this class as
    // the wrapped class contains it
    private BeanFactory parentBeanfactory;

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
	
    // FIXME Added as there is a problem using methods with argument type ending with
    // Factory in Equinox DS
    public void setParentBeanFactory(CocoonSpringBeanRegistry factory) {
        this.parentBeanfactory = factory;
    }

    /*
	 * TODO (DF/RP) move activation into a seperate thread
	 */
	protected void activate(ComponentContext componentContext) throws Exception {
        this.componentContext = componentContext;
        
        // if the logger and setter hasn't been injected look for them in the
        // parent bean factory if there is one
        if (this.logger == null && this.parentBeanfactory != null) {
            this.logger = (Logger) this.parentBeanfactory.getBean(ProcessingUtil.LOGGER_ROLE);
        }
        if (this.settings == null && this.parentBeanfactory != null) {
            this.settings = (Settings) this.parentBeanfactory.getBean(Settings.ROLE);
        }
        
        // create a minimal OSGi servlet context
        Context osgiServletContext = new OSGiServletContext(this.logger, componentContext);     

        AvalonEnvironment avalonEnvironment = this.createAvalonEnvironment(osgiServletContext);
		
		// get the configuration file property
		String configFile= (String) this.componentContext.getProperties().get(CONFIG_FILE);
		if(configFile == null) {
			throw new ECMConfigurationFileNotSetException("You have to provide a ECM configurationf file!");
		}
		if (this.parentBeanfactory == null)
		    this.parentBeanfactory = BeanFactoryUtil.createRootBeanFactory(avalonEnvironment, osgiServletContext);
		ConfigurationInfo springBeanConfiguration = ConfigReader.readConfiguration(configFile, avalonEnvironment);
		this.beanFactory = BeanFactoryUtil.createBeanFactory(avalonEnvironment, springBeanConfiguration,
				null, this.parentBeanfactory, false);
        this.beanFactory.addBeanPostProcessor(new ServiceRegistrationPostProcessor());
        this.beanFactory.preInstantiateSingletons();
        // What is this for?
		//Store store = (Store) beanFactory.getBean(Store.ROLE);
		//this.logger.debug("Store: " + store);
    }

    /**
     * create an Avalon environment (it's some kind of container for Avalon related information)
     * @return the Avalon environment
     * @throws ServletException
     * @throws MalformedURLException
     */
    private AvalonEnvironment createAvalonEnvironment(Context osgiServletContext) throws ServletException, MalformedURLException {
        URL manifestUrl = this.componentContext.getBundleContext().getBundle().getEntry(MANIFEST_FILE);
		String contextPath = manifestUrl.toString();
		contextPath = manifestUrl.toString().substring(0, contextPath.length() - (MANIFEST_FILE.length() - 1));
		
		this.logger.debug("Context path: " + contextPath);

		// create a minimal Avalon Context
		DefaultContext avalonContext = CoreUtil.createContext(this.settings, osgiServletContext, contextPath, null);
		
		// create an Avalon environment (it's some kind of container for Avalon related information)
		AvalonEnvironment avalonEnvironment = new AvalonEnvironment();
		avalonEnvironment.context = avalonContext;
		avalonEnvironment.logger = this.logger;	
		avalonEnvironment.settings = this.settings;
        return avalonEnvironment;
    }
    
    protected void deactivate(ComponentContext componentContext) {
        this.beanFactory.destroySingletons();
    }
    
    private class ServiceRegistrationPostProcessor implements DestructionAwareBeanPostProcessor {
        private BundleContext bundleContext =
            OSGiSpringECMFactory.this.componentContext.getBundleContext();
        /** Mapping from service instance to ServiceReference */
        private Map serviceRegistrations = new HashMap();
        
        public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
            if (isServiceSelector(bean)) {
                info("ServiceSelector destruction: ", bean, beanName);                
            } else if (isSingleton(beanName)) {
                synchronized (this) {
                    ServiceRegistration registration =
                        (ServiceRegistration) this.serviceRegistrations.remove(beanName);
                    if (registration != null)
                        registration.unregister();
                }
                info("Singleton destruction: ", bean, beanName);
            } else if (isFactoryBean(bean)) {
                synchronized (this) {
                    ServiceRegistration registration =
                        (ServiceRegistration) this.serviceRegistrations.remove(beanName);
                    if (registration != null)
                        registration.unregister();
                }
                info("Factory bean destruction: ", bean, beanName);
            } else {
                info("========= destruction: ", bean, beanName);                
            }
        }

        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            String itfName = OSGiSpringBridge.getServiceInterface(beanName);
            String hint = OSGiSpringBridge.getServiceHint(beanName);
            Dictionary properties = null;
            if (hint != null) {
                properties = new Hashtable();
                properties.put(OSGiSpringBridge.HINT_PROPERTY, hint);
            }
            if (isServiceSelector(bean)) {
                // TODO implement
                info("ServiceSelector initialization: ", bean, beanName);                
            } else if (isSingleton(beanName)) {
                // register the bean into the OSGi service registry
                logger.debug("Register interface=" + itfName + " hint=" + hint + " service=" + bean);
                ServiceRegistration registration =
                    this.bundleContext.registerService(itfName, bean, properties);
                synchronized (this) {
                    // keep track on registred services
                    this.serviceRegistrations.put(beanName, registration);
                }
                info("Singleton initialization: ", bean, beanName);
            } else if (isFactoryBean(bean)) {
                Object service = new FactoryBeanServiceFactory((FactoryBean) bean);
                logger.debug("Register FactoryBean=" + itfName + " hint=" + hint + " service=" + service);
                ServiceRegistration registration =
                    this.bundleContext.registerService(itfName, service, properties);
                synchronized (this) {
                    // keep track on registred services
                    this.serviceRegistrations.put(beanName, registration);
                }
                info("Factory bean initialization: ", bean, beanName);
            } else {
                // TODO some kind of proxy or factory is needed for components that
                // are not singletons
                info("========= initialization: ", bean, beanName);                
            }
            String[] aliases = getAliases(beanName);
            if (aliases.length > 0) {
                System.out.print("ALIAS " + beanName + "[ ");
                for (int i = 0; i < aliases.length; i++) {
                    System.out.print(aliases[i] + " ");
                }
                System.out.println("]");
            }
            return bean;
        }

        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }
        
        public boolean isFactoryBean(Object bean) {
            return bean instanceof FactoryBean;
        }
        
        public boolean isServiceSelector(Object bean) {
            return bean instanceof ServiceSelector;
        }
        
        private void info(String info, Object bean, String beanName) {
            Class beanClass = getType(beanName);
            logger.debug(info + beanName +
                    " singleton=" + isSingleton(beanName) +
                    " beanFactory=" + isFactoryBean(bean) +
                    " class=" + beanClass);
        }
        
        // Embed a Spring FactoryBean as an OSGi ServiceFactory 
        public class FactoryBeanServiceFactory implements ServiceFactory {
            private FactoryBean factoryBean;
            
            public FactoryBeanServiceFactory(FactoryBean factoryBean) {
                this.factoryBean = factoryBean;
            }

            public Object getService(Bundle bundle, ServiceRegistration registration) {
                try {
                    // FIXME the OSGi contracts require IIUC, that the returned object is
                    // a singleton that can be cached, don't know if this is fullfilled.
                    return this.factoryBean.getObject();
                } catch (Exception e) {
                    throw new RuntimeException("Cannot get service", e);
                }
            }

            // The FactoryBean have no method for returning the bean to the manager
            public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
                
            }
        }
    }
    
	// ~~~~~~~~~~~~~~~ delegating to this.beanFactory ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~	

	/**	
	 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#addBeanPostProcessor(org.springframework.beans.factory.config.BeanPostProcessor)
	 */
	public void addBeanPostProcessor(BeanPostProcessor arg0) {
		this.beanFactory.addBeanPostProcessor(arg0);
	}

	/**
	 * @throws BeansException
	 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#applyBeanPostProcessorsAfterInitialization(java.lang.Object, java.lang.String)
	 */
	public Object applyBeanPostProcessorsAfterInitialization(Object arg0, String arg1) throws BeansException {
		return this.beanFactory.applyBeanPostProcessorsAfterInitialization(arg0, arg1);
	}

	/**
	 * @throws BeansException
	 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#applyBeanPostProcessorsBeforeInitialization(java.lang.Object, java.lang.String)
	 */
	public Object applyBeanPostProcessorsBeforeInitialization(Object arg0, String arg1) throws BeansException {
		return this.beanFactory.applyBeanPostProcessorsBeforeInitialization(arg0, arg1);
	}

	/**
	 * @throws BeansException
	 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#applyBeanPropertyValues(java.lang.Object, java.lang.String)
	 */
	public void applyBeanPropertyValues(Object arg0, String arg1) throws BeansException {
		this.beanFactory.applyBeanPropertyValues(arg0, arg1);
	}

	/**
	 * @throws BeansException
	 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#autowire(java.lang.Class, int, boolean)
	 */
	public Object autowire(Class arg0, int arg1, boolean arg2) throws BeansException {
		return this.beanFactory.autowire(arg0, arg1, arg2);
	}

	/**
	 * @throws BeansException
	 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#autowireBeanProperties(java.lang.Object, int, boolean)
	 */
	public void autowireBeanProperties(Object arg0, int arg1, boolean arg2) throws BeansException {
		this.beanFactory.autowireBeanProperties(arg0, arg1, arg2);
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#containsBean(java.lang.String)
	 */
	public boolean containsBean(String arg0) {
		return this.beanFactory.containsBean(arg0);
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#containsBeanDefinition(java.lang.String)
	 */
	public boolean containsBeanDefinition(String arg0) {
		return this.beanFactory.containsBeanDefinition(arg0);
	}

	/**
	 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#containsSingleton(java.lang.String)
	 */
	public boolean containsSingleton(String arg0) {
		return this.beanFactory.containsSingleton(arg0);
	}

	/**
	 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#destroySingletons()
	 */
	public void destroySingletons() {
		this.beanFactory.destroySingletons();
	}

	/**
	 * @throws NoSuchBeanDefinitionException
	 * @see org.springframework.beans.factory.BeanFactory#getAliases(java.lang.String)
	 */
	public String[] getAliases(String arg0) throws NoSuchBeanDefinitionException {
		return this.beanFactory.getAliases(arg0);
	}

	/**
	 * @throws BeansException
	 * @see org.springframework.beans.factory.BeanFactory#getBean(java.lang.String, java.lang.Class)
	 */
	public Object getBean(String arg0, Class arg1) throws BeansException {
		return this.beanFactory.getBean(arg0, arg1);
	}

	/**
	 * @throws BeansException
	 * @see org.springframework.beans.factory.BeanFactory#getBean(java.lang.String)
	 */
	public Object getBean(String arg0) throws BeansException {
		return this.beanFactory.getBean(arg0);
	}

	/**
	 * @throws NoSuchBeanDefinitionException
	 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#getBeanDefinition(java.lang.String)
	 */
	public BeanDefinition getBeanDefinition(String arg0) throws NoSuchBeanDefinitionException {
		return this.beanFactory.getBeanDefinition(arg0);
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeanDefinitionCount()
	 */
	public int getBeanDefinitionCount() {
		return this.beanFactory.getBeanDefinitionCount();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeanDefinitionNames()
	 */
	public String[] getBeanDefinitionNames() {
		return this.beanFactory.getBeanDefinitionNames();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeanNamesForType(java.lang.Class, boolean, boolean)
	 */
	public String[] getBeanNamesForType(Class arg0, boolean arg1, boolean arg2) {
		return this.beanFactory.getBeanNamesForType(arg0, arg1, arg2);
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeanNamesForType(java.lang.Class)
	 */
	public String[] getBeanNamesForType(Class arg0) {
		return this.beanFactory.getBeanNamesForType(arg0);
	}

	/**
	 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#getBeanPostProcessorCount()
	 */
	public int getBeanPostProcessorCount() {
		return this.beanFactory.getBeanPostProcessorCount();
	}

	/**
	 * @throws BeansException
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeansOfType(java.lang.Class, boolean, boolean)
	 */
	public Map getBeansOfType(Class arg0, boolean arg1, boolean arg2) throws BeansException {
		return this.beanFactory.getBeansOfType(arg0, arg1, arg2);
	}

	/**
	 * @throws BeansException
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeansOfType(java.lang.Class)
	 */
	public Map getBeansOfType(Class arg0) throws BeansException {
		return this.beanFactory.getBeansOfType(arg0);
	}

	/**
	 * @see org.springframework.beans.factory.HierarchicalBeanFactory#getParentBeanFactory()
	 */
	public BeanFactory getParentBeanFactory() {
		return this.beanFactory.getParentBeanFactory();
	}

	/**
	 * @throws NoSuchBeanDefinitionException
	 * @see org.springframework.beans.factory.BeanFactory#getType(java.lang.String)
	 */
	public Class getType(String arg0) throws NoSuchBeanDefinitionException {
		return this.beanFactory.getType(arg0);
	}

	/**
	 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#ignoreDependencyInterface(java.lang.Class)
	 */
	public void ignoreDependencyInterface(Class arg0) {
		this.beanFactory.ignoreDependencyInterface(arg0);
	}

	/**
	 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#ignoreDependencyType(java.lang.Class)
	 */
	public void ignoreDependencyType(Class arg0) {
		this.beanFactory.ignoreDependencyType(arg0);
	}

	/**
	 * @throws NoSuchBeanDefinitionException
	 * @see org.springframework.beans.factory.BeanFactory#isSingleton(java.lang.String)
	 */
	public boolean isSingleton(String arg0) throws NoSuchBeanDefinitionException {
		return this.beanFactory.isSingleton(arg0);
	}

	/**
	 * @throws BeansException
	 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#preInstantiateSingletons()
	 */
	public void preInstantiateSingletons() throws BeansException {
		this.beanFactory.preInstantiateSingletons();
	}

	/**
	 * @throws BeansException
	 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#registerAlias(java.lang.String, java.lang.String)
	 */
	public void registerAlias(String arg0, String arg1) throws BeansException {
		this.beanFactory.registerAlias(arg0, arg1);
	}

	/**
	 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#registerCustomEditor(java.lang.Class, java.beans.PropertyEditor)
	 */
	public void registerCustomEditor(Class arg0, PropertyEditor arg1) {
		this.beanFactory.registerCustomEditor(arg0, arg1);
	}

	/**
	 * @throws BeansException
	 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#registerSingleton(java.lang.String, java.lang.Object)
	 */
	public void registerSingleton(String arg0, Object arg1) throws BeansException {
		this.beanFactory.registerSingleton(arg0, arg1);
	}

	/**
	 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#setParentBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	public void setParentBeanFactory(BeanFactory arg0) {
		this.beanFactory.setParentBeanFactory(arg0);
	}

    /**
     * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#addPropertyEditorRegistrar(org.springframework.beans.PropertyEditorRegistrar)
     */
    public void addPropertyEditorRegistrar(PropertyEditorRegistrar arg0) {
        this.beanFactory.addPropertyEditorRegistrar(arg0);
    }

    /**
     * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#destroyScopedBean(java.lang.String)
     */
    public void destroyScopedBean(String arg0) {
        this.beanFactory.destroyScopedBean(arg0);
    }

    /**
     * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#getBeanClassLoader()
     */
    public ClassLoader getBeanClassLoader() {
        return this.beanFactory.getBeanClassLoader();
    }

    /**
     * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#isCacheBeanMetadata()
     */
    public boolean isCacheBeanMetadata() {
        return this.beanFactory.isCacheBeanMetadata();
    }

    /**
     * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#registerScope(java.lang.String, org.springframework.beans.factory.config.Scope)
     */
    public void registerScope(String arg0, Scope arg1) {
        this.beanFactory.registerScope(arg0, arg1);
    }

    /**
     * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#setBeanClassLoader(java.lang.ClassLoader)
     */
    public void setBeanClassLoader(ClassLoader arg0) {
        this.beanFactory.setBeanClassLoader(arg0);
    }

    /**
     * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#setCacheBeanMetadata(boolean)
     */
    public void setCacheBeanMetadata(boolean arg0) {
        this.beanFactory.setCacheBeanMetadata(arg0);
    }

    /**
     * @see org.springframework.beans.factory.HierarchicalBeanFactory#containsLocalBean(java.lang.String)
     */
    public boolean containsLocalBean(String arg0) {
        return this.beanFactory.containsLocalBean(arg0);
    }

    /**
     * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#configureBean(java.lang.Object, java.lang.String)
     */
    public Object configureBean(Object arg0, String arg1) throws BeansException {
        return this.beanFactory.configureBean(arg0, arg1);
    }

    /**
     * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#createBean(java.lang.Class, int, boolean)
     */
    public Object createBean(Class arg0, int arg1, boolean arg2) throws BeansException {
        return this.beanFactory.createBean(arg0, arg1, arg2);
    }

    /**
     * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#initializeBean(java.lang.Object, java.lang.String)
     */
    public Object initializeBean(Object arg0, String arg1) throws BeansException {
        return this.beanFactory.initializeBean(arg0, arg1);
    }
}
