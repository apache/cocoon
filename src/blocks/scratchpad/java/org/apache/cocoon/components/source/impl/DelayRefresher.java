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
package org.apache.cocoon.components.source.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.cocoon.caching.IdentifierCacheKey;
import org.apache.cocoon.components.cron.CronJob;
import org.apache.cocoon.components.cron.JobScheduler;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;

/**
 * Default implementation of the refresher.
 * 
 * @since 2.1.1
 * @version CVS $Id: DelayRefresher.java,v 1.5 2004/04/15 08:05:56 cziegeler Exp $
 */
public class DelayRefresher extends AbstractLogEnabled
implements Contextualizable, Serviceable, Parameterizable, Disposable, ThreadSafe, Refresher, CronJob {
    
    private static final String PARAM_CACHE_ROLE          = "cache-role";
    private static final String PARAM_CACHE_EXPIRES       = "cache-expires";
    private static final String PARAM_UPDATE_TARGET_ROLE  = "update-target-role";
    private static final String PARAM_WRITE_INTERVAL      = "write-interval";
	private static final String PARAM_WRITE_FILE          = "write-file";
	
	private static final String DEFAULT_WRITE_FILE        = "refresher-targets.xml";
	
    private static final String CACHE_KEY                 = "cache-key";
	
    private static final String TAGNAME_TARGET            = "target";
	private static final String ATTR_CACHE                = "cache";
	private static final String ATTR_EXPIRES              = "expires";
    private static final String ATTR_KEY                  = "key";
	private static final String ATTR_URI                  = "uri";
    
    // service dependencies
    protected ServiceManager manager;
    protected SourceResolver resolver;
	protected JobScheduler scheduler;
    
    // the role name of the update CronJob
    protected String updateTarget;
    
    // the scheduled targets to be persisted and recovered upon restart
    protected Map entries = Collections.synchronizedMap(new HashMap());
    
    // the cocoon working directory
    protected File workDir;
    
    // the source to persist entries to
    protected Source writeSource;
    
    // whether anything changed to the entries since last persisting them
    protected boolean changed = false;
    
    
    // ---------------------------------------------------- Lifecycle
    
    public DelayRefresher() {
    }

	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
	 */
	public void contextualize(Context context) throws ContextException {
		this.workDir = (File) context.get(Constants.CONTEXT_WORK_DIR);
	}
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.scheduler = (JobScheduler) this.manager.lookup(JobScheduler.ROLE);
        this.resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) throws ParameterException {
        this.updateTarget = parameters.getParameter(PARAM_UPDATE_TARGET_ROLE, CronJob.ROLE + "/UpdateTarget");
        int writeInterval = parameters.getParameterAsInteger(PARAM_WRITE_INTERVAL, 0);
        if (writeInterval > 0) {
            this.setupRefreshJobSource(parameters);
            final Configuration conf = this.readRefreshJobConfiguration();
            this.setupRefreshJobs(conf);
            this.registerSelfWithScheduler(writeInterval);
        }
        else {
        	if (getLogger().isInfoEnabled()) {
				getLogger().info("Not writing update targets to file.");
        	}
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
    	execute(null);
        if (this.manager != null) {
            this.manager.release(this.scheduler);
            this.scheduler = null;
            if (this.resolver != null) {
				this.resolver.release(this.writeSource);
                this.writeSource = null;
                this.manager.release(this.resolver);
                this.resolver = null;
            }
            this.manager = null;
        }
    }
    
    // ---------------------------------------------------- Refresher implementation
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.components.source.impl.Refresher#refresh(org.apache.cocoon.caching.SimpleCacheKey, java.lang.String, long, java.lang.String)
     */
    public void refresh(IdentifierCacheKey cacheKey,
                        String uri,
                        String cacheRole,
                        Parameters parameters)
    throws SourceException {
        
        final String name = cacheKey.getKey();
		final int expires = parameters.getParameterAsInteger(PARAM_CACHE_EXPIRES, -1);
		
		if (expires > 0) {
			TargetConfiguration conf = (TargetConfiguration) this.entries.get(name);
			if (conf == null) {
				conf = new TargetConfiguration(cacheKey, uri, cacheRole, parameters);
				try {
					this.scheduler.addPeriodicJob(name,
												  this.updateTarget,
												  expires,
												  true,
												  conf.parameters,
												  conf.map);
				} catch (CascadingException e) {
					throw new SourceException("Failure scheduling update job.", e);
				}
				this.entries.put(name, conf);
			} else {
				conf.update(uri, cacheRole, parameters);
			}
			
			this.changed = true;
		}
    }
    
    // ---------------------------------------------------- CronJob implementation
    
    /**
     * Persists the job configurations.
     */
    public void execute(String name) {
        if (this.changed && this.writeSource != null) {
            this.changed = false;
            try {
                final OutputStream stream = ((ModifiableSource) this.writeSource).getOutputStream();
                final Writer writer = new OutputStreamWriter(stream);
                
                writer.write("<targets>\n");
                final Iterator iter = this.entries.values().iterator();
                while (iter.hasNext()) {
                    this.writeRefreshJobConfiguration(writer, (TargetConfiguration) iter.next());
                }
                writer.write("</targets>\n");
                writer.flush();
                writer.close();
            } catch (IOException e) {
            	if (getLogger().isDebugEnabled()) {
            		getLogger().debug("Error writing targets to file.", e);
            	}
            }
            
        }
    }
    
    /**
	 * @param writeInterval
	 */
	private void registerSelfWithScheduler(int writeInterval) {
		try {
		    this.scheduler.addPeriodicJob(this.getClass().getName(), 
		                                  this,
		                                  writeInterval,
		                                  true,
		                                  null,
		                                  null);
		} catch (CascadingException ignore) {
            if (this.getLogger().isDebugEnabled()) {
				this.getLogger().debug("Registering self with scheduler, ignoring exception:", ignore);
			}
		}
	}

	/**
	 * @param conf
	 */
	private void setupRefreshJobs(final Configuration conf) {
		if ( conf != null ) {
		    final Configuration[] childs = conf.getChildren(TAGNAME_TARGET);
		    if ( childs != null ) {
		        for(int i=0; i < childs.length; i++) {
		            try {
		                this.setupSingleRefreshJob(childs[i]);
		            } catch (CascadingException ignore) {
		                if (this.getLogger().isDebugEnabled()) {
							this.getLogger().debug("Setting up refresh job, ignoring exception:", ignore);
						}
		            }
		        }
		    }
		}
	}

	/**
	 * @param childs
	 * @param i
	 * @throws ConfigurationException
	 * @throws CascadingException
	 */
	private void setupSingleRefreshJob(final Configuration conf) throws ConfigurationException, CascadingException {
		
        final String uri = URLDecoder.decode(conf.getAttribute(ATTR_URI));
		final String cache = conf.getAttribute(ATTR_CACHE);
        final int expires = conf.getAttributeAsInteger(ATTR_EXPIRES);
		final String key = URLDecoder.decode(conf.getAttribute(ATTR_KEY));
		final IdentifierCacheKey cacheKey = new IdentifierCacheKey(key, false);
		
        final Parameters parameters = Parameters.fromConfiguration(conf);
        
        final TargetConfiguration tc = new TargetConfiguration(cacheKey, uri, cache, parameters);
		
        this.entries.put(key, tc);
		final String name = cacheKey.getKey();
		
		this.scheduler.addPeriodicJob(name,
                                      this.updateTarget,
		                              expires,
		                              true,
		                              tc.parameters,
		                              tc.map);
	}

	/**
	 * @return
	 */
	private Configuration readRefreshJobConfiguration() {
		SAXConfigurationHandler b = new SAXConfigurationHandler();
		try {
		    SourceUtil.toSAX(this.manager, this.writeSource, this.writeSource.getMimeType(), b);
		} catch (Exception ignore) {
		    this.getLogger().warn("Unable to read configuration from " + this.writeSource.getURI());
		}
		final Configuration conf = b.getConfiguration();
		return conf;
	}

	/**
	 * @param parameters
	 * @throws ParameterException
	 */
	private void setupRefreshJobSource(Parameters parameters) throws ParameterException {
		try {
			final String fileName = parameters.getParameter(PARAM_WRITE_FILE, DEFAULT_WRITE_FILE);
			final File file = new File(workDir, fileName);
		    this.writeSource = this.resolver.resolveURI(file.toString());
		} catch (IOException ioe) {
		    throw new ParameterException("Error getting write-source.", ioe);
		}
		if (!(this.writeSource instanceof ModifiableSource)) {
		    throw new ParameterException("Write-source is not modifiable.");
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("Write source location: " + this.writeSource.getURI());
		}
	}

    /**
	 * @param writer
	 * @param iter
	 * @throws IOException
	 */
	private void writeRefreshJobConfiguration(Writer writer, final TargetConfiguration c) throws IOException {
        writer.write("<"+TAGNAME_TARGET+" "+ATTR_URI+"=\"");
        writer.write(URLEncoder.encode(c.parameters.getParameter(ATTR_URI, "")));
        writer.write("\" "+ATTR_EXPIRES+"=\"");
        writer.write(c.parameters.getParameter(PARAM_CACHE_EXPIRES, "0"));
        writer.write("\" "+ATTR_CACHE+"=\"");
        writer.write(c.parameters.getParameter(PARAM_CACHE_ROLE, ""));
        writer.write("\" "+ATTR_KEY+"=\"");
        writer.write(URLEncoder.encode(((IdentifierCacheKey) c.map.get(CACHE_KEY)).getKey()));
        writer.write("\"/>\n");
	}
	
	/**
	 * Configuration data holder for scheduled targets.
	 */
	class TargetConfiguration {
        
        final Map map;
		Parameters parameters;
        
        TargetConfiguration(IdentifierCacheKey cacheKey,
                            String uri,
                            String cacheRole,
                            Parameters parameters) {
            this.map = new HashMap();
            this.map.put(CACHE_KEY, cacheKey);
            update(uri, cacheRole, parameters);
            
        }
        
        void update(String uri, String cacheRole, Parameters parameters) {
            this.parameters = parameters;
            this.parameters.setParameter(ATTR_URI, uri);
            this.parameters.setParameter(PARAM_CACHE_ROLE, cacheRole);
        }
        
    }

}
