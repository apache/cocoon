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
package org.apache.cocoon.portal.wsrp.logging;

import org.apache.commons.logging.Log;
import org.apache.wsrp4j.log.Logger;

/**
 * A wrapper for the cocoon logger<br/>
 *
 * @version $Id$
 */
public class WSRPLogger implements Logger {

    /** The default logger */
    protected final Log logger;

    /**
     * constructor<br/>
     * 
     * @param logger
     */
    public WSRPLogger(Log logger) {
        this.logger = logger;
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#entry(int, java.lang.String)
     */
    public void entry(int logLevel, String loggingMethod) {
        this.entry(logLevel, loggingMethod, null);
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#entry(int, java.lang.String, java.lang.Object)
     */
    public void entry(int logLevel, String loggingMethod, Object parm1) {
        this.entry(logLevel, loggingMethod, new Object[] { parm1 });
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#entry(int, java.lang.String, java.lang.Object[])
     */
    public void entry(int logLevel, String loggingMethod, Object[] parms) {
        this.text(logLevel, loggingMethod, "Entering method", parms);
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#exit(int, java.lang.String)
     */
    public void exit(int logLevel, String loggingMethod) {
        this.text(logLevel, loggingMethod, "Exiting method.");
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#exit(int, java.lang.String, byte)
     */
    public void exit(int logLevel, String loggingMethod, byte retValue) {
        this.exit(logLevel, loggingMethod, new Byte(retValue));
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#exit(int, java.lang.String, short)
     */
    public void exit(int logLevel, String loggingMethod, short retValue) {
        this.exit(logLevel, loggingMethod, new Short(retValue));
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#exit(int, java.lang.String, int)
     */
    public void exit(int logLevel, String loggingMethod, int retValue) {
        this.exit(logLevel, loggingMethod, new Integer(retValue));
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#exit(int, java.lang.String, long)
     */
    public void exit(int logLevel, String loggingMethod, long retValue) {
        this.exit(logLevel, loggingMethod, new Long(retValue));
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#exit(int, java.lang.String, float)
     */
    public void exit(int logLevel, String loggingMethod, float retValue) {
        this.exit(logLevel, loggingMethod, new Float(retValue));
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#exit(int, java.lang.String, double)
     */
    public void exit(int logLevel, String loggingMethod, double retValue) {
        this.exit(logLevel, loggingMethod, new Double(retValue));
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#exit(int, java.lang.String, char)
     */
    public void exit(int logLevel, String loggingMethod, char retValue) {
        this.exit(logLevel, loggingMethod, new Character(retValue));
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#exit(int, java.lang.String, boolean)
     */
    public void exit(int logLevel, String loggingMethod, boolean retValue) {
        this.exit(logLevel, loggingMethod, Boolean.valueOf(retValue));
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#exit(int, java.lang.String, java.lang.Object)
     */
    public void exit(int logLevel, String loggingMethod, Object retValue) {
        this.text(logLevel, loggingMethod, "Exiting method. Returned value: {0}", retValue);
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#isLogging(int)
     */
    public boolean isLogging(int logLevel) {
        if (logLevel == Logger.ERROR ) {
            return this.logger.isErrorEnabled();
        } else if ( logLevel == Logger.INFO ) {            
            return this.logger.isInfoEnabled();
        } else if ( logLevel == Logger.WARN ) {            
            return this.logger.isWarnEnabled();
        } else if ( logLevel == Logger.TRACE_HIGH ) {            
            return this.logger.isInfoEnabled();
        } else if ( logLevel == Logger.TRACE_MEDIUM ) {            
            return this.logger.isDebugEnabled();
        } else if ( logLevel == Logger.TRACE_LOW ) {            
            return this.logger.isDebugEnabled();
        }
        return false;
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#stackTrace(int, java.lang.String, java.lang.String)
     */
    public void stackTrace(int logLevel, String loggingMethod, String text) {
        this.text(logLevel, loggingMethod, new Throwable("Stacktrace"), text);
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#text(int, java.lang.String, java.lang.String)
     */
    public void text(int logLevel, String loggingMethod, String text) {
        this.text(logLevel, loggingMethod, text, null);
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#text(int, java.lang.String, java.lang.String, java.lang.Object)
     */
    public void text(int logLevel, String loggingMethod, String text, Object parm1) {
        this.text(logLevel, loggingMethod, text, new Object[] { parm1 });
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#text(int, java.lang.String, java.lang.String, java.lang.Object[])
     */
    public void text(int logLevel, String loggingMethod, String text, Object[] parms) {
        this.text(logLevel, loggingMethod, (Throwable) null, text, parms);
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#text(int, java.lang.String, java.lang.Throwable, java.lang.String)
     */
    public void text(int logLevel, String loggingMethod, Throwable t, String text) {
        this.text(logLevel, loggingMethod, t, text, null);
    }

    /**
     * @see org.apache.wsrp4j.log.Logger#text(int, java.lang.String, java.lang.Throwable, java.lang.String, java.lang.Object[])
     */
    public void text(int logLevel, String loggingMethod, Throwable t, String text, Object[] parms) {
        if (!this.isLogging(logLevel)) {
            return;
        }
        StringBuffer msgBuffer = new StringBuffer();
        if (loggingMethod != null) {
            msgBuffer.append(loggingMethod);
            msgBuffer.append(" - ");
        }
        if (text != null) {
            msgBuffer.append(text);
        }
        if (parms != null) {
            msgBuffer.append("\nParameters:\n");
            for (int i = 0; i < parms.length; i++) {
                msgBuffer.append(parms[i]);
            }
        }

        if (logLevel == Logger.ERROR ) {
            this.logger.error(msgBuffer.toString(), t);
        } else if ( logLevel == Logger.INFO ) {            
            this.logger.info(msgBuffer.toString(), t);
        } else if ( logLevel == Logger.WARN ) {            
            this.logger.warn(msgBuffer.toString(), t);
        } else if ( logLevel == Logger.TRACE_HIGH ) {            
            this.logger.info(msgBuffer.toString(), t);
        } else if ( logLevel == Logger.TRACE_MEDIUM ) {            
            this.logger.debug(msgBuffer.toString(), t);
        } else if ( logLevel == Logger.TRACE_LOW ) {            
            this.logger.debug(msgBuffer.toString(), t);
        }
    }
}
