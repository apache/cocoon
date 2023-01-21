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
package org.apache.cocoon.components.slide.impl;

import org.apache.commons.logging.Log;
import org.apache.slide.util.logger.Logger;

/**
 * The class represent an adapter for the logger for jakarta slide
 *
 * @version $Id$
 */
public class SlideLoggerAdapter implements Logger {

    private Log logger;
    private int level;

    
    public SlideLoggerAdapter(Log logger) {
        this.logger = logger;
        this.level = ERROR;
    }

    public void log(Object data, Throwable t, String channel, int level) {
        if (level == CRITICAL) {
            this.logger.fatal(data.toString(), t);
        } else if (level == ERROR) {
            this.logger.error(data.toString(), t);
        } else if (level == WARNING) {
            this.logger.warn(data.toString(), t);
        } else if (level == INFO) {
            this.logger.info(data.toString(), t);
        } else if (level == DEBUG) {
            this.logger.debug(data.toString(), t);
        } else {
            this.logger.error(data.toString(), t);
        }
    }

    /**
     * Log an object thru the specified channel and with the specified level.
     *
     * @param data The object to log.
     * @param channel The channel name used for logging.
     * @param level The level used for logging.
     */
    public void log(Object data, String channel, int level) {
        if (level == CRITICAL) {
            this.logger.fatal(data.toString());
        } else if (level == ERROR) {
            this.logger.error(data.toString());
        } else if (level == WARNING) {
            this.logger.warn(data.toString());
        } else if (level == INFO) {
            this.logger.info(data.toString());
        } else if (level == DEBUG) {
            this.logger.debug(data.toString());
        } else {
            this.logger.error(data.toString());
        }
    }

    /**
     * Log an object with the specified level.
     *
     * @param data The object to log.
     * @param level The level used for logging.
     */
    public void log(Object data, int level) {
        if (level == CRITICAL) {
            this.logger.fatal(data.toString());
        } else if (level == ERROR) {
            this.logger.error(data.toString());
        } else if (level == WARNING) {
            this.logger.warn(data.toString());
        } else if (level == INFO) {
            this.logger.info(data.toString());
        } else if (level == DEBUG) {
            this.logger.debug(data.toString());
        } else {
            this.logger.error(data.toString());
        }
    }

    /**
     * Log an object.
     *
     * @param data The object to log.
     */
    public void log(Object data) {
        if (level == CRITICAL) {
            this.logger.fatal(data.toString());
        } else if (level == ERROR) {
            this.logger.error(data.toString());
        } else if (level == WARNING) {
            this.logger.warn(data.toString());
        } else if (level == INFO) {
            this.logger.info(data.toString());
        } else if (level == DEBUG) {
            this.logger.debug(data.toString());
        } else {
            this.logger.error(data.toString());
        }
    }

    /**
     * Set the logger level for the default channel
     *
     * @param level the logger level
     */
    public void setLoggerLevel(int level) {
        this.level = level;
    }

    /**
     * Set the logger level for the specified channel
     *
     * @param channel
     * @param level the logger level
     */
    public void setLoggerLevel(String channel, int level) {
        this.level = level;
    }

    /**
     * Get the logger level for the default channel
     * @return logger level
     */
    public int getLoggerLevel() {
        return level;
    }

    /**
     * Get the logger level for the specified channel
     *
     * @param channel the channel
     * @return logger level
     */
    public int getLoggerLevel(String channel) {
        if (this.logger.isDebugEnabled()) {
            return DEBUG;
        } else if (this.logger.isInfoEnabled()) {
            return INFO;
        } else if (this.logger.isWarnEnabled()) {
            return WARNING;
        } else if (this.logger.isErrorEnabled()) {
            return ERROR;
        } else if (this.logger.isFatalEnabled()) {
            return CRITICAL;
        } else {
            return ERROR;
        }
    }

    /**
     * Check if the channel with the specified level is enabled for logging.
     *
     * @param channel The channel specification
     * @param level   The level specification
     */
    public boolean isEnabled(String channel, int level) {
        if (this.logger.isDebugEnabled()) {
            return DEBUG <= level;
        } else if (this.logger.isInfoEnabled()) {
            return INFO <= level;
        } else if (this.logger.isWarnEnabled()) {
            return WARNING <= level;
        } else if (this.logger.isErrorEnabled()) {
            return ERROR <= level;
        } else if (this.logger.isFatalEnabled()) {
            return CRITICAL <= level;
        } else {
            return ERROR <= level;
        }
    }

    /**
     * Check if the default channel with the specified level is enabled for logging.
     *
     * @param level   The level specification
     */
    public boolean isEnabled(int level) {
        if (this.logger.isDebugEnabled()) {
            return DEBUG <= level;
        } else if (this.logger.isInfoEnabled()) {
            return INFO <= level;
        } else if (this.logger.isWarnEnabled()) {
            return WARNING <= level;
        } else if (this.logger.isErrorEnabled()) {
            return ERROR <= level;
        } else if (this.logger.isFatalEnabled()) {
            return CRITICAL <= level;
        } else {
            return ERROR <= level;
        }
    }
}
