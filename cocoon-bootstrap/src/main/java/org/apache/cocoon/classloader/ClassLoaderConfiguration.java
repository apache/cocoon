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
package org.apache.cocoon.classloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.classloader.fam.Monitor;
import org.apache.commons.jci.listeners.NotificationListener;
import org.apache.commons.jci.stores.ResourceStore;

/**
 * The configuration for a {@link ClassLoaderFactory}.
 * @version $Id$
 * @since 2.2
 */
public class ClassLoaderConfiguration {

    protected final List includes = new ArrayList();
    protected final List excludes = new ArrayList();
    protected final List sourceDirectories = new ArrayList();
    protected final List classDirectories = new ArrayList();
    protected final List libDirectories = new ArrayList();
    protected final Map storeDirectories = new HashMap();
    private Monitor monitor;
    private NotificationListener notificationListener;

    public void addInclude(String include) {
        this.includes.add(include);
    }

    public void addExclude(String include) {
        this.excludes.add(include);
    }

    public void addSourceDirectory(String sourceDir) {
        this.sourceDirectories.add(sourceDir);
    }

    public void addClassDirectory(String include) {
        this.classDirectories.add(include);
    }

    public void addLibDirectory(String include) {
        this.libDirectories.add(include);
    }

    public List getSourceDirectories() {
        return sourceDirectories;
    }

    public List getClassDirectories() {
        return classDirectories;
    }

    public List getLibDirectories() {
        return libDirectories;
    }
    
    public List getExcludes() {
        return excludes;
    }
    
    public List getIncludes() {
        return includes;
    }

    public void addStore(String dir, ResourceStore store) {
        this.storeDirectories.put(dir, store);
    }

    public ResourceStore getStore(String dir) {
        return (ResourceStore)this.storeDirectories.get(dir);
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
        
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public void setNotificationListener(NotificationListener notificationListener) {
        this.notificationListener = notificationListener;        
    }

    public NotificationListener getNotificationListener() {
        return this.notificationListener;
    }
}
