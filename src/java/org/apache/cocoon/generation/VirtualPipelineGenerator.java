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

import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.sitemap.impl.AbstractVirtualSitemapComponent;
import org.apache.cocoon.xml.XMLConsumer;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;


public class VirtualPipelineGenerator extends AbstractVirtualSitemapComponent
    implements Generator {

    protected XMLConsumer consumer;

    protected String getTypeName() {
        return "generator";
    }

    public void setConsumer(XMLConsumer consumer) {
        this.consumer = consumer;
    }

    public void generate()
    throws IOException, SAXException, ProcessingException {

        // Should use SourceResolver of the this components' sitemap, not caller sitemap
        // Have to switch to another environment...
        Environment env = EnvironmentHelper.getCurrentEnvironment();
        String oldPrefix = env.getURIPrefix();
        String oldURI    = env.getURI();

        // save callers resolved sources if there are any
        Map oldSourceMap = (Map)env.getAttribute(this.sourceMapName);
        // place for resolved sources
        env.setAttribute(this.sourceMapName, this.sourceMap);

        try {
            try {
                String uri = (String) this.context.get(Constants.CONTEXT_ENV_URI);
                String prefix = (String) this.context.get(Constants.CONTEXT_ENV_PREFIX);
                env.setURI(prefix, uri);
                
                this.pipeline.prepareInternal(env);
            } catch (Exception e) {
                throw new ProcessingException("Oops", e);
            } finally {
                // Restore context
                env.setURI(oldPrefix, oldURI);
            }

            this.pipeline.process(env, this.consumer);

        } finally {
            // restore sourceMap
            if (oldSourceMap != null)
                env.setAttribute(this.sourceMapName, oldSourceMap);
            else
                env.removeAttribute(this.sourceMapName);
        }
    }
 }
