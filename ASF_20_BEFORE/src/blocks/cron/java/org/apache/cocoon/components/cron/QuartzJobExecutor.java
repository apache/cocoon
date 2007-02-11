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

import java.util.Map;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


/**
 * This component is resposible to launch a {@link CronJob}s in a Quart Scheduler.
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: QuartzJobExecutor.java,v 1.4 2004/02/28 04:23:56 antonio Exp $
 *
 * @since 2.1.1
 */
public class QuartzJobExecutor
implements Job {
    /** Map key for the run status */
    static final String DATA_MAP_KEY_ISRUNNING = "QuartzJobExecutor.isRunning";

    /* (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(final JobExecutionContext context)
    throws JobExecutionException {
        final JobDataMap data = context.getJobDetail().getJobDataMap();
        data.put(QuartzJobScheduler.DATA_MAP_JOB_EXECUTION_CONTEXT, context);

        final Logger logger = (Logger)data.get(QuartzJobScheduler.DATA_MAP_LOGGER);
        final String name = (String)data.get(QuartzJobScheduler.DATA_MAP_NAME);
        final Boolean canRunConcurrentlyB = ((Boolean)data.get(QuartzJobScheduler.DATA_MAP_RUN_CONCURRENT));
        final boolean canRunConcurrently = ((canRunConcurrentlyB == null) ? true : canRunConcurrentlyB.booleanValue());

        if (!canRunConcurrently) {
            Boolean isRunning = (Boolean)data.get(DATA_MAP_KEY_ISRUNNING);

            if ((null != isRunning) && isRunning.booleanValue()) {
                logger.warn("Cron job name '" + name +
                            " already running but configured to not allow concurrent runs. Will discard this scheduled run");

                return;
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Scheduling cron job named '" + name + "'");
        }

        Object job = null;
        String jobrole = null;
        ServiceManager manager = null;

        try {
            jobrole = (String)data.get(QuartzJobScheduler.DATA_MAP_ROLE);

            if (null == jobrole) {
                job = data.get(QuartzJobScheduler.DATA_MAP_OBJECT);
            } else {
                manager = (ServiceManager)data.get(QuartzJobScheduler.DATA_MAP_MANAGER);
                job = manager.lookup(jobrole);
            }

            if (job instanceof ConfigurableCronJob) {
                final Parameters params = (Parameters)data.get(QuartzJobScheduler.DATA_MAP_PARAMETERS);
                final Map objects = (Map)data.get(QuartzJobScheduler.DATA_MAP_OBJECTMAP);
                ((ConfigurableCronJob)job).setup(params, objects);
            }

            data.put(DATA_MAP_KEY_ISRUNNING, Boolean.TRUE);

            if (job instanceof Job) {
                ((Job)job).execute(context);
            } else if (job instanceof CronJob) {
                ((CronJob)job).execute(name);
            } else if (job instanceof Runnable) {
                ((Runnable)job).run();
            } else {
                logger.error("job named '" + name + "' is of invalid class: " + job.getClass().getName());
            }
        } catch (final Throwable t) {
            logger.error("Cron job name '" + name + " died.", t);

            if (t instanceof JobExecutionException) {
                throw (JobExecutionException)t;
            }
        } finally {
            data.put(DATA_MAP_KEY_ISRUNNING, Boolean.FALSE);

            if (null != manager) {
                manager.release(job);
            }
        }
    }
}
