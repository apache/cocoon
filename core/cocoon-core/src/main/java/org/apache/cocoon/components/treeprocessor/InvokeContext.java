/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.components.treeprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.sitemap.SitemapErrorHandler;

/**
 * The invocation context of <code>ProcessingNode</code>s.
 *
 * <p>This class serves two purposes:
 * <ul>
 *   <li>Avoid explicit enumeration of all needed parameters in
 *       {@link ProcessingNode#invoke(org.apache.cocoon.environment.Environment, InvokeContext)},
 *       thus allowing easier addition of new parameters,</li>
 *   <li>Hold pipelines, and provide "just in time" lookup for them.</li>
 * </ul>
 *
 * @version $Id$
 */
public class InvokeContext extends AbstractLogEnabled
                           implements Serviceable, Disposable {

    private List mapStack = new ArrayList();
    private HashMap nameToMap = new HashMap();
    private HashMap mapToName = new HashMap();

    /** True if building pipeline only, not processing it. */
    private boolean isBuildingPipelineOnly;

    /** The redirector */
    protected Redirector redirector;


    /** The current component manager, as set by the last call to compose() or recompose() */
    private ServiceManager currentManager;

    /** The component manager that was used to get the pipelines */
    private ServiceManager pipelinesManager;

    /** The name of the processing pipeline component */
    protected String processingPipelineName;

    /** The parameters for the processing pipeline */
    protected Parameters processingPipelineParameters;

    /** The object model used to resolve processingPipelineParameters */
    protected Map processingPipelineObjectModel;

    /** The Selector for the processing pipeline */
    protected ServiceSelector pipelineSelector;

    /** The ProcessingPipeline used */
    protected ProcessingPipeline processingPipeline;

    /** The internal pipeline description */
    protected Processor.InternalPipelineDescription internalPipelineDescription;

    /** The error handler for the pipeline. */
    protected SitemapErrorHandler errorHandler;

    /** The last processor */
    protected Processor lastProcessor;

    /**
     * Create an <code>InvokeContext</code> without existing pipelines. This also means
     * the current request is external.
     */
    public InvokeContext() {
        this.isBuildingPipelineOnly = false;
    }

    /**
     * Determines if the Pipeline been set for this context
     */
    public boolean pipelineIsSet() {
	    return (this.processingPipeline != null);
    }

    /**
     * Create an <code>InvokeContext</code>
     */
    public InvokeContext(boolean isBuildingPipelineOnly) {
        this.isBuildingPipelineOnly = isBuildingPipelineOnly;
    }

    /**
     * Serviceable interface
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.currentManager = manager;
        if (this.processingPipeline != null) {
            this.processingPipeline.setProcessorManager(manager);
        }
    }

    /**
     * Informs the context about a new pipeline section
     */
    public void inform(String     pipelineName,
                       Parameters parameters,
                       Map        objectModel) {
        this.processingPipelineName = pipelineName;
        this.processingPipelineParameters = parameters;
        this.processingPipelineObjectModel = objectModel;
    }
    
    public Parameters getPipelineParameters() {
        return this.processingPipelineParameters;
    }
    
    public String getPipelineType() {
        return this.processingPipelineName;
    }

    /**
     * Get the current <code>ProcessingPipeline</code>
     */
    public ProcessingPipeline getProcessingPipeline()
    throws Exception {
        if (this.processingPipeline == null) {
            // Keep current manager for proper release
            this.pipelinesManager = this.currentManager;

            this.pipelineSelector = (ServiceSelector)this.pipelinesManager.lookup(ProcessingPipeline.ROLE+"Selector");
            this.processingPipeline = (ProcessingPipeline)this.pipelineSelector.select(this.processingPipelineName);
            this.processingPipeline.setProcessorManager(this.currentManager);

            this.processingPipeline.setup(this.processingPipelineParameters);
            this.processingPipeline.setErrorHandler(this.errorHandler);
        }
        return this.processingPipeline;
    }

    /**
     * Set the processing pipeline for sub-sitemaps
     */
    public void setInternalPipelineDescription(Processor.InternalPipelineDescription desc) {
        this.processingPipeline = desc.processingPipeline;
        this.pipelinesManager = desc.pipelineManager;
        this.lastProcessor = desc.lastProcessor;
        this.pipelineSelector = desc.pipelineSelector;
        this.internalPipelineDescription = new Processor.InternalPipelineDescription(
                this.processingPipeline, this.pipelineSelector, this.pipelinesManager);
        this.internalPipelineDescription.lastProcessor = this.lastProcessor;
        this.internalPipelineDescription.prefix = desc.prefix;
        this.internalPipelineDescription.uri = desc.uri;
    }

    /**
     * Get the pipeline description
     */
    public Processor.InternalPipelineDescription getInternalPipelineDescription(Environment env) {
        if ( this.internalPipelineDescription == null ) {
            this.internalPipelineDescription = new Processor.InternalPipelineDescription(
                    this.processingPipeline, this.pipelineSelector, this.pipelinesManager);
            this.internalPipelineDescription.lastProcessor = this.lastProcessor;
            this.internalPipelineDescription.prefix = env.getURIPrefix();
            this.internalPipelineDescription.uri = env.getURI();
        }
        return this.internalPipelineDescription;
    }

    /**
     * Set the last processor
     */
    public void setLastProcessor(Processor p) {
        this.lastProcessor = p;
    }

    /**
     * Are we building a pipeline (and not executing it) ?
     */
    public final boolean isBuildingPipelineOnly() {
        return this.isBuildingPipelineOnly;
    }

    /**
     * Get the current Map stack used to resolve expressions.
     */
    public final List getMapStack() {
        return this.mapStack;
    }

    /**
     * Get the result Map by anchor name
     */
    public final Map getMapByAnchor(String anchor) {
        return (Map) this.nameToMap.get(anchor);
    }

    /**
     * Push a Map on top of the current Map stack.
     */
    public final void pushMap(String anchorName, Map map) {
        this.mapStack.add(map);

        if (getLogger().isDebugEnabled()) {
            dumpParameters();
        }

        if (anchorName != null) {
            if (!this.nameToMap.containsKey(anchorName)) {
                this.nameToMap.put(anchorName,map);
                this.mapToName.put(map,anchorName);
            } else {
                if (getLogger().isErrorEnabled()) {
                    getLogger().error("name [" + anchorName + "] clashes");
                }
            }
        }
    }

    /**
     * Dumps all sitemap parameters to log
     */
    protected void dumpParameters() {
        if (!this.mapStack.isEmpty()) {
            StringBuffer sb = new StringBuffer();

            sb.append("\nCurrent Sitemap Parameters:\n");
            String path = "";

            for (int i = this.mapStack.size() - 1; i >= 0; i--) {
                Map map = (Map) this.mapStack.get(i);
                sb.append("LEVEL ").append(i+1);
                if (this.mapToName.containsKey(map)) {
                    sb.append(" is named '").append(String.valueOf(this.mapToName.get(map))).append("'");
                }
                sb.append("\n");

                for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
                    final Map.Entry me = (Map.Entry)iter.next();
                    final Object key = me.getKey();
                    sb.append("PARAM: '").append(path).append(key).append("' ");
                    sb.append("VALUE: '").append(map.get(key)).append("'\n");
                }

                path = "../" + path;
            }

            getLogger().debug(sb.toString());
        }
    }

    /**
     * Pop the topmost element of the current Map stack.
     */
    public final void popMap() {
        Object map = this.mapStack.remove(this.mapStack.size() - 1);
        Object name = this.mapToName.get(map);
        this.mapToName.remove(map);
        this.nameToMap.remove(name);
    }

    /**
     * Set the redirector to be used by nodes that need it.
     *
     * @param redirector the redirector
     */
    public void setRedirector(Redirector redirector) {
        this.redirector = redirector;
    }

    /**
     * Get the redirector to be used by nodes that need it.
     *
     * @return the redirector
     */
    public Redirector getRedirector() {
        return this.redirector;
    }

    /**
     * Release the pipelines, if any, if they were looked up by this context.
     */
    public void dispose() {
        if (this.internalPipelineDescription == null && this.pipelinesManager != null) {
            if (this.pipelineSelector != null) {
                this.pipelineSelector.release(this.processingPipeline);
                this.processingPipeline = null;
                this.pipelinesManager.release(this.pipelineSelector);
                this.pipelineSelector = null;
            }
            this.pipelinesManager = null;

            this.processingPipelineName = null;
            this.processingPipelineParameters = null;
            this.processingPipelineObjectModel = null;
        }
    }

    /**
     * Set the error handler for the pipeline.
     */
    public void setErrorHandler(SitemapErrorHandler handler) {
        this.errorHandler = handler;
    }
}
