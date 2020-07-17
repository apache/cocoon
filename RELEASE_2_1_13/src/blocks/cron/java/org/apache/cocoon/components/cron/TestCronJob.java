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

import java.io.InputStreamReader;
import java.util.Date;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

/**
 * A simple test CronJob which also calls a pipeline internally.
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @author <a href="http://apache.org/~reinhard">Reinhard Poetz</a>
 * @version CVS $Id$
 * @since 2.1.1
 */
public class TestCronJob extends ServiceableCronJob
                         implements Configurable, ConfigurableCronJob {

    /** Parameter key for the message */
    public static final String PARAMETER_MESSAGE = "TestCronJob.Parameter.Message";

    /** Parameter key for the sleep value */
    public static final String PARAMETER_SLEEP = "TestCronJob.Parameter.Sleep";

    /** Parameter key for the pipeline to be called */
    public static final String PARAMETER_PIPELINE = "TestCronJob.Parameter.Pipeline";

    /** The configured message. */
    private String msg;

    /** The configured sleep time. */
    private int sleep;

    /** The pipeline to be called. */
    private String pipeline;

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(final Configuration config)
    throws ConfigurationException {
        msg = config.getChild("msg").getValue("I was not configured");
        sleep = config.getChild("sleep").getValueAsInteger(11000);
        pipeline = config.getChild("pipeline").getValue("samples/hello-world/hello.xhtml");
    }

    /**
     * @see org.apache.cocoon.components.cron.CronJob#execute(java.lang.String)
     */
    public void execute(String name) {
        if ( this.getLogger().isInfoEnabled() ) {
            getLogger().info("CronJob " + name + " launched at " + new Date() + " with message '" + msg +
                             "' and sleep timeout of " + sleep + "ms");
        }

        SourceResolver resolver = null;
        Source src = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            src = resolver.resolveURI("cocoon://" + this.pipeline);

            final InputStreamReader r = new InputStreamReader(src.getInputStream());
            try {
                boolean append = this.getLogger().isInfoEnabled();
                StringBuffer sb = new StringBuffer();
                char[] b = new char[8192];
                int n;

                while((n = r.read(b)) > 0) {
                    if ( append ) {
                        sb.append(b, 0, n);
                    }
                }

                if ( append ) {
                    getLogger().info("CronJob " + name + " called pipeline " + pipeline +
                                     " and received following content:\n" + sb.toString());
                }
            } finally {
                r.close();
            }

        } catch(Exception e) {
            throw new CascadingRuntimeException("CronJob " + name + " raised an exception.", e);
        } finally {
            if (resolver != null) {
                resolver.release(src);
                this.manager.release(resolver);
                resolver = null;
                src = null;
            }
        }

        try {
            Thread.sleep(sleep);
        } catch (final InterruptedException ie) {
            //getLogger().error("CronJob " + name + " interrupted", ie);
        }

        if ( this.getLogger().isInfoEnabled() ) {
            getLogger().info("CronJob " + name + " finished at " + new Date() + " with message '" + msg +
                             "' and sleep timeout of " + sleep + "ms");
        }
    }

    /**
     * @see org.apache.cocoon.components.cron.ConfigurableCronJob#setup(org.apache.avalon.framework.parameters.Parameters, java.util.Map)
     */
    public void setup(Parameters params, Map objects) {
        if (null != params) {
            msg = params.getParameter(PARAMETER_MESSAGE, msg);
            sleep = params.getParameterAsInteger(PARAMETER_SLEEP, sleep);
            pipeline = params.getParameter(PARAMETER_PIPELINE, pipeline );

        }
    }
}
