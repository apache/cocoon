/*
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components.slide.impl;

import org.apache.avalon.framework.logger.Logger;

/**
 * The class represent an adapter for the logger for jakarta slide
 *
 * @author <a href="mailto:stephan@vern.chem.tu-berlin.de">Stephan Michels</a>
 * @version CVS $Id: SlideLoggerAdapter.java,v 1.1 2003/12/02 19:18:45 unico Exp $
 */
public class SlideLoggerAdapter implements org.apache.slide.util.logger.Logger {
    private Logger logger;
    private int currentLogLevel = ERROR;

    public SlideLoggerAdapter(Logger logger) {
        this.logger = logger;
    }

    /**
     * Log an object thru the specified channel and with the specified level.
     *
     * @param data The object to log.
     * @param channel The channel name used for logging.
     * @param level The level used for logging.
     */
    public void log(Object data, String channel, int level) {
        if (level==CRITICAL) {
            this.logger.fatalError(data.toString());
        } else if (level==ERROR) {
            this.logger.error(data.toString());
        } else if (level==WARNING) {
            this.logger.warn(data.toString());
        } else if (level==INFO) {
            this.logger.info(data.toString());
        } else if (level==DEBUG) {
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
        if (level==CRITICAL) {
            this.logger.fatalError(data.toString());
        } else if (level==ERROR) {
            this.logger.error(data.toString());
        } else if (level==WARNING) {
            this.logger.warn(data.toString());
        } else if (level==INFO) {
            this.logger.info(data.toString());
        } else if (level==DEBUG) {
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
       if (currentLogLevel==CRITICAL) {
           this.logger.fatalError(data.toString());
       } else if (currentLogLevel==ERROR) {
           this.logger.error(data.toString());
       } else if (currentLogLevel==WARNING) {
           this.logger.warn(data.toString());
       } else if (currentLogLevel==INFO) {
           this.logger.info(data.toString());
       } else if (currentLogLevel==DEBUG) {
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
        currentLogLevel = level;
    }

    /**
     * Set the logger level for the specified channel
     *
     * @param channel
     * @param level the logger level
     */
    public void setLoggerLevel(String channel, int level) {
        currentLogLevel = level;
    }

    /**
     * Get the logger level for the default channel
     * @return logger level
     */
    public int getLoggerLevel() {
        return currentLogLevel;
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
        } else if (this.logger.isFatalErrorEnabled() ) {
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
            return DEBUG<=level;
        } else if (this.logger.isInfoEnabled()) {
            return INFO<=level;
        } else if (this.logger.isWarnEnabled()) {
            return WARNING<=level;
        } else if (this.logger.isErrorEnabled()) {
            return ERROR<=level;
        } else if (this.logger.isFatalErrorEnabled() ) {
            return CRITICAL<=level;
        } else {
            return ERROR<=level;
        }
    }

    /**
     * Check if the default channel with the specified level is enabled for logging.
     *
     * @param level   The level specification
     */
    public boolean isEnabled(int level) {
        if (this.logger.isDebugEnabled()) {
            return DEBUG<=level;
        } else if (this.logger.isInfoEnabled()) {
            return INFO<=level;
        } else if (this.logger.isWarnEnabled()) {
            return WARNING<=level;
        } else if (this.logger.isErrorEnabled()) {
            return ERROR<=level;
        } else if (this.logger.isFatalErrorEnabled() ) {
            return CRITICAL<=level;
        } else {
            return ERROR<=level;
        }
    }
}

