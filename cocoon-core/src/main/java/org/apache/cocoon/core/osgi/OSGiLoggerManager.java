/*
 * Copyright 2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.osgi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.logger.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * An implementation of Avalon's <code>LoggerManager</code> on top of OSGi's <code>LogService</code>.
 * OSGi's service provides no way to check if a particular log level is enabled. Rather than always
 * considering all levels to be enabled, which can lead to useless expensive expressions, the maximum
 * log level is given at manager creation time, and is uses by all log categories within the bundle.
 * 
 * @version $Id$
 * @since 2.2
 */
public class OSGiLoggerManager implements LoggerManager {
    
    LogService logService;
    int maxLevel;
    
    /**
     * Create an <code>OSGiLoggerManager</code>.
     * 
     * @param ctx the <code>BundleContext</code> used to get the <code>LogService</code>
     * @param maxLevel the maximum log level (error is the lowest, debug the highest).
     */
    public OSGiLoggerManager(final BundleContext ctx, int maxLevel) {
        this.maxLevel = maxLevel;
        
        // Lookup the log service
        final ServiceReference logRef = ctx.getServiceReference(LogService.class.getName());
        this.logService = (LogService)ctx.getService(logRef);
        
        // FIXME: check if we need this
        ctx.addBundleListener(new BundleListener() {

            public void bundleChanged(BundleEvent ev) {
                if (ev.getType() == BundleEvent.STOPPED) {
                    // release the log service.
                    ctx.ungetService(logRef);
                }
            }
        });
    }

    /** Loggers by category */
    private Map loggers = Collections.synchronizedMap(new HashMap());

    public Logger getLoggerForCategory(String category) {
        Logger result = (Logger)loggers.get(category);
        if (result == null) {
            result = new OSGiLogger(category);
            loggers.put(category, result);
        }
        return result;
    }

    public Logger getDefaultLogger() {
        return getLoggerForCategory("");
    }
    
    /**
     * A Logger delegating to OSGi's LogService
     */
    private class OSGiLogger implements Logger {
        
        private String category;

        OSGiLogger(String category) {
            this.category = category;
        }
        
        private void log(int level, String msg) {
            if (level <= maxLevel) {
                logService.log(level, "[" + category + "] " + msg);
            }
        }
        
        private void log(int level, String msg, Throwable thr) {
            if (level <= maxLevel) {
                logService.log(level, msg, thr);
            }
        }
        
        public Logger getChildLogger(String category) {
            return getLoggerForCategory(this.category.length() == 0 ? category : this.category + "." + category);
        }
        
        private boolean isLevelEnabled(int level) {
            return level <= maxLevel;
        }

        public void debug(String msg) {
            log(LogService.LOG_DEBUG, msg);
        }

        public void debug(String msg, Throwable thr) {
            log(LogService.LOG_DEBUG, msg, thr);
            
        }

        public boolean isDebugEnabled() {
            return isLevelEnabled(LogService.LOG_DEBUG);
        }

        public void info(String msg) {
            log(LogService.LOG_INFO, msg);
        }

        public void info(String msg, Throwable thr) {
            log(LogService.LOG_INFO, msg, thr);
        }

        public boolean isInfoEnabled() {
            return isLevelEnabled(LogService.LOG_INFO);
        }

        public void warn(String msg) {
            log(LogService.LOG_WARNING, msg);
        }

        public void warn(String msg, Throwable thr) {
            log(LogService.LOG_WARNING, msg, thr);
        }

        public boolean isWarnEnabled() {
            return isLevelEnabled(LogService.LOG_WARNING);
        }

        public void error(String msg) {
            log(LogService.LOG_ERROR, msg);
        }

        public void error(String msg, Throwable thr) {
            log(LogService.LOG_ERROR, msg, thr);
        }

        public boolean isErrorEnabled() {
            return isLevelEnabled(LogService.LOG_ERROR);
        }

        public void fatalError(String msg) {
            // OSGi has no "fatal" level
            log(LogService.LOG_ERROR, msg);
        }

        public void fatalError(String msg, Throwable thr) {
            // OSGi has no "fatal" level
            log(LogService.LOG_ERROR, msg, thr);
        }

        public boolean isFatalErrorEnabled() {
            // OSGi has no "fatal" level
           return isLevelEnabled(LogService.LOG_ERROR);
        }
    }
}
