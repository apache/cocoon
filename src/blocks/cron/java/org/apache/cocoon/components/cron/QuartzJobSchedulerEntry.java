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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;


/**
 * Implementation of the JobSchedulerEntry interface for the QuartzJobScheduler
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: QuartzJobSchedulerEntry.java,v 1.4 2004/03/05 13:01:49 bdelacretaz Exp $
 */
public class QuartzJobSchedulerEntry
implements JobSchedulerEntry {
    /** The data map */
    private final JobDataMap m_data;

    /** The detail */
    private final JobDetail m_detail;

    /** The scheduler reference */
    private final Scheduler m_scheduler;

    /** The date formatter */
    private final SimpleDateFormat m_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** The name */
    private final String m_name;

    /** The trigger */
    private final Trigger m_trigger;

    /**
     * Construct an JobSchedulerEntry
     *
     * @param name The name of the job
     * @param scheduler The QuartzJobScheduler
     *
     * @throws SchedulerException in case of failures
     */
    public QuartzJobSchedulerEntry(final String name, final Scheduler scheduler)
    throws SchedulerException {
        m_scheduler = scheduler;
        m_name = name;
        m_detail = m_scheduler.getJobDetail(name, QuartzJobScheduler.DEFAULT_QUARTZ_JOB_GROUP);
        m_data = m_detail.getJobDataMap();
        m_trigger = m_scheduler.getTrigger(name, QuartzJobScheduler.DEFAULT_QUARTZ_JOB_GROUP);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobSchedulerEntry#getJobName()
     */
    public String getJobName() {
        String name = (String)m_data.get(QuartzJobScheduler.DATA_MAP_ROLE);

        if (null == name) {
            name = m_data.get(QuartzJobScheduler.DATA_MAP_OBJECT).getClass().getName();
        }

        return name;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobSchedulerEntry#getName()
     */
    public String getName() {
        return m_name;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobSchedulerEntry#getNextTime()
     */
    public Date getNextTime() {
        return m_trigger.getNextFireTime();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobSchedulerEntry#isRunning()
     */
    public boolean isRunning() {
        Boolean runs = (Boolean)m_data.get(QuartzJobExecutor.DATA_MAP_KEY_ISRUNNING);

        if (null != runs) {
            return runs.booleanValue();
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.JobSchedulerEntry#getSchedule()
     */
    public String getSchedule() {
        if (m_trigger instanceof CronTrigger) {
            return "cron: " + ((CronTrigger)m_trigger).getCronExpression();
        } else if (m_trigger instanceof SimpleTrigger) {
            if (((SimpleTrigger)m_trigger).getRepeatInterval() == 0) {
                return "once: at " + m_formatter.format(m_trigger.getFinalFireTime());
            }

            return "periodic: every " + (((SimpleTrigger)m_trigger).getRepeatInterval() / 1000) + "s";
        } else {
            return "next: " + m_formatter.format(m_trigger.getNextFireTime());
        }
    }
}
