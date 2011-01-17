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
package org.apache.cocoon.tools.rcl.springreloader;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * This implementation of a {@link ConfigurableWebApplicationContext} is completely synchronized. It wraps all calls and
 * delegates them to an internally managed ConfigurableWebApplicationContext instance.
 * 
 * Additionally it provides a {@link #reload()} method which exchanges the internal application context with a newly
 * created one.
 * 
 * @version $Id$
 */
public class SynchronizedConfigureableWebApplicationContext implements ConfigurableWebApplicationContext {

    private ConfigurableWebApplicationContext appContext;

    public SynchronizedConfigureableWebApplicationContext() throws BeansException {
        try {
            this.appContext = BeanUtils.instantiateClass(XmlWebApplicationContext.class);
        } catch (BeanInstantiationException e) {
            throw new RuntimeException("Can't create Spring application context.", e);
        }
    }

    public synchronized void addApplicationListener(ApplicationListener arg0) {
        this.appContext.addApplicationListener(arg0);
    }

    public synchronized void addBeanFactoryPostProcessor(BeanFactoryPostProcessor arg0) {
        this.appContext.addBeanFactoryPostProcessor(arg0);
    }

    public synchronized void close() {
        this.appContext.close();
    }

    public synchronized boolean containsBean(String arg0) {
        return this.appContext.containsBean(arg0);
    }

    public synchronized boolean containsBeanDefinition(String arg0) {
        return this.appContext.containsBeanDefinition(arg0);
    }

    public synchronized boolean containsLocalBean(String arg0) {
        return this.appContext.containsLocalBean(arg0);
    }

    public synchronized <A extends Annotation> A findAnnotationOnBean(String arg0, Class<A> arg1) {
        return this.appContext.findAnnotationOnBean(arg0, arg1);
    }

    public synchronized String[] getAliases(String arg0) {
        return this.appContext.getAliases(arg0);
    }

    public synchronized AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
        return this.appContext.getAutowireCapableBeanFactory();
    }

    public synchronized <T> T getBean(Class<T> requiredType) throws BeansException {
        return this.appContext.getBean(requiredType);
    }

    public synchronized Object getBean(String arg0) throws BeansException {
        return this.appContext.getBean(arg0);
    }

    public synchronized <T> T getBean(String arg0, Class<T> arg1) throws BeansException {
        return this.appContext.getBean(arg0, arg1);
    }

    public Object getBean(String arg0, Object... arg1) throws BeansException {
        return this.appContext.getBean(arg0, arg1);
    }

    public synchronized int getBeanDefinitionCount() {
        return this.appContext.getBeanDefinitionCount();
    }

    public synchronized String[] getBeanDefinitionNames() {
        return this.appContext.getBeanDefinitionNames();
    }

    public synchronized ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException {
        return this.appContext.getBeanFactory();
    }

    public synchronized String[] getBeanNamesForType(Class arg0) {
        return this.appContext.getBeanNamesForType(arg0);
    }

    public synchronized String[] getBeanNamesForType(Class arg0, boolean arg1, boolean arg2) {
        return this.appContext.getBeanNamesForType(arg0, arg1, arg2);
    }

    public <T> Map<String, T> getBeansOfType(Class<T> arg0) throws BeansException {
        return this.appContext.getBeansOfType(arg0);
    }

    public <T> Map<String, T> getBeansOfType(Class<T> arg0, boolean arg1, boolean arg2) throws BeansException {
        return this.appContext.getBeansOfType(arg0, arg1, arg2);
    }

    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> arg0) throws BeansException {
        return this.appContext.getBeansWithAnnotation(arg0);
    }

    public synchronized ClassLoader getClassLoader() {
        return this.appContext.getClassLoader();
    }

    public synchronized String[] getConfigLocations() {
        return this.appContext.getConfigLocations();
    }

    public synchronized String getDisplayName() {
        return this.appContext.getDisplayName();
    }

    public synchronized String getId() {
        return this.appContext.getId();
    }

    public synchronized String getMessage(MessageSourceResolvable arg0, Locale arg1) throws NoSuchMessageException {
        return this.appContext.getMessage(arg0, arg1);
    }

    public synchronized String getMessage(String arg0, Object[] arg1, Locale arg2) throws NoSuchMessageException {
        return this.appContext.getMessage(arg0, arg1, arg2);
    }

    public synchronized String getMessage(String arg0, Object[] arg1, String arg2, Locale arg3) {
        return this.appContext.getMessage(arg0, arg1, arg2, arg3);
    }

    public synchronized String getNamespace() {
        return this.appContext.getNamespace();
    }

    public synchronized ApplicationContext getParent() {
        return this.appContext.getParent();
    }

    public synchronized BeanFactory getParentBeanFactory() {
        return this.appContext.getParentBeanFactory();
    }

    public synchronized Resource getResource(String arg0) {
        return this.appContext.getResource(arg0);
    }

    public synchronized Resource[] getResources(String arg0) throws IOException {
        return this.appContext.getResources(arg0);
    }

    public synchronized ServletConfig getServletConfig() {
        return this.appContext.getServletConfig();
    }

    public synchronized ServletContext getServletContext() {
        return this.appContext.getServletContext();
    }

    public synchronized long getStartupDate() {
        return this.appContext.getStartupDate();
    }

    public synchronized Class getType(String arg0) throws NoSuchBeanDefinitionException {
        return this.appContext.getType(arg0);
    }

    public synchronized boolean isActive() {
        return this.appContext.isActive();
    }

    public synchronized boolean isPrototype(String arg0) throws NoSuchBeanDefinitionException {
        return this.appContext.isPrototype(arg0);
    }

    public synchronized boolean isRunning() {
        return this.appContext.isRunning();
    }

    public synchronized boolean isSingleton(String arg0) throws NoSuchBeanDefinitionException {
        return this.appContext.isSingleton(arg0);
    }

    public synchronized boolean isTypeMatch(String arg0, Class arg1) throws NoSuchBeanDefinitionException {
        return this.appContext.isTypeMatch(arg0, arg1);
    }

    public synchronized void publishEvent(ApplicationEvent arg0) {
        this.appContext.publishEvent(arg0);
    }

    public synchronized void refresh() throws BeansException, IllegalStateException {
        this.appContext.refresh();
    }

    public synchronized void registerShutdownHook() {
        this.appContext.registerShutdownHook();
    }

    public synchronized void reload() {
        ConfigurableWebApplicationContext newAppContext = null;

        try {
            newAppContext = BeanUtils.instantiateClass(XmlWebApplicationContext.class);
        } catch (BeanInstantiationException e) {
            throw new RuntimeException("Can't create Spring application context.", e);
        }

        newAppContext.setParent(this.appContext.getParent());
        newAppContext.setServletContext(this.appContext.getServletContext());

        this.appContext.close();
        this.appContext = newAppContext;
        this.appContext.refresh();
    }

    public synchronized void setConfigLocation(String configLocation) {
        this.appContext.setConfigLocation(configLocation);
    }

    public synchronized void setConfigLocations(String[] arg0) {
        this.appContext.setConfigLocations(arg0);
    }

    public void setId(String arg0) {
        this.appContext.setId(arg0);
    }

    public synchronized void setNamespace(String arg0) {
        this.appContext.setNamespace(arg0);
    }

    public synchronized void setParent(ApplicationContext arg0) {
        this.appContext.setParent(arg0);
    }

    public synchronized void setServletConfig(ServletConfig arg0) {
        this.appContext.setServletConfig(arg0);
    }

    public synchronized void setServletContext(ServletContext arg0) {
        this.appContext.setServletContext(arg0);
    }

    public synchronized void start() {
        this.appContext.start();
    }

    public synchronized void stop() {
        this.appContext.stop();
    }
}