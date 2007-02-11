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

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.lang.enums.Enum;
import org.apache.commons.lang.enums.ValuedEnum;

/**
 * This class provides a special static "deprecation" logger.
 * All deprecated code should use this logger to log messages into the 
 * deprecation logger. This makes it easier for users to find out if they're 
 * using deprecated stuff.
 * <p>
 * Additionally, it is possible to set the forbidden level of deprecation messages (default
 * is to forbid ERROR, i.e. allow up to WARN). Messages equal to or above the forbidden level
 * will lead to throwing a {@link DeprecationException}. Setting the forbidden level to
 * FATAL_ERROR allows running legacy applications using deprecated features (tolerant mode), and
 * setting the forbidden level to DEBUG will run in strict mode, forbidding all deprecations.
 * <p>
 * Note that according to the above, issuing a fatalError log always raises an exception, and
 * can therefore be used when detecting old features that have been totally removed.
 *
 * @version $Id$
 */
public class Deprecation {
    
    /**
     * The deprecation logger.
     */
    public static final Logger logger = new LoggerWrapper(new ConsoleLogger());
    
    private static final int DEBUG_VALUE = 0;
    private static final int INFO_VALUE = 1;
    private static final int WARN_VALUE = 2;
    private static final int ERROR_VALUE = 3;
    private static final int FATAL_VALUE = 3;
    private static final int FATAL_ERROR_VALUE = 4;
    
    /**
     * Debug deprecation messages indicate features that are no more considered "current"
     * or "best practice", but for which no removal is currently foreseen.
     */
    public static final LogLevel DEBUG = new LogLevel("DEBUG", DEBUG_VALUE);

    /**
     * Info deprecation messages indicate features that are no more considered "current"
     * or "best practice", and that will probably be removed in future releases.
     */
    public static final LogLevel INFO = new LogLevel("INFO", INFO_VALUE);

    /**
     * Warning deprecation messages indicate features that will be removed in the next major
     * version (e.g. 2.1.x --> 2.2.0). Such features should not be used if the application is
     * planned to be migrated to newer Cocoon versions.
     */
    public static final LogLevel WARN = new LogLevel("WARN", WARN_VALUE);

    /**
     * Error deprecation messages indicate features that will be removed in the next minor
     * version (e.g. 2.1.6 --> 2.1.7). Although still functional, users are stronly invited to
     * not use them.
     */
    public static final LogLevel ERROR = new LogLevel("ERROR", ERROR_VALUE);

    /**
     * Fatal error deprecation messages indicate features that used to exist but have been removed
     * in the current version. Logging such a message always throws a {@link DeprecationException}.
     */
    public static final LogLevel FATAL_ERROR = new LogLevel("FATAL_ERROR", FATAL_ERROR_VALUE);
    
    public static final class LogLevel extends ValuedEnum {
        private LogLevel(String text, int value) {
            super(text, value);
        }
        
        public static LogLevel getLevel(String level) {
            return (LogLevel)Enum.getEnum(LogLevel.class, level);
        }
    }

    public static void setLogger(Logger newLogger) {
        // Note: the "logger" attribute is not of type LoggerWrapper so that it appears
        // as a standard Logger in the javadocs.
        ((LoggerWrapper)logger).setLogger(newLogger);
    }
    
    public static void setForbiddenLevel(LogLevel level) {
        // If null, reset to the default level
        if (level == null) {
            level = ERROR;
        }
        ((LoggerWrapper)logger).setForbiddenLevel(level);
    }
    
    /**
     * Wraps a logger, and throws an DeprecatedException if the message level is
     * higher than the allowed one.
     */
    private static class LoggerWrapper implements Logger {
        
        private Logger realLogger;
        // up to warn is allowed
        private int forbiddenLevel = ERROR_VALUE;
        
        public LoggerWrapper(Logger logger) {
            this.realLogger = logger;
        }
        
        public void setLogger(Logger logger) {
            // Unwrap a wrapped logger
            while(logger instanceof LoggerWrapper) {
                logger = ((LoggerWrapper)logger).realLogger;
            }
            this.realLogger = logger;
        }
        
        public void setForbiddenLevel(LogLevel level) {
            this.forbiddenLevel = level.getValue();
        }
        
        private void throwException(int level, String message) {
            if (level >= this.forbiddenLevel) {
                throw new DeprecationException(message);
            }
        }
        
        private boolean isThrowingException(int level) {
            return level >= this.forbiddenLevel;
        }
        
        public void debug(String message) {
            realLogger.debug(message);
            throwException(DEBUG_VALUE, message);
        }
        public void debug(String message, Throwable thr) {
            realLogger.debug(message, thr);
            throwException(DEBUG_VALUE, message);
        }
        public void info(String message) {
            realLogger.info(message);
            throwException(INFO_VALUE, message);
        }
        public void info(String message, Throwable thr) {
            realLogger.info(message, thr);
            throwException(INFO_VALUE, message);
        }
        public void warn(String message) {
            realLogger.warn(message);
            throwException(WARN_VALUE, message);
        }
        public void warn(String message, Throwable thr) {
            realLogger.warn(message, thr);
            throwException(WARN_VALUE, message);
        }
        public void error(String message) {
            realLogger.error(message);
            throwException(ERROR_VALUE, message);
        }
        public void error(String message, Throwable thr) {
            realLogger.error(message, thr);
            throwException(ERROR_VALUE, message);
        }
        public void fatalError(String message) {
            realLogger.fatalError(message);
            throwException(FATAL_VALUE, message);
        }
        public void fatalError(String message, Throwable thr) {
            realLogger.fatalError(message, thr);
            throwException(FATAL_VALUE, message);
        }
        public boolean isDebugEnabled() {
            // Enable level also if it is set to throw an exception, so that
            // logging the message occurs, and then throws it.
            return isThrowingException(DEBUG_VALUE) || realLogger.isDebugEnabled();
        }
        public boolean isInfoEnabled() {
            return isThrowingException(INFO_VALUE) || realLogger.isInfoEnabled();
        }
        public boolean isWarnEnabled() {
            return isThrowingException(WARN_VALUE) || realLogger.isWarnEnabled();
        }
        public boolean isErrorEnabled() {
            return isThrowingException(ERROR_VALUE) || realLogger.isErrorEnabled();
        }
        public boolean isFatalErrorEnabled() {
            return true;
        }
        public Logger getChildLogger(String message) {
            return realLogger.getChildLogger(message);
        }
    }
}
