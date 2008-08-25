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
package org.apache.cocoon.components.flow;

import java.util.ArrayList;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.SingleThreaded;
import org.apache.excalibur.source.SourceUtil;

import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.processing.ProcessInfoProvider;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * Abstract superclass for various scripting languages used by Cocoon
 * for flow control. Defines some useful behavior like the ability to
 * reload script files if they get modified (useful when doing
 * development), and passing the control to Cocoon's sitemap for
 * result page generation.
 * <p>
 * Flow intrepreters belonging to different sitemaps should be isolated. To achieve this,
 * class implements the {@link org.apache.avalon.framework.thread.SingleThreaded}. Since
 * the sitemap engine looks up the flow intepreter once at sitemap build time, this ensures
 * that each sitemap will use a different instance of this class. But that instance will
 * handle all flow calls for a given sitemap, and must therefore be thread safe.
 *
 * @since March 15, 2002
 * @version $Id$
 */
public abstract class AbstractInterpreter extends AbstractLogEnabled
                                          implements Serviceable, Contextualizable, Interpreter,
                                                     SingleThreaded, Configurable, Disposable {

    // The instance ID of this interpreter, used to identify user scopes
    private String instanceID;

    protected org.apache.avalon.framework.context.Context avalonContext;
    /**
     * List of source locations that need to be resolved.
     */
    protected ArrayList needResolve = new ArrayList();

    protected ServiceManager manager;
    protected ContinuationsManager continuationsMgr;
    protected ProcessInfoProvider processInfoProvider;
    protected ObjectModel newObjectModel;

    /** The settings of Cocoon. */
    protected Settings settings;

    /**
     * Whether reloading of scripts should be done. Specified through
     * the "reload-scripts" attribute in <code>flow.xmap</code>.
     */
    protected boolean reloadScripts;

    /**
     * Interval between two checks for modified script files. Specified
     * through the "check-time" XML attribute in <code>flow.xmap</code>.
     */
    protected long checkTime;

    /**
     * Set the unique ID for this interpreter, which can be used to distinguish user value scopes
     * attached to the session.
     */
    public void setInterpreterID(String interpreterID) {
        this.instanceID = interpreterID;
    }

    /**
     * Get the unique ID for this interpreter, which can be used to distinguish user value scopes
     * attached to the session.
     *
     * @return a unique ID for this interpreter
     */
    public String getInterpreterID() {
        return this.instanceID;
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        this.reloadScripts = config.getChild("reload-scripts").getValueAsBoolean(this.settings.isReloadingEnabled("flow"));
        this.checkTime = config.getChild("check-time").getValueAsLong(this.settings.getReloadDelay("flow"));
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager sm) throws ServiceException {
        this.manager = sm;
        this.continuationsMgr = (ContinuationsManager)sm.lookup(ContinuationsManager.ROLE);
        this.settings = (Settings)this.manager.lookup(Settings.ROLE);
        this.processInfoProvider = (ProcessInfoProvider)this.manager.lookup(ProcessInfoProvider.ROLE);
        this.newObjectModel = (ObjectModel)this.manager.lookup(ObjectModel.ROLE);
    }

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(org.apache.avalon.framework.context.Context aContext)
    throws ContextException{
        this.avalonContext = aContext;
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( this.continuationsMgr );
            this.manager.release( this.settings );
            this.manager.release( this.processInfoProvider );
            this.manager.release(this.newObjectModel);
            this.continuationsMgr = null;
            this.settings = null;
            this.processInfoProvider = null;
            this.newObjectModel = null;
            this.manager = null;
        }
    }

    /**
     * Registers a source file with the interpreter. Using this method
     * an implementation keeps track of all the script files which are
     * compiled. This allows them to reload the script files which get
     * modified on the file system.
     *
     * <p>The parsing/compilation of a script file by an interpreter
     * happens in two phases. In the first phase the file's location is
     * registered in the <code>needResolve</code> array.
     *
     * <p>The second is possible only when a Cocoon
     * <code>Environment</code> is passed to the Interpreter. This
     * allows the file location to be resolved using Cocoon's
     * <code>SourceFactory</code> class.
     *
     * <p>Once a file's location can be resolved, it is removed from the
     * <code>needResolve</code> array and placed in the
     * <code>scripts</code> hash table. The key in this hash table is
     * the file location string, and the value is a
     * DelayedRefreshSourceWrapper instance which keeps track of when
     * the file needs to re-read.
     *
     * @param source the location of the script
     *
     * @see org.apache.cocoon.environment.Environment
     * @see org.apache.cocoon.components.source.impl.DelayedRefreshSourceWrapper
     */
    public void register(String source) {
        synchronized (this) {
            needResolve.add(source);
        }
    }

    /**
     * @see org.apache.cocoon.components.flow.Interpreter#forwardTo(java.lang.String, java.lang.Object, org.apache.cocoon.components.flow.WebContinuation, org.apache.cocoon.environment.Redirector)
     */
    public void forwardTo(String uri, Object bizData,
                          WebContinuation continuation,
                          Redirector redirector)
    throws Exception {
        if (SourceUtil.indexOfSchemeColon(uri) == -1) {
            uri = "cocoon:/" + uri;
            final Map objectModel = this.processInfoProvider.getObjectModel();
            FlowHelper.setWebContinuation(objectModel, newObjectModel, continuation);
            FlowHelper.setContextObject(objectModel, newObjectModel, bizData);
            if (redirector.hasRedirected()) {
                throw new IllegalStateException("Pipeline has already been processed for this request");
            }
            // this is a hint for the redirector
            objectModel.put("cocoon:forward", "true");
            redirector.redirect(false, uri);
        } else if (SourceUtil.getScheme(uri).equals("servlet")) {
            if (redirector.hasRedirected()) {
                throw new IllegalStateException("Pipeline has already been processed for this request");
            }
            final Map objectModel = this.processInfoProvider.getObjectModel();
            FlowHelper.setWebContinuation(objectModel, newObjectModel, continuation);
            FlowHelper.setContextObject(objectModel, newObjectModel, bizData);
            redirector.redirect(false, uri);
        } else {
            throw new Exception("uri is not allowed to contain a scheme (cocoon:/ is always automatically used)");
        }
    }

    /**
     * @see org.apache.cocoon.components.flow.Interpreter#getScriptExtension()
     */
    public String getScriptExtension() {
        return null;
    }
}
