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

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.environment.background.BackgroundEnvironment;
import org.apache.cocoon.util.NullOutputStream;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


/**
 * This component is resposible to launch a {@link CronJob}s in a Quart Scheduler.
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: QuartzJobExecutor.java,v 1.7 2004/03/22 12:45:32 unico Exp $
 *
 * @since 2.1.1
 */
public class QuartzJobExecutor
implements Job {
    /** Map key for the run status */
    static final String DATA_MAP_KEY_ISRUNNING = "QuartzJobExecutor.isRunning";
    
    /** Shared instance (no state, as it does nothing) */
    static final OutputStream NULL_OUTPUT = new NullOutputStream();

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
        
        ServiceManager manager = (ServiceManager)data.get(QuartzJobScheduler.DATA_MAP_MANAGER);
		org.apache.cocoon.environment.Context envContext =
			(org.apache.cocoon.environment.Context)data.get(QuartzJobScheduler.DATA_MAP_ENV_CONTEXT);
        BackgroundEnvironment env;
		try {
			env = new BackgroundEnvironment(logger, envContext, manager);
		} catch (MalformedURLException mue) {
			// Unlikely to happen
			throw new JobExecutionException(mue);
		}
        CocoonComponentManager.enterEnvironment(env, env.getManager(), env.getProcessor());
        boolean release = false;
        try {
            jobrole = (String)data.get(QuartzJobScheduler.DATA_MAP_ROLE);

            if (null == jobrole) {
                job = data.get(QuartzJobScheduler.DATA_MAP_OBJECT);
            } else {
                job = manager.lookup(jobrole);
                release = true;
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
            
            CocoonComponentManager.leaveEnvironment();

            if (release && null != manager) {
                manager.release(job);
            }
        }
    }
}
