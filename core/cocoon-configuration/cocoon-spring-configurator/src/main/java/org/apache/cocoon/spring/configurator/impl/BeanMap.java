/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.spring.configurator.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * This is a map implementation collecting all beans of a specific type (class)
 * from a spring bean factory.
 * The beans are available through their configured bean id.
 *
 * The map has a lazy implementation: the beans are not searched on instantiation
 * but the first time the map is accessed. This avoids any startup ordering problems.
 *
 * By default the map searches in the bean factory it is used in and in all the parent
 * bean factories (if there are any). This behaviour can be changed by calling the
 * {@link #setCheckParent(boolean)} method.
 *
 * @version $Id$
 * @since 1.0.1
 */
public class BeanMap
    implements Map, BeanFactoryAware {

    /** The real map. */
    protected Map beanMap = new HashMap();

    /** Is the map initialized? */
    protected boolean initialized = false;

    /** The bean factory. */
    protected ListableBeanFactory beanFactory;

    /** The class for all beans in this map. */
    protected Class beanClass;

    /** Do we strip the prefix from the bean name? */
    protected boolean stripPrefix = true;

    /** Do we check the parent factories? */
    protected boolean checkParent = true;

    /** Do we check for properties? */
    protected List hasProperties = new ArrayList();

    /** Which property should we use to key the map? */
    protected String keyProperty;

    /**
     * Get all the bean's from the bean factory and put them into
     * a map using their id.
     * @param beanNames The bean names to load.
     */
    protected void load(Set beanNames) {
        final String prefix1 = this.beanClass.getName() + '.';
        final String prefix2 = this.beanClass.getName() + '/';
        final Iterator i = beanNames.iterator();
        while ( i.hasNext() ) {
            final String beanName = (String)i.next();
            String key = beanName;
            if ( this.stripPrefix && (beanName.startsWith(prefix1) || beanName.startsWith(prefix2)) ) {
                key = key.substring(prefix1.length());
            }
            if(this.hasProperties.size() > 0) {
                final Object bean = this.beanFactory.getBean(beanName);
                final BeanWrapperImpl wrapper = new BeanWrapperImpl(bean);
                boolean isOk = true;
                final Iterator iter = this.hasProperties.iterator();
                while(iter.hasNext()) {
                    final String propName = (String)iter.next();
                    if(!wrapper.isReadableProperty(propName)) {
                        isOk = false;
                    }
                }
                if(isOk) {
                    if( this.keyProperty != null && this.keyProperty.length() > 0 && wrapper.isReadableProperty(this.keyProperty)) {
                        key = (String)wrapper.getPropertyValue(this.keyProperty);
                    }
                    this.beanMap.put(key, bean);
                }
            } else {
                final Object bean = this.beanFactory.getBean(beanName);
                final BeanWrapperImpl wrapper = new BeanWrapperImpl(bean);
                if( this.keyProperty != null && this.keyProperty.length() > 0 && wrapper.isReadableProperty(this.keyProperty)) {
                    key = (String)wrapper.getPropertyValue(this.keyProperty);
                }
                this.beanMap.put(key, bean);
            }
        }
    }

    /**
     * Check if the bean is already initialized.
     * If not, the bean's are searched in the bean factory.
     */
    protected void checkInit() {
        if ( !this.initialized ) {
            synchronized (this) {
                if ( !this.initialized ) {
                    // although it is unlikely, but if this bean is used outside spring
                    // it will just contain an empty map
                    if ( this.beanFactory != null ) {
                        final Set beanNames = new HashSet();
                        this.getBeanNames(this.beanFactory, beanNames);
                        this.load(beanNames);
                    }
                    this.initialized = true;
                }
            }
        }
    }

    /**
     * Get all bean names for the given type.
     * @param factory The bean factory.
     * @param beanNames The set containing the resulting bean names.
     */
    protected void getBeanNames(ListableBeanFactory factory, Set beanNames) {
        // check parent first
        if ( this.checkParent ) {
            if ( factory instanceof HierarchicalBeanFactory ) {
                if ( ((HierarchicalBeanFactory)factory).getParentBeanFactory() != null ) {
                    this.getBeanNames((ListableBeanFactory)((HierarchicalBeanFactory)factory).getParentBeanFactory(), beanNames);
                }
            }
        }
        // get all bean names for our class
        final String[] names = factory.getBeanNamesForType(this.beanClass);
        for (int i = 0; i < names.length; i++) {
            beanNames.add(names[i]);
        }
    }

    /**
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory factory) throws BeansException {
        if ( !(factory instanceof ListableBeanFactory) ) {
            throw new BeanDefinitionStoreException("BeanFactory must be listable.");
        }
        this.beanFactory = (ListableBeanFactory)factory;
    }

    public void setStripPrefix(boolean stripPrefix) {
        this.stripPrefix = stripPrefix;
    }

    public void setCheckParent(boolean checkParent) {
        this.checkParent = checkParent;
    }

    public void setHasProperties(String pHasProperties) {
        final StringTokenizer tokenizer = new StringTokenizer(pHasProperties, " \t\n\r\f,");
        final List propNames = new ArrayList();
        while(tokenizer.hasMoreTokens()) {
            propNames.add( tokenizer.nextToken() );
        }
        this.hasProperties = propNames;
    }

    public void setKeyProperty(String pKeyProperty) {
        this.keyProperty = pKeyProperty;
    }

    public void setType(Class typeClass) {
        this.beanClass = typeClass;
    }

    /**
     * @see java.util.Map#clear()
     */
    public void clear() {
        // no need to call checkInit as we clear the map!
        this.initialized = true;
        this.beanMap.clear();
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        this.checkInit();
        return this.beanMap.containsKey(key);
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        this.checkInit();
        return this.beanMap.containsValue(value);
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
        this.checkInit();
        return this.beanMap.entrySet();
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        this.checkInit();
        return this.beanMap.get(key);
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        this.checkInit();
        return this.beanMap.isEmpty();
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
        this.checkInit();
        return this.beanMap.keySet();
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value) {
        this.checkInit();
        return this.beanMap.put(key, value);
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map t) {
        this.checkInit();
        this.beanMap.putAll(t);
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key) {
        this.checkInit();
        return this.beanMap.remove(key);
    }

    /**
     * @see java.util.Map#size()
     */
    public int size() {
        this.checkInit();
        return this.beanMap.size();
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection values() {
        this.checkInit();
        return this.beanMap.values();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        this.checkInit();
        return this.beanMap.equals(obj);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        this.checkInit();
        return this.beanMap.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        this.checkInit();
        return this.beanMap.toString();
    }
}
