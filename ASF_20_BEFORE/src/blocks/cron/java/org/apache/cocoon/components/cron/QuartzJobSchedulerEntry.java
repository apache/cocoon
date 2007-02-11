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
 * @version CVS $Id: QuartzJobSchedulerEntry.java,v 1.3 2003/09/05 10:22:21 giacomo Exp $
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
