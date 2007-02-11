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
package org.apache.cocoon.portal.pluto.service.log;

import org.apache.commons.logging.Log;

/**
 * Our own log service logging to an avalon logger.
 *
 * @version $Id$
 */
public class LoggerImpl 
implements org.apache.pluto.services.log.Logger {

    /** The logger to use */
    protected Log logger;

    /** Constructor */
    public LoggerImpl(Log logger) {
        this.logger = logger;
    }

    /**
     * @see org.apache.pluto.services.log.Logger#debug(java.lang.String, java.lang.Throwable)
     */
    public void debug(String aMessage, Throwable aThrowable) {
        this.logger.debug(aMessage, aThrowable);
    }

    /**
     * @see org.apache.pluto.services.log.Logger#debug(java.lang.String)
     */
    public void debug(String aMessage) {
        this.logger.debug(aMessage);
    }

    /**
     * @see org.apache.pluto.services.log.Logger#error(java.lang.String, java.lang.Throwable)
     */
    public void error(String aMessage, Throwable aThrowable) {
        this.logger.error(aMessage, aThrowable);
    }

    /**
     * @see org.apache.pluto.services.log.Logger#info(java.lang.String)
     */
    public void info(String aMessage) {
        this.logger.info(aMessage);
    }

    /**
     * @see org.apache.pluto.services.log.Logger#isDebugEnabled()
     */
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    /**
     * @see org.apache.pluto.services.log.Logger#isErrorEnabled()
     */
    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }

    /**
     * @see org.apache.pluto.services.log.Logger#isInfoEnabled()
     */
    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    /**
     * @see org.apache.pluto.services.log.Logger#isWarnEnabled()
     */
    public boolean isWarnEnabled() {
        return this.logger.isWarnEnabled();
    }

    /**
     * @see org.apache.pluto.services.log.Logger#warn(java.lang.String)
     */
    public void warn(String aMessage) {
        this.logger.warn(aMessage);
    }

    /**
     * @see org.apache.pluto.services.log.Logger#error(java.lang.Throwable)
     */
    public void error(Throwable throwable) {
        this.logger.error("Exception", throwable);
    }

    /**
     * @see org.apache.pluto.services.log.Logger#error(java.lang.String)
     */
    public void error(String aMessage) {
        this.logger.error(aMessage);
    }
}
