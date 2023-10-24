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
package org.apache.cocoon.components.treeprocessor.sitemap;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.cocoon.ConnectionResetException;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.SimpleParentProcessingNode;
import org.apache.cocoon.environment.Environment;

/**
 * Handles &lt;map:pipelines&gt;
 *
 * @version $Id$
 */
public final class PipelinesNode extends SimpleParentProcessingNode
                                 implements Serviceable, Disposable {

    private ErrorHandlerHelper errorHandlerHelper;


    /**
     * Constructor
     */
    public PipelinesNode() {
        super(null);
        this.errorHandlerHelper = new ErrorHandlerHelper();
    }

    /**
     * Pass manager to {@link ErrorHandlerHelper}.
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.errorHandlerHelper.service(manager);
    }

    public void setErrorHandler(ProcessingNode node) {
        this.errorHandlerHelper.setErrorHandler(node);
    }

    public void setChildren(ProcessingNode[] nodes) {
        // Mark the last pipeline so that it can throw a ResourceNotFoundException
        ((PipelineNode) nodes[nodes.length - 1]).setLast(true);
        super.setChildren(nodes);
    }

    /**
     * Process the environment. Also adds a <code>SourceResolver</code>
     * and a <code>Redirector</code> in the object model. The previous resolver and
     * redirector, if any, are restored before return.
     */
    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {

        // Perform any common invoke functionality
        super.invoke(env, context);

        try {
            // FIXME: Is there any useful information that can be passed as top-level parameters,
            //        such as the URI of the mount point ?

            return invokeNodes(this.children, env, context);

        } catch (ConnectionResetException e) {
            // Will be reported by CocoonServlet, rethrowing
            throw e;

        } catch (Exception ex) {
            // Invoke pipelines handler
            return this.errorHandlerHelper.invokeErrorHandler(ex, env, context);
        }
    }

    public void dispose() {
        this.errorHandlerHelper = null;
    }
}
