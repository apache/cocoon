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
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.caching.SimpleCacheKey;
import org.apache.cocoon.components.cron.ConfigurableCronJob;
import org.apache.cocoon.components.cron.CronJob;
import org.apache.cocoon.components.cron.JobScheduler;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;

/**
 * Default implementation of the refresher
 * 
 * <component class="org.apache.cocoon.components.source.impl.RefresherImpl" role="org.apache.cocoon.components.source.impl.Refresher"/>
 * 
 * @since 2.1.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: RefresherImpl.java,v 1.5 2004/03/06 21:00:39 haul Exp $
 */
public class RefresherImpl 
    extends AbstractLogEnabled
    implements Parameterizable, ThreadSafe, Refresher, Serviceable, Disposable, CronJob {
    
    private static final String PARAM_CACHE_ROLE = "cache-role";
    private static final String PARAM_CACHE_EXPIRES = "cache-expires";
    private static final String PARAM_SCHEDULER_TARGET = "scheduler-target";
    private static final String PARAM_WRITE_INTERVAL = "write-interval";
    private static final String PARAM_WRITE_TARGET = "write-target";

    private static final String CACHE_KEY = "cache-key";
	
    private static final String TAGNAME_TARGET = "target";
	private static final String ATTR_CACHE = "cache";
	private static final String ATTR_EXPIRES = "expires";
    private static final String ATTR_KEY = "key";
	private static final String ATTR_URI = "uri";

    protected String schedulerTarget;
    
    protected ServiceManager manager;
    
    protected JobScheduler   scheduler;
    
    protected Map            entries = Collections.synchronizedMap(new HashMap());
    
    protected SourceResolver resolver;
    
    protected Source         writeSource;
    
    /** changed */
    protected boolean changed = false;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) throws ParameterException {
        this.schedulerTarget = parameters.getParameter(PARAM_SCHEDULER_TARGET);
        int writeInterval = parameters.getParameterAsInteger(PARAM_WRITE_INTERVAL, 0);
        if ( writeInterval > 0) {
            this.setupRefreshJobSource(parameters);
            final Configuration conf = this.readRefreshJobConfiguration();
            this.setupRefreshJobs(conf);
            this.registerSelfWithScheduler(parameters, writeInterval);
        }
    }

    /**
	 * @param parameters
	 * @param writeInterval
	 */
	private void registerSelfWithScheduler(Parameters parameters, int writeInterval) {
		try {
		    this.scheduler.addPeriodicJob(this.getClass().getName(), 
		                          parameters.getParameter(PARAM_WRITE_TARGET),
		                          (long)writeInterval * 1000,
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
							this.getLogger().debug("Setting up refresh jobs, ignoring exception:", ignore);
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
		String uri = URLDecoder.decode(conf.getAttribute(ATTR_URI));
		int expires = conf.getAttributeAsInteger(ATTR_EXPIRES);
		String cache = conf.getAttribute(ATTR_CACHE);                        
		String key = URLDecoder.decode(conf.getAttribute(ATTR_KEY));
		SimpleCacheKey cacheKey = new SimpleCacheKey(key, false);
		TargetConfiguration tc = new TargetConfiguration(cacheKey, uri, expires, cache);
		this.entries.put(key, tc);
		final String name = cacheKey.getKey();
		
		this.scheduler.addPeriodicJob(name, this.schedulerTarget,
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
		    this.writeSource = this.resolver.resolveURI(parameters.getParameter("write-source"));
		} catch (IOException ioe) {
		    throw new ParameterException("Error getting write-source.", ioe);
		}
		if (!(this.writeSource instanceof ModifiableSource)) {
		    throw new ParameterException("Write-source is not modifiable.");
		}
	}

	/* (non-Javadoc)
     * @see org.apache.cocoon.components.source.impl.Refresher#refresh(org.apache.cocoon.caching.SimpleCacheKey, java.lang.String, long, java.lang.String)
     */
    public void refresh(SimpleCacheKey cacheKey,
                        String uri,
                        long expires,
                        String cacheRole) 
    throws SourceException {
        TargetConfiguration conf = new TargetConfiguration(cacheKey, uri, expires, cacheRole);

        // call the target the first time
        ConfigurableCronJob t = null;
        final String name = cacheKey.getKey();
        try {
            t = (ConfigurableCronJob)this.manager.lookup(this.schedulerTarget);
            t.setup(conf.parameters, conf.map);
            t.execute(name);
        } catch (ServiceException se) {
            throw new SourceException("Unable to lookup target " + this.schedulerTarget, se);
        } finally {
            this.manager.release(t);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.scheduler = (JobScheduler)this.manager.lookup(JobScheduler.ROLE);
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            this.manager.release(this.scheduler);
            this.scheduler = null;
            this.manager = null;
            if (this.resolver != null) {
                this.resolver.release(this.writeSource);
                this.writeSource = null;
                this.manager.release(this.resolver);
                this.resolver = null;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.source.impl.Refresher#refreshPeriodically(org.apache.cocoon.caching.SimpleCacheKey, java.lang.String, long, java.lang.String)
     */
    public void refreshPeriodically(SimpleCacheKey cacheKey,
                                    String uri,
                                    long expires,
                                    String cacheRole) {
        TargetConfiguration conf = (TargetConfiguration) this.entries.get(cacheKey);
        if ( conf == null ) {
            conf = new TargetConfiguration(cacheKey, uri, expires, cacheRole);
            this.entries.put(cacheKey, conf);
        } else {
            conf.update(uri, expires, cacheRole);
        }

        final String name = cacheKey.getKey();
        try {
            this.scheduler.addPeriodicJob(name, this.schedulerTarget,
                                  expires,
                                  true,
                                  conf.parameters,
                                  conf.map);
        } catch (CascadingException ignore) {
        }

        this.changed = true;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.cornerstone.services.scheduler.Target#targetTriggered(java.lang.String)
     */
    public void execute(String name) {
        if ( this.changed && this.writeSource != null ) {
            this.changed = false;
            try {
                OutputStream stream = ((ModifiableSource)this.writeSource).getOutputStream();
                Writer writer = new OutputStreamWriter(stream);
                
                writer.write("<targets>\n");
                final Iterator iter = this.entries.values().iterator();
                while ( iter.hasNext() ) {
                    this.writeRefreshJobConfiguration(writer, (TargetConfiguration) iter.next()); 
                }
                writer.write("</targets>\n");
                writer.flush();
                writer.close();
            } catch (IOException ignore) {
            }
            
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
		writer.write(URLEncoder.encode(((SimpleCacheKey)c.map.get(CACHE_KEY)).getKey()));
		writer.write("\"/>\n");
	}

	class TargetConfiguration {
        
        public Parameters parameters;
        
        public Map        map;
        
        public TargetConfiguration(SimpleCacheKey cacheKey,
                            String uri,
                            long expires,
                            String cacheRole) {
            this.parameters = new Parameters();
            this.map = new HashMap();

            this.update(uri, expires, cacheRole);
            this.map.put(CACHE_KEY, cacheKey);
        }
        
        public void update(String uri, long expires, String cacheRole) {
            this.parameters.setParameter(ATTR_URI,uri);
            this.parameters.setParameter(PARAM_CACHE_ROLE, cacheRole);
            this.parameters.setParameter(PARAM_CACHE_EXPIRES, String.valueOf(expires));
        }
        
    }
}
