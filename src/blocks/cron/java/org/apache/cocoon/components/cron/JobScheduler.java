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

import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.parameters.Parameters;


/**
 * This component schedules jobs.
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: JobScheduler.java,v 1.7 2004/03/08 13:43:42 unico Exp $
 *
 * @since 2.1.1
 */
public interface JobScheduler {
    /** The role of a JobScheduler */
    String ROLE = JobScheduler.class.getName();

    /**
     * Get the names of all scheduled jobs.
     *
     * @return state of execution successfullness
     */
    String[] getJobNames();

    /**
     * Get the JobSchedulerEntry for a scheduled job
     *
     * @return the entry
     */
    JobSchedulerEntry getJobSchedulerEntry(String jobname);

    /**
     * Schedule a time based job.  Note that if a job with the same name has already beed added it is overwritten.
     *
     * @param name the name of the job
     * @param jobrole The Avalon components role name of the job itself
     * @param cronSpec the time specification using a cron expression
     * @param canRunConcurrently whether this job can run even previous scheduled runs are still running
     */
    void addJob(String name, String jobrole, String cronSpec, boolean canRunConcurrently)
    throws CascadingException;

    /**
     * Schedule a time based job.  Note that if a job with the same name has already beed added it is overwritten.
     *
     * @param name the name of the job
     * @param jobrole The Avalon components role name of the job itself
     * @param cronSpec the time specification using a cron expression
     * @param canRunConcurrently whether this job can run even previous scheduled runs are still running
     * @param params Additional Parameters to setup CronJob
     * @param objects A Map with additional object to setup CronJob
     */
    void addJob(String name, String jobrole, String cronSpec, boolean canRunConcurrently, Parameters params, Map objects)
    throws CascadingException;

    /**
     * Schedule a time based job.  Note that if a job with the same name has already beed added it is overwritten.
     *
     * @param name the name of the job
     * @param job The job object itself. It must implement either CronJob, Runnable or might also be an implementation
     *        specific class (i.e. org.quartz.Job)
     * @param cronSpec the time specification using a cron expression
     * @param canRunConcurrently whether this job can run even previous scheduled runs are still running
     */
    void addJob(String name, Object job, String cronSpec, boolean canRunConcurrently)
    throws CascadingException;

    /**
     * Schedule a job.  Note that if a job with the same name has already beed added it is overwritten.
     *
     * @param name the name of the job
     * @param job The job object itself. It must implement either CronJob, Runnable or might also be an implementation
     *        specific class (i.e. org.quartz.Job)
     * @param cronSpec the time specification using a cron expression
     * @param canRunConcurrently whether this job can run even previous scheduled runs are still running
     * @param params Additional Parameters to setup CronJob
     * @param objects A Map with additional object to setup CronJob
     */
    void addJob(String name, Object job, String cronSpec, boolean canRunConcurrently, Parameters params, Map objects)
    throws CascadingException;

    /**
     * Schedule a periodic job. The job is started the first time when the period has passed.  Note that if a job with
     * the same name has already beed added it is overwritten.
     *
     * @param name the name of the job
     * @param jobrole The Avalon components role name of the job itself
     * @param period Every period seconds this job is started
     * @param canRunConcurrently whether this job can run even previous scheduled runs are still running
     * @param params Additional Parameters to setup CronJob
     * @param objects A Map with additional object to setup CronJob
     */
    void addPeriodicJob(String name, String jobrole, long period, boolean canRunConcurrently, Parameters params,
                        Map objects)
    throws CascadingException;
    
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
    void addPeriodicJob(String name, Object job, long period, boolean canRunConcurrently, Parameters params,
                        Map objects)
    throws CascadingException;

    /**
     * Fire a job once immediately
     *
     * @param jobrole The Avalon components role name of the job itself
     *
     * @return success state adding the job
     */
    boolean fireJob(String jobrole);

    /**
     * Fire a CronJob once immediately
     *
     * @param job The job object itself. It must implement either CronJob, Runnable or might also be an implementation
     *        specific class (i.e. org.quartz.Job)
     *
     * @return whether the job has been successfully started
     */
    boolean fireJob(Object job);

    /**
     * Fire a job once immediately
     *
     * @param jobrole The Avalon components role name of the job itself
     * @param params Additional Parameters to setup CronJob
     * @param objects A Map with additional object to setup CronJob
     *
     * @return whether the job has been successfully started
     */
    boolean fireJob(String jobrole, Parameters params, Map objects)
    throws CascadingException;

    /**
     * Fire a job once immediately
     *
     * @param job The job object itself. It must implement either CronJob, Runnable or might also be an implementation
     *        specific class (i.e. org.quartz.Job)
     * @param params Additional Parameters to setup CronJob
     * @param objects A Map with additional object to setup CronJob
     *
     * @return whether the job has been successfully started
     */
    boolean fireJob(Object job, Parameters params, Map objects)
    throws CascadingException;

    /**
     * Fire a job once at a specific date Note that if a job with the same name has already beed added it is
     * overwritten.
     *
     * @param date The date this job should be scheduled
     * @param name the name of the job
     * @param jobrole The Avalon components role name of the job itself
     */
    void fireJobAt(Date date, String name, String jobrole)
    throws CascadingException;

    /**
     * Fire a job once at a specific date Note that if a job with the same name has already beed added it is
     * overwritten.
     *
     * @param date The date this job should be scheduled
     * @param name the name of the job
     * @param jobrole The Avalon components role name of the job itself
     * @param params Additional Parameters to setup CronJob
     * @param objects A Map with additional object to setup CronJob
     */
    void fireJobAt(Date date, String name, String jobrole, Parameters params, Map objects)
    throws CascadingException;

    /**
     * Fire a job once at a specific date Note that if a job with the same name has already beed added it is
     * overwritten.
     *
     * @param date The date this job should be scheduled
     * @param name the name of the job
     * @param job The job object itself. It must implement either CronJob, Runnable or might also be an implementation
     *        specific class (i.e. org.quartz.Job)
     */
    void fireJobAt(Date date, String name, Object job)
    throws CascadingException;

    /**
     * Fire a job once at a specific date Note that if a job with the same name has already beed added it is
     * overwritten.
     *
     * @param date The date this job should be scheduled
     * @param name the name of the job
     * @param job The job object itself. It must implement either CronJob, Runnable or might also be an implementation
     *        specific class (i.e. org.quartz.Job)
     * @param params Additional Parameters to setup CronJob
     * @param objects A Map with additional object to setup CronJob
     */
    void fireJobAt(Date date, String name, Object job, Parameters params, Map objects)
    throws CascadingException;

    /**
     * Remove a scheduled job by name.
     *
     * @param name the name of the job
     */
    void removeJob(String name)
    throws NoSuchElementException;
}
