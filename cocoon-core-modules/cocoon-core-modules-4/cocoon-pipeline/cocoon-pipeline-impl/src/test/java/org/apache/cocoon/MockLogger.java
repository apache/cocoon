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
package org.apache.cocoon;

import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Mock logger for test cases. Delegates to a Commons Logging logger.
 * 
 * @version $Id$
 */
public class MockLogger implements Logger {
    protected Log logger;
    
    /**
     * Create a new logger for a given class.
     * 
     * @param clazz The class.
     */
    public MockLogger(Class clazz) {
        this.logger = LogFactory.getLog(clazz);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.Logger#debug(java.lang.String)
     */
    public void debug(String msg) {
        this.logger.debug(msg);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.Logger#debug(java.lang.String, java.lang.Throwable)
     */
    public void debug(String msg, Throwable t) {
        this.logger.debug(msg, t);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.Logger#isDebugEnabled()
     */
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.Logger#info(java.lang.String)
     */
    public void info(String msg) {
        this.logger.info(msg);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.Logger#info(java.lang.String, java.lang.Throwable)
     */
    public void info(String msg, Throwable t) {
        this.logger.info(msg, t);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.Logger#isInfoEnabled()
     */
    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.Logger#warn(java.lang.String)
     */
    public void warn(String msg) {
        this.logger.warn(msg);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.Logger#warn(java.lang.String, java.lang.Throwable)
     */
    public void warn(String msg, Throwable t) {
        this.logger.warn(msg, t);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.Logger#isWarnEnabled()
     */
    public boolean isWarnEnabled() {
        return this.logger.isWarnEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.Logger#error(java.lang.String)
     */
    public void error(String msg) {
        this.logger.error(msg);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.Logger#error(java.lang.String, java.lang.Throwable)
     */
    public void error(String msg, Throwable t) {
        this.logger.error(msg, t);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.Logger#isErrorEnabled()
     */
    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.Logger#fatalError(java.lang.String)
     */
    public void fatalError(String msg) {
        this.logger.fatal(msg);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.Logger#fatalError(java.lang.String, java.lang.Throwable)
     */
    public void fatalError(String msg, Throwable t) {
        this.logger.fatal(msg, t);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.Logger#isFatalErrorEnabled()
     */
    public boolean isFatalErrorEnabled() {
        return this.logger.isFatalEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.Logger#getChildLogger(java.lang.String)
     */
    public Logger getChildLogger(String arg0) {
        return null;
    }

}
