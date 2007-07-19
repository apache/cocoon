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
package org.apache.cocoon.objectmodel;

import java.util.Map;

import org.apache.commons.collections.MultiMap;

/**
 * ObjectModel is a special {@link Map} that can have multiple values associated to the same key. If there is only one
 * value associated with key then ObjectModel will behave exactly as {@link Map}. If there is more than one value associated with
 * key then ObjectModel's methods will operate on {@link java.util.Collection Collections} associated with key. 
 * 
 * Another constrain is that {@link java.util.Collection} for each key is compliant with LIFO list constracts. 
 */
public interface ObjectModel extends MultiMap {
    
    public static final String CONTEXTBEAN = "contextBean";
    public static final String NAMESPACE = "namespace";

    
    /** 
     * This method behaves almost exactly as {@link MultiMap#get(Object)} method. The only difference is that value itself is returned
     * instead of {@link java.util.Collection} containing that value.
     */
    public Object get(Object key);
    
    /**
     * Marks new local context. Such mark is useful to do a clean up of entries. 
     */
    public void markLocalContext();
    
    /**
     * Cleans up entries put to ObjectModel since last {@link #markLocalContext()} call.
     */
    public void cleanupLocalContext();
}
