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

import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * Pipeline component scope that scopes objects per one pipeline component.
 *
 * @version $Id$
 * @since 2.2
 */
public final class PipelineComponentScope implements Scope {

    private PipelineComponentScopeHolder holder;

    public PipelineComponentScopeHolder getHolder() {
        return holder;
    }

    public void setHolder(PipelineComponentScopeHolder holder) {
        this.holder = holder;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#get(java.lang.String,
     * org.springframework.beans.factory.ObjectFactory)
     */
    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        Object bean = holder.getBeans().get(name);
        if (bean == null) {
            bean = objectFactory.getObject();
            holder.getBeans().put(name, bean);
            if (bean instanceof ObjectModel && holder.getInScope()) {
                //FIXME: This should be moved to separate BeanPostProcessor
                ((ObjectModel) bean).setParent((ObjectModel) holder.getParentBeans().get(name));
            }
        }
        return bean;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#getConversationId()
     */
    @Override
    public String getConversationId() {
        // There is no conversation id concept for the pipeline component scope
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#registerDestructionCallback(java.lang.String,
     * java.lang.Runnable)
     */
    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        holder.getDestructionCallbacks().put(name, callback);
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#remove(java.lang.String)
     */
    @Override
    public Object remove(String name) {
        Object bean = holder.getBeans().get(name);
        if (bean != null) {
            holder.getBeans().remove(name);
            holder.getDestructionCallbacks().remove(name);
        }
        return bean;
    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }
}
