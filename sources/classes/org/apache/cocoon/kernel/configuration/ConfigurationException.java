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
package org.apache.cocoon.kernel.configuration;

/**
 * <p>An exception identifying an error in a configuration operation.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.3 $)
 */
public class ConfigurationException extends Exception {

    /** <p>The associated {@link Configuration} element.</p> */
    private Configuration configuration = null;

    /**
     * <p>Create a new {@link ConfigurationException} instance.</p>
     */
    public ConfigurationException() {
        super();
    }

    /**
     * <p>Create a new {@link ConfigurationException} instance with a
     * specified detail message.</p>
     *
     * @param message the detail message of this exception.
     */
    public ConfigurationException(String message) {
        super(message);
    }
    
    /**
     * <p>Create a new {@link ConfigurationException} instance with a
     * specified detail message and a related {@link Configuration}.</p>
     *
     * @param message the detail message of this exception.
     * @param configuration the related configuration element.
     */
    public ConfigurationException(String message, Configuration configuration) {
        super(message + (configuration == null ? "" :
                         " [" + configuration.location() + "]"));
    }
    
    /**
     * <p>Create a new {@link ConfigurationException} instance with a
     * specified detail message and cause.</p>
     *
     * @param message the detail message of this exception.
     * @param cause the cause of this exception.
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * <p>Create a new {@link ConfigurationException} instance with a
     * specified detail message and cause.</p>
     *
     * @param message the detail message of this exception.
     * @param configuration the related configuration element.
     * @param cause the cause of this exception.
     */
    public ConfigurationException(String message, Configuration configuration,
                                  Throwable cause) {
        super(message + (configuration == null ? "" :
                         " [" + configuration.location() + "]"), cause);
    }
    
    /**
     * <p>Create a new {@link ConfigurationException} instance with a
     * specified cause.</p>
     *
     * @param cause the cause of this exception.
     */
    public ConfigurationException(Throwable cause) {
        super((cause == null ? null : cause.toString()), cause);
    }

    /**
     * <p>Return the associated {@link Configuration} element.</p>
     *
     * @return a {@link Configuration} element or <b>null</b>.
     */
    public Configuration getConfiguration() {
        return(this.configuration);
    }

    /**
     * <p>Return the associated {@link Configuration} location if known.</p>
     *
     * @see Configuration#location()
     * @return a <b>non null</b> location {@link String}.
     */
    public String getLocation() {
        if (this.configuration != null) return(configuration.location());
        return("null@-1,-1");
    }
}
