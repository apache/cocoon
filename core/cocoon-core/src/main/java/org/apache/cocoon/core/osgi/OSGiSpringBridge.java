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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * The {@link OSGiSpringBridge} is used to provide OSGi services as Spring beans.
 * It can also be used as parent bean factory for {@link OSGiSpringECMFactory} services.
 * 
 * @version $Id$
 */
public class OSGiSpringBridge implements BeanFactory {

	public boolean containsBean(String arg0) {
		return false;
	}

	public String[] getAliases(String arg0) throws NoSuchBeanDefinitionException {
		return null;
	}

	public Object getBean(String arg0) throws BeansException {
		return null;
	}

	public Object getBean(String arg0, Class arg1) throws BeansException {
		return null;
	}

	public Class getType(String arg0) throws NoSuchBeanDefinitionException {
		return null;
	}

	public boolean isSingleton(String arg0) throws NoSuchBeanDefinitionException {
		return false;
	}

}
