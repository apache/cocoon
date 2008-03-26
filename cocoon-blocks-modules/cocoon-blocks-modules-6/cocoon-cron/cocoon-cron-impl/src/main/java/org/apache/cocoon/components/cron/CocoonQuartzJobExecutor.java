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

import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.background.BackgroundEnvironment;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;

/**
 * This component is resposible to launch a {@link CronJob}s in a Quartz Scheduler.
 * <p>
 * Jobs started with this executor have a {@link BackgroundEnvironment} already set up,
 * meaning they can use the SourceResolver to access "cocoon:" pipelines.
 * <p>
 * Also, these pipelines will be able to access the job's parameters and objectmap defined
 * by the various {@link org.apache.cocoon.components.cron.JobScheduler#fireJob(String, Parameters, Map)}
 * methods using the "<code>cron-parameters</code>" and "<code>cron-objectmap</code>" request
 * attributes, respectively.
 *
 * @version $Id$
 *
 * @since 2.1.1
 */
public class CocoonQuartzJobExecutor extends QuartzJobExecutor {

    private BackgroundEnvironment m_env;
    private Processor m_processor;

    protected void setup(JobDataMap data) throws JobExecutionException {
        super.setup(data);

        Context envContext;
        try {
            envContext = (Context) m_context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        } catch (ContextException e) {
        	throw new JobExecutionException(e);
        }
        
        m_env = new BackgroundEnvironment(envContext);
        
        Request req = ObjectModelHelper.getRequest(m_env.getObjectModel());
        Map objects = (Map)data.get(QuartzJobScheduler.DATA_MAP_OBJECTMAP);
        if (objects != null) {
            req.setAttribute("cron-objectmap", objects);
        }

        Parameters params = (Parameters)data.get(QuartzJobScheduler.DATA_MAP_PARAMETERS);
        if (params != null) {
            req.setAttribute("cron-parameters", params);
        }
        
        try {
            m_processor = (Processor) m_manager.lookup(Processor.ROLE);
        } catch (ServiceException e) {
            throw new JobExecutionException(e);
        }

        m_env.startingProcessing();
        
        try {
            EnvironmentHelper.enterProcessor(m_processor, m_env);
        }
        catch (ProcessingException e) {
            throw new JobExecutionException(e);
        }
    }

    protected void release(JobDataMap data) {
        super.release(data);

        EnvironmentHelper.leaveProcessor();
        m_env.finishingProcessing();

        if (m_manager != null) {
            m_manager.release(m_processor);
        }
    }

}
