/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.kernel.logging;

/**
 * <p>The {@link Logger} represents the root class for all core loggers of
 * the framework.</p>
 *
 * <p>This implementation never logs.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public class Logger {

    /**
     * <p>Create a new {@link Logger}.</p>
     */
    public Logger() {
        super();
    }
    
    /**
     * <p>Log a debug message.</p>
     *
     * @param message the message.
     */
    public void debug(String message) {
    }
    
    /**
     * <p>Log a debug message.</p>
     *
     * @param message the message.
     * @param throwable the throwable.
     */
    public void debug(String message, Throwable throwable) {
    }
    
    /**
     * <p>Log a info message.</p>
     *
     * @param message the message.
     */
    public void info(String message) {
    }
    
    /**
     * <p>Log a info message.</p>
     *
     * @param message the message.
     * @param throwable the throwable.
     */
    public void info(String message, Throwable throwable) {
    }
   
    /**
     * <p>Log a warn message.</p>
     *
     * @param message the message.
     */
    public void warn(String message) {
    }
    
    /**
     * <p>Log a warn message.</p>
     *
     * @param message the message.
     * @param throwable the throwable.
     */
    public void warn(String message, Throwable throwable) {
    }
    
    /**
     * <p>Log an error message.</p>
     *
     * @param message the message.
     */
    public void error(String message) {
    }
    
    /**
     * <p>Log a error message.</p>
     *
     * @param message the message.
     * @param throwable the throwable.
     */
    public void error(String message, Throwable throwable) {
    }
    
    /**
     * <p>Log a fatal error message.</p>
     *
     * @param message the message.
     */
    public void fatal(String message) {
    }
    
    /**
     * <p>Log a fatal error message.</p>
     *
     * @param message the message.
     * @param throwable the throwable.
     */
    public void fatal(String message, Throwable throwable) {
    }
}
