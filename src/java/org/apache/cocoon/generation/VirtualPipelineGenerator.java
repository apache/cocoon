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
package org.apache.cocoon.generation;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.pipeline.VirtualProcessingPipeline;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.xml.XMLConsumer;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;

/**
 * TODO List:
 * <ul>
 * <li>Implement parameters support
 * <li>Resolve src parameter and pass to the pipeline
 * <li>Refactor ProcessingPipelines implementations
 * <li>Implement caching
 * </ul>
 */
public class VirtualPipelineGenerator implements Generator, Serviceable,
                                                 Contextualizable, Configurable {

    private Context context;
    private ServiceManager manager;
    private XMLConsumer consumer;
    private ProcessingNode node;
    private ProcessingPipeline pipeline;


    private class MyInvokeContext extends InvokeContext {
        public MyInvokeContext() throws Exception {
            super(true);
            super.processingPipeline = new VirtualProcessingPipeline(VirtualPipelineGenerator.this.context);
        }
    }

    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    public void configure(Configuration configuration) throws ConfigurationException {
        String name = configuration.getAttribute("name");
        try {
            this.node = (ProcessingNode) this.context.get(Constants.CONTEXT_VPC_PREFIX + "generator-" + name);
        } catch (ContextException e) {
            throw new ConfigurationException("Can not find VirtualPipelineGenerator '" + name + "' configuration");
        }
    }

    public void setConsumer(XMLConsumer consumer) {
        this.consumer = consumer;
    }

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {

        Environment env = EnvironmentHelper.getCurrentEnvironment();
        String oldPrefix = env.getURIPrefix();
        String oldURI    = env.getURI();
        try {
            String uri = (String) this.context.get(Constants.CONTEXT_ENV_URI);
            String prefix = (String) this.context.get(Constants.CONTEXT_ENV_URI);
            env.setURI(prefix, uri);

            MyInvokeContext invoker = new MyInvokeContext();
            invoker.service(this.manager);
            this.node.invoke(env, invoker);
            this.pipeline = invoker.getProcessingPipeline();
        } catch (Exception e) {
            throw new ProcessingException("Oops", e);
        } finally {
            // Restore context
            env.setURI(oldPrefix, oldURI);
        }
    }

    public void generate()
    throws IOException, SAXException, ProcessingException {

        // Should use SourceResolver of the this components' sitemap, not caller sitemap
        // Have to switch to another environment...
        Environment env = EnvironmentHelper.getCurrentEnvironment();
        String oldPrefix = env.getURIPrefix();
        String oldURI    = env.getURI();
        try {
            String uri = (String) this.context.get(Constants.CONTEXT_ENV_URI);
            String prefix = (String) this.context.get(Constants.CONTEXT_ENV_URI);
            env.setURI(prefix, uri);

            this.pipeline.prepareInternal(env);
        } catch (Exception e) {
            throw new ProcessingException("Oops", e);
        } finally {
            // Restore context
            env.setURI(oldPrefix, oldURI);
        }

        this.pipeline.process(env, this.consumer);
    }
}
