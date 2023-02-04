/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.util.avalon;

import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.logging.Log;

/**
 * Commons Logging to Avalon Logger adapter.
 *
 * @version $Id$
 */
public class CLLoggerWrapper implements Logger {

    protected final Log log;

    public CLLoggerWrapper(Log l) {
        this.log = l;
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#debug(java.lang.String, java.lang.Throwable)
     */
    public void debug(String arg0, Throwable arg1) {
        log.debug(arg0, arg1);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#debug(java.lang.String)
     */
    public void debug(String arg0) {
        log.debug(arg0);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#error(java.lang.String, java.lang.Throwable)
     */
    public void error(String arg0, Throwable arg1) {
        log.error(arg0, arg1);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#error(java.lang.String)
     */
    public void error(String arg0) {
        log.error(arg0);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#fatalError(java.lang.String, java.lang.Throwable)
     */
    public void fatalError(String arg0, Throwable arg1) {
        log.fatal(arg0, arg1);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#fatalError(java.lang.String)
     */
    public void fatalError(String arg0) {
        log.fatal(arg0);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#info(java.lang.String, java.lang.Throwable)
     */
    public void info(String arg0, Throwable arg1) {
        log.info(arg0, arg1);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#info(java.lang.String)
     */
    public void info(String arg0) {
        log.info(arg0);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#isDebugEnabled()
     */
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#isErrorEnabled()
     */
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#isInfoEnabled()
     */
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#isWarnEnabled()
     */
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#warn(java.lang.String, java.lang.Throwable)
     */
    public void warn(String arg0, Throwable arg1) {
        log.warn(arg0, arg1);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#warn(java.lang.String)
     */
    public void warn(String arg0) {
        log.warn(arg0);
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#getChildLogger(java.lang.String)
     */
    public Logger getChildLogger(String arg0) {
        return this;
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#isFatalErrorEnabled()
     */
    public boolean isFatalErrorEnabled() {
        return this.log.isFatalEnabled();
    }
}
