/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.components.flow;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.SingleThreaded;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.flow.util.PipelineUtil;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.Redirector;
import org.apache.excalibur.source.SourceUtil;

/**
 * Abstract superclass for various scripting languages used by Cocoon
 * for flow control. Defines some useful behavior like the ability to
 * reload script files if they get modified (useful when doing
 * development), and passing the control to Cocoon's sitemap for
 * result page generation.
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @since March 15, 2002
 * @version CVS $Id: AbstractInterpreter.java,v 1.20 2004/03/08 13:57:37 cziegeler Exp $
 */
public abstract class AbstractInterpreter extends AbstractLogEnabled
  implements Serviceable, Contextualizable, Interpreter,
             SingleThreaded, Configurable, Disposable
{
    protected org.apache.avalon.framework.context.Context avalonContext;

    /**
     * List of source locations that need to be resolved.
     */
    protected ArrayList needResolve = new ArrayList();

    protected org.apache.cocoon.environment.Context context;
    protected ServiceManager manager;
    protected ContinuationsManager continuationsMgr;
    
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

    public void configure(Configuration config) throws ConfigurationException {
        reloadScripts = config.getChild("reload-scripts").getValueAsBoolean(false);
        checkTime = config.getChild("check-time").getValueAsLong(1000L);
    }

    /**
     * Serviceable
     */
    public void service(ServiceManager sm) throws ServiceException {
        this.manager = sm;
        this.continuationsMgr = (ContinuationsManager)sm.lookup(ContinuationsManager.ROLE);
    }

    public void contextualize(org.apache.avalon.framework.context.Context context)
    throws ContextException{
        this.avalonContext = context;
        this.context = (Context)context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( this.continuationsMgr );
            this.continuationsMgr = null;
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
     * @see org.apache.cocoon.components.source.SourceFactory
     * @see org.apache.cocoon.environment.Environment
     * @see org.apache.cocoon.components.source.impl.DelayedRefreshSourceWrapper
     */
    public void register(String source)
    {
        synchronized(this) {
            needResolve.add(source);
        }
    }

    /**
     * Call the Cocoon sitemap for the given URI, sending the output of the
     * eventually matched pipeline to the specified outputstream.
     *
     * @param uri The URI for which the request should be generated.
     * @param biz Extra data associated with the subrequest.
     * @param out An OutputStream where the output should be written to.
     * @exception Exception If an error occurs.
     */
    public void process(String uri, Object biz, OutputStream out)
        throws Exception 
    {
        // FIXME (SW): should we deprecate this method in favor of PipelineUtil?
        PipelineUtil pipeUtil = new PipelineUtil();
        pipeUtil.contextualize(this.avalonContext);
        pipeUtil.service(this.manager);
        pipeUtil.processToStream(uri, biz, out);
    }

    public void forwardTo(String uri, Object bizData,
                          WebContinuation continuation,
                          Redirector redirector)
        throws Exception
    {
        if (SourceUtil.indexOfSchemeColon(uri) == -1) {
            uri = "cocoon:/" + uri;
            Map objectModel = ContextHelper.getObjectModel(this.avalonContext);
            FlowHelper.setWebContinuation(objectModel, continuation);
            FlowHelper.setContextObject(objectModel, bizData);
            if (redirector.hasRedirected()) {
                throw new IllegalStateException("Pipeline has already been processed for this request");
            }
            redirector.redirect(false, uri);
        } else {
            throw new Exception("uri is not allowed to contain a scheme (cocoon:/ is always automatically used)");
        }
    }
}
