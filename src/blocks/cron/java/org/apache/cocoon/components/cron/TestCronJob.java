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
package org.apache.cocoon.components.cron;

import java.io.InputStream;
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
 * @version CVS $Id: TestCronJob.java,v 1.8 2004/05/25 07:28:24 cziegeler Exp $
 *
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

    /** The configured message */
    private String m_msg;

    /** The configured sleep time */
    private int m_sleep;
    
    /** The pipeline to be called */
    private String pipeline = null;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(final Configuration config)
    	throws ConfigurationException {
        m_msg = config.getChild("msg").getValue("I was not configured");
        m_sleep = config.getChild("sleep").getValueAsInteger(11000);
        pipeline = config.getChild("pipeline").getValue("samples/hello-world/hello.xhtml");
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.CronJob#execute(java.lang.String)
     */
    public void execute(String name) {
		getLogger().info("CronJob " + name + " launched at " + new Date() + " with message '" + m_msg +
						 "' and sleep timeout of " + m_sleep + "ms");
		
        SourceResolver resolver = null;
        Source src = null;
        try {
			resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
			src = resolver.resolveURI("cocoon://" + pipeline);
			InputStream is = src.getInputStream();
			
			InputStreamReader reader = new InputStreamReader(is);
			StringBuffer sb = new StringBuffer();
			char[] b = new char[8192];
			int n;

			while((n = reader.read(b)) > 0) {
				sb.append(b, 0, n); 
			}
			
			reader.close();

            getLogger().info("Cronjob " + name + " called pipeline " + pipeline + 
                    " and received following content:\n" + sb.toString() );
        } catch(Exception e) {
            throw new CascadingRuntimeException("CronJob " + name + " raised an exception", e);
        } finally {
            if ( resolver != null ) {
			    resolver.release(src);
                this.manager.release(resolver);
                resolver = null;
			    src = null;
            }
        }
       
		try {
			Thread.sleep(m_sleep);
		} catch (final InterruptedException ie) {
			//getLogger().error("CronJob " + name + " interrupted", ie);
		}

		getLogger().info("CronJob " + name + " finished at " + new Date() + " with message '" + m_msg +
						 "' and sleep timeout of " + m_sleep + "ms");
			
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.ConfigurableCronJob#setup(org.apache.avalon.framework.parameters.Parameters, java.util.Map)
     */
    public void setup(Parameters params, Map objects) {
        if (null != params) {
            m_msg = params.getParameter(PARAMETER_MESSAGE, m_msg);
            m_sleep = params.getParameterAsInteger(PARAMETER_SLEEP, m_sleep);
            pipeline = params.getParameter(PARAMETER_PIPELINE, pipeline );
            
        }
    }    
}
