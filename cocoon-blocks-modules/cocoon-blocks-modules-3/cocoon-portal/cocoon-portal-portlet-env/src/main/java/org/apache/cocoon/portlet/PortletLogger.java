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
package org.apache.cocoon.portlet;

import org.apache.avalon.framework.logger.Logger;

import javax.portlet.PortletContext;

/**
 * Logger for JSR-168 Portlet context.
 *
 * @version $Id$
 */
public class PortletLogger implements Logger {

    /** Typecode for debugging messages. */
    public static final int LEVEL_DEBUG = 0;

    /** Typecode for informational messages. */
    public static final int LEVEL_INFO = 1;

    /** Typecode for warning messages. */
    public static final int LEVEL_WARN = 2;

    /** Typecode for error messages. */
    public static final int LEVEL_ERROR = 3;

    /** Typecode for fatal error messages. */
    public static final int LEVEL_FATAL = 4;

    /** Typecode for disabled log levels. */
    public static final int LEVEL_DISABLED = 5;

    private final PortletContext portletContext;
    private final int logLevel;

    /**
     * Creates a new Logger with the priority set to DEBUG.
     */
    public PortletLogger( final PortletContext servletConfig ) {
        this( servletConfig, LEVEL_DEBUG );
    }

    /**
     * Creates a new Logger.
     * @param portletContext PortletContext to log messages to
     * @param logLevel log level typecode
     */
    public PortletLogger( final PortletContext portletContext, final int logLevel ) {
        this.portletContext = portletContext;
        this.logLevel = logLevel;

        if ( this.portletContext == null ) {
            throw new NullPointerException( "portletContext" );
        }
        if ( this.logLevel < LEVEL_DEBUG || this.logLevel > LEVEL_DISABLED ) {
            throw new IllegalArgumentException( "Bad logLevel: " + this.logLevel );
        }
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#debug(java.lang.String)
     */
    public void debug( final String message ) {
        debug( message, null );
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#debug(java.lang.String, java.lang.Throwable)
     */
    public void debug( final String message, final Throwable throwable ) {
        if( this.logLevel <= LEVEL_DEBUG ) {
            this.portletContext.log( "[DEBUG] " + message, throwable );
        }
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#isDebugEnabled()
     */
    public boolean isDebugEnabled() {
        return this.logLevel <= LEVEL_DEBUG;
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#info(java.lang.String)
     */
    public void info( final String message ) {
        info( message, null );
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#info(java.lang.String, java.lang.Throwable)
     */
    public void info( final String message, final Throwable throwable ) {
        if( this.logLevel <= LEVEL_INFO ) {
            this.portletContext.log( "[INFO] " + message, throwable );
        }
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#isInfoEnabled()
     */
    public boolean isInfoEnabled() {
        return this.logLevel <= LEVEL_INFO;
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#warn(java.lang.String)
     */
    public void warn( final String message ) {
        warn( message, null );
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#warn(java.lang.String, java.lang.Throwable)
     */
    public void warn( final String message, final Throwable throwable ) {
        if( this.logLevel <= LEVEL_WARN ) {
            this.portletContext.log( "[WARNING] " + message, throwable );
        }
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#isWarnEnabled()
     */
    public boolean isWarnEnabled() {
        return this.logLevel <= LEVEL_WARN;
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#error(java.lang.String)
     */
    public void error( final String message ) {
        error( message, null );
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#error(java.lang.String, java.lang.Throwable)
     */
    public void error( final String message, final Throwable throwable ) {
        if( this.logLevel <= LEVEL_ERROR ) {
            this.portletContext.log( "[ERROR] " + message, throwable );
        }
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#isErrorEnabled()
     */
    public boolean isErrorEnabled() {
        return this.logLevel <= LEVEL_ERROR;
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#fatalError(java.lang.String)
     */
    public void fatalError( final String message ) {
        fatalError( message, null );
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#fatalError(java.lang.String, java.lang.Throwable)
     */
    public void fatalError( final String message, final Throwable throwable ) {
        if( this.logLevel <= LEVEL_FATAL ) {
            this.portletContext.log( "[FATAL ERROR] " + message, throwable );
        }
    }

    /**
     * @see org.apache.avalon.framework.logger.Logger#isFatalErrorEnabled()
     */
    public boolean isFatalErrorEnabled() {
        return this.logLevel <= LEVEL_FATAL;
    }

    /**
     * Just returns this logger (<code>PortletLogger</code> is not hierarchical).
     *
     * @param name ignored
     * @return this logger
     * @see org.apache.avalon.framework.logger.Logger#getChildLogger(java.lang.String)
     */
    public Logger getChildLogger( final String name ) {
        return this;
    }
}
