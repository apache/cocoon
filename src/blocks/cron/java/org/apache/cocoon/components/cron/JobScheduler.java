/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: JobScheduler.java,v 1.4 2003/09/04 15:59:09 giacomo Exp $
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
     * Schedule a periodic job.
     * The job is started the first time when the period has passed. 
     * Note that if a Job already has same name then it is overwritten.
     *
     * @param name the name of the job
     * @param jobrole The Avalon components role name of the job itself
     * @param period Every period seconds this job is started
     * @param canRunConcurrently whether this job can run even previous scheduled runs are still running
     * @param params Additional Parameters to setup CronJob
     * @param objects A Map with additional object to setup CronJob
     */
    void addPeriodicJob(String name, 
                        String jobrole, 
                        long period, 
                        boolean canRunConcurrently,
                        Parameters params, 
                        Map objects)
    throws CascadingException;

    /**
     * Schedule a time based job. Note that if a CronJob already has same name then it is overwritten.
     *
     * @param name the name of the job
     * @param jobrole The Avalon components role name of the job itself
     * @param cronSpec the time specification using a cron expression
     * @param canRunConcurrently whether this job can run even previous scheduled runs are still running
     */
    void addJob(String name, String jobrole, String cronSpec, boolean canRunConcurrently)
        throws CascadingException;

    /**
     * Schedule a time based job. Note that if a CronJob already has same name then it is overwritten.
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
     * Schedule a job. Note that if a CronJob already has the same name then it is overwritten.
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
     * Schedule a job. Note that if a CronJob already has the same name then it is overwritten.
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
     * Fire a job once at a specific date
     *
     * @param date The date this job should be scheduled
     * @param name the name of the job
     * @param jobrole The Avalon components role name of the job itself
     */
    void fireJobAt(Date date, String name, String jobrole)
        throws CascadingException;

    /**
     * Fire a job once immediately
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
     * Fire a CronJob once immediately
     *
     * @param date The date this job should be scheduled
     * @param name the name of the job
     * @param job The job object itself. It must implement either CronJob, Runnable or might also be an implementation
     *        specific class (i.e. org.quartz.Job)
     */
    void fireJobAt(Date date, String name, Object job)
        throws CascadingException;

    /**
     * Fire a job once immediately
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
