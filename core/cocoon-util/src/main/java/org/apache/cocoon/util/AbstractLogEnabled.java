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
package org.apache.cocoon.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Support for logging. This class is the replacement for the
 * <code>org.apache.avalon.framework.logger.AbstractLogEnabled</code>.
 *
 * <p>By default this class uses the commons-logging logger configured
 * for the current class. However it is possible to supply custom logger
 * by calling {@link #setLogger(Log)}.
 *
 * @version $Id$
 */
public abstract class AbstractLogEnabled {

    /** By default we use the logger for this class. */
    private Log logger = LogFactory.getLog(getClass());

    public Log getLogger() {
        return this.logger;
    }

    public void setLogger(Log l) {
        this.logger = l;
    }
}
