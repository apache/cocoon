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

import java.util.Map;

import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
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
 * @version CVS $Id$
 *
 * @since 2.1.1
 */
public class QuartzJobExecutor implements Job {

    protected Logger m_logger;
    protected Context m_context;
    protected ServiceManager m_manager;

    /* (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(final JobExecutionContext context) throws JobExecutionException {

        final JobDataMap data = context.getJobDetail().getJobDataMap();

        final String name = (String) data.get(QuartzJobScheduler.DATA_MAP_NAME);

        m_logger = (Logger) data.get(QuartzJobScheduler.DATA_MAP_LOGGER);
        m_context = (Context) data.get(QuartzJobScheduler.DATA_MAP_CONTEXT);
        m_manager = (ServiceManager) data.get(QuartzJobScheduler.DATA_MAP_MANAGER);
        
        final Boolean canRunConcurrentlyB = ((Boolean) data.get(QuartzJobScheduler.DATA_MAP_RUN_CONCURRENT));
        final boolean canRunConcurrently = ((canRunConcurrentlyB == null) ? true : canRunConcurrentlyB.booleanValue());

        if (!canRunConcurrently) {
            Boolean isRunning = (Boolean) data.get(QuartzJobScheduler.DATA_MAP_KEY_ISRUNNING);
            if (Boolean.TRUE.equals(isRunning)) {
                m_logger.warn("Cron job name '" + name +
                            " already running but configured to not allow concurrent runs. Will discard this scheduled run");
                return;
            }
        }

        if (m_logger.isInfoEnabled()) {
            m_logger.info("Executing cron job named '" + name + "'");
        }

        setup(data);

        Object job = null;
        String jobrole = null;
        boolean release = false;
        boolean dispose = false;
        try {
            jobrole = (String) data.get(QuartzJobScheduler.DATA_MAP_ROLE);

            if (null == jobrole) {
                job = data.get(QuartzJobScheduler.DATA_MAP_OBJECT);
                ContainerUtil.enableLogging(job, m_logger);
                ContainerUtil.contextualize(job, m_context);
                ContainerUtil.service(job, m_manager);
                dispose = true;
            } else {
                job = m_manager.lookup(jobrole);
                release = true;
            }

            if (job instanceof ConfigurableCronJob) {
                final Parameters params = (Parameters) data.get(QuartzJobScheduler.DATA_MAP_PARAMETERS);
                final Map objects = (Map) data.get(QuartzJobScheduler.DATA_MAP_OBJECTMAP);
                ((ConfigurableCronJob) job).setup(params, objects);
            }

            if (job instanceof Job) {
                ((Job) job).execute(context);
            } else if (job instanceof CronJob) {
                ((CronJob) job).execute(name);
            } else if (job instanceof Runnable) {
                ((Runnable) job).run();
            } else {
                m_logger.error("job named '" + name + "' is of invalid class: " + job.getClass().getName());
            }
        } catch (final Throwable t) {
            m_logger.error("Cron job name '" + name + "' died.", t);

            if (t instanceof JobExecutionException) {
                throw (JobExecutionException) t;
            }
        } finally {

            release(data);

            if (m_manager != null && release) {
                m_manager.release(job);
            }
            if (dispose) {
            	ContainerUtil.dispose(job);
            }
        }
    }

    protected void setup(JobDataMap data) throws JobExecutionException {
        data.put(QuartzJobScheduler.DATA_MAP_KEY_ISRUNNING, Boolean.TRUE);
    }

    protected void release(JobDataMap data) {
        data.put(QuartzJobScheduler.DATA_MAP_KEY_ISRUNNING, Boolean.FALSE);
    }

}
