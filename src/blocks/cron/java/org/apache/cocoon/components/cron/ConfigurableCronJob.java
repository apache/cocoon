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

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;


/**
 * This is a configurable cron job. Before the execute/run method is called the setup method is invoked.
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: ConfigurableCronJob.java,v 1.3 2004/03/05 13:01:49 bdelacretaz Exp $
 *
 * @since 2.1.1
 */
public interface ConfigurableCronJob
extends CronJob {
    /**
     * Setup CronJob with additional information first
     *
     * @param pars Any Parameters
     * @param objects Some additional Objects
     */
    void setup(Parameters pars, Map objects);
}
