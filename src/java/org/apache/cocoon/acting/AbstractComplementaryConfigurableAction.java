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
package org.apache.cocoon.acting;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;

/**
 * Set up environment for configurable form handling data.  This group
 * of actions are unique in that they employ a terciary mapping.
 *
 * Each configuration file must use the same format in order to be
 * effective.  The name of the root configuration element is irrelevant.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Id: AbstractComplementaryConfigurableAction.java,v 1.6 2004/04/01 12:29:47 cziegeler Exp $
 */
public abstract class AbstractComplementaryConfigurableAction extends ConfigurableServiceableAction {
    private static Map configurations = new HashMap();

    /**
     * Set up the complementary configuration file.  Please note that
     * multiple Actions can share the same configurations.  By using
     * this approach, we can limit the number of config files.
     * Also note that the configuration file does not have to be a file.
     *
     * Defaults to reload configuration file it has changed.
     */
    protected Configuration getConfiguration(String descriptor) throws ConfigurationException {
        boolean reloadable = Constants.DESCRIPTOR_RELOADABLE_DEFAULT;
        if (this.settings.containsKey("reloadable"))
            reloadable = Boolean.valueOf((String) this.settings.get("reloadable")).booleanValue();
        return this.getConfiguration(descriptor, null, reloadable);
    }

    /**
     * Set up the complementary configuration file.  Please note that
     * multiple Actions can share the same configurations.  By using
     * this approach, we can limit the number of config files.
     * Also note that the configuration file does not have to be a file.
     */
    protected Configuration getConfiguration(String descriptor, SourceResolver resolver, boolean reloadable) throws ConfigurationException {
        ConfigurationHelper conf = null;

        if (descriptor == null) {
            throw new ConfigurationException("The form descriptor is not set!");
        }

        synchronized (AbstractComplementaryConfigurableAction.configurations) {
            Source resource = null;
            try {
                resource = resolver.resolveURI(descriptor);
                conf = (ConfigurationHelper) AbstractComplementaryConfigurableAction.configurations.get(resource.getURI());
                if (conf == null || (reloadable && conf.lastModified != resource.getLastModified())) {
                    getLogger().debug("(Re)Loading " + descriptor);

                    if (conf == null) {
                        conf = new ConfigurationHelper();
                    }

                    SAXConfigurationHandler builder = new SAXConfigurationHandler();
                    SourceUtil.parse(this.manager, resource, builder);

                    conf.lastModified = resource.getLastModified();
                    conf.configuration = builder.getConfiguration();

                    AbstractComplementaryConfigurableAction.configurations.put(resource.getURI(), conf);
                } else {
                    getLogger().debug("Using cached configuration for " + descriptor);
                }
            } catch (Exception e) {
                getLogger().error("Could not configure Database mapping environment", e);
                throw new ConfigurationException("Error trying to load configurations for resource: "
                    + (resource == null ? "null" : resource.getURI()));
            } finally {
                resolver.release(resource);
            }
        }

        return conf.configuration;
    }
}
