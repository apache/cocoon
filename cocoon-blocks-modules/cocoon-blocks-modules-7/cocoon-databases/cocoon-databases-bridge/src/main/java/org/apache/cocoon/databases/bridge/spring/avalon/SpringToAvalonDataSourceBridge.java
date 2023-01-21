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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.databases.bridge.spring.avalon;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;

/**
 * This class replaces standard Avalon's DataSourceSelector to provide access to database connections
 * defined as Spring beans.
 *  
 * @version $Id$
 */
public class SpringToAvalonDataSourceBridge implements ServiceSelector {
    private ServiceSelector dataSourceSelector;
    private Map springDataSources;
    
    private Set sourcesFromDataSourceSelector;
    
    public SpringToAvalonDataSourceBridge() {
        sourcesFromDataSourceSelector = new HashSet();
    }

    public boolean isSelectable(Object policy) {
        return dataSourceSelector.isSelectable(policy) || springDataSources.containsKey(policy);
    }

    public void release(Object object) {
        if (sourcesFromDataSourceSelector.contains(object)) {
            sourcesFromDataSourceSelector.remove(object);
            dataSourceSelector.release(object);
        }
    }

    public Object select(Object policy) throws ServiceException {
        if (dataSourceSelector.isSelectable(policy)) {
            Object object = dataSourceSelector.select(policy);
            sourcesFromDataSourceSelector.add(object);
            return object;
        }
        else if (springDataSources.containsKey(policy)) {
            Object object = (DataSource) springDataSources.get(policy);
            if (!(object instanceof SpringToAvalonDataSourceWrapper))
                throw new ClassCastException("Bean with key '" + policy + "' is not SpringToAvalonDataSourceWrapper class. " +
                		"Only this wrapper is allowed when you want to use DataSources defined in Spring way in Avalon components");
            return object;
        }
        else
            return null;
    }

    public Map getSpringDataSources() {
        return springDataSources;
    }

    public void setSpringDataSources(Map springDataSources) {
        this.springDataSources = springDataSources;
    }

    public ServiceSelector getDataSourceSelector() {
        return dataSourceSelector;
    }

    public void setDataSourceSelector(ServiceSelector dataSourceSelector) {
        this.dataSourceSelector = dataSourceSelector;
    }

}
