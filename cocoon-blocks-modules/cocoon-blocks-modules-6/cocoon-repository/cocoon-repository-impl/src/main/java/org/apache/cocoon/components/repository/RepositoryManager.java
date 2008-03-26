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
package org.apache.cocoon.components.repository;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.repository.helpers.CredentialsToken;
import org.apache.cocoon.util.AbstractLogEnabled;


/**
 * A factory component to create instances of repositories.
 *
 * @version $Id$
 */
public class RepositoryManager extends AbstractLogEnabled
                               implements Serviceable, Disposable, Configurable, ThreadSafe {

    /** The Avalon role name */
    public static final String ROLE = RepositoryManager.class.getName();
    
    /* The ServiceManager */
    private ServiceManager manager;

    /* A HashMap holding the repositories configurations */
    private Map repos = new HashMap();

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration) throws ConfigurationException {

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("configuring repository manager");
        }

        Configuration[] children = configuration.getChildren();
        for (int i = 0; i < children.length; i++) {

            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("found repository: " + children[i].getAttribute("class"));
            }
            this.repos.put(children[i].getAttribute("name"), children[i]);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        this.manager = null;
    }

    /**
     * get instance of repository.
     * 
     * @param hint  identifies the repository implementation to load.
     * @param credentials  the user credentials the repository instance is initialized with.
     * @return  the repository instance.
     */
    public Repository getRepository(String hint, CredentialsToken credentials) throws ProcessingException {

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("get repository for: " + hint);
        }

        String className = null;
        try {
            Configuration repoConfiguration = (Configuration) this.repos.get(hint);
            className = repoConfiguration.getAttribute("class");
            Class repoClass = Class.forName(className);

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Loading class" + className);
            }

            Repository repo = (Repository) repoClass.newInstance();
            LifecycleHelper.setupComponent(repo,
                                           getLogger(),
                                           null,
                                           this.manager,
                                           repoConfiguration);
        
            repo.setCredentials(credentials);
            return repo;

        } catch (ConfigurationException e) {
            throw new ProcessingException("Could not get configuration for " + hint, e);
        } catch (ClassNotFoundException e) {
            throw new ProcessingException("Could not load class " + className, e);
        } catch (InstantiationException e) {
            throw new ProcessingException("Could not instantiate class " + className, e);
        } catch (IllegalAccessException e) {
            throw new ProcessingException("Could not instantiate class " + className, e);
        } catch (Exception e) {
            throw new ProcessingException("Could not setup component " + className, e);
        }
    }
}
