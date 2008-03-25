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

import org.apache.commons.lang.enums.ValuedEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;

/**
 * This class provides a special static "deprecation" logger.
 * All deprecated code should use this logger to log messages into the 
 * deprecation logger. This makes it easier for users to find out if they're 
 * using deprecated stuff.
 *
 * <p>Additionally, it is possible to set the forbidden level of deprecation messages (default
 * is to forbid ERROR, i.e. allow up to WARN). Messages equal to or above the forbidden level
 * will lead to throwing a {@link DeprecationException}. Setting the forbidden level to
 * FATAL_ERROR allows running legacy applications using deprecated features (tolerant mode), and
 * setting the forbidden level to DEBUG will run in strict mode, forbidding all deprecations.
 *
 * <p>Note that according to the above, issuing a <code>fatal</code> log message always raises
 * an exception, and can therefore be used when detecting old features that have been completely
 * removed.
 *
 * @version $Id$
 */
public class Deprecation {
    
    /**
     * The deprecation logger.
     */
    public static final Log logger = new LoggerWrapper(getLog());

    private static Log getLog() {
        SimpleLog log = new SimpleLog("cocoon.deprecation");
        log.setLevel(SimpleLog.LOG_LEVEL_ALL);
        return log;
    }

    private static final int DEBUG_VALUE = 0;
    private static final int INFO_VALUE = 1;
    private static final int WARN_VALUE = 2;
    private static final int ERROR_VALUE = 3;
    private static final int FATAL_VALUE = 4;

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
     * Fatal deprecation messages indicate features that used to exist but have been removed
     * in the current version. Logging such a message always throws a {@link DeprecationException}.
     */
    public static final LogLevel FATAL = new LogLevel("FATAL", FATAL_VALUE);

    
    public static final class LogLevel extends ValuedEnum {
        private LogLevel(String text, int value) {
            super(text, value);
        }
        
        public static LogLevel getLevel(String level) {
            return (LogLevel)ValuedEnum.getEnum(LogLevel.class, level);
        }
    }


    public static void setLogger(Log newLogger) {
        // Note: the "logger" attribute is not of type LoggerWrapper so that it appears
        // as a standard Logger in the javadocs.
        ((LoggerWrapper) logger).setLogger(newLogger);
    }
    
    public static void setForbiddenLevel(LogLevel level) {
        // If null, reset to the default level
        if (level == null) {
            level = ERROR;
        }
        ((LoggerWrapper) logger).setForbiddenLevel(level);
    }


    /**
     * Wraps a logger, and throws an DeprecatedException if the message level is
     * higher than the allowed one.
     */
    private static class LoggerWrapper implements Log {
        
        private Log delegate;
        // up to warn is allowed
        private int forbiddenLevel = ERROR_VALUE;

        
        private LoggerWrapper(Log logger) {
            this.delegate = logger;
        }
        
        private void setLogger(Log logger) {
            // Unwrap a wrapped logger
            while (logger instanceof LoggerWrapper) {
                logger = ((LoggerWrapper) logger).delegate;
            }
            this.delegate = logger;
        }
        
        private void setForbiddenLevel(LogLevel level) {
            this.forbiddenLevel = level.getValue();
        }
        
        private void throwException(int level, Object message) {
            if (level >= this.forbiddenLevel) {
                throw new DeprecationException(String.valueOf(message));
            }
        }
        
        private boolean isThrowingException(int level) {
            return level >= this.forbiddenLevel;
        }


        public void trace(Object message) {
            delegate.trace(message);
            throwException(DEBUG_VALUE, message);
        }

        public void trace(Object message, Throwable thr) {
            delegate.trace(message, thr);
            throwException(DEBUG_VALUE, message);
        }

        public void debug(Object message) {
            delegate.debug(message);
            throwException(DEBUG_VALUE, message);
        }

        public void debug(Object message, Throwable thr) {
            delegate.debug(message, thr);
            throwException(DEBUG_VALUE, message);
        }

        public void info(Object message) {
            delegate.info(message);
            throwException(INFO_VALUE, message);
        }

        public void info(Object message, Throwable thr) {
            delegate.info(message, thr);
            throwException(INFO_VALUE, message);
        }

        public void warn(Object message) {
            delegate.warn(message);
            throwException(WARN_VALUE, message);
        }

        public void warn(Object message, Throwable thr) {
            delegate.warn(message, thr);
            throwException(WARN_VALUE, message);
        }

        public void error(Object message) {
            delegate.error(message);
            throwException(ERROR_VALUE, message);
        }

        public void error(Object message, Throwable thr) {
            delegate.error(message, thr);
            throwException(ERROR_VALUE, message);
        }

        public void fatal(Object message) {
            delegate.fatal(message);
            throwException(FATAL_VALUE, message);
        }

        public void fatal(Object message, Throwable thr) {
            delegate.fatal(message, thr);
            throwException(FATAL_VALUE, message);
        }

        public boolean isTraceEnabled() {
            return isThrowingException(DEBUG_VALUE) || delegate.isTraceEnabled();
        }

        public boolean isDebugEnabled() {
            // Enable level also if it is set to throw an exception, so that
            // logging the message occurs, and then throws it.
            return isThrowingException(DEBUG_VALUE) || delegate.isDebugEnabled();
        }

        public boolean isInfoEnabled() {
            return isThrowingException(INFO_VALUE) || delegate.isInfoEnabled();
        }

        public boolean isWarnEnabled() {
            return isThrowingException(WARN_VALUE) || delegate.isWarnEnabled();
        }

        public boolean isErrorEnabled() {
            return isThrowingException(ERROR_VALUE) || delegate.isErrorEnabled();
        }

        public boolean isFatalEnabled() {
            return true;
        }
    }
}
