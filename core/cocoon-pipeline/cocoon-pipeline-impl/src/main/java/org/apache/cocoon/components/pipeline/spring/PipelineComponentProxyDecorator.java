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
package org.apache.cocoon.components.pipeline.spring;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.transformation.Transformer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;


public class PipelineComponentProxyDecorator implements BeanPostProcessor {
    
    private PipelineComponentScopeHolder holder;

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Generator || bean instanceof Transformer || bean instanceof Serializer) {
            bean = Proxy.newProxyInstance(bean.getClass().getClassLoader(), getInterfaces(bean.getClass()), 
                                          new ScopeChangerProxy(bean, holder));
        }
        return bean;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    
    public PipelineComponentScopeHolder getHolder() {
        return holder;
    }

    public void setHolder(PipelineComponentScopeHolder holder) {
        this.holder = holder;
    }
    
    private class ScopeChangerProxy implements InvocationHandler {
        
        private Map beans;
        private Map destructionCallbacks;
        private PipelineComponentScopeHolder holder;
        
        private Object wrapped;
        
        public ScopeChangerProxy(Object wrapped, PipelineComponentScopeHolder holder) {
            this.wrapped = wrapped;
            this.holder = holder;
            this.beans = new HashMap();
            this.destructionCallbacks = new HashMap();
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result;
            boolean modified = false;
            try {
                if (!holder.getInScope()) {
                    holder.setParentBeans(holder.getBeans());
                    holder.setParentDestructionCallbacks(holder.getDestructionCallbacks());
                    holder.setBeans(beans);
                    holder.setDestructionCallbacks(destructionCallbacks);
                    holder.setInScope(true);
                    modified = true;
                }
                result = method.invoke(wrapped, args);
            } finally {
                if (modified) {
                    holder.setBeans(holder.getParentBeans());
                    holder.setDestructionCallbacks(holder.getParentDestructionCallbacks());
                    holder.setParentBeans(null);
                    holder.setParentDestructionCallbacks(null);
                    holder.setInScope(false);
                }
            }
            return result;
        }

    }
    
    //Copied from org.apache.cocoon.servletservice.DispatcherServlet
    
    private void getInterfaces(Set interfaces, Class clazz) {
        Class[] clazzInterfaces = clazz.getInterfaces();
        for (int i = 0; i < clazzInterfaces.length; i++) {
            //add all interfaces extended by this interface or directly
            //implemented by this class
            getInterfaces(interfaces, clazzInterfaces[i]);
        }

        //the superclazz is null if class is instanceof Object, is
        //an interface, a primitive type or void
        Class superclazz = clazz.getSuperclass();
        if (superclazz != null) {
            //add all interfaces of the superclass to the list
            getInterfaces(interfaces, superclazz);
        }

        interfaces.addAll(Arrays.asList(clazzInterfaces));
    }

    private Class[] getInterfaces(Class clazz) {
        Set interfaces = new LinkedHashSet();
        getInterfaces(interfaces, clazz);
        return (Class[]) interfaces.toArray(new Class[interfaces.size()]);
    }

}
