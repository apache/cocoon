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

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.cocoon.components.cron.ServiceableCronJob;

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
 * @version CVS $Id: CocoonPipelineCronJob.java,v 1.1 2004/06/09 10:55:40 jeremy Exp $
 *
 * @since 2.1.5
 */
public class CocoonPipelineCronJob 
	extends ServiceableCronJob
	implements Configurable  {

	private String CONFIG_FILE_PARAM = "pipeline";
	private String CONFIG_FILE_DEAFAULT= "";
	private String pipeline = "";
	private SourceResolver resolver = null;
	private Source src = null;

	public void execute(String name) {
		if (getLogger ().isDebugEnabled ()) {
			getLogger().debug ("CocoonPipelineCronJob: " + name + ", calling pipeline: " + pipeline);
		}
		try {
			resolver = (SourceResolver)this.manager.lookup (SourceResolver.ROLE);
			src = resolver.resolveURI ("cocoon://" + pipeline);
			InputStream is = src.getInputStream();
			InputStreamReader reader = new InputStreamReader (is);
			StringBuffer sb = new StringBuffer ();
			char[] b = new char[8192];
			int n;
			while((n = reader.read (b)) > 0) {
				sb.append (b, 0, n); 
			}
			reader.close ();
			if (getLogger ().isInfoEnabled ()) {
				getLogger ().info ("CocoonPipelineCronJob: " + name + ", called pipeline: " + pipeline + ", and received following content:\n" + sb.toString() );
			}
		} catch(Exception e) {
			throw new CascadingRuntimeException ("CocoonPipelineCronJob: " + name + ", raised an exception: ", e);
		} finally {
			if (resolver != null) {
				resolver.release (src);
				this.manager.release (resolver);
				resolver = null;
				src = null;
			}
		}
	}

	public void configure(final Configuration config) throws ConfigurationException {
		pipeline = config.getChild (CONFIG_FILE_PARAM).getValue (CONFIG_FILE_DEAFAULT);
		if ("".equals (pipeline)) {
			throw new ConfigurationException ("CocoonPipelineCronJob has no pipeline configured.");
		}
	}

}
