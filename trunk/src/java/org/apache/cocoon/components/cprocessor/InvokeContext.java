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
package org.apache.cocoon.components.cprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.cprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Redirector;

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
 * @version CVS $Id: InvokeContext.java,v 1.10 2004/03/08 13:57:39 cziegeler Exp $
 */
public class InvokeContext extends AbstractLogEnabled implements Serviceable, Disposable{

    private List mapStack = new ArrayList();
    private HashMap nameToMap = new HashMap();
    private HashMap mapToName = new HashMap();

    private boolean isBuildingPipelineOnly;

    /** The current component manager, as set by the last call to service() or reservice() (?) */
    private ServiceManager currentManager;

    /** The component manager that was used to get the pipelines */
    private ServiceManager pipelinesManager;

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
	if (this.processingPipeline != null)
		return true;
	return false;
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
    public void service(ServiceManager manager) throws ServiceException {
        this.currentManager = manager;
    }
    
    public void reservice(ServiceManager manager) throws ServiceException {

        this.currentManager = manager;
        if (this.processingPipeline != null) {
            this.processingPipeline.reservice(manager);
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

            this.processingPipeline = (ProcessingPipeline)this.pipelinesManager.lookup(ProcessingPipeline.ROLE);
            this.processingPipeline.reservice( this.pipelinesManager );
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
    }

    /**
     * Get the pipeline description
     */
    public Processor.InternalPipelineDescription getInternalPipelineDescription(Environment env) {
        if ( this.internalPipelineDescription == null ) {
            this.internalPipelineDescription = new Processor.InternalPipelineDescription(
                    this.processingPipeline, this.pipelinesManager);
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

        if (getLogger().isDebugEnabled()) {
            dumpParameters();
        }

        if (anchorName != null) {
            if (!nameToMap.containsKey(anchorName)) {
                nameToMap.put(anchorName,map);
                mapToName.put(map,anchorName);
            }
            else {
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

            getLogger().debug(sb.toString());
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
            if (this.processingPipeline != null) {
                this.pipelinesManager.release( this.processingPipeline );
                this.processingPipeline = null;
            }
            this.pipelinesManager = null;
            this.processingPipelineParameters = null;
        }

    }
}
