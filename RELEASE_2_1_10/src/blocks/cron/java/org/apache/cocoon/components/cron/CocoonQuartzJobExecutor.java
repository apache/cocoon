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

import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.component.WrapperComponentManager;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.Constants;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.background.BackgroundEnvironment;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;

/**
 * This component is resposible to launch a {@link CronJob}s in a Quart Scheduler.
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id$
 *
 * @since 2.1.1
 */
public class CocoonQuartzJobExecutor extends QuartzJobExecutor {

    private Object m_key;
    private BackgroundEnvironment m_env;
    private Processor m_processor;

    protected void setup(JobDataMap data) throws JobExecutionException {
        super.setup(data);
        org.apache.cocoon.environment.Context envContext;
        try {
            envContext =
                (org.apache.cocoon.environment.Context) m_context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        } catch (ContextException e) {
        	throw new JobExecutionException(e);
        }
        
        try {
            m_env = new BackgroundEnvironment(m_logger, envContext);
        } catch (MalformedURLException e) {
            // Unlikely to happen
            throw new JobExecutionException(e);
        }

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

        m_key = CocoonComponentManager.startProcessing(m_env);
        CocoonComponentManager.enterEnvironment(m_env, new WrapperComponentManager(m_manager), m_processor);
    }

    protected void release(JobDataMap data) {
        super.release(data);
        CocoonComponentManager.leaveEnvironment();
        CocoonComponentManager.endProcessing(m_env, m_key);
        if (m_manager != null) {
            m_manager.release(m_processor);
        }
    }

}
