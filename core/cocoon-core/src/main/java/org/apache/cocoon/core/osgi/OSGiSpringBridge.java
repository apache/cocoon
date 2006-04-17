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

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * The {@link OSGiSpringBridge} is used to provide OSGi services as Spring beans.
 * It can also be used as parent bean factory for {@link OSGiSpringECMFactory} services.
 * 
 * @version $Id$
 */
public class OSGiSpringBridge implements CocoonSpringBeanRegistry {
    private BundleContext bundleContext;
    
    public static final String HINT_PROPERTY = "component.hint";
    
    protected void activate(ComponentContext componentContext) {
        System.out.println("OSGiSpringBridge: activate");
        this.bundleContext = componentContext.getBundleContext();
    }
    
    protected void deactivate(ComponentContext componentContext) {
        
    }

	public boolean containsBean(String beanName) {
        System.out.println("containsBean name=" + beanName);
	    try {
            return getServiceReference(this.bundleContext, beanName) != null;
        } catch (InvalidSyntaxException e) {
            return false;
        }
	}

	public String[] getAliases(String beanName) throws NoSuchBeanDefinitionException {
        if (!containsBean(beanName))
            throw new NoSuchBeanDefinitionException(beanName);
        // FIXME aliases should probably be registred as services in some way
		return new String[]{};
	}

	public Object getBean(String beanName) throws BeansException {
	    return getBean(beanName, null);
    }

	public Object getBean(String beanName, Class clazz) throws BeansException {
        System.out.println("getBean name=" + beanName + " class=" +
                (clazz != null ? clazz.getName() : ""));
        ServiceReference reference = null;
        try {
            reference = getServiceReference(this.bundleContext, beanName);
        } catch (InvalidSyntaxException e) {
            throw new FatalBeanException("Cannot look up OSGi service " + beanName, e);
        }
        if (reference == null) {
            throw new NoSuchBeanDefinitionException(beanName);
        }
        // TODO services are never returned to the context,
        // don't know how to fix that with Spring
        Object bean = this.bundleContext.getService(reference);
        
        if (clazz != null && !clazz.isInstance(bean))
            throw new BeanNotOfRequiredTypeException(beanName, clazz, bean.getClass());
        
        return bean;
	}

	public Class getType(String beanName) throws NoSuchBeanDefinitionException {
	    return getBean(beanName).getClass();
    }

	public boolean isSingleton(String beanName) throws NoSuchBeanDefinitionException {
        if (!containsBean(beanName))
            throw new NoSuchBeanDefinitionException(beanName);
        // all OSGi services are supposed to be singletons
		return true;
	}

    public static ServiceReference getServiceReference(BundleContext ctx, String role)
    throws InvalidSyntaxException {
        ServiceReference result;
        String itf = getServiceInterface(role); 
        String hint = getServiceHint(role);

        if (hint == null) {
            // Single interface role
            result = ctx.getServiceReference(itf);
        } else {
            // '*' is used as wildcard character in LDAP filters, it is also used
            // as hint for the URLSourceFactory and need to be escaped
            if ("*".equals(hint))
                hint = "\\*";
            // Hinted role: create query
            String query = "(" + HINT_PROPERTY + "=" + hint + ")";
            ServiceReference[] results = ctx.getServiceReferences(itf, query);
            result = (results != null && results.length > 1) ? results[0] : null;
        }

        return result;
    }

    public static String getServiceInterface(String role) {
        int pos = role.indexOf('/');
        
        return pos == -1 ? role : role.substring(0, pos);
    }
    
    public static String getServiceHint(String role) {
        int pos = role.indexOf('/');
        return pos == -1 ? null : role.substring(pos+1);
    }
}
