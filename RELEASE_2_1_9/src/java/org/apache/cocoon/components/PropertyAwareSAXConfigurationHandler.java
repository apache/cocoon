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

import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.util.Settings;

/**
 * Property Aware SAX Configuration Handler.
 * This component extends the {@link SAXConfigurationHandler}
 * by creating configurations that can contain placeholders for System Properties.
 *
 * @version SVN $Id: $
 */
public class PropertyAwareSAXConfigurationHandler extends SAXConfigurationHandler {

    private Settings settings;
    private Logger logger;

    /**
     * Constructor
     * @param settings The Settings to use when processing the configuration
     */
    public PropertyAwareSAXConfigurationHandler(Settings settings, Logger logger) {
        super();
        this.settings = settings;
        this.logger = logger;
    }
    /**
     * Create a new <code>PropertyAwareConfiguration</code> with the specified
     * local name and location.
     *
     * @param localName a <code>String</code> value
     * @param location a <code>String</code> value
     * @return a <code>DefaultConfiguration</code> value
     */
    protected DefaultConfiguration createConfiguration( final String localName,
                                                        final String location )
    {
        return new PropertyAwareConfiguration(localName, location, this.settings, this.logger);
    }
}