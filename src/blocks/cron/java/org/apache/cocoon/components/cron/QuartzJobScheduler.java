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

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import org.quartz.impl.DirectSchedulerFactory;

import org.quartz.simpl.RAMJobStore;

import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;
import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;


/**
 * This component can either schedule jobs or directly execute one.
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: QuartzJobScheduler.java,v 1.10 2004/03/08 13:43:42 unico Exp $
 *
 * @since 2.1.1
 */
public class QuartzJobScheduler
extends AbstractLogEnabled
implements JobScheduler, Component, ThreadSafe, Serviceable, Configurable, Startable, Disposable, Contextualizable {
    /** ThreadPool policy RUN */
    private static final String POLICY_RUN = "RUN";

    /** ThreadPool policy WAIT */
    private static final String POLICY_WAIT = "WAIT";

    /** ThreadPool policy ABORT */
    private static final String POLICY_ABORT = "ABORT";

    /** ThreadPool policy DISCARD */
    private static final String POLICY_DISCARD = "DISCARD";

    /** ThreadPool policy DISCARD-OLDEST */
    private static final String POLICY_DISCARD_OLDEST = "DISCARDOLDEST";

    /** Map key for the component role */
    static final String DATA_MAP_ROLE = "QuartzJobScheduler.ROLE";

    /** Map key for the job object */
    static final String DATA_MAP_OBJECT = "QuartzJobScheduler.Object";

    /** Map key for the job name */
    static final String DATA_MAP_NAME = "QuartzJobScheduler.JobName";

    /** Map key for the service manager */
    static final String DATA_MAP_MANAGER = "QuartzJobScheduler.ServiceManager";

    /** Map key for the logger */
    static final String DATA_MAP_LOGGER = "QuartzJobScheduler.Logger";

    /** Map key for the concurrent run property */
    static final String DATA_MAP_RUN_CONCURRENT = "QuartzJobScheduler.RunConcurrently";

    /** Map key for additional Parameters */
    static final String DATA_MAP_PARAMETERS = "QuartzJobScheduler.Parameters";

    /** Map key for additional Object Map */
    static final String DATA_MAP_OBJECTMAP = "QuartzJobScheduler.Map";

    /** Map key for the last JobExecutionContext */
    static final String DATA_MAP_JOB_EXECUTION_CONTEXT = "QuartzJobScheduler.JobExecutionContext";

    /** The group name */
    static final String DEFAULT_QUARTZ_JOB_GROUP = "Cocoon";

    /** The scheduler name */
    static final String DEFAULT_QUARTZ_SCHEDULER_NAME = "Cocoon";

    /** The PooledExecutor instance */
    private PooledExecutor m_executor;

    /** The quartz scheduler */
    private Scheduler m_scheduler;

    /** The ServiceManager instance */
    private ServiceManager m_manager;

    /** Should we wait for running jobs to terminate on shutdown ? */
    private boolean m_shutdownGraceful;

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobScheduler#getJobNames()
     */
    public String[] getJobNames() {
        try {
            final String[] names = m_scheduler.getJobNames(DEFAULT_QUARTZ_JOB_GROUP);
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
            return new QuartzJobSchedulerEntry(jobname, m_scheduler);
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
        try {
            // If cocoon reloads (or is it the container that reload us?) 
            // we cannot create the same scheduler again
            final String runID = new Date().toString().replace(' ', '_');
            final ThreadPool pool = createThreadPool(config.getChild("thread-pool"));
            DirectSchedulerFactory.getInstance().createScheduler(DEFAULT_QUARTZ_SCHEDULER_NAME, runID, pool,
                                                                 new RAMJobStore());
            // m_scheduler = DirectSchedulerFactory.getInstance().getScheduler(DEFAULT_QUARTZ_SCHEDULER_NAME, runID);
            m_scheduler = DirectSchedulerFactory.getInstance().getScheduler(DEFAULT_QUARTZ_SCHEDULER_NAME);
        } catch (final SchedulerException se) {
            throw new ConfigurationException("cannot create a quartz scheduler", se);
        }

        final Configuration[] triggers = config.getChild("triggers").getChildren("trigger");
        createTriggers(triggers);

        if (getLogger().isDebugEnabled() && (triggers.length == 0)) {
            getLogger().debug("no triggers configured at startup");
        }
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

            m_scheduler.shutdown(m_shutdownGraceful);
            m_scheduler = null;
        } catch (final SchedulerException se) {
            getLogger().error("failure during scheduler shutdown", se);
        }

        m_executor = null;
    }

	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
	 */
	public void contextualize(Context context) throws ContextException {
		org.apache.cocoon.environment.Context c = (org.apache.cocoon.environment.Context)context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
		System.out.println("context: " + c.getRealPath("/") );
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
            job = m_manager.lookup(jobrole);

            return fireJob(jobrole, job);
        } catch (final ServiceException se) {
            getLogger().error("cannot fire job " + jobrole, se);
        } finally {
            m_manager.release(job);
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
            job = m_manager.lookup(jobrole);

            if (job instanceof ConfigurableCronJob) {
                ((ConfigurableCronJob)job).setup(params, objects);
            }

            return fireJob(jobrole, job);
        } catch (final ServiceException se) {
            getLogger().error("cannot fire job " + jobrole, se);
        } finally {
            m_manager.release(job);
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
            if (m_scheduler.deleteJob(name, DEFAULT_QUARTZ_JOB_GROUP)) {
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
        m_manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Startable#start()
     */
    public void start()
    throws Exception {
        m_scheduler.start();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Startable#stop()
     */
    public void stop()
    throws Exception {
        m_scheduler.pause();
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
            final JobDetail jobdetail = m_scheduler.getJobDetail(name, DEFAULT_QUARTZ_JOB_GROUP);

            if (jobdetail != null) {
                removeJob(name);
            }
        } catch (final SchedulerException se) {
        }

        jobDataMap.put(DATA_MAP_NAME, name);
        jobDataMap.put(DATA_MAP_LOGGER, getLogger());
        jobDataMap.put(DATA_MAP_MANAGER, m_manager);
        jobDataMap.put(DATA_MAP_RUN_CONCURRENT, new Boolean(canRunConcurrently));

        if (null != params) {
            jobDataMap.put(DATA_MAP_PARAMETERS, params);
        }

        if (null != objects) {
            jobDataMap.put(DATA_MAP_OBJECTMAP, objects);
        }

        final JobDetail detail = new JobDetail(name, DEFAULT_QUARTZ_JOB_GROUP, QuartzJobExecutor.class);
        detail.setJobDataMap(jobDataMap);

        if (getLogger().isInfoEnabled()) {
            getLogger().info("Adding CronJob '" + trigger.getFullName() + "'");
        }

        try {
            m_scheduler.scheduleJob(detail, trigger);
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

    /**
     * Create a ThreadPool
     *
     * @param poolConfig Configuration element for the thread pool
     *
     * @return ThreadPool
     */
    private ThreadPool createThreadPool(final Configuration poolConfig) {
        final boolean useQueueing = poolConfig.getChild("use-queueing").getValueAsBoolean(false);
        final int queueSize = poolConfig.getChild("queue-size").getValueAsInteger(-1);

        if (useQueueing) {
            if (queueSize > 0) {
                m_executor = new PooledExecutor(new BoundedBuffer(queueSize));
            } else {
                m_executor = new PooledExecutor(new LinkedQueue());
            }
        } else {
            m_executor = new PooledExecutor();
        }

        final int maxPoolSize = poolConfig.getChild("max-pool-size").getValueAsInteger(-1);

        if (maxPoolSize > 0) {
            m_executor.setMaximumPoolSize(maxPoolSize);
        } else {
            m_executor.setMaximumPoolSize(PooledExecutor.DEFAULT_MAXIMUMPOOLSIZE);
        }

        final int minPoolSize = poolConfig.getChild("min-pool-size").getValueAsInteger(-1);

        if (minPoolSize > 0) {
            m_executor.setMinimumPoolSize(minPoolSize);
        } else {
            m_executor.setMinimumPoolSize(PooledExecutor.DEFAULT_MINIMUMPOOLSIZE);
        }

        final int keepAliveTimeMs = poolConfig.getChild("keep-alive-time-ms").getValueAsInteger(-1);

        if (keepAliveTimeMs > 0) {
            m_executor.setKeepAliveTime(keepAliveTimeMs);
        } else {
            m_executor.setKeepAliveTime(PooledExecutor.DEFAULT_KEEPALIVETIME);
        }

        final String blockPolicy = poolConfig.getChild("block-policy").getValue(null);

        if (blockPolicy != null) {
            if (blockPolicy.equalsIgnoreCase(POLICY_ABORT)) {
                m_executor.abortWhenBlocked();
            } else if (blockPolicy.equalsIgnoreCase(POLICY_DISCARD)) {
                m_executor.discardWhenBlocked();
            } else if (blockPolicy.equalsIgnoreCase(POLICY_DISCARD_OLDEST)) {
                m_executor.discardOldestWhenBlocked();
            } else if (blockPolicy.equalsIgnoreCase(POLICY_RUN)) {
                m_executor.runWhenBlocked();
            } else if (blockPolicy.equalsIgnoreCase(POLICY_WAIT)) {
                m_executor.waitWhenBlocked();
            } else {
                getLogger().warn("Unknown block-policy configuration '" + blockPolicy + "'. Should be one of '" +
                                 POLICY_ABORT + "','" + POLICY_DISCARD + "','" + POLICY_DISCARD_OLDEST + "','" +
                                 POLICY_RUN + "','" + POLICY_WAIT + "'. Will use '" + POLICY_RUN + "'");
            }
        }

        m_shutdownGraceful = poolConfig.getChild("shutdown-graceful").getValueAsBoolean(true);

        final int shutdownWaitTimeMs = poolConfig.getChild("shutdown-wait-time-ms").getValueAsInteger(-1);
        final ThreadPool pool = new ThreadPool(m_executor, shutdownWaitTimeMs);
        pool.enableLogging(getLogger());

        if (getLogger().isInfoEnabled()) {
            getLogger().info("using a PooledExecutor as ThreadPool with queueing=" + useQueueing +
                             (useQueueing ? (",queue-size=" + ((queueSize > 0) ? ("" + queueSize) : "default")) : "") +
                             ",max-pool-size=" + m_executor.getMaximumPoolSize() + ",min-pool-size=" +
                             m_executor.getMinimumPoolSize() + ",keep-alive-time-ms=" + m_executor.getKeepAliveTime() +
                             ",block-policy='" + blockPolicy + "',shutdown-wait-time-ms=" +
                             ((shutdownWaitTimeMs > 0) ? ("" + shutdownWaitTimeMs) : "default"));
        }

        return pool;
    }

    /**
     * Create the tiggers
     *
     * @param tiggers array of tigger configuration elements
     *
     * @throws ConfigurationException thrown in case of configuration failures
     */
    private void createTriggers(final Configuration[] tiggers)
    throws ConfigurationException {
        for (int i = 0; i < tiggers.length; i++) {
            String cron = tiggers[i].getChild("cron").getValue(null);

            if (null == cron) {
                final String seconds = tiggers[i].getChild("seconds").getValue("0");
                final String minutes = tiggers[i].getChild("minutes").getValue("*");
                final String hours = tiggers[i].getChild("hours").getValue("*");
                final String days = tiggers[i].getChild("days").getValue("*");
                final String months = tiggers[i].getChild("months").getValue("*");
                final String weekdays = tiggers[i].getChild("weekdays").getValue("?");
                final String years = tiggers[i].getChild("years").getValue("*");
                cron = seconds + " " + minutes + " " + hours + " " + days + " " + months + " " + weekdays + " " +
                       years;
            }

            try {
                addJob(tiggers[i].getAttribute("name"), tiggers[i].getAttribute("target"), cron,
                       tiggers[i].getAttributeAsBoolean("concurrent-runs", true));
            } catch (final CascadingException ce) {
                throw new ConfigurationException("failed adding trigger to scheduler", ce);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobScheduler#fireTarget(java.lang.Object)
     */
    private boolean fireJob(final String name, final Object job) {
        try {
            if (job instanceof CronJob) {
                m_executor.execute(new Runnable() {
                        public void run() {
                            ((CronJob)job).execute(name);
                        }
                    });
            } else if (job instanceof Runnable) {
                m_executor.execute((Runnable)job);
            } else {
                getLogger().error("job named '" + name + "' is of invalid class: " + job.getClass().getName());

                return false;
            }

            return true;
        } catch (final InterruptedException ie) {
            getLogger().error("job " + name + " interrupted", ie);
        }

        return false;
    }

    /**
     * A ThreadPool for the Quartz Scheduler based on Doug Leas concurrency utilities PooledExecutor
     *
     * @author <a href="mailto:giacomo@otego.com">Giacomo Pati</a>
     * @version CVS $Id: QuartzJobScheduler.java,v 1.10 2004/03/08 13:43:42 unico Exp $
     */
    private static class ThreadPool
    extends AbstractLogEnabled
    implements org.quartz.spi.ThreadPool {
        /** Our executor thread pool */
        private PooledExecutor m_executor;

        /** How long to wait for running jobs to terminate on disposition */
        private int m_shutdownWaitTimeMs;

        /**
         *
         */
        public ThreadPool(final PooledExecutor executor, final int shutownWaitTimeMs) {
            super();
            m_executor = executor;
            m_shutdownWaitTimeMs = shutownWaitTimeMs;
        }

        /* (non-Javadoc)
         * @see org.quartz.spi.ThreadPool#getPoolSize()
         */
        public int getPoolSize() {
            return m_executor.getMaximumPoolSize();
        }

        /* (non-Javadoc)
         * @see org.quartz.spi.ThreadPool#initialize()
         */
        public void initialize() {
        }

        /* (non-Javadoc)
         * @see org.quartz.spi.ThreadPool#runInThread(java.lang.Runnable)
         */
        public boolean runInThread(final Runnable job) {
            try {
                m_executor.execute(job);
            } catch (final InterruptedException ie) {
                getLogger().error("Cronjob failed", ie);
            }

            return true;
        }

        /* (non-Javadoc)
         * @see org.quartz.spi.ThreadPool#shutdown(boolean)
         */
        public void shutdown(final boolean waitForJobsToComplete) {
            if (waitForJobsToComplete) {
                m_executor.shutdownAfterProcessingCurrentlyQueuedTasks();
            } else {
                m_executor.shutdownNow();
            }

            try {
                if (m_shutdownWaitTimeMs > 0) {
                    if (!m_executor.awaitTerminationAfterShutdown(m_shutdownWaitTimeMs)) {
                        getLogger().warn("scheduled cron jobs are not terminating within " + m_shutdownWaitTimeMs +
                                         "ms, Will shut them down by interruption");
                        m_executor.interruptAll();
                        m_executor.shutdownNow();
                    }
                }

                m_executor.awaitTerminationAfterShutdown();
            } catch (final InterruptedException ie) {
                getLogger().error("cannot shutdown Executor", ie);
            }
        }
    }


}
