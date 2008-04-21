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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This is an implementation of the {@link Settings} object.
 * The value can either be set using the various setter methods
 * or through properties ({@link #configure(Properties)}.
 *
 * The object can be set to read-only using {@link #makeReadOnly()}. From that
 * on the object is immutable and can't be changed anymore.
 *
 * @version $Id$
 * @since 1.0
 */
public class MutableSettings implements Settings {

    /** Are we still mutable? */
    protected boolean readOnly = false;

    /** Prefix for properties. */
    protected static final String KEYPREFIX = "org.apache.cocoon.";

    /** The properties used to configure Cocoon. */
    protected final Properties properties = new Properties();

    /**
     * Allow reinstantiating (reloading) of the cocoon instance. If this is
     * set to "yes" or "true", a new cocoon instance can be created using
     * the request parameter "cocoon-reload". It also enables that Cocoon is
     * reloaded when cocoon.xconf changes. Default is no for security reasons.
     */
    protected boolean reloadingEnabled;

    /**
     * This parameter is used to list classes that should be loaded at
     * initialization time of the servlet. For example, JDBC Drivers used need to
     * be named here. Additional entries may be inserted here during build
     * depending on your build properties.
     */
    protected final List loadClasses = new ArrayList();

    /**
     * This parameter allows to specify where Cocoon should create its page
     * and other objects cache. The path specified can be either absolute or
     * relative to the context path of the servlet. On windows platform,
     * absolute directory must start with volume: C:\Path\To\Cache\Directory.
     */
    protected String cacheDirectory;

    /**
     * This parameter allows to specify where Cocoon should put it's
     * working files. The path specified is either absolute or relative
     * to the context path of the Cocoon servlet. On windows platform,
     * absolute directory must start with volume: C:\Path\To\Work\Directory.
     */
    protected String workDirectory;

    /**
     * Set form encoding. This will be the character set used to decode request
     * parameters. If not set the ISO-8859-1 encoding will be assumed.
    */
    protected String formEncoding;

    /**
     * Delay between reload checks for the configuration.
     */
    protected long configurationReloadDelay;

    /** The time the cocoon instance was created. */
    protected long creationTime;

    /** 
     * The container encoding.
     * @see Settings#KEY_CONTAINER_ENCODING 
     */
    protected String containerEncoding;

    /** The optional parent settings object. */
    protected Settings parent;

    /** Running mode. */
    protected final String runningMode;

    /**
     * Create a new settings object.
     */
    public MutableSettings(String mode) {
        // set default values
        this.reloadingEnabled = SettingsDefaults.RELOADING_ENABLED_DEFAULT;
        this.configurationReloadDelay = SettingsDefaults.DEFAULT_CONFIGURATION_RELOAD_DELAY;
        this.containerEncoding = SettingsDefaults.DEFAULT_CONTAINER_ENCODING;
        this.runningMode = mode;
        this.creationTime = System.currentTimeMillis();
    }

    /**
     * Create a new child settings object.
     * @param parent The parent settings object.
     */
    public MutableSettings(Settings parent) {
        if ( parent == null ) {
            throw new IllegalArgumentException("Parent is not allowed to be null.");
        }
        this.parent = parent;
        this.runningMode = parent.getRunningMode();
    }

    /**
     * Fill from a properties object
     */
    public void configure(Properties props) {
        this.checkWriteable();
        if ( props != null ) {
            final Iterator i = props.entrySet().iterator();
            while ( i.hasNext() ) {
                final Map.Entry current = (Map.Entry)i.next();
                String key = current.getKey().toString();
                if ( key.startsWith(KEYPREFIX) ) {
                    final String value = current.getValue().toString();

                    if ( key.equals(KEY_RELOAD_DELAY) ) {
                        this.setConfigurationReloadDelay(Long.valueOf(value).longValue());
                    } else if ( key.equals(KEY_RELOADING) ) {
                        this.setReloadingEnabled(Boolean.valueOf(value).booleanValue());
                    } else if ( key.equals(KEY_CACHE_DIRECTORY) ) {
                        this.setCacheDirectory(value);
                    } else if ( key.equals(KEY_WORK_DIRECTORY) ) {
                        this.setWorkDirectory(value);
                    } else if ( key.equals(KEY_FORM_ENCODING) ) {
                        this.setFormEncoding(value);
                    } else if ( key.startsWith(KEY_LOAD_CLASSES) ) {
                        this.addToLoadClasses(value);
                    } else if ( key.startsWith(KEY_CONTAINER_ENCODING ) ) {
                        this.setContainerEncoding(value);
                    }
                }
            }
            this.properties.putAll(props);
        }
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#isReloadingEnabled(java.lang.String)
     */
    public boolean isReloadingEnabled(String type) {
        if ( type == null ) {
            if ( parent != null ) {
                return parent.isReloadingEnabled(type);
            }
            return this.reloadingEnabled;
        }
        String o = this.getProperty(KEY_RELOADING + '.' + type);
        if ( o != null ) {
            return Boolean.valueOf(o).booleanValue();
        }
        if ( this.parent != null ) {
            return this.parent.isReloadingEnabled(type);
        }
        return this.reloadingEnabled;
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getCacheDirectory()
     */
    public String getCacheDirectory() {
        if ( this.parent != null ) {
            return this.parent.getCacheDirectory();
        }
        return this.cacheDirectory;
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getFormEncoding()
     */
    public String getFormEncoding() {
        if ( this.parent != null ) {
            return this.parent.getFormEncoding();
        }
        return this.formEncoding;
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getContainerEncoding()
     */
    public String getContainerEncoding() {
        if ( this.parent != null ) {
            return this.parent.getContainerEncoding();
        }
        return this.containerEncoding;
    }

    /**
     * Set the container encoding.
     * @param value The new encoding value.
     */
    public void setContainerEncoding(String value) {
        this.checkSubSetting();
        this.containerEncoding = value;
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getLoadClasses()
     */
    public List getLoadClasses() {
        // we don't ask the parent here as the classes of the parent
        // have already been loaded
        return this.loadClasses;
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getWorkDirectory()
     */
    public String getWorkDirectory() {
        if ( this.parent != null ) {
            return this.parent.getWorkDirectory();
        }
        return this.workDirectory;
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getReloadDelay(java.lang.String)
     */
    public long getReloadDelay(String type) {
        if ( type == null ) {
            if ( parent != null ) {
                return parent.getReloadDelay(type);
            }
            return this.configurationReloadDelay;
        }
        String o = this.getProperty(KEY_RELOAD_DELAY + '.' + type);
        if ( o != null ) {
            return Long.valueOf(o).longValue();
        }
        if ( this.parent != null ) {
            return this.parent.getReloadDelay(type);
        }
        return this.configurationReloadDelay;
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getProperty(java.lang.String)
     */
    public String getProperty(String name) {
        return this.getProperty(name, null);
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getProperty(java.lang.String, java.lang.String)
     */
    public String getProperty(String key, String defaultValue) {
        if ( key == null ) {
            return defaultValue;
        }
        String value = null;
        if ( key.startsWith(KEYPREFIX) ) {
            if ( key.equals(KEY_RELOAD_DELAY) ) {
                value = String.valueOf(this.getReloadDelay(null));
            } else if ( key.equals(KEY_RELOADING) ) {
                value = String.valueOf(this.isReloadingEnabled(null));
            } else if ( key.equals(KEY_CACHE_DIRECTORY) ) {
                value = this.getCacheDirectory();
            } else if ( key.equals(KEY_WORK_DIRECTORY) ) {
                value = this.getWorkDirectory();
            } else if ( key.equals(KEY_FORM_ENCODING) ) {
                value = this.getFormEncoding();
            } else if ( key.equals(KEY_LOAD_CLASSES) ) {
                value = this.toString(this.getLoadClasses());
            } else if ( key.equals(KEY_CONTAINER_ENCODING) ) {
                value = this.containerEncoding;
            }
        }

        if ( value == null ) {
            value = this.properties.getProperty(key);
        }

        if ( value == null ) {
            if ( this.parent != null ) {
                value = this.parent.getProperty(key, defaultValue);
            } else {
                value = defaultValue;
            }
        }
        return value;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Settings:\n" +
          "Running mode : " + this.getRunningMode()+ '\n' +
          KEY_RELOAD_DELAY + " : " + this.getReloadDelay(null) + '\n' +
          KEY_RELOADING + " : " + this.isReloadingEnabled(null) + '\n' +
          KEY_LOAD_CLASSES + " : " + this.toString(this.getLoadClasses()) + '\n' +
          KEY_CACHE_DIRECTORY + " : " + this.getCacheDirectory() + '\n' +
          KEY_WORK_DIRECTORY + " : " + this.getWorkDirectory() + '\n' +
          KEY_FORM_ENCODING + " : " + this.getFormEncoding() + '\n' +
          KEY_CONTAINER_ENCODING + " : " + this.getContainerEncoding() + '\n';
    }

    /**
     * Helper method to make a string out of a list of objects.
     */
    protected String toString(List a) {
        final StringBuffer buffer = new StringBuffer();
        final Iterator i = a.iterator();
        boolean first = true;
        while ( i.hasNext() ) {
            if ( first ) {
                first = false;
            } else {
                buffer.append(", ");
            }
            buffer.append(i.next());
        }
        return buffer.toString();        
    }

    /**
     * @param allowReload The allowReload to set.
     */
    public void setReloadingEnabled(boolean allowReload) {
        this.checkWriteable();
        this.checkSubSetting();
        this.reloadingEnabled = allowReload;
    }

    /**
     * @param cacheDirectory The cacheDirectory to set.
     */
    public void setCacheDirectory(String cacheDirectory) {
        this.checkWriteable();
        this.checkSubSetting();
        this.cacheDirectory = cacheDirectory;
    }

    /**
     * @param formEncoding The formEncoding to set.
     */
    public void setFormEncoding(String formEncoding) {
        this.checkWriteable();
        this.checkSubSetting();
        this.formEncoding = formEncoding;
    }

    /**
     * @param className The loadClasses to set.
     */
    public void addToLoadClasses(String className) {
        this.checkWriteable();
        this.loadClasses.add(className);
    }

    /**
     * @param workDirectory The workDirectory to set.
     */
    public void setWorkDirectory(String workDirectory) {
        this.checkWriteable();
        this.checkSubSetting();
        this.workDirectory = workDirectory;
    }

    /**
     * @param configurationReloadDelay The configurationReloadDelay to set.
     */
    public void setConfigurationReloadDelay(long configurationReloadDelay) {
        this.checkWriteable();
        this.checkSubSetting();
        this.configurationReloadDelay = configurationReloadDelay;
    }

    /**
     * Mark this object as read-only.
     */
    public void makeReadOnly() {
        this.readOnly = false;
    }

    /**
     * check if this configuration is writeable.
     *
     * @throws IllegalStateException if this setting is read-only
     */
    protected final void checkWriteable()
    throws IllegalStateException {
        if( this.readOnly ) {
            throw new IllegalStateException
                ( "Settings is read only and can not be modified anymore." );
        }
    }

    /**
     * check if this configuration is tried to be set for a sub settings
     * object.
     *
     * @throws IllegalStateException if this setting is a sub setting
     */
    protected final void checkSubSetting()
    throws IllegalStateException {
        if( this.parent != null ) {
            throw new IllegalStateException
                ( "This value can only be changed for the root settings object." );
        }
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getCreationTime()
     */
    public long getCreationTime() {
        if ( this.parent != null ) {
            return this.parent.getCreationTime();
        }
        return this.creationTime;
    }

    /**
     * Set the creation time of the current cocoon instance.
     */
    public void setCreationTime(long value) {
        // we don't check for writable here as this value is set after the whole
        // container is setup
        this.checkSubSetting();
        this.creationTime = value;
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getPropertyNames(java.lang.String)
     */
    public List getPropertyNames(String keyPrefix) {
        final List props = new ArrayList();
        final Iterator kI = this.properties.keySet().iterator();
        while ( kI.hasNext() ) {
            final String name = (String)kI.next();
            if ( name.startsWith(keyPrefix) && !props.contains(name) ) {
                props.add(name);
            }
        }
        if ( this.parent != null ) {
            final List parentList = this.parent.getPropertyNames(keyPrefix);
            final Iterator i = parentList.iterator();
            while ( i.hasNext() ) {
                final String name = (String)i.next();
                if ( !props.contains(name) ) {
                    props.add(name);
                }
            }
        }
        return props;
    }
    
    /**
     * @see org.apache.cocoon.configuration.Settings#getPropertyNames()
     */
    public List getPropertyNames() {
        final List props = new ArrayList();
        final Iterator kI = this.properties.keySet().iterator();
        while ( kI.hasNext() ) {
            final String name = (String)kI.next();
            if (!props.contains(name) ) {
                props.add(name);
            }
        }
        if ( this.parent != null ) {
            final List parentList = this.parent.getPropertyNames();
            final Iterator i = parentList.iterator();
            while ( i.hasNext() ) {
                final String name = (String)i.next();
                if ( !props.contains(name) ) {
                    props.add(name);
                }
            }
        }
        return props;
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getRunningMode()
     */
    public String getRunningMode() {
        return this.runningMode;
    }
}
