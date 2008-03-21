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
 * This implementation of a {@link ConfigurableWebApplicationContext} is completly synchronized. It wraps
 * all calls and delegates them to an internally managed ConfigurableWebApplicationContext instance.
 *
 * Additionally it provides a {@link #reload()} method which exchanges the interal application context with a
 * newly created one.
 *
 * @version $Id$
 */
public class SynchronizedConfigureableWebApplicationContext implements ConfigurableWebApplicationContext {

    private ConfigurableWebApplicationContext appContext;

    public SynchronizedConfigureableWebApplicationContext() throws BeansException {
        try {
            appContext = (ConfigurableWebApplicationContext)
                    BeanUtils.instantiateClass(XmlWebApplicationContext.class);
        } catch (BeanInstantiationException e) {
            throw new RuntimeException("Can't create Spring application context.", e);
        }
    }

    public synchronized void reload() {
        ConfigurableWebApplicationContext newAppContext = null;
        try {
            newAppContext = (ConfigurableWebApplicationContext)
                BeanUtils.instantiateClass(XmlWebApplicationContext.class);
        } catch (BeanInstantiationException e) {
            throw new RuntimeException("Can't create Spring application context.", e);
        }
        newAppContext.setParent(appContext.getParent());
        newAppContext.setServletContext(appContext.getServletContext());
        appContext.close();
        appContext = newAppContext;
        appContext.refresh();
    }

    public synchronized boolean containsBean(String arg0) {
        return appContext.containsBean(arg0);
    }

    public synchronized boolean containsBeanDefinition(String arg0) {
        return appContext.containsBeanDefinition(arg0);
    }

    public synchronized boolean containsLocalBean(String arg0) {
        return appContext.containsLocalBean(arg0);
    }

    public synchronized String[] getAliases(String arg0) {
        return appContext.getAliases(arg0);
    }

    public synchronized AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
        return appContext.getAutowireCapableBeanFactory();
    }

    public synchronized Object getBean(String arg0, Class arg1) throws BeansException {
        return appContext.getBean(arg0, arg1);
    }

    public synchronized Object getBean(String arg0) throws BeansException {
        return appContext.getBean(arg0);
    }

    // Make the class compilable with Spring 2.1
    // FIXME: delegate to appContext, when updating to Spring 2.1
    public Object getBean(String arg0, Object[] arg1) throws BeansException {
        throw new UnsupportedOperationException();
        //return appContext.getBean(arg0, arg1);
    }

    public synchronized int getBeanDefinitionCount() {
        return appContext.getBeanDefinitionCount();
    }

    public synchronized String[] getBeanDefinitionNames() {
        return appContext.getBeanDefinitionNames();
    }

    public synchronized String[] getBeanNamesForType(Class arg0, boolean arg1, boolean arg2) {
        return appContext.getBeanNamesForType(arg0, arg1, arg2);
    }

    public synchronized String[] getBeanNamesForType(Class arg0) {
        return appContext.getBeanNamesForType(arg0);
    }

    public synchronized Map getBeansOfType(Class arg0, boolean arg1, boolean arg2) throws BeansException {
        return appContext.getBeansOfType(arg0, arg1, arg2);
    }

    public synchronized Map getBeansOfType(Class arg0) throws BeansException {
        return appContext.getBeansOfType(arg0);
    }

    public synchronized ClassLoader getClassLoader() {
        return appContext.getClassLoader();
    }

    public synchronized String getDisplayName() {
        return appContext.getDisplayName();
    }

    public synchronized String getMessage(MessageSourceResolvable arg0, Locale arg1) throws NoSuchMessageException {
        return appContext.getMessage(arg0, arg1);
    }

    public synchronized String getMessage(String arg0, Object[] arg1, Locale arg2) throws NoSuchMessageException {
        return appContext.getMessage(arg0, arg1, arg2);
    }

    public synchronized String getMessage(String arg0, Object[] arg1, String arg2, Locale arg3) {
        return appContext.getMessage(arg0, arg1, arg2, arg3);
    }

    public synchronized ApplicationContext getParent() {
        return appContext.getParent();
    }

    public synchronized BeanFactory getParentBeanFactory() {
        return appContext.getParentBeanFactory();
    }

    public synchronized Resource getResource(String arg0) {
        return appContext.getResource(arg0);
    }

    public synchronized Resource[] getResources(String arg0) throws IOException {
        return appContext.getResources(arg0);
    }

    public synchronized long getStartupDate() {
        return appContext.getStartupDate();
    }

    public synchronized Class getType(String arg0) throws NoSuchBeanDefinitionException {
        return appContext.getType(arg0);
    }

    public synchronized boolean isPrototype(String arg0) throws NoSuchBeanDefinitionException {
        return appContext.isPrototype(arg0);
    }

    public synchronized boolean isSingleton(String arg0) throws NoSuchBeanDefinitionException {
        return appContext.isSingleton(arg0);
    }

    public synchronized boolean isTypeMatch(String arg0, Class arg1) throws NoSuchBeanDefinitionException {
        return appContext.isTypeMatch(arg0, arg1);
    }

    public synchronized void publishEvent(ApplicationEvent arg0) {
        appContext.publishEvent(arg0);
    }

    public synchronized ServletContext getServletContext() {
        return appContext.getServletContext();
    }

    public synchronized String[] getConfigLocations() {
        return appContext.getConfigLocations();
    }

    public synchronized String getNamespace() {
        return appContext.getNamespace();
    }

    public synchronized ServletConfig getServletConfig() {
        return appContext.getServletConfig();
    }

    public synchronized void setConfigLocations(String[] arg0) {
        appContext.setConfigLocations(arg0);

    }

    public synchronized void setNamespace(String arg0) {
        appContext.setNamespace(arg0);
    }

    public synchronized void setServletConfig(ServletConfig arg0) {
        appContext.setServletConfig(arg0);
    }

    public synchronized void setServletContext(ServletContext arg0) {
        appContext.setServletContext(arg0);

    }

    public synchronized void addApplicationListener(ApplicationListener arg0) {
        appContext.addApplicationListener(arg0);
    }

    public synchronized void addBeanFactoryPostProcessor(BeanFactoryPostProcessor arg0) {
        appContext.addBeanFactoryPostProcessor(arg0);
    }

    public synchronized void close() {
        appContext.close();
    }

    public synchronized ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException {
        return appContext.getBeanFactory();
    }

    public synchronized boolean isActive() {
        return appContext.isActive();
    }

    public synchronized void refresh() throws BeansException, IllegalStateException {
        appContext.refresh();
    }

    public synchronized void registerShutdownHook() {
        appContext.registerShutdownHook();
    }

    public synchronized void setParent(ApplicationContext arg0) {
        appContext.setParent(arg0);
    }

    public synchronized boolean isRunning() {
        return appContext.isRunning();
    }

    public synchronized void start() {
        appContext.start();
    }

    public synchronized void stop() {
        appContext.stop();
    }
}