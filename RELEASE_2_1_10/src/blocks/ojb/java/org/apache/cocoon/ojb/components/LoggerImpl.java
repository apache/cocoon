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
package org.apache.cocoon.ojb.components;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.ojb.broker.util.configuration.Configuration;
import org.apache.ojb.broker.util.configuration.ConfigurationException;
import org.apache.ojb.broker.util.logging.Logger;
import org.apache.ojb.broker.util.logging.LoggingConfiguration;

/**
 * OJB logger implementation delegating to the Avalon logger.
 *
 * <p>This class has two faces to it:
 * <dl>
 * <dt>Avalon Component</dt>
 * <dd>Instance of the class created and managed by Avalon container.
 * When instance is initialized, it obtains logger instance to be used
 * by OJB.</dd>
 * <dt>OJB Managed Class</dt>
 * <dd>Instances of the class are created and managed by OJB, as defined
 * in the OJB <code>OJB.properties</code> file. Each OJB managed instance
 * of the class will have access to the logger object initialized
 * by Avalon managed instance of the class.</dd>
 * </dl>
 *
 * It is important that Avalon component is initialized before any access
 * to OJB API is made.</p>
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version $Id$
 */
public class LoggerImpl extends AbstractLogEnabled
                        implements Component, ThreadSafe, Initializable,
                                   Logger {

    /**
     * Root logger for all OJB loggers
     */
    private static org.apache.avalon.framework.logger.Logger LOGGER;

    private final String name;
    private transient int level;
    private transient org.apache.avalon.framework.logger.Logger logger;

    /**
     * Constructor used by Container
     */
    public LoggerImpl() {
        this.name = null;
    }

    /**
     * Constructor used by OJB 1.0 to create a logger instance
     */
    public LoggerImpl(String name) {
        this.name = name.startsWith("org.apache.ojb.")? name.substring(15): name;
    }

    /**
     * Constructor used by OJB 1.1 to create a logger instance
     */
    public LoggerImpl(String name, LoggingConfiguration config) {
        this(name);
    }

    /**
     * Set root logger instance which will be used by OJB
     */
    public void initialize() {
        LOGGER = getLogger();
    }

    protected int getLevel() {
        if (logger == null) {
            this.logger = LOGGER.getChildLogger(this.name);
            if (this.logger.isDebugEnabled()) this.level = DEBUG;
            else if (this.logger.isInfoEnabled()) this.level = INFO;
            else if (this.logger.isWarnEnabled()) this.level = WARN;
            else if (this.logger.isErrorEnabled()) this.level = ERROR;
            else this.level = FATAL;
        }
        return level;
    }

    public String getName() {
        return name;
    }

    public void debug(Object message) {
        if (DEBUG >= getLevel()) {
            logger.debug(toString(message));
        }
    }

    public void debug(Object message, Throwable t) {
        if (DEBUG >= getLevel()) {
            logger.debug(toString(message), t);
        }
    }

    public void safeDebug(String message, Object obj) {
        if (DEBUG >= getLevel()) {
            logger.debug(message + " : " + toString(obj));
        }
    }

    public void safeDebug(String message, Object obj, Throwable t) {
        if (DEBUG >= getLevel()) {
            logger.debug(message + " : " + toString(obj), t);
        }
    }

    public void info(Object message) {
        if (INFO >= getLevel()) {
            logger.info(toString(message));
        }
    }

    public void info(Object message, Throwable t) {
        if (INFO >= getLevel()) {
            logger.info(toString(message), t);
        }
    }

    public void safeInfo(String message, Object obj) {
        if (INFO >= getLevel()) {
            logger.info(message + " : " + toString(obj));
        }
    }

    public void safeInfo(String message, Object obj, Throwable t) {
        if (INFO >= getLevel()) {
            logger.info(message + " : " + toString(obj), t);
        }
    }

    public void warn(Object message) {
        if (WARN >= getLevel()) {
            logger.warn(toString(message));
        }
    }

    public void warn(Object message, Throwable t) {
        if (WARN >= getLevel()) {
            logger.warn(toString(message), t);
        }
    }

    public void safeWarn(String message, Object obj) {
        if (WARN >= getLevel()) {
            logger.warn(message + " : " + toString(obj));
        }
    }

    public void safeWarn(String message, Object obj, Throwable t) {
        if (WARN >= getLevel()) {
            logger.warn(message + " : " + toString(obj), t);
        }
    }

    public void error(Object message) {
        if (ERROR >= getLevel()) {
            logger.error(toString(message));
        }
    }

    public void error(Object message, Throwable t) {
        if (ERROR >= getLevel()) {
            logger.error(toString(message), t);
        }
    }

    public void safeError(String message, Object obj) {
        if (ERROR >= getLevel()) {
            logger.error(message + " : " + toString(obj));
        }
    }

    public void safeError(String message, Object obj, Throwable t) {
        if (ERROR >= getLevel()) {
            logger.error(message + " : " + toString(obj), t);
        }
    }

    public void fatal(Object message) {
        if (FATAL >= getLevel()) {
            logger.fatalError(toString(message));
        }
    }

    public void fatal(Object message, Throwable t) {
        if (FATAL >= getLevel()) {
            logger.fatalError(toString(message), t);
        }
    }

    public void safeFatal(String message, Object obj) {
        if (FATAL >= getLevel()) {
            logger.fatalError(message + " : " + toString(obj));
        }
    }

    public void safeFatal(String message, Object obj, Throwable t) {
        if (FATAL >= getLevel()) {
            logger.fatalError(message + " : " + toString(obj), t);
        }
    }

    public boolean isDebugEnabled() {
        return isEnabledFor(DEBUG);
    }

    public boolean isEnabledFor(int priority) {
        return priority >= getLevel();
    }

    /*
     * @see org.apache.ojb.broker.util.configuration.Configurable#configure(Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
    }

    private String toString(Object obj) {
        if (obj != null) {
            try {
                return obj.toString();
            } catch (Throwable throwable) {
                return "BAD toString() impl for " + obj.getClass().getName();
            }
        }

        return "null";
    }
}
