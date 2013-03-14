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
package org.apache.cocoon.util.log;

import org.apache.avalon.framework.logger.Logger;
import org.slf4j.LoggerFactory;

/**
 * Avalon Logger wrapping a slf4j logger.
 * 
 * @author <a href="mailto:cdamioli@apache.org">Cédric Damioli</a>
 * @author <a href="mailto:lmedioni@temenos.com">Laurent Medioni</a>
 * @version $Id$
 */
public class SLF4JLoggerAdapter implements Logger {

    private org.slf4j.Logger slf4jLogger;
    
    public SLF4JLoggerAdapter(org.slf4j.Logger slf4jLogger) {
        this.slf4jLogger = slf4jLogger;
    }

    public void debug(String message, Throwable throwable) {
        this.slf4jLogger.debug(message, throwable);
    }

    public void debug(String message) {
        this.slf4jLogger.debug(message); 
    }

    public void error(String message, Throwable throwable) {
        this.slf4jLogger.error(message, throwable);
    }

    public void error(String message) {
        this.slf4jLogger.error(message);
    }

    public void fatalError(String message, Throwable throwable) {
        this.slf4jLogger.error(message, throwable);
    }

    public void fatalError(String message) {
        this.slf4jLogger.error(message);
    }

    public Logger getChildLogger(String name) {
        String current = this.slf4jLogger.getName();
        org.slf4j.Logger child = null;
        if (current == null || current.trim().length() == 0){
            child = LoggerFactory.getLogger(name);              
        } else {
            child = LoggerFactory.getLogger(slf4jLogger.getName() + "." + name);
        }
        return new SLF4JLoggerAdapter(child);
    }

    public void info(String message, Throwable throwable) {
        this.slf4jLogger.info(message, throwable);       
    }

    public void info(String message) {
        this.slf4jLogger.info(message);  
    }

    public boolean isDebugEnabled() {
        return this.slf4jLogger.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return this.slf4jLogger.isErrorEnabled();
    }

    public boolean isFatalErrorEnabled() {
        return this.slf4jLogger.isErrorEnabled();
    }

    public boolean isInfoEnabled() {
        return this.slf4jLogger.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return this.slf4jLogger.isWarnEnabled();
    }

    public void warn(String message, Throwable throwable) {
        this.slf4jLogger.warn(message, throwable);
    }

    public void warn(String message) {
        this.slf4jLogger.warn(message);
    }
}
