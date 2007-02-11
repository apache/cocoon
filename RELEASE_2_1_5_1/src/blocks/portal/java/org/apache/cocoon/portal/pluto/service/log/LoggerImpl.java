/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto.service.log;

import org.apache.avalon.framework.logger.Logger;

/**
 * Our own log service logging to an avalon logger
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: LoggerImpl.java,v 1.1 2004/03/10 12:56:29 cziegeler Exp $
 */
public class LoggerImpl 
implements org.apache.pluto.services.log.Logger {

    /** The logger to use */
    protected Logger logger;
    
    /** Constructor */
    public LoggerImpl(Logger logger) {
        this.logger = logger;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#debug(java.lang.String, java.lang.String, java.lang.Throwable)
     */
    public void debug(String aMessage, Throwable aThrowable) {
        this.logger.debug(aMessage, aThrowable);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#debug(java.lang.String, java.lang.String)
     */
    public void debug(String aMessage) {
        this.logger.debug(aMessage);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#error(java.lang.String, java.lang.String, java.lang.Throwable)
     */
    public void error(String aMessage, Throwable aThrowable) {
        this.logger.error(aMessage, aThrowable);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#info(java.lang.String, java.lang.String)
     */
    public void info(String aMessage) {
        this.logger.info(aMessage);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#isDebugEnabled(java.lang.String)
     */
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#isErrorEnabled(java.lang.String)
     */
    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#isInfoEnabled(java.lang.String)
     */
    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#isWarnEnabled(java.lang.String)
     */
    public boolean isWarnEnabled() {
        return this.logger.isWarnEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#warn(java.lang.String, java.lang.String)
     */
    public void warn(String aMessage) {
        this.logger.warn(aMessage);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.Logger#error(java.lang.Throwable)
     */
    public void error(Throwable throwable) {
        this.logger.error("Exception", throwable);
    }
    
    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.Logger#error(java.lang.String)
     */
    public void error(String aMessage) {
        this.logger.error(aMessage);
    }
}
