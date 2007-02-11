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
package org.apache.cocoon.components;

import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.util.SettingsHelper;
import org.apache.cocoon.util.Settings;

/**
 * Property Aware Configuration.
 * This component extends the {@link DefaultConfiguration}
 * by suppporting configurations that can contain placeholders for System Properties.
 *
 * @version SVN $Id: $
 */
public class PropertyAwareConfiguration extends DefaultConfiguration {
    private Settings settings;
    private Logger logger;

    /**
     * Copy constructor, to create a clone of another configuration.
     * To modify children, use <code>getChild()</code>,
     * <code>removeChild()</code> and <code>addChild()</code>.
     *
     * @param config   the <code>Configuration</code> to copy
     * @param deepCopy true will cause clones of the children to be added,
     *                 false will add the original instances and is thus
     *                 faster.
     * @param settings The Settings to use when resolving tokens
     * @param logger   A Logger to use
     * @throws ConfigurationException if an error occurs when copying
     */
    public PropertyAwareConfiguration(Configuration config, boolean deepCopy, Settings settings, Logger logger)
            throws ConfigurationException {
        super(config, deepCopy);
        this.settings = settings;
        this.logger = logger;
    }

    /**
     * Shallow copy constructor, suitable for craeting a writable clone of
     * a read-only configuration. To modify children, use <code>getChild()</code>,
     * <code>removeChild()</code> and <code>addChild()</code>.
     *
     * @param config the <code>Configuration</code> to copy
     * @param settings The Settings to use when resolving tokens
     * @param logger A Logger to use
     * @throws ConfigurationException if an error occurs when copying
     */
    public PropertyAwareConfiguration(Configuration config, Settings settings, Logger logger)
            throws ConfigurationException {
        super(config);
        this.settings = settings;
        this.logger = logger;
    }

    /**
     * Create a new <code>DefaultConfiguration</code> instance.
     *
     * @param name a <code>String</code> value
     */
    public PropertyAwareConfiguration(final String name, Settings settings, Logger logger) {
        super(name);
        this.settings = settings;
        this.logger = logger;
    }

    /**
     * Create a new <code>DefaultConfiguration</code> instance.
     *
     * @param name     a <code>String</code> value
     * @param location a <code>String</code> value
     * @param settings The Settings to use when resolving tokens
     * @param logger A Logger to use
     */
    public PropertyAwareConfiguration(final String name, final String location, Settings settings, Logger logger) {
        super(name, location);
        this.settings = settings;
        this.logger = logger;
    }

    /**
     * Create a new <code>DefaultConfiguration</code> instance.
     *
     * @param name     config node name
     * @param location Builder-specific locator string
     * @param ns       Namespace string (typically a URI). Should not be null; use ""
     *                 if no namespace.
     * @param prefix   A short string prefixed to element names, associating
     *                 elements with a longer namespace string. Should not be null; use "" if no
     *                 namespace.
     * @param settings The Settings to use when resolving tokens
     * @param logger A Logger to use
     */
    public PropertyAwareConfiguration(final String name,
                                      final String location,
                                      final String ns,
                                      final String prefix,
                                      Settings settings,
                                      Logger logger) {
        super(name, location, ns, prefix);
        this.settings = settings;
        this.logger = logger;
    }

    /**
     * Create a new <code>DefaultConfiguration</code> instance.
     *
     * @param config   A DefaultConfiguration
     * @param settings The Settings to use when resolving tokens
     * @param logger A Logger to use
     */
    public PropertyAwareConfiguration(final DefaultConfiguration config, Settings settings, Logger logger)
            throws ConfigurationException {
        super(config, false);
        this.settings = settings;
        this.logger = logger;
    }

    /**
     * Set the value of this <code>Configuration</code> object to the specified string.
     *
     * @param value a <code>String</code> value
     */
    public void setValue(final String value) {
        super.setValue(SettingsHelper.replace(value, this.settings, this.logger));
    }

    /**
     * Set the value of the specified attribute to the specified string.
     *
     * @param name  name of the attribute to set
     * @param value a <code>String</code> value
     */
    public void setAttribute(final String name, final String value) {
        super.setAttribute(name, SettingsHelper.replace(value, this.settings, this.logger));
    }
}