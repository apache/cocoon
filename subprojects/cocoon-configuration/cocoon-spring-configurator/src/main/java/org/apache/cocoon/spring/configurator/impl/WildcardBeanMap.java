/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.spring.configurator.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.PatternMatchUtils;

/**
 * Similar to the {@link BeanMap} it collects beans from a {@link BeanFactory}. But instead of
 * checking the type of the bean, its bean name is matched against a wildcard expression (e.g.
 * <code>some.beans.*</code>).
 *
 * @see BeanMap
 */
public class WildcardBeanMap extends BeanMap {

    protected String wildcard;

    public String getWildcard() {
        return this.wildcard;
    }

    public void setWildcard(String wildcard) {
        this.wildcard = wildcard;
    }

    /**
     * Find all beans that match the wildcard expression. Expression resolving is done based on the
     * default Spring wildcard pattern matching
     * {@link PatternMatchUtils#simpleMatch(String, String)}.
     *
     * @see org.apache.cocoon.spring.configurator.impl.BeanMap#lookupBeans(org.springframework.beans.factory.ListableBeanFactory)
     */
    @Override
    protected String[] lookupBeans(ListableBeanFactory factory) {
        List<String> filteredBeanNames = new ArrayList<String>();

        for (String beanName : factory.getBeanNamesForType(null)) {
            if (PatternMatchUtils.simpleMatch(this.wildcard, beanName)) {
                filteredBeanNames.add(beanName);
            }
        }

        return filteredBeanNames.toArray(new String[0]);
    }

    @Override
    protected Object stripPrefix(String beanName) {
        return beanName;
    }
}
