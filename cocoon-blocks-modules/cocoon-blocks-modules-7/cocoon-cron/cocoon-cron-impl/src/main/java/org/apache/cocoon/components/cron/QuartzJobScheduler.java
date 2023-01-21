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

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
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
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.thread.RunnableManager;
import org.apache.cocoon.thread.ThreadPool;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.util.avalon.CLLoggerWrapper;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.impl.jdbcjobstore.InvalidConfigurationException;
import org.quartz.impl.jdbcjobstore.JobStoreSupport;
import org.quartz.simpl.RAMJobStore;
import org.quartz.spi.JobStore;
import org.quartz.spi.TriggerFiredBundle;
import org.quartz.utils.ConnectionProvider;
import org.quartz.utils.DBConnectionManager;
import org.quartz.utils.JNDIConnectionProvider;

/**
 * This component can either schedule jobs or directly execute one.
 *
 * @version $Id$
 *
 * @since 2.1.1
 */
public class QuartzJobScheduler extends AbstractLogEnabled
                                implements JobScheduler, ThreadSafe,
                                           Serviceable, Configurable, Startable,
                                           Disposable, Contextualizable, Initializable {

    /** Map key for the component role */
    static final String DATA_MAP_ROLE = "QuartzJobScheduler.ROLE";

    /** Map key for the job object */
    static final String DATA_MAP_OBJECT = "QuartzJobScheduler.Object";

    /** Map key for the job name */
    static final String DATA_MAP_NAME = "QuartzJobScheduler.JobName";

    /** Map key for the avalon context */
    static final String DATA_MAP_CONTEXT = "QuartzJobScheduler.Context";

    /** Map key for the service manager */
    static final String DATA_MAP_MANAGER = "QuartzJobScheduler.ServiceManager";

    /**
     * Map key for the logger
     * @deprecated Use commons logging.
     */
    static final String DATA_MAP_LOGGER = "QuartzJobScheduler.Logger";

    /** Map key for the concurrent run property */
    static final String DATA_MAP_RUN_CONCURRENT = "QuartzJobScheduler.RunConcurrently";

    /** Map key for additional Parameters */
    static final String DATA_MAP_PARAMETERS = "QuartzJobScheduler.Parameters";

    /** Map key for additional Object Map */
    static final String DATA_MAP_OBJECTMAP = "QuartzJobScheduler.Map";

    /* Map key for the last JobExecutionContext
    static final String DATA_MAP_JOB_EXECUTION_CONTEXT = "QuartzJobScheduler.JobExecutionContext"; */

    /** Map key for the run status */
    static final String DATA_MAP_KEY_ISRUNNING = "QuartzJobExecutor.isRunning";


    /** The group name */
    static final String DEFAULT_QUARTZ_JOB_GROUP = "Cocoon";

    /** The scheduler name */
    static final String DEFAULT_QUARTZ_SCHEDULER_NAME = "Cocoon";

    /** The Avalon Context instance */
    private Context context;

    /** The PooledExecutor instance */
    private ThreadPool executor;

    /** The quartz scheduler */
    private Scheduler scheduler;

    /** The ServiceManager instance */
    private ServiceManager manager;

    /** The configuration, parsed in initialize() */
    private Configuration config;

    /** Should we wait for running jobs to terminate on shutdown ? */
    private boolean m_shutdownGraceful;

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobScheduler#getJobNames()
     */
    public String[] getJobNames() {
        try {
            final String[] names = scheduler.getJobNames(DEFAULT_QUARTZ_JOB_GROUP);
            Arrays.sort(names);

            return names;
        } catch (final SchedulerException se) {
            getLogger().error("could not gather job names", se);
        }

        return new String[0];
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobScheduler#getSchedulerEntry(java.lang.String)
     */
    public JobSchedulerEntry getJobSchedulerEntry(String jobname) {
        try {
            return new QuartzJobSchedulerEntry(jobname, scheduler);
        } catch (final Exception e) {
            getLogger().error("cannot create QuartzJobSchedulerEntry", e);
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobScheduler#addJob(java.lang.String, java.lang.Object, java.lang.String, boolean, org.apache.avalon.framework.parameters.Parameters, java.util.Map)
     */
    public void addJob(final String name, final Object job, final String cronSpec, final boolean canRunConcurrently,
                       final Parameters params, final Map objects)
    throws CascadingException {
        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(DATA_MAP_OBJECT, job);
        addJob(name, jobDataMap, cronSpec, canRunConcurrently, params, objects);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobScheduler#addJob(java.lang.String, java.lang.String, java.lang.String, boolean, org.apache.avalon.framework.parameters.Parameters, java.util.Map)
     */
    public void addJob(final String name, final String jobrole, final String cronSpec,
                       final boolean canRunConcurrently, final Parameters params, final Map objects)
    throws CascadingException {
        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(DATA_MAP_ROLE, jobrole);
        addJob(name, jobDataMap, cronSpec, canRunConcurrently, params, objects);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobScheduler#addJob(java.lang.String, java.lang.Object, java.lang.String, boolean)
     */
    public void addJob(final String name, final Object job, final String cronSpec, final boolean canRunConcurrently)
    throws CascadingException {
        if (!(job instanceof CronJob) && !(job instanceof Runnable) && !(job instanceof Job)) {
            throw new CascadingException("Job object is neither an instance of " + CronJob.class.getName() + "," +
                                         Runnable.class.getName() + " nor " + Job.class.getName());
        }

        addJob(name, job, cronSpec, canRunConcurrently, null, null);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobScheduler#addJob(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public void addJob(final String name, final String jobrole, final String cronSpec, final boolean canRunConcurrently)
    throws CascadingException {
        addJob(name, jobrole, cronSpec, canRunConcurrently, null, null);
    }

    /**
     * Schedule a period job. Note that if a Job already has same name then it is overwritten.
     *
     * @param name the name of the job
     * @param jobrole The Avalon components role name of the job itself
     * @param period Every period seconds this job is started
     * @param canRunConcurrently whether this job can run even previous scheduled runs are still running
     * @param params additional Parameters to be passed to the job
     * @param objects additional objects to be passed to the job
     *
     * @throws CascadingException in case of failures
     */
    public void addPeriodicJob(String name, String jobrole, long period, boolean canRunConcurrently, Parameters params,
                               Map objects)
    throws CascadingException {
        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(DATA_MAP_ROLE, jobrole);

        final long ms = period * 1000;
        final SimpleTrigger timeEntry =
            new SimpleTrigger(name, DEFAULT_QUARTZ_JOB_GROUP, new Date(System.currentTimeMillis() + ms), null,
                              SimpleTrigger.REPEAT_INDEFINITELY, ms);

        addJob(name, jobDataMap, timeEntry, canRunConcurrently, params, objects);
    }

    /**
     * Schedule a periodic job. The job is started the first time when the period has passed.  Note that if a job with
     * the same name has already beed added it is overwritten.
     *
     * @param name the name of the job
     * @param job The job object itself. It must implement either CronJob, Runnable or might also be an implementation
     *        specific class (i.e. org.quartz.Job)
     * @param period Every period seconds this job is started
     * @param canRunConcurrently whether this job can run even previous scheduled runs are still running
     * @param params Additional Parameters to setup CronJob
     * @param objects A Map with additional object to setup CronJob
     */
    public void addPeriodicJob(String name, Object job, long period, boolean canRunConcurrently, Parameters params,
                               Map objects)
    throws CascadingException {
        if (!(job instanceof CronJob) && !(job instanceof Runnable) && !(job instanceof Job)) {
            throw new CascadingException("Job object is neither an instance of " + CronJob.class.getName() + "," +
                                         Runnable.class.getName() + " nor " + Job.class.getName());
        }
        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(DATA_MAP_OBJECT, job);

        final long ms = period * 1000;
        final SimpleTrigger timeEntry =
            new SimpleTrigger(name, DEFAULT_QUARTZ_JOB_GROUP, new Date(System.currentTimeMillis() + ms), null,
                              SimpleTrigger.REPEAT_INDEFINITELY, ms);

        addJob(name, jobDataMap, timeEntry, canRunConcurrently, params, objects);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(final Configuration config)
    throws ConfigurationException {
        this.config = config;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        try {
            if (getLogger().isInfoEnabled()) {
                getLogger().info("shutting down scheduler " +
                                 (m_shutdownGraceful ? "graceful (waiting for running jobs to complete)"
                                  : "immediately (killing running jobs)"));
            }

            scheduler.shutdown(m_shutdownGraceful);
            scheduler = null;
        } catch (final SchedulerException se) {
            getLogger().error("failure during scheduler shutdown", se);
        }

        this.executor = null;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
    	this.context = context;
    }

    public void initialize() throws Exception {
        try {
            // If cocoon reloads (or is it the container that reload us?)
            // we cannot create the same scheduler again
            final String runID = new Date().toString().replace(' ', '_');
            final QuartzThreadPool pool = createThreadPool(this.config.getChild("thread-pool"));
            final JobStore store = createJobStore(DEFAULT_QUARTZ_SCHEDULER_NAME, runID, this.config.getChild("store"));
            DirectSchedulerFactory.getInstance().createScheduler(DEFAULT_QUARTZ_SCHEDULER_NAME, runID, pool, store);
            // scheduler = DirectSchedulerFactory.getInstance().getScheduler(DEFAULT_QUARTZ_SCHEDULER_NAME, runID);
            scheduler = DirectSchedulerFactory.getInstance().getScheduler(DEFAULT_QUARTZ_SCHEDULER_NAME);
        } catch (final SchedulerException se) {
            throw new ConfigurationException("cannot create a quartz scheduler", se);
        }

        final Configuration[] triggers = this.config.getChild("triggers").getChildren("trigger");
        createTriggers(triggers);

        // We're finished with the configuration
        this.config = null;

        if (getLogger().isDebugEnabled() && (triggers.length == 0)) {
            getLogger().debug("no triggers configured at startup");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobScheduler#fireTarget(java.lang.Object)
     */
    public boolean fireJob(final Object job) {
        return fireJob(job.getClass().getName(), job);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobScheduler#fireTarget(java.lang.String)
     */
    public boolean fireJob(final String jobrole) {
        Object job = null;

        try {
            job = manager.lookup(jobrole);

            return fireJob(jobrole, job);
        } catch (final ServiceException se) {
            getLogger().error("cannot fire job " + jobrole, se);
        } finally {
            manager.release(job);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobScheduler#fireJob(java.lang.Object, org.apache.avalon.framework.parameters.Parameters, java.util.Map)
     */
    public boolean fireJob(final Object job, final Parameters params, final Map objects)
    throws CascadingException {
        if (job instanceof ConfigurableCronJob) {
            ((ConfigurableCronJob)job).setup(params, objects);
        }

        return fireJob(job);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobScheduler#fireJob(java.lang.String, org.apache.avalon.framework.parameters.Parameters, java.util.Map)
     */
    public boolean fireJob(final String jobrole, final Parameters params, final Map objects)
    throws CascadingException {
        Object job = null;

        try {
            job = manager.lookup(jobrole);

            if (job instanceof ConfigurableCronJob) {
                ((ConfigurableCronJob)job).setup(params, objects);
            }

            return fireJob(jobrole, job);
        } catch (final ServiceException se) {
            getLogger().error("cannot fire job " + jobrole, se);
        } finally {
            manager.release(job);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobScheduler#fireJobAt(java.util.Date, java.lang.String, java.lang.Object)
     */
    public void fireJobAt(final Date date, final String name, final Object job)
    throws CascadingException {
        fireJobAt(date, name, job, null, null);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobScheduler#fireJobAt(java.util.Date, java.lang.String, java.lang.String)
     */
    public void fireJobAt(final Date date, final String name, final String jobrole)
    throws CascadingException {
        fireJobAt(date, name, jobrole, null, null);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobScheduler#fireJobAt(java.util.Date, java.lang.String, java.lang.Object, org.apache.avalon.framework.parameters.Parameters, java.util.Map)
     */
    public void fireJobAt(final Date date, final String name, final Object job, final Parameters params,
                          final Map objects)
    throws CascadingException {
        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(DATA_MAP_OBJECT, job);
        addJob(name, jobDataMap, date, true, params, objects);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobScheduler#fireJobAt(java.util.Date, java.lang.String, java.lang.String, org.apache.avalon.framework.parameters.Parameters, java.util.Map)
     */
    public void fireJobAt(final Date date, final String name, final String jobrole, final Parameters params,
                          final Map objects)
    throws CascadingException {
        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(DATA_MAP_ROLE, jobrole);
        addJob(name, jobDataMap, date, true, params, objects);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobScheduler#removeJob(java.lang.String)
     */
    public void removeJob(final String name)
    throws NoSuchElementException {
        try {
            if (scheduler.deleteJob(name, DEFAULT_QUARTZ_JOB_GROUP)) {
                getLogger().info("job " + name + " removed by request");
            } else {
                getLogger().error("couldn't remove requested job " + name);
            }
        } catch (final SchedulerException se) {
            getLogger().error("cannot remove job " + name, se);
            throw new NoSuchElementException(se.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(final ServiceManager manager)
    throws ServiceException {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Startable#start()
     */
    public void start()
    throws Exception {
        scheduler.start();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Startable#stop()
     */
    public void stop()
    throws Exception {
        scheduler.standby();
    }

    /**
     * Add a job to the scheduler
     *
     * @param name The name of the job to add
     * @param jobDataMap The JobDataMap to use for this job
     * @param date the date to schedule this job
     * @param canRunConcurrently whether this job can be run concurrently
     * @param params Additional Parameters to setup CronJob
     * @param objects A Map with additional object to setup CronJob
     *
     * @throws CascadingException thrown in case of errors
     */
    private void addJob(final String name, final JobDataMap jobDataMap, final Date date,
                        final boolean canRunConcurrently, final Parameters params, final Map objects)
    throws CascadingException {
        final SimpleTrigger trigger = new SimpleTrigger(name, DEFAULT_QUARTZ_JOB_GROUP, date);
        addJob(name, jobDataMap, trigger, canRunConcurrently, params, objects);
    }

    /**
     * Add a job to the scheduler
     *
     * @param name The name of the job to add
     * @param jobDataMap The JobDataMap to use for this job
     * @param cronSpec a Cron time expression
     * @param canRunConcurrently whether this job can be run concurrently
     * @param params Additional Parameters to setup CronJob
     * @param objects A Map with additional object to setup CronJob
     *
     * @throws CascadingException thrown in case of errors
     */
    private void addJob(final String name, final JobDataMap jobDataMap, final String cronSpec,
                        final boolean canRunConcurrently, final Parameters params, final Map objects)
    throws CascadingException {
        final CronTrigger cronJobEntry = new CronTrigger(name, DEFAULT_QUARTZ_JOB_GROUP);

        try {
            cronJobEntry.setCronExpression(cronSpec);
        } catch (final ParseException pe) {
            throw new CascadingException(pe.getMessage(), pe);
        }

        addJob(name, jobDataMap, cronJobEntry, canRunConcurrently, params, objects);
    }

    /**
     * Add a job to the scheduler
     *
     * @param name The name of the job to add
     * @param jobDataMap The JobDataMap to use for this job
     * @param trigger a Trigger
     * @param canRunConcurrently whether this job can be run concurrently
     * @param params Additional Parameters to setup CronJob (might be null)
     * @param objects A Map with additional object to setup CronJob (might be null)
     *
     * @throws CascadingException thrown in case of errors
     */
    private void addJob(final String name, final JobDataMap jobDataMap, final Trigger trigger,
                        final boolean canRunConcurrently, final Parameters params, final Map objects)
    throws CascadingException {
        try {
            final JobDetail jobdetail = scheduler.getJobDetail(name, DEFAULT_QUARTZ_JOB_GROUP);
            if (jobdetail != null) {
                removeJob(name);
            }
        } catch (final SchedulerException ignored) {
        }

        initDataMap(jobDataMap, name, canRunConcurrently, params, objects);

        final JobDetail detail = createJobDetail(name, jobDataMap);

        if (getLogger().isInfoEnabled()) {
            getLogger().info("Adding CronJob '" + trigger.getFullName() + "'");
        }

        try {
            scheduler.scheduleJob(detail, trigger);
        } catch (final SchedulerException se) {
            throw new CascadingException(se.getMessage(), se);
        }

        if (getLogger().isDebugEnabled()) {
            if (trigger instanceof CronTrigger) {
                getLogger().debug("Time schedule summary:\n" + ((CronTrigger)trigger).getExpressionSummary());
            } else {
                getLogger().debug("Next scheduled time: " + trigger.getNextFireTime());
            }
        }
    }

    protected JobDataMap initDataMap(JobDataMap jobDataMap, String jobName, boolean concurent,
                                     Parameters params, Map objects) {
        jobDataMap.put(DATA_MAP_NAME, jobName);
        jobDataMap.put(DATA_MAP_LOGGER, new CLLoggerWrapper(getLogger()));
        jobDataMap.put(DATA_MAP_CONTEXT, this.context);
        jobDataMap.put(DATA_MAP_MANAGER, this.manager);
        jobDataMap.put(DATA_MAP_RUN_CONCURRENT, (concurent? Boolean.TRUE: Boolean.FALSE));
        if (null != params) {
            jobDataMap.put(DATA_MAP_PARAMETERS, params);
        }
        if (null != objects) {
            jobDataMap.put(DATA_MAP_OBJECTMAP, objects);
        }
        return jobDataMap;
    }

    protected JobDetail createJobDetail(String name, JobDataMap jobDataMap) {
        final JobDetail detail = new JobDetail(name, DEFAULT_QUARTZ_JOB_GROUP, QuartzJobExecutor.class);
        detail.setJobDataMap(jobDataMap);
        return detail;
    }

    /**
     * Create a QuartzThreadPool
     *
     * @param poolConfig Configuration element for the thread pool
     *
     * @return QuartzThreadPool
     */
    private QuartzThreadPool createThreadPool(final Configuration poolConfig)
    throws ServiceException {
        final int queueSize = poolConfig.getChild("queue-size").getValueAsInteger(-1);
        final int maxPoolSize = poolConfig.getChild("max-pool-size").getValueAsInteger(-1);
        final int minPoolSize = poolConfig.getChild("min-pool-size").getValueAsInteger(-1);
        final int keepAliveTimeMs = poolConfig.getChild("keep-alive-time-ms").getValueAsInteger(-1);
        final String blockPolicy = poolConfig.getChild("block-policy").getValue(null);
        m_shutdownGraceful = poolConfig.getChild("shutdown-graceful").getValueAsBoolean(true);
        final int shutdownWaitTimeMs = poolConfig.getChild("shutdown-wait-time-ms").getValueAsInteger(-1);
        final RunnableManager runnableManager = (RunnableManager)this.manager.lookup(RunnableManager.ROLE);
        this.executor = runnableManager.createPool(queueSize,
                                                   maxPoolSize,
                                                   minPoolSize,
                                                   Thread.NORM_PRIORITY,
                                                   false, // no daemon
                                                   keepAliveTimeMs,
                                                   blockPolicy,
                                                   m_shutdownGraceful,
                                                   shutdownWaitTimeMs);
        return new QuartzThreadPool(this.executor);
    }

    /**
     * Create the triggers
     *
     * @param triggers array of trigger configuration elements
     *
     * @throws ConfigurationException thrown in case of configuration failures
     */
    private void createTriggers(final Configuration[] triggers)
    throws ConfigurationException {
        for (int i = 0; i < triggers.length; i++) {
            String cron = triggers[i].getChild("cron").getValue(null);

            if (null == cron) {
                final String seconds = triggers[i].getChild("seconds").getValue("0");
                final String minutes = triggers[i].getChild("minutes").getValue("*");
                final String hours = triggers[i].getChild("hours").getValue("*");
                final String days = triggers[i].getChild("days").getValue("*");
                final String months = triggers[i].getChild("months").getValue("*");
                final String weekdays = triggers[i].getChild("weekdays").getValue("?");
                final String years = triggers[i].getChild("years").getValue("*");
                cron = seconds + " " + minutes + " " + hours + " " + days + " " + months + " " + weekdays + " " +
                       years;
            }

            try {
                addJob(triggers[i].getAttribute("name"), triggers[i].getAttribute("target"), cron,
                       triggers[i].getAttributeAsBoolean("concurrent-runs", true));
            } catch (final CascadingException ce) {
                throw new ConfigurationException("failed adding trigger to scheduler", ce);
            }
        }
    }

    private JobStore createJobStore(String instanceName, String instanceID, final Configuration configuration)
    throws ConfigurationException {
        String type = configuration.getAttribute("type", "ram");
        if (type.equals("ram")) {
            return new RAMJobStore();
        }

        JobStoreSupport store;
        if (type.equals("tx")) {
            store = new QuartzJobStoreTX(this.manager, this.context);
        } else if (type.equals("cmt")) {
            store = new QuartzJobStoreCMT(this.manager, this.context);
        } else {
            throw new ConfigurationException("Unknown store type: " + type);
        }

        Configuration dsConfig = configuration.getChild("datasource", false);
        if (dsConfig == null) {
            throw new ConfigurationException("Store " + type + " requires datasource configuration.");
        }

        String dsName = dsConfig.getValue();
        String dsType = dsConfig.getAttribute("provider", "jndi");

        ConnectionProvider provider;
        if (dsType.equals("jndi")) {
            provider = new JNDIConnectionProvider(dsName, false);
        } else if (dsType.equals("excalibur")) {
            provider = new DataSourceComponentConnectionProvider(dsName, this.manager);
        } else {
            // assume class name
            try {
                provider = (ConnectionProvider)Class.forName(dsType).newInstance();
            } catch (Exception e) {
                throw new ConfigurationException("Could not instantiate ConnectionProvider class " + dsType);
            }
        }

        store.setInstanceName(instanceName);
        store.setInstanceId(instanceID);
        store.setDataSource(dsType + ":" + dsName);
        DBConnectionManager.getInstance().addConnectionProvider(dsType + ":" + dsName, provider);

        String delegate = configuration.getAttribute("delegate", null);
        try {
            if (delegate != null) {
                store.setDriverDelegateClass(delegate);
            }
        } catch (InvalidConfigurationException e) {
            throw new ConfigurationException("Could not instantiate DriverDelegate class " + delegate, e);
        }

        return store;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobScheduler#fireTarget(java.lang.Object)
     */
    private boolean fireJob(final String name, final Object job) {
        try {
            if (job instanceof CronJob || job instanceof Job) {
                JobDataMap jobDataMap = new JobDataMap();
                jobDataMap.put(DATA_MAP_OBJECT, job);
                initDataMap(jobDataMap, name, true, null, null);

                final JobDetail detail = createJobDetail(name, jobDataMap);

                final Trigger trigger = new SimpleTrigger(name, DEFAULT_QUARTZ_JOB_GROUP);

                TriggerFiredBundle fireBundle = new TriggerFiredBundle(detail, trigger, null, false, null, null, null, null);

                final Job executor = createJobExecutor();
                final JobExecutionContext context = new JobExecutionContext(this.scheduler, fireBundle, executor);

                this.executor.execute(new Runnable() {
                        public void run() {
                            try {
                                executor.execute(context);
                            } catch (JobExecutionException e) {
                                getLogger().error("Job '" + job + "' died.", e);
                            }
                        }
                    });
            } else if (job instanceof Runnable) {
                this.executor.execute((Runnable)job);
            } else {
                getLogger().error("Job named '" + name + "' is of invalid class: " + job.getClass().getName());
                return false;
            }

            return true;
        } catch (final InterruptedException ie) {
            getLogger().error("job " + name + " interrupted", ie);
        }

        return false;
    }

    protected Job createJobExecutor() {
        return new QuartzJobExecutor();
    }

    /**
     * A QuartzThreadPool for the Quartz Scheduler based on Doug Leas concurrency utilities PooledExecutor
     *
     * @version $Id$
     */
    private static class QuartzThreadPool extends AbstractLogEnabled
                                          implements org.quartz.spi.ThreadPool {
        /** Our executor thread pool */
        private ThreadPool executor;

        /**
         *
         */
        public QuartzThreadPool(final ThreadPool executor) {
            super();
            this.executor = executor;
        }

        /* (non-Javadoc)
         * @see org.quartz.spi.QuartzThreadPool#getPoolSize()
         */
        public int getPoolSize() {
            return this.executor.getMaxPoolSize();
        }

        /* (non-Javadoc)
         * @see org.quartz.spi.QuartzThreadPool#initialize()
         */
        public void initialize() {
        }

        /* (non-Javadoc)
         * @see org.quartz.spi.QuartzThreadPool#runInThread(java.lang.Runnable)
         */
        public boolean runInThread(final Runnable job) {
            try {
                this.executor.execute(job);
            } catch (final InterruptedException ie) {
                getLogger().error("Cronjob failed", ie);
            }

            return true;
        }

        /* (non-Javadoc)
         * @see org.quartz.spi.QuartzThreadPool#shutdown(boolean)
         */
        public void shutdown(final boolean waitForJobsToComplete) {
            // the pool is managed by the runnable manager, so we should not shut it down
            this.executor = null;
        }
    }
}
