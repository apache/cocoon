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
package org.apache.cocoon.components.cron;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * A simple CronJob which calls an internal cocoon:// pipeline.
 *
 * You must provide it with a &lt;pipeline&gt;pipeline/to/call&lt;/pipeline&gt; parameter in cocoon.xconf
 * Your supplied pipeline String will have "cocoon://" prepended to it.
 * If you set info log enabled, this will write the output of the pipeline to the cron log
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @author <a href="http://apache.org/~reinhard">Reinhard Poetz</a>
 * @author <a href="http://apache.org/~jeremy/">Jeremy Quinn</a>
 * @version $Id$
 *
 * @since 2.1.5
 */
public class CocoonPipelineCronJob extends ServiceableCronJob
                                   implements Configurable, ConfigurableCronJob  {

    public static final String PIPELINE_PARAM = "pipeline";

    private String configuredPipeline;
    private String pipeline;

    public void execute(String name) {

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("CocoonPipelineCronJob: " + name + ", calling pipeline: " + pipeline);
        }

        SourceResolver resolver = null;
        Source src = null;
        InputStream is = null;
        InputStreamReader reader = null;
        try {
            boolean append = getLogger().isInfoEnabled();
            resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
            src = resolver.resolveURI("cocoon://" + pipeline);

            is = src.getInputStream();
            reader = new InputStreamReader(is);
            StringBuffer sb = new StringBuffer();
            char[] b = new char[8192];
            int n;
            while ((n = reader.read (b)) > 0) {
                if ( append ) {
                    sb.append(b, 0, n);
                }
            }
            reader.close();
            if (append) {
                getLogger().info("CocoonPipelineCronJob: " + name + ", called pipeline: " +
                                   pipeline + ", and received following content:\n" + sb.toString());
            }
        } catch (Exception e) {
            throw new CascadingRuntimeException("CocoonPipelineCronJob: " + name + ", raised an exception: ", e);
        } finally {
            try {
                if (reader != null) reader.close();
                if (is != null) is.close();
            } catch (IOException e) {
                throw new CascadingRuntimeException("CocoonPipelineCronJob: " + name + ", raised an exception: ", e);
            }
            if (resolver != null) {
                resolver.release(src);
                this.manager.release(resolver);
                resolver = null;
                src = null;
            }
        }
	}

    public void configure(final Configuration config) throws ConfigurationException {
        this.configuredPipeline = config.getChild(PIPELINE_PARAM).getValue(null);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.ConfigurableCronJob#setup(org.apache.avalon.framework.parameters.Parameters, java.util.Map)
     */
    public void setup(Parameters params, Map objects) {
        if (params != null) {
            pipeline = params.getParameter(PIPELINE_PARAM, configuredPipeline);
        } else {
            pipeline = configuredPipeline;      
        }
    }
}
