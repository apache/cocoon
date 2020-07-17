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

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;

import org.quartz.Calendar;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.jdbcjobstore.DriverDelegate;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.utils.Key;
import org.quartz.utils.TriggerStatus;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Wrapper around another DriverDelegate instance.
 *
 * <p>This wrapper makes sure that three Cocoon specific transient objects are
 * removed from the JobDataMap before serializing it into the database, and
 * populated back into the map when JobDetailMap is loaded from the database.
 * These objects are:</p>
 * <ul>
 * <li>Logger</li>
 * <li>ServiceManager</li>
 * <li>Context</li>
 * <li>
 *
 * @version CVS $Id$
 * @since 2.1.6
 */
public class QuartzDriverDelegate implements DriverDelegate {
    private Logger logger;
    private ServiceManager manager;
    private Context context;
    private DriverDelegate delegate;

    public QuartzDriverDelegate(Logger logger, ServiceManager manager, Context context, DriverDelegate delegate) {
        this.logger = logger;
        this.manager = manager;
        this.context = context;
        this.delegate = delegate;
    }

    public int insertJobDetail(Connection conn, JobDetail job)
    throws IOException, SQLException {
        removeTransientData(job);
        return delegate.insertJobDetail(conn, job);
    }

    public int updateJobDetail(Connection conn, JobDetail job)
    throws IOException, SQLException {
        removeTransientData(job);
        return delegate.updateJobDetail(conn, job);
    }

    public int updateJobData(Connection conn, JobDetail job)
    throws IOException, SQLException {
        removeTransientData(job);
        return delegate.updateJobData(conn, job);
    }

    private void removeTransientData(JobDetail job) {
        JobDataMap map = job.getJobDataMap();
        if (map != null) {
            this.logger.debug("QuartzDriverDelegate: Removing transient data");
            map.remove(QuartzJobScheduler.DATA_MAP_LOGGER);
            map.remove(QuartzJobScheduler.DATA_MAP_CONTEXT);
            map.remove(QuartzJobScheduler.DATA_MAP_MANAGER);
        }
    }


    public JobDetail selectJobDetail(Connection conn, String jobName,
                                     String groupName, ClassLoadHelper loadHelper)
    throws ClassNotFoundException, IOException, SQLException {
        JobDetail job = delegate.selectJobDetail(conn, jobName, groupName, loadHelper);
        if (job != null) {
            JobDataMap map = job.getJobDataMap();
            if (map != null) {
                this.logger.debug("QuartzDriverDelegate: Adding transient data");
                map.put(QuartzJobScheduler.DATA_MAP_LOGGER, this.logger);
                map.put(QuartzJobScheduler.DATA_MAP_CONTEXT, this.context);
                map.put(QuartzJobScheduler.DATA_MAP_MANAGER, this.manager);
            }
        }
        return job;
    }


    //
    // Delegate all other methods
    //

    public int updateTriggerStatesFromOtherStates(Connection conn,
            String newState, String oldState1, String oldState2)
            throws SQLException {
        return delegate.updateTriggerStatesFromOtherStates(conn, newState, oldState1, oldState2);
    }

    public Key[] selectMisfiredTriggers(Connection conn, long ts)
            throws SQLException {
        return delegate.selectMisfiredTriggers(conn, ts);
    }

    public Key[] selectMisfiredTriggersInState(Connection conn, String state,
            long ts) throws SQLException {
        return delegate.selectMisfiredTriggersInState(conn, state, ts);
    }

    public Key[] selectMisfiredTriggersInGroupInState(Connection conn,
            String groupName, String state, long ts) throws SQLException {
        return delegate.selectMisfiredTriggersInGroupInState(conn, groupName, state, ts);
    }

    public Trigger[] selectTriggersForRecoveringJobs(Connection conn)
            throws SQLException, IOException, ClassNotFoundException {
        return delegate.selectTriggersForRecoveringJobs(conn);
    }

    public int deleteFiredTriggers(Connection conn) throws SQLException {
        return delegate.deleteFiredTriggers(conn);
    }

    public int deleteFiredTriggers(Connection conn, String instanceId)
            throws SQLException {
        return delegate.deleteFiredTriggers(conn, instanceId);
    }

    public int deleteVolatileFiredTriggers(Connection conn) throws SQLException {
        return delegate.deleteVolatileFiredTriggers(conn);
    }

    public Key[] selectVolatileTriggers(Connection conn) throws SQLException {
        return delegate.selectVolatileTriggers(conn);
    }

    public Key[] selectVolatileJobs(Connection conn) throws SQLException {
        return delegate.selectVolatileJobs(conn);
    }

    public Key[] selectTriggerNamesForJob(Connection conn, String jobName,
            String groupName) throws SQLException {
        return delegate.selectTriggerNamesForJob(conn, jobName, groupName);
    }

    public int deleteJobListeners(Connection conn, String jobName,
            String groupName) throws SQLException {
        return delegate.deleteJobListeners(conn, jobName, groupName);
    }

    public int deleteJobDetail(Connection conn, String jobName, String groupName)
            throws SQLException {
        return delegate.deleteJobDetail(conn, jobName, groupName);
    }

    public boolean isJobStateful(Connection conn, String jobName,
            String groupName) throws SQLException {
        return delegate.isJobStateful(conn, jobName, groupName);
    }

    public boolean jobExists(Connection conn, String jobName, String groupName)
            throws SQLException {
        return delegate.jobExists(conn, jobName, groupName);
    }

    public int insertJobListener(Connection conn, JobDetail job, String listener)
            throws SQLException {
        return delegate.insertJobListener(conn, job, listener);
    }

    public String[] selectJobListeners(Connection conn, String jobName,
            String groupName) throws SQLException {
        return delegate.selectJobListeners(conn, jobName, groupName);
    }

    public int selectNumJobs(Connection conn) throws SQLException {
        return delegate.selectNumJobs(conn);
    }

    public String[] selectJobGroups(Connection conn) throws SQLException {
        return delegate.selectJobGroups(conn);
    }

    public String[] selectJobsInGroup(Connection conn, String groupName)
            throws SQLException {
        return delegate.selectJobsInGroup(conn, groupName);
    }

    public int insertTrigger(Connection conn, Trigger trigger, String state,
            JobDetail jobDetail) throws SQLException, IOException {
        return delegate.insertTrigger(conn, trigger, state, jobDetail);
    }

    public int insertSimpleTrigger(Connection conn, SimpleTrigger trigger)
            throws SQLException {
        return delegate.insertSimpleTrigger(conn, trigger);
    }

    public int insertBlobTrigger(Connection conn, Trigger trigger)
            throws SQLException, IOException {
        return delegate.insertBlobTrigger(conn, trigger);
    }

    public int insertCronTrigger(Connection conn, CronTrigger trigger)
            throws SQLException {
        return delegate.insertCronTrigger(conn, trigger);
    }

    public int updateTrigger(Connection conn, Trigger trigger, String state,
            JobDetail jobDetail) throws SQLException, IOException {
        return delegate.updateTrigger(conn, trigger, state, jobDetail);
    }

    public int updateSimpleTrigger(Connection conn, SimpleTrigger trigger)
            throws SQLException {
        return delegate.updateSimpleTrigger(conn, trigger);
    }

    public int updateCronTrigger(Connection conn, CronTrigger trigger)
            throws SQLException {
        return delegate.updateCronTrigger(conn, trigger);
    }

    public int updateBlobTrigger(Connection conn, Trigger trigger)
            throws SQLException, IOException {
        return delegate.updateBlobTrigger(conn, trigger);
    }

    public boolean triggerExists(Connection conn, String triggerName,
            String groupName) throws SQLException {
        return delegate.triggerExists(conn, triggerName, groupName);
    }

    public int updateTriggerState(Connection conn, String triggerName,
            String groupName, String state) throws SQLException {
        return delegate.updateTriggerState(conn, triggerName, groupName, state);
    }

    public int updateTriggerStateFromOtherState(Connection conn,
            String triggerName, String groupName, String newState,
            String oldState) throws SQLException {
        return delegate.updateTriggerStateFromOtherState(conn, triggerName, groupName, newState, oldState);
    }

    public int updateTriggerStateFromOtherStates(Connection conn,
            String triggerName, String groupName, String newState,
            String oldState1, String oldState2, String oldState3)
            throws SQLException {
        return delegate.updateTriggerStateFromOtherStates(conn, triggerName, groupName, newState, oldState1, oldState2, oldState3);
    }

    public int updateTriggerStateFromOtherStatesBeforeTime(Connection conn,
            String newState, String oldState1, String oldState2, long time)
            throws SQLException {
        return delegate.updateTriggerStateFromOtherStatesBeforeTime(conn, newState, oldState1, oldState2, time);
    }

    public int updateTriggerGroupStateFromOtherStates(Connection conn,
            String groupName, String newState, String oldState1,
            String oldState2, String oldState3) throws SQLException {
        return delegate.updateTriggerGroupStateFromOtherStates(conn, groupName, newState, oldState1, oldState2, oldState3);
    }

    public int updateTriggerGroupStateFromOtherState(Connection conn,
            String groupName, String newState, String oldState)
            throws SQLException {
        return delegate.updateTriggerGroupStateFromOtherState(conn, groupName, newState, oldState);
    }

    public int updateTriggerStatesForJob(Connection conn, String jobName,
            String groupName, String state) throws SQLException {
        return delegate.updateTriggerStatesForJob(conn, jobName, groupName, state);
    }

    public int updateTriggerStatesForJobFromOtherState(Connection conn,
            String jobName, String groupName, String state, String oldState)
            throws SQLException {
        return delegate.updateTriggerStatesForJobFromOtherState(conn, jobName, groupName, state, oldState);
    }

    public int deleteTriggerListeners(Connection conn, String triggerName,
            String groupName) throws SQLException {
        return delegate.deleteTriggerListeners(conn, triggerName, groupName);
    }

    public int insertTriggerListener(Connection conn, Trigger trigger,
            String listener) throws SQLException {
        return delegate.insertTriggerListener(conn, trigger, listener);
    }

    public String[] selectTriggerListeners(Connection conn, String triggerName,
            String groupName) throws SQLException {
        return delegate.selectTriggerListeners(conn, triggerName, groupName);
    }

    public int deleteSimpleTrigger(Connection conn, String triggerName,
            String groupName) throws SQLException {
        return delegate.deleteSimpleTrigger(conn, triggerName, groupName);
    }

    public int deleteBlobTrigger(Connection conn, String triggerName,
            String groupName) throws SQLException {
        return delegate.deleteBlobTrigger(conn, triggerName, groupName);
    }

    public int deleteCronTrigger(Connection conn, String triggerName,
            String groupName) throws SQLException {
        return delegate.deleteCronTrigger(conn, triggerName, groupName);
    }

    public int deleteTrigger(Connection conn, String triggerName,
            String groupName) throws SQLException {
        return delegate.deleteTrigger(conn, triggerName, groupName);
    }

    public int selectNumTriggersForJob(Connection conn, String jobName,
            String groupName) throws SQLException {
        return delegate.selectNumTriggersForJob(conn, jobName, groupName);
    }

    public JobDetail selectJobForTrigger(Connection conn, String triggerName,
            String groupName, ClassLoadHelper loadHelper) throws SQLException, ClassNotFoundException {
        return delegate.selectJobForTrigger(conn, triggerName, groupName, loadHelper);
    }

    public List selectStatefulJobsOfTriggerGroup(Connection conn,
            String groupName) throws SQLException {
        return delegate.selectStatefulJobsOfTriggerGroup(conn, groupName);
    }

    public Trigger[] selectTriggersForJob(Connection conn, String jobName,
            String groupName) throws SQLException, ClassNotFoundException,
            IOException {
        return delegate.selectTriggersForJob(conn, jobName, groupName);
    }

    public Trigger[] selectTriggersForCalendar(Connection conn, String calName)
            throws SQLException, ClassNotFoundException, IOException {
        return delegate.selectTriggersForCalendar(conn, calName);
    }

    public Trigger selectTrigger(Connection conn, String triggerName,
            String groupName) throws SQLException, ClassNotFoundException,
            IOException {
        return delegate.selectTrigger(conn, triggerName, groupName);
    }

    public String selectTriggerState(Connection conn, String triggerName,
            String groupName) throws SQLException {
        return delegate.selectTriggerState(conn, triggerName, groupName);
    }

    public TriggerStatus selectTriggerStatus(Connection conn,
            String triggerName, String groupName) throws SQLException {
        return delegate.selectTriggerStatus(conn, triggerName, groupName);
    }

    public int selectNumTriggers(Connection conn) throws SQLException {
        return delegate.selectNumTriggers(conn);
    }

    public String[] selectTriggerGroups(Connection conn) throws SQLException {
        return delegate.selectTriggerGroups(conn);
    }

    public String[] selectTriggersInGroup(Connection conn, String groupName)
            throws SQLException {
        return delegate.selectTriggersInGroup(conn, groupName);
    }

    public Key[] selectTriggersInState(Connection conn, String state)
            throws SQLException {
        return delegate.selectTriggersInState(conn, state);
    }

    public int insertPausedTriggerGroup(Connection conn, String groupName)
            throws SQLException {
        return delegate.insertPausedTriggerGroup(conn, groupName);
    }

    public int deletePausedTriggerGroup(Connection conn, String groupName)
            throws SQLException {
        return delegate.deletePausedTriggerGroup(conn, groupName);
    }

    public int deleteAllPausedTriggerGroups(Connection conn)
            throws SQLException {
        return delegate.deleteAllPausedTriggerGroups(conn);
    }

    public boolean isTriggerGroupPaused(Connection conn, String groupName)
            throws SQLException {
        return delegate.isTriggerGroupPaused(conn, groupName);
    }

    public Set selectPausedTriggerGroups(Connection conn)
        throws SQLException {
        return delegate.selectPausedTriggerGroups(conn);
    }

    public boolean isExistingTriggerGroup(Connection conn, String groupName)
            throws SQLException {
        return delegate.isExistingTriggerGroup(conn, groupName);
    }

    public int insertCalendar(Connection conn, String calendarName,
            Calendar calendar) throws IOException, SQLException {
        return delegate.insertCalendar(conn, calendarName, calendar);
    }

    public int updateCalendar(Connection conn, String calendarName,
            Calendar calendar) throws IOException, SQLException {
        return delegate.updateCalendar(conn, calendarName, calendar);
    }

    public boolean calendarExists(Connection conn, String calendarName)
            throws SQLException {
        return delegate.calendarExists(conn, calendarName);
    }

    public Calendar selectCalendar(Connection conn, String calendarName)
            throws ClassNotFoundException, IOException, SQLException {
        return delegate.selectCalendar(conn, calendarName);
    }

    public boolean calendarIsReferenced(Connection conn, String calendarName)
            throws SQLException {
        return delegate.calendarIsReferenced(conn, calendarName);
    }

    public int deleteCalendar(Connection conn, String calendarName)
            throws SQLException {
        return delegate.deleteCalendar(conn, calendarName);
    }

    public int selectNumCalendars(Connection conn) throws SQLException {
        return delegate.selectNumCalendars(conn);
    }

    public String[] selectCalendars(Connection conn) throws SQLException {
        return delegate.selectCalendars(conn);
    }

    public long selectNextFireTime(Connection conn) throws SQLException {
        return delegate.selectNextFireTime(conn);
    }

    public Key selectTriggerForFireTime(Connection conn, long fireTime)
            throws SQLException {
        return delegate.selectTriggerForFireTime(conn, fireTime);
    }

    public int insertFiredTrigger(Connection conn, Trigger trigger,
            String state, JobDetail jobDetail) throws SQLException {
        return delegate.insertFiredTrigger(conn, trigger, state, jobDetail);
    }

    public List selectFiredTriggerRecords(Connection conn, String triggerName,
            String groupName) throws SQLException {
        return delegate.selectFiredTriggerRecords(conn, triggerName, groupName);
    }

    public List selectFiredTriggerRecordsByJob(Connection conn, String jobName,
            String groupName) throws SQLException {
        return delegate.selectFiredTriggerRecordsByJob(conn, jobName, groupName);
    }

    public List selectInstancesFiredTriggerRecords(Connection conn,
            String instanceName) throws SQLException {
        return delegate.selectInstancesFiredTriggerRecords(conn, instanceName);
    }

    public int deleteFiredTrigger(Connection conn, String entryId)
            throws SQLException {
        return delegate.deleteFiredTrigger(conn, entryId);
    }

    public int selectJobExecutionCount(Connection conn, String jobName,
            String jobGroup) throws SQLException {
        return delegate.selectJobExecutionCount(conn, jobName, jobGroup);
    }

    public int insertSchedulerState(Connection conn, String instanceId,
            long checkInTime, long interval, String recoverer)
            throws SQLException {
        return delegate.insertSchedulerState(conn, instanceId, checkInTime, interval, recoverer);
    }

    public int deleteSchedulerState(Connection conn, String instanceId)
            throws SQLException {
        return delegate.deleteSchedulerState(conn, instanceId);
    }

    public List selectSchedulerStateRecords(Connection conn, String instanceId)
            throws SQLException {
        return delegate.selectSchedulerStateRecords(conn, instanceId);
    }

    public JobDataMap selectTriggerJobDataMap(Connection conn, String triggerName, String groupName) throws SQLException, ClassNotFoundException, IOException {
        return delegate.selectTriggerJobDataMap(conn, triggerName, groupName);
    }

    public int updateSchedulerState(Connection conn, String instanceId,
            long checkInTime, String recoverer) throws SQLException {
        return delegate.updateSchedulerState(conn, instanceId, checkInTime, recoverer);
    }
}
