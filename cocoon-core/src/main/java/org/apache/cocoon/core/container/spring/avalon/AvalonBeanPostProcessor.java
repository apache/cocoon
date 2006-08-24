package org.apache.cocoon.core.container.spring.avalon;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.core.container.spring.ComponentInfo;
import org.apache.cocoon.core.container.spring.ConfigurationInfo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

/**
 * This is a Spring BeanPostProcessor adding support for the Avalon lifecycle interfaces.
 * @version $Id$
 * @since 2.2
 */
public class AvalonBeanPostProcessor
    implements DestructionAwareBeanPostProcessor, BeanFactoryAware {

    protected static final Configuration EMPTY_CONFIG = new DefaultConfiguration("empty");

    protected Logger logger;
    protected Context context;
    protected BeanFactory beanFactory;
    protected ConfigurationInfo configurationInfo;

    /**
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory factory) throws BeansException {
        this.beanFactory = factory;
    }

    public ConfigurationInfo getConfigurationInfo() {
        return configurationInfo;
    }

    public void setConfigurationInfo(ConfigurationInfo configurationInfo) {
        this.configurationInfo = configurationInfo;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object, java.lang.String)
     */
    public Object postProcessAfterInitialization(Object bean, String beanName)
    throws BeansException {
        try {
            ContainerUtil.start(bean);
        } catch (Exception e) {
            throw new BeanInitializationException("Unable to start bean " + beanName, e);
        }
        return bean;
    }

    /**
     * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
     */
    public Object postProcessBeforeInitialization(Object bean, String beanName)
    throws BeansException {
        final ComponentInfo info = (ComponentInfo)this.configurationInfo.getComponents().get(beanName);
        try {
            if ( info == null ) {
                // no info so we just return the bean and don't apply any lifecycle interfaces
                return bean;
            }
            if ( info.getLoggerCategory() != null ) {
                ContainerUtil.enableLogging(bean, this.logger.getChildLogger(info.getLoggerCategory()));
            } else {
                ContainerUtil.enableLogging(bean, this.logger);
            }
            ContainerUtil.contextualize(bean, this.context);
            ContainerUtil.service(bean, (ServiceManager)this.beanFactory.getBean(ServiceManager.class.getName()));
            Configuration config = info.getConfiguration();
            if ( config == null ) {
                config = EMPTY_CONFIG;
            }
            if ( bean instanceof Configurable ) {
                ContainerUtil.configure(bean, config);
            } else if ( bean instanceof Parameterizable ) {
                Parameters p = info.getParameters();
                if ( p == null ) {
                    p = Parameters.fromConfiguration(config);
                    info.setParameters(p);
                }
                ContainerUtil.parameterize(bean, p);
            }
            ContainerUtil.initialize(bean);
        } catch (Exception e) {
            throw new BeanCreationException("Unable to initialize Avalon component with role " + beanName, e);
        }
        return bean;
    }

    /**
     * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor#postProcessBeforeDestruction(java.lang.Object, java.lang.String)
     */
    public void postProcessBeforeDestruction(Object bean, String beanName)
    throws BeansException {
        try {
            ContainerUtil.stop(bean);
        } catch (Exception e) {
            throw new BeanInitializationException("Unable to stop bean " + beanName, e);
        }
        ContainerUtil.dispose(bean);
    }
}
