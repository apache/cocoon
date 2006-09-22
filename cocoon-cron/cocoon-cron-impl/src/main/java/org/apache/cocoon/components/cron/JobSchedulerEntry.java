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

import java.util.Date;


/**
 * Interface for classes holding scheduled job entries.
 *
 * @version $Id$
 */
public interface JobSchedulerEntry {
    /**
     * The name of the role/class of the job
     *
     * @return Name of the role/class of this job
     */
    String getJobName();

    /**
     * Return name of entry.
     *
     * @return the name of the entry
     */
    String getName();

    /**
     * Retrieve time when this job should run next.
     *
     * @return the time in milliseconds when job should run
     */
    Date getNextTime();

    /**
     * Is this job currently running?
     *
     * @return whether this job is currently running?
     */
    boolean isRunning();

    /**
     * Get a human readable representation of the schedule of this entry. Is up to an implementation how it presents
     * the schedule for this entry
     *
     * @return the human readable representation of the schedule of this entry
     */
    String getSchedule();
}
