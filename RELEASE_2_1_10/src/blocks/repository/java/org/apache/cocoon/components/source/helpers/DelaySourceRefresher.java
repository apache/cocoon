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
package org.apache.cocoon.components.source.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.WrapperComponentManager;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;

import org.apache.cocoon.Constants;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.thread.RunnableManager;
import org.apache.cocoon.environment.background.BackgroundEnvironment;
import org.apache.cocoon.util.NetUtils;

/**
 * Default implementation of the refresher.
 *
 * @since 2.1.1
 * @version $Id$
 */
public class DelaySourceRefresher extends AbstractLogEnabled
                                  implements Contextualizable, Serviceable, Configurable,
                                             Disposable, ThreadSafe, SourceRefresher {

    private static final String PARAM_WRITE_FILE          = "write-file";

	private static final String DEFAULT_WRITE_FILE        = "refresher-targets.xml";

    private static final String TAGNAME_TARGET            = "target";
	private static final String ATTR_KEY                  = "key";
	private static final String ATTR_URI                  = "uri";
    private static final String ATTR_INTERVAL             = "interval";


    protected Context context;

    // service dependencies
    protected ServiceManager manager;
    protected SourceResolver resolver;
    protected RunnableManager runnable;

    // the scheduled targets to be persisted and recovered upon restart
    protected Map entries = Collections.synchronizedMap(new HashMap());

    // the cocoon working directory
    protected File workDir;

    /** The source to persist refresher entries into */
    protected File configFile;

    // whether anything changed to the entries since last persisting them
    protected volatile boolean changed;

    protected ConfigurationTask configurationTask;


    // ---------------------------------------------------- Lifecycle

	/* (non-Javadoc)
	 * @see Contextualizable#contextualize(Context)
	 */
	public void contextualize(Context context) throws ContextException {
        this.context = context;
        this.workDir = (File) context.get(Constants.CONTEXT_WORK_DIR);
	}

    /* (non-Javadoc)
     * @see Serviceable#service(ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
        this.runnable = (RunnableManager) this.manager.lookup(RunnableManager.ROLE);
    }

    public void configure(Configuration configuration) throws ConfigurationException {
        Parameters parameters = Parameters.fromConfiguration(configuration);
        long interval = parameters.getParameterAsLong("interval", 0);
        if (interval > 0) {
            String fileName = parameters.getParameter(PARAM_WRITE_FILE, DEFAULT_WRITE_FILE);
            this.configFile = new File(this.workDir, fileName);
            if (this.configFile.exists() && !this.configFile.canWrite()) {
                throw new ConfigurationException("Parameter 'write-source' resolves to not modifiable file: " +
                                                 this.configFile);
            }
            if (!this.configFile.getParentFile().exists() && !this.configFile.getParentFile().mkdirs()) {
                throw new ConfigurationException("Can not create parent directory for: " +
                                                 this.configFile);
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Write source location: " + this.configFile);
            }

            setupRefreshJobs(readRefreshJobConfiguration());
            startConfigurationTask(interval);
        } else {
        	if (getLogger().isInfoEnabled()) {
				getLogger().info("Not writing update targets to file.");
        	}
        }

        // Setup any in-line configured tasks
        setupRefreshJobs(configuration);
    }

    /* (non-Javadoc)
     * @see Disposable#dispose()
     */
    public void dispose() {
    	stopConfigurationTask();
        if (this.runnable != null) {
            this.manager.release(this.runnable);
            this.runnable = null;
        }
        if (this.resolver != null) {
            this.manager.release(this.resolver);
            this.resolver = null;
        }
        this.manager = null;
    }

    // ---------------------------------------------------- SourceRefresher implementation

    /* (non-Javadoc)
     * @see SourceRefresher#refresh
     */
    public void refresh(String name,
                        String uri,
                        Parameters parameters)
    throws SourceException {
		final long interval = parameters.getParameterAsLong(PARAM_CACHE_INTERVAL, -1);
		if (uri != null && interval > 0) {
            addRefreshSource(name, uri, interval, interval);
		} else {
            removeRefreshSource(name);
        }
    }

    protected void addRefreshSource(String key, String uri, long delay, long interval) {
        RefresherTask task = (RefresherTask) this.entries.get(key);
        if (task == null) {
            // New source added.
            task = new RefresherTask(key, uri, interval);
            task.enableLogging(getLogger());
            this.entries.put(key, task);
            this.runnable.execute(task, interval, interval);
            this.changed = true;
        } else if (task.interval != interval) {
            // Existing source refresh interval updated.
            task.update(uri, interval);
            this.runnable.remove(task);
            this.runnable.execute(task, interval, interval);
            this.changed = true;
        } else {
            // No change.
        }
    }

    protected void removeRefreshSource(String key) {
        RefresherTask task = (RefresherTask) this.entries.get(key);
        if (task != null) {
            this.entries.remove(key);
            this.runnable.remove(task);
            this.changed = true;
        }
    }

    // ---------------------------------------------------- Implementation

    /**
     *
     */
    private Configuration readRefreshJobConfiguration() {
        Source source = null;
        SAXConfigurationHandler b = new SAXConfigurationHandler();
        try {
            if (this.configFile.exists()) {
                source = this.resolver.resolveURI(this.configFile.toURL().toString());
                SourceUtil.toSAX(this.manager, source, source.getMimeType(), b);
            }
        } catch (Exception ignore) {
            getLogger().warn("Unable to read configuration from " + this.configFile);
        } finally {
            if (source != null) {
                this.resolver.release(source);
            }
        }
        return b.getConfiguration();
    }


    /**
	 * @param conf
	 */
	private void setupRefreshJobs(final Configuration conf) {
        if (conf != null) {
            final Configuration[] children = conf.getChildren(TAGNAME_TARGET);
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    try {
                        setupSingleRefreshJob(children[i]);
                    } catch (CascadingException ignore) {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("Setting up refresh job, ignoring exception:", ignore);
                        }
                    }
                }
            }
        }
    }

    /**
	 * @param conf
	 * @throws ConfigurationException
	 */
	private void setupSingleRefreshJob(final Configuration conf) throws ConfigurationException {
		try {
            String key = NetUtils.decode(conf.getAttribute(ATTR_KEY), "utf-8");
            String uri = NetUtils.decode(conf.getAttribute(ATTR_URI), "utf-8");
            long interval = conf.getAttributeAsLong(ATTR_INTERVAL);
            addRefreshSource(key, uri, 10, interval);
        } catch (UnsupportedEncodingException e) {
            /* Won't happen */
        }
	}

    /**
     * @param interval
     */
    protected void startConfigurationTask(long interval) {
        configurationTask = new ConfigurationTask();
        configurationTask.enableLogging(getLogger());
        runnable.execute(configurationTask, interval, interval);
    }

    protected void stopConfigurationTask() {
        if (this.configurationTask != null) {
            this.runnable.remove(this.configurationTask);
            this.configurationTask.run();
            this.configurationTask = null;
        }
    }

    /**
     * Task which writes refresher configuraiton into the source.
     */
    protected class ConfigurationTask extends AbstractLogEnabled
                                      implements Runnable {
        public void run() {
            if (changed) {
                // Reset the flag.
                changed = false;

                boolean success = true;
                Writer writer = null;
                try {
                    writer = new OutputStreamWriter(new FileOutputStream(configFile), "utf-8");
                    writer.write("<targets>\n");

                    try {
                        final Iterator i = entries.values().iterator();
                        while (i.hasNext()) {
                            RefresherTask task = (RefresherTask) i.next();
                            writer.write(task.toXML());
                        }
                    } catch (ConcurrentModificationException e) {
                        // List of targets has been changed, unable to save it completely.
                        // Will re-try writing the list next time.
                        success = false;
                    }

                    writer.write("</targets>\n");
                } catch (IOException e) {
                    // Got I/O exception while writing the list.
                    // Will re-try writing the list next time.
                    success = false;
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Error writing targets to file.", e);
                    }
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e) { /* ignored */ }
                    }
                }

                // Set the flag to run next time if failed this time
                if (!success) {
                    changed = true;
                }
            }
        }
    }

    protected class RefresherTask extends AbstractLogEnabled
                                  implements Runnable {
        private String key;
        private String uri;
        private long interval;


        public RefresherTask(String key, String uri, long interval) {
            this.key = key;
            this.uri = uri;
            this.interval = interval;
        }

        public void run() {
            if (this.uri != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Refreshing " + this.uri);
                }

                // Setup Environment
                final BackgroundEnvironment env;
                try {
                    org.apache.cocoon.environment.Context ctx =
                            (org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
                    env = new BackgroundEnvironment(getLogger(), ctx);
                } catch (ContextException e) {
                    throw new CascadingRuntimeException("No context found", e);
                } catch (MalformedURLException e) {
                    // Unlikely to happen
                    throw new CascadingRuntimeException("Invalid URL", e);
                }
                Processor processor;
                try {
                    processor = (Processor) manager.lookup(Processor.ROLE);
                } catch (ServiceException e) {
                    throw new CascadingRuntimeException("No processor found", e);
                }

                final Object key = CocoonComponentManager.startProcessing(env);
                CocoonComponentManager.enterEnvironment(env, new WrapperComponentManager(manager), processor);
                try {
                    // Refresh Source
                    Source source = null;
                    try {
                        source = resolver.resolveURI(uri);
                        source.refresh();
                    } catch (IOException e) {
                        getLogger().error("Error refreshing source", e);
                    } finally {
                        if (source != null) {
                            resolver.release(source);
                        }
                    }
                } finally {
                    CocoonComponentManager.leaveEnvironment();
                    CocoonComponentManager.endProcessing(env, key);
                    if (manager != null) {
                        manager.release(processor);
                    }
                }
            }
        }

        public void update(String uri, long interval) {
            this.uri = uri;
            this.interval = interval;
        }

        public String toXML() {
            String key = null;
            String uri = null;
            try {
                key = NetUtils.encode(this.key, "utf-8");
                uri = NetUtils.encode(this.uri, "utf-8");
            } catch (UnsupportedEncodingException e) {
                /* Won't happen */
            }
            StringBuffer s = new StringBuffer();
            s.append('<').append(TAGNAME_TARGET).append(' ');
            s.append(ATTR_KEY).append("=\"").append(key).append("\" ");
            s.append(ATTR_URI).append("=\"").append(uri).append("\" ");
            s.append(ATTR_INTERVAL).append("=\"").append(interval).append("\" />\n");
            return s.toString();
        }
    }
}
