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

import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.environment.Environment;

/**
 * Handler for &lt;map:flow&gt; element in the sitemap.
 *
 * @since September 13, 2002
 * @version $Id$
 */
public class FlowNode extends AbstractProcessingNode
                      implements Serviceable, Disposable {

    private final String language;
    private ServiceManager manager;
    private Interpreter interpreter;


    public FlowNode(String language) {
        this.language = language;
    }

    /**
     * Lookup an flow {@link org.apache.cocoon.components.flow.Interpreter}
     * instance to hold the scripts defined within the <code>&lt;map:flow&gt;</code>
     * in the sitemap.
     *
     * @param manager a <code>ServiceManager</code> value
     * @exception ServiceException if no flow interpreter could be obtained
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;

        try {
            // Obtain the Interpreter instance for this language
            this.interpreter = (Interpreter) this.manager.lookup(Interpreter.ROLE + '/' + language);
            // Set interpreter ID as URI of the flow node (full sitemap file path)
            this.interpreter.setInterpreterID(this.location.getURI());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(language,
                                       "FlowNode: Couldn't obtain a flow interpreter for '" + language +
                                       "' at " + getLocation(), e);
        }
    }

    /**
     * This method should never be called by the TreeProcessor, since a
     * <code>&lt;map:flow&gt;</code> element should not be in an
     * "executable" sitemap node.
     *
     * @param env an <code>Environment</code> value
     * @param context an <code>InvokeContext</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean invoke(Environment env, InvokeContext context) throws Exception {
        return true;
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            this.manager.release(this.interpreter);
            this.interpreter = null;
            this.manager = null;
        }
    }
}
