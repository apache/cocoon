/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.configuration;

import java.util.List;

/**
 * This object holds the global configuration of Cocoon.
 *
 * @version $Id$
 * @since 1.0
 */
public interface Settings {

    /** The role to lookup this bean. */
    String ROLE = Settings.class.getName();

    /** Name of the property specifying a custom user properties file. */
    String PROPERTY_USER_SETTINGS = "org.apache.cocoon.settings";

    /**
     * This parameter is used to list classes that should be loaded at
     * initialization time of the servlet. For example, JDBC Drivers used need to
     * be named here. Additional entries may be inserted here during build
     * depending on your build properties.
     */
    String KEY_LOAD_CLASSES = "org.apache.cocoon.classloader.load.classes";

    /**
     * This parameter allows to specify where Cocoon should create its page
     * and other objects cache. The path specified can be either absolute or
     * relative to the context path of the servlet. On windows platform,
     * absolute directory must start with volume: C:\Path\To\Cache\Directory.
     */
    String KEY_CACHE_DIRECTORY = "org.apache.cocoon.cache.directory";

    /**
     * This parameter allows to specify where Cocoon should put it's
     * working files. The path specified is either absolute or relative
     * to the context path of the Cocoon servlet. On windows platform,
     * absolute directory must start with volume: C:\Path\To\Work\Directory.
     */
    String KEY_WORK_DIRECTORY = "org.apache.cocoon.work.directory";

    /**
     * Set form encoding. This will be the character set used to decode request
     * parameters. If not set the ISO-8859-1 encoding will be assumed.
    */
    String KEY_FORM_ENCODING = "org.apache.cocoon.formencoding";

    /**
     * Set encoding used by the container. If not set the ISO-8859-1 encoding
     * will be assumed.
     * Since the servlet specification requires that the ISO-8859-1 encoding
     * is used (by default), you should never change this value unless
     * you have a buggy servlet container.
     */
    String KEY_CONTAINER_ENCODING = "org.apache.cocoon.containerencoding";

    /**
     * Allow reinstantiating (reloading) of the cocoon instance. If this is
     * set to "yes" or "true", a new cocoon instance can be created using
     * the request parameter "cocoon-reload". It also enables that Cocoon is
     * reloaded when cocoon.xconf changes. Default is no for security reasons.
     */
    String KEY_RELOADING = "org.apache.cocoon.reloading";

    /**
     * Delay between reload checks for the configuration.
     */
    String KEY_RELOAD_DELAY = "org.apache.cocoon.reload-delay";

    /**
     * Get the value of a property.
     * @param key The name of the property.
     * @return The value of the property or null.
     */
    String getProperty(String key);

    /**
     * Get the value of a property.
     * @param key The name of the property.
     * @param defaultValue The value returned if the property is not available.
     * @return The value of the property or if the property cannot
     *         be found the default value.
     */
    String getProperty(String key, String defaultValue);

    /**
     * Return all available property names starting with the prefix.
     * @param keyPrefix The prefix each property name must have.
     * @return A list of property names (including the prefix) or
     *         an empty list.
     */
    List getPropertyNames(String keyPrefix);

    /**
     * Return all available property names.
     * @return A list of all property names or an empty list.
     */
    List getPropertyNames();

    /**
     * @return Returns the loadClasses.
     * @see #KEY_LOAD_CLASSES
     */
    List getLoadClasses();

    /**
     * @return Returns the workDirectory.
     * @see #KEY_WORK_DIRECTORY
     */
    String getWorkDirectory();

    /**
     * @return Returns the formEncoding.
     * @see #KEY_FORM_ENCODING
     */
    String getFormEncoding();

    /**
     * @return Returns the container encoding
     * @see #KEY_CONTAINER_ENCODING
     */
    String getContainerEncoding();

    /**
     * @return Returns the cacheDirectory.
     * @see #KEY_CACHE_DIRECTORY
     */
    String getCacheDirectory();

    /**
     * The creation time of the current settings instance.
     * @return The creation time.
     */
    long getCreationTime();

    /**
     * This method can be used by components to query if they are
     * configured to check for reloading.
     * @param type The type of the component that wants to check for reload.
     * @return Returns if reloading is enabled for this component.
     * @see #KEY_RELOADING
     */
    boolean isReloadingEnabled(String type);

    /**
     * This method can be used by components to get the configured
     * delay period inbetween checks.
     * @param type The type of the component that wants to check for reload.
     * @return Returns the delay inbetween checks in milliseconds.
     * @see #KEY_RELOAD_DELAY
     */
    long getReloadDelay(String type);

    /**
     * Return the current running mode.
     * @return The current running mode.
     */
    String getRunningMode();
}
