/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.container.CocoonComponentManager;
import org.apache.cocoon.components.cprocessor.variables.VariableResolver;

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
 * @version CVS $Id: InvokeContext.java,v 1.3 2004/01/08 11:13:07 cziegeler Exp $
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

            this.processingPipeline = (ProcessingPipeline) 
                this.pipelinesManager.lookup(ProcessingPipeline.ROLE);
            this.processingPipeline.reservice( this.pipelinesManager );
            this.processingPipeline.setup(
                  VariableResolver.buildParameters(this.processingPipelineParameters,
                                                   this, this.processingPipelineObjectModel)
            );
            if (this.isBuildingPipelineOnly) {
                CocoonComponentManager.addComponentForAutomaticRelease(this.pipelinesManager,
                                                                       this.processingPipeline);
            }
        }
        return this.processingPipeline;
    }

    /**
     * Set the processing pipeline for sub-sitemaps
     */
    public void setProcessingPipeline(ProcessingPipeline pipeline) {
        this.processingPipeline = pipeline;
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
    protected void dumpParameters()
    {
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
        if (!this.isBuildingPipelineOnly && this.pipelinesManager != null) {
            if (this.processingPipeline != null) {
                this.pipelinesManager.release( this.processingPipeline );
                this.processingPipeline = null;
            }
            this.pipelinesManager = null;
            this.processingPipelineParameters = null;
        }

    }
}
