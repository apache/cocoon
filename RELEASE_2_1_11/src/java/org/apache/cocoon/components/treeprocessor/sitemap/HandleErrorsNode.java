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

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.commons.lang.SystemUtils;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.treeprocessor.AbstractParentProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.environment.Environment;

/**
 * Handles &lt;map:handle-errors&gt;
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version $Id$
 */
public final class HandleErrorsNode extends AbstractParentProcessingNode {

    private ProcessingNode[] children;
    private int statusCode;
    private boolean internal;
    private boolean external;

    /**
     * @param statusCode Value of the type attribute: 404 (deprecated), 500 (deprecated), or -1 (no attribute present).
     * @param scope Value of the error handler scope attribute: external, internal, always.
     */
    public HandleErrorsNode(int statusCode, String scope)
    throws ConfigurationException {
        this.statusCode = statusCode;
        
        if ("internal".equals(scope)) {
            this.internal = true;
        } else if ("external".equals(scope)) {
            this.external = true;
        } else if ("always".equals(scope)) {
            this.internal = true;
            this.external = true;
        } else {
            throw new ConfigurationException("Unrecognized value of when attribute on <handle-errors> at " +
                                             getLocation());
        }
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public boolean isInternal() {
        return this.internal;
    }

    public boolean isExternal() {
        return this.external;
    }

    public void setChildren(ProcessingNode[] nodes) {
        this.children = nodes;
    }

    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {

        if (getLogger().isInfoEnabled()) {
            getLogger().info("Processing handle-errors at " + getLocation());
        }

		if (statusCode == -1) {
            // No 'type' attribute : new Cocoon 2.1 behaviour, no implicit generator
            try {
                return invokeNodes(this.children, env, context);

            } catch (ProcessingException e) {
                // Handle the various cases related to the transition from implicit generators in handle-errors to
                // explicit ones, in order to provide meaningful messages that will ease the migration
                if (e.getMessage().indexOf("Must set a generator before adding") != -1) {

                    env.getObjectModel().remove(Constants.NOTIFYING_OBJECT);
                    throw new ProcessingException(
                        "Incomplete pipeline: 'handle-error' without a 'type' must include a generator, at " +
                        getLocation() + SystemUtils.LINE_SEPARATOR +
                        "Either add a generator (preferred) or a type='500' attribute (deprecated) on 'handle-errors'");
                }

                // Rethrow the exception
                throw e;
            }
		} else {
		    // A 'type' attribute is present : add the implicit generator
            context.getProcessingPipeline().setGenerator("<notifier>", "", Parameters.EMPTY_PARAMETERS, Parameters.EMPTY_PARAMETERS);

            try {
                return invokeNodes(this.children, env, context);
            } catch (ProcessingException e) {
                if (e.getMessage().indexOf("Generator already set") != -1){

                    env.getObjectModel().remove(Constants.NOTIFYING_OBJECT);
                    throw new ProcessingException(
                            "Error: 'handle-error' with a 'type' attribute has an implicit generator, at " +
                            getLocation() + SystemUtils.LINE_SEPARATOR +
                            "Please remove the 'type' attribute on 'handle-error'");
                }
                // Rethrow the exception
                throw e;
            }
        }
    }
}
