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
package org.apache.cocoon.components.treeprocessor;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Recomposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Redirector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The invocation context of <code>ProcessingNode</code>s.
 * <p>
 * This class serves two purposes :
 * <ul><li>Avoid explicit enumeration of all needed parameters in
 *         {@link ProcessingNode#invoke(org.apache.cocoon.environment.Environment, InvokeContext)},
 *         thus allowing easier addition of new parameters,
 *     <li>Hold pipelines, and provide "just in time" lookup for them.
 * </ul>
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id: InvokeContext.java,v 1.9 2004/06/11 12:24:21 vgritsenko Exp $
 */

public class InvokeContext 
extends AbstractLogEnabled
implements Recomposable, Disposable {

    private List mapStack = new ArrayList();
    private HashMap nameToMap = new HashMap();
    private HashMap mapToName = new HashMap();

    private boolean isBuildingPipelineOnly;

    /** The current component manager, as set by the last call to compose() or recompose() */
    private ComponentManager currentManager;

    /** The component manager that was used to get the pipelines */
    private ComponentManager pipelinesManager;

    /** The name of the processing pipeline component */
    protected String processingPipelineName;

    /** The parameters for the processing pipeline */
    protected Map processingPipelineParameters;
    
    /** The object model used to resolve processingPipelineParameters */
    protected Map processingPipelineObjectModel;

    /** The ProcessingPipeline used */
    protected ProcessingPipeline processingPipeline;

    /** The internal pipeline description */
    protected Processor.InternalPipelineDescription internalPipelineDescription;

    /** The last processor */
    protected Processor lastProcessor;
    
    /** The redirector */
    protected Redirector redirector;
    
    /** The Selector for the processing pipeline */
    protected ComponentSelector pipelineSelector;
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
     * Composable Interface
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.currentManager = manager;
    }

    /**
     * Recomposable interface
     */
    public void recompose(ComponentManager manager) throws ComponentException {

        this.currentManager = manager;

        if (this.processingPipeline instanceof Recomposable) {
            ((Recomposable)this.processingPipeline).recompose(manager);
        }
    }

    /**
     * Informs the context about a new pipeline section
     */
    public void inform(String pipelineName,
                       Map    parameters,
                       Map    objectModel) {
        this.processingPipelineName = pipelineName;
        this.processingPipelineParameters = parameters;
        this.processingPipelineObjectModel = objectModel;
    }

    /**
     * Get the current <code>ProcessingPipeline</code>
     */
    public ProcessingPipeline getProcessingPipeline()
    throws Exception {
        if (this.processingPipeline == null) {
            // Keep current manager for proper release
            this.pipelinesManager = this.currentManager;

            this.pipelineSelector = (ComponentSelector)this.pipelinesManager.lookup(ProcessingPipeline.ROLE+"Selector");
            this.processingPipeline = (ProcessingPipeline)this.pipelineSelector.select(this.processingPipelineName);
            if (this.processingPipeline instanceof Recomposable) {
                ((Recomposable)this.processingPipeline).recompose(this.pipelinesManager);
            }
            this.processingPipeline.setup(
                  VariableResolver.buildParameters(this.processingPipelineParameters,
                                                   this, this.processingPipelineObjectModel)
            );
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
        return((Map) this.nameToMap.get(anchor));
    }

    /**
     * Push a Map on top of the current Map stack.
     */
    public final void pushMap(String anchorName, Map map) {
        mapStack.add(map);

        if (this.getLogger().isDebugEnabled()) {
            dumpParameters();
        }

        if (anchorName != null) {
            if (!nameToMap.containsKey(anchorName)) {
                nameToMap.put(anchorName,map);
                mapToName.put(map,anchorName);
            }
            else {
                if (this.getLogger().isErrorEnabled()) {
                    this.getLogger().error("name [" + anchorName + "] clashes");
                }
            }
        }
    }

    /**
     * Dumps all sitemap parameters to log
     */
    protected void dumpParameters() {
        if (!mapStack.isEmpty()) {
            StringBuffer sb = new StringBuffer();

            sb.append("\nCurrent Sitemap Parameters:\n");
            String path = "";

            for (int i = mapStack.size() - 1; i >= 0; i--) {
                Map map = (Map) mapStack.get(i);
                sb.append("LEVEL ").append(i+1);
                if (mapToName.containsKey(map)) {
                    sb.append(" is named '").append(String.valueOf(mapToName.get(map))).append("'");
                }
                sb.append("\n");

                Iterator keys = map.keySet().iterator();
                while (keys.hasNext()) {
                    Object key = keys.next();
                    sb.append("PARAM: '").append(path).append(key).append("' ");
                    sb.append("VALUE: '").append(map.get(key)).append("'\n");
                }

                path = "../" + path;
            }

            this.getLogger().debug(sb.toString());
        }

    }


    /**
     * Pop the topmost element of the current Map stack.
     */
    public final void popMap() {
        Object map = mapStack.remove(mapStack.size() - 1);
        Object name = mapToName.get(map);
        mapToName.remove(map);
        nameToMap.remove(name);
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
     * Prepare this context for reuse
     *
     */
    public final void reset() {
        this.mapStack.clear();
        this.mapToName.clear();
        this.nameToMap.clear();
        dispose();
    }

    /**
     * Release the pipelines, if any, if they were looked up by this context.
     */
    public void dispose() {
        // Release pipelines, if any
        if (this.internalPipelineDescription == null && this.pipelinesManager != null) {
            if ( this.pipelineSelector != null) {
                this.pipelineSelector.release( (Component)this.processingPipeline);
                this.processingPipeline = null;
                this.pipelinesManager.release( this.pipelineSelector );
                this.pipelineSelector = null;
            }
            this.pipelinesManager = null;
            this.processingPipelineParameters = null;
        }

    }
}
