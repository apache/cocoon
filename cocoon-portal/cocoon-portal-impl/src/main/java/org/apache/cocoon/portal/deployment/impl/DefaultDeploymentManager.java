/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.deployment.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.thread.RunnableManager;
import org.apache.cocoon.portal.deployment.DeploymentEvent;
import org.apache.cocoon.portal.deployment.DeploymentException;
import org.apache.cocoon.portal.deployment.DeploymentManager;
import org.apache.cocoon.portal.deployment.DeploymentObject;
import org.apache.cocoon.portal.deployment.DeploymentStatus;
import org.apache.cocoon.portal.deployment.UndeploymentEvent;
import org.apache.cocoon.portal.impl.AbstractComponent;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;

/**
 * This is the default deployment manager scanning a directory for artifacts to deploy.
 *
 * TODO - Undeployment is not covered yet
 * TODO - Handling of ignored files is not really covered yet
 * TODO - Handling of sources other than files is not implemented yet.
 *
 * @see DeploymentManager 
 * @version $Id$
 */
public class DefaultDeploymentManager
    extends AbstractComponent
    implements DeploymentManager, Parameterizable, Startable, Runnable {

    /** Wait ten seconds before scanning. */
    protected static final int STARTUP_DELAY = 10 * 1000;

    /** Source resolver. */
    protected SourceResolver resolver;

    /** Are we already started? */
    protected boolean started = false;

    /** The threadpool name to be used for daemon thread. */
    protected String threadPoolName = "daemon";

    /** Do we enable hot deployment? */
    protected boolean hotDeployment = true;

    /** The delay between each scan task. Default is one minute. */
    protected long scanningDelay = 60 * 1000;

    /** All locations to look for deployable artifacts. */
    protected String[] deploymentUris;

    /** These files have been ignored during the last scan. */
    protected final Map failedArtifacts;

    /** Already deployed artifacts. */
    protected final List deployedArtifacts;

    /**
     * Default Constructor.
     */
    public DefaultDeploymentManager() {
        this.failedArtifacts = new HashMap();
        this.deployedArtifacts = new ArrayList();
    }

    /**
     * @see org.apache.cocoon.portal.impl.AbstractComponent#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.resolver);
            this.resolver = null;
        }
        super.dispose();
    }

    /**
     * @see org.apache.cocoon.portal.impl.AbstractComponent#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }

    /**
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) throws ParameterException {
        this.scanningDelay = parameters.getParameterAsLong("scanning-delay", this.scanningDelay);
        this.hotDeployment = parameters.getParameterAsBoolean("hot-deployment", this.hotDeployment);
        final String deploymentDirs = parameters.getParameter("deployment-sources", "conf/deploy");
        final StringTokenizer st = new StringTokenizer(deploymentDirs, ",");
        this.deploymentUris = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            this.deploymentUris[i] = st.nextToken();
            i++;
        }
        this.threadPoolName = parameters.getParameter("thread-pool-name", this.threadPoolName);
    }

    /**
     * Test the provided locations.
     */
    protected void testSources()
    throws IOException {
        if ( this.getLogger().isInfoEnabled() ) {
            this.getLogger().info("Testing configured deployment sources.");
        }
        for (int i = 0; i < this.deploymentUris.length; i++) {
            Source source = null;
            try {
                source = this.resolver.resolveURI(this.deploymentUris[i]);
                if (!source.exists()) {
                    this.getLogger().warn("Deployment source '" + source.getURI() + 
                                          "' does not exist. It will be ignored for deployment.");
                    this.deploymentUris[i] = null;
                } else if ( !(source instanceof TraversableSource) ) {
                    this.getLogger().warn("Deployment source '" + source.getURI() +
                                          "' is not a directory. It will be ignored for deployment.");
                    this.deploymentUris[i] = null;
                } else {
                    // use absolute url
                    this.deploymentUris[i] = source.getURI();
                }
            } finally {
                this.resolver.release(source);
            }
        }        
    }

    /**
     * @see org.apache.cocoon.portal.impl.AbstractComponent#initialize()
     */
    public void initialize()
    throws Exception {
        super.initialize();
        if ( this.getLogger().isInfoEnabled() ) {
            this.getLogger().info("Starting auto deployment service: " + this.getClass().getName());
            this.getLogger().info("Hot-Deployment: " + this.hotDeployment);
            this.getLogger().info("Deployment scanning delay: " + this.scanningDelay);
            if (this.scanningDelay < 1) {
                this.getLogger().info("Scanning delay set to " + this.scanningDelay
                                    + " has disabled automatic scanning of deployment directories.");
            }
        }

        if (this.scanningDelay > 0) {
            this.testSources();
        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Startable#start()
     */
    public void start() throws Exception {
        if ( this.scanningDelay > 0 ) {
            if ( this.hotDeployment ) {
                this.started = true;
                RunnableManager runnableManager = null;
                try {
                    runnableManager = (RunnableManager)this.manager.lookup(RunnableManager.ROLE);
                    runnableManager.execute(this.threadPoolName, this, STARTUP_DELAY);
                } finally {
                    this.manager.release(runnableManager);
                }
                if ( this.getLogger().isInfoEnabled() ) {
                    this.getLogger().info("Deployment scanner successfuly started!");
                }
            } else {
                Thread.sleep(STARTUP_DELAY);
                this.scan();                
            }
        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Startable#stop()
     */
    public void stop() throws Exception {
        if ( this.started ) {
            if ( this.getLogger().isInfoEnabled() ) {
                this.getLogger().info("Deployment scanner stopped.");
            }
            this.started = false;
        }
    }

    /**
     * @see org.apache.cocoon.portal.deployment.DeploymentManager#deploy(org.apache.excalibur.source.Source)
     */
    public synchronized DeploymentStatus deploy(Source source)
    throws DeploymentException {
        DeploymentObject deploymentObject = new DefaultDeploymentObject(source);
        DeploymentEvent event = null;
        try {
            event = new DeploymentEventImpl(deploymentObject);
            this.portalService.getEventManager().send(event);
        } finally {
            if ( deploymentObject != null ) {
                try {
                    deploymentObject.close();
                } catch (IOException e) {                    
                }
            }
        }
        return event;
    }

    /**
     * @see org.apache.cocoon.portal.deployment.DeploymentManager#undeploy(String)
     */
    public synchronized DeploymentStatus undeploy(String uri)
    throws DeploymentException {
        UndeploymentEvent event = null;
        event = new UndeploymentEventImpl(uri);
        this.portalService.getEventManager().send(event);

        return event;
    }

    /**
     * @see org.apache.cocoon.portal.deployment.DeploymentManager#scan()
     */
    public synchronized void scan() {
        Collection deploymentArtifacts = this.getAllDeploymentArtifactSources();
        Iterator i = deploymentArtifacts.iterator();
        final List newlyProcessedArtifacts = new ArrayList();
        try {
            while ( i.hasNext() ) {
                // check for new deployment
                final Source current = (Source)i.next();
                newlyProcessedArtifacts.add(current.getURI());
                if (!ignoreSource(current) && !this.deployedArtifacts.contains(current.getURI()) ) {
                    DeploymentStatus status = null;
                    Exception de = null;
                    try {
                        status = this.deploy(current);
                    } catch (Exception e) {                    
                        de = e;
                    }
                    
                    if ( status != null
                         && status.getStatus() == DeploymentStatus.STATUS_OKAY ) {
                        if ( this.getLogger().isInfoEnabled() ) {
                            this.getLogger().info("Deployed source " + current.getURI());
                        }
                        this.deployedArtifacts.add(current.getURI());
                    } else {
                        if ( de != null ) {
                            this.getLogger().error("Failure deploying " + current.getURI(), de);                            
                        } else if (status == null
                                   || status.getStatus() == DeploymentStatus.STATUS_EVAL) {
                            this.getLogger().warn("Unrecognized source: " + current.getURI());
                        } else {
                            this.getLogger().error("Failure deploying " + current.getURI());
                        }
                        this.failedArtifacts.put(current.getURI(), new Long(current.getLastModified()));
                    }
                }
            }
        } finally {
            // release everything
            this.releaseSources(deploymentArtifacts);
        }
        // now check for undeployment
        i = this.deployedArtifacts.iterator();
        while ( i.hasNext() ) {
            final String uri = (String)i.next();
            if ( !newlyProcessedArtifacts.contains(uri) ) {
                // remove this artifact from the deployed list, even if undeployment fails
                i.remove();
                DeploymentStatus status = null;
                Exception de = null;
                try {
                    status = this.undeploy(uri);
                } catch (Exception e) {                    
                    de = e;
                }
                if ( status != null
                        && status.getStatus() == DeploymentStatus.STATUS_OKAY ) {
                    if ( this.getLogger().isInfoEnabled() ) {
                        this.getLogger().info("Undeployed source " + uri);
                    }
                } else {
                    if ( de != null ) {
                        this.getLogger().error("Failure undeploying " + uri, de);                            
                    } else if (status == null
                               || status.getStatus() == DeploymentStatus.STATUS_EVAL) {
                        this.getLogger().warn("Unrecognized deployed source: " + uri);
                    } else {
                        this.getLogger().error("Failure deploying " + uri);
                    }
                }
            }
        }
    }

    protected boolean ignoreSource(Source source) {
        Long previousModified = (Long)this.failedArtifacts.get(source.getURI());
        if (previousModified != null) {
            if (previousModified.longValue() != source.getLastModified()) {
                this.failedArtifacts.remove(source.getURI());
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * This method scans all deployment sources and returns a list of all
     * found artifacts. 
     */
    protected Collection getAllDeploymentArtifactSources() {
        final ArrayList sourceList = new ArrayList();
        boolean release = true;
        try {
            for (int i = 0; i < this.deploymentUris.length; i++) {
                Source source = null;
                try {
                    if ( this.deploymentUris[i] != null ) {
                        source = this.resolver.resolveURI(this.deploymentUris[i]);
                        if ( source.exists() && source instanceof TraversableSource ) {
                            final TraversableSource ts = (TraversableSource)source;
                            sourceList.addAll(ts.getChildren());
                        }
                    }
                } catch (IOException ioe) {
                    this.getLogger().warn("Exception during scanning of " + this.deploymentUris[i], ioe);
                } finally {
                    this.resolver.release(source);
                }
            }
            release = false;
        } finally {
            // release all sources in case of an error
            if ( release ) {
                this.releaseSources(sourceList);
            }
        }

        return sourceList;
    }

    protected void releaseSources(Collection c) {
        final Iterator i = c.iterator();
        while ( i.hasNext() ) {
            final Source current = (Source)i.next();
            this.resolver.release(current);
        }
        c.clear();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
       while (this.started) {
            this.scan();
            try {
                Thread.sleep(this.scanningDelay);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}