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
package org.apache.cocoon.el.objectmodel;

import java.util.Map;

import org.apache.cocoon.el.util.MultiMap;

/**
 * ObjectModel is a special {@link Map} that cannot be modified using standard {@link Map} methods, except
 * {@link #put(Object, Object)} method.
 */
public interface ObjectModel extends Map {

    String ROLE = ObjectModel.class.getName();

    String CONTEXTBEAN = "contextBean";

    String NAMESPACE = "namespace";

    String PARAMETERS_PATH = "cocoon/parameters";

    /**
     * @return a {@link MultiMap} that contains all stored values in all context in LIFO-compliant order.
     *         Returned {@link MultiMap} is {@link org.apache.commons.collections.Unmodifiable}.
     */
    MultiMap getAll();

    /**
     * Works exactly the same way as {@link Map#put(Object, Object)} but previous value associated to <code>key</code>
     * is not lost in a case {@link #markLocalContext()} was called before. The previous value is stored and can be
     * recovered by calling {@link #markLocalContext()}.
     */
    Object put(Object key, Object value);

    /**
     * @see #put(Object, Object)
     */
    void putAll(Map mapToCopy);

    /**
     * Marks new local context. Such mark is useful to do a clean up of entries.
     */
    void markLocalContext();

    /**
     * Cleans up entries put to ObjectModel since last {@link #markLocalContext()} call.
     */
    void cleanupLocalContext();

    /**
     * Puts object at certain <code>path</code>. Each segment of path is separated by "/" symbol. This method
     * supports only traversing through objects implementing {@link Map} interface. If certain segment does not exist it
     * will be created automatically.
     *
     * @param path
     *            where the <code>value</code> should be put at
     * @param value that is going to be put
     */
    void putAt(String path, Object value);

    /**
     * <p>
     * Copies properties (both static and dynamic) of current context bean to the Object Model. The method is useful
     * when you want, for example, to access properties of context bean in JEXL expression and omit
     * <code>contextBean.</code> part.
     * </p>
     *
     * @see org.apache.commons.jxpath.JXPathBeanInfo.isDynamic() for description of static and dynamic properties
     */
    void fillContext();

    /**
     * <p>Sets parent object model so newly created instance can inherit values from parent but cannot modify it.</p>
     *
     * @param parentObjectModel
     */
    void setParent(ObjectModel parentObjectModel);
}
