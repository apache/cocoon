/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.osgi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.logger.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;

/**
 * @version $Id$
 */
public class OSGiLogger implements Logger {
	
	/** Set the default loglevel to 4 which is DEBUG */
	private static final int DEFAULT_LOG_LEVEL = 4;

    private String category = "";
    private LogService logService;
	private ComponentContext componentContext;
	private int logLevel;
    
    protected void activate(ComponentContext componentContext) {
    	this.componentContext = componentContext;
    	String logLevelProperty = (String) this.componentContext.getProperties().get("logLevel");
    	if(logLevelProperty == null) {
    		logLevel = DEFAULT_LOG_LEVEL;
    	}
    	else if(logLevelProperty.equalsIgnoreCase("ERROR")) {
    		logLevel = LogService.LOG_ERROR;
    	}
    	else if(logLevelProperty.equalsIgnoreCase("WARN")) {
    		logLevel = LogService.LOG_WARNING;
    	}    
    	else if(logLevelProperty.equalsIgnoreCase("INFO")) {
    		logLevel = LogService.LOG_INFO;
    	}      	
    	else if(logLevelProperty.equalsIgnoreCase("DEBUG")) {
    		logLevel = LogService.LOG_DEBUG;
    	}
    }

    protected String getCategory() {
		return category;
	}

	protected void setCategory(String category) {
		this.category = category;
	}

	protected LogService getLogService() {
		return logService;
	}

	protected void setLogService(LogService logService) {
		this.logService = logService;
	}

    
    private void log(int level, String msg) {
        if (level <= this.logLevel) {
            logService.log(level, "[" + category + "] " + msg);
        }
    }
    
    private void log(int level, String msg, Throwable thr) {
        if (level <= this.logLevel) {
            logService.log(level, msg, thr);
        }
    }
    
    private static Map loggers = Collections.synchronizedMap(new HashMap());

    protected Logger getLoggerForCategory(String category) {
        Logger result = (Logger) loggers.get(category);
        if (result == null) {
            result = new OSGiLogger();
            ((OSGiLogger) result).setCategory(category);
            ((OSGiLogger) result).setLogLevel(this.logLevel);
            ((OSGiLogger) result).setLogService(this.logService);            
            loggers.put(category, result);
        }
        return result;
    }   
    
    private boolean isLevelEnabled(int level) {
        return level <= this.logLevel;
    }

    
    // ~~~~~~~~~~~~~~~~~~~ logging methods ~~~~~~~~~~~~~~~~~~~~~~~
    
    public Logger getChildLogger(String category) {
        return getLoggerForCategory(this.category.length() == 0 ? category : this.category + "." + category);
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

	protected int getLogLevel() {
		return logLevel;
	}

	protected void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
	}

}
