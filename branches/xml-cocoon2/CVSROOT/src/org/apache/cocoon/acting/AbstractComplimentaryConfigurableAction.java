/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.acting;

import java.util.Map;
import java.util.HashMap;
import java.net.URL;

import org.apache.avalon.Component;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.ConfigurationBuilder;
import org.apache.avalon.configuration.ConfigurationException;
import org.apache.avalon.configuration.SAXConfigurationHandler;

import org.apache.cocoon.components.url.URLFactory;
import org.apache.cocoon.Roles;
import org.apache.cocoon.components.parser.Parser;

import org.xml.sax.InputSource;

/**
 * Set up environment for configurable form handling data.  This group
 * of actions are unique in that they employ a terciary mapping.
 *
 * Each configuration file must use the same format in order to be
 * effective.  The name of the root configuration element is irrelevant.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-03-12 04:38:31 $
 */
public abstract class AbstractComplimentaryConfigurableAction extends ComposerAction {
    private static Map configurations = new HashMap();

    /**
     * Set up the complementary configuration file.  Please note that
     * multiple Actions can share the same configurations.  By using
     * this approach, we can limit the number of config files.
     * Also note that the configuration file does not have to be a file.
     */
    protected Configuration getConfiguration(String descriptor) throws ConfigurationException {
        Configuration conf = null;

        if (descriptor == null) {
            throw new ConfigurationException("The form descriptor is not set!");
        }

        synchronized (AbstractComplimentaryConfigurableAction.configurations) {
            conf = (Configuration) AbstractComplimentaryConfigurableAction.configurations.get(descriptor);

            if (conf == null) {
                URLFactory urlFactory = null;
                Parser parser = null;
                URL resource = null;

                try {
                    urlFactory = (URLFactory) this.manager.lookup(Roles.URL_FACTORY);
                    resource = urlFactory.getURL(descriptor);

                    parser = (Parser)this.manager.lookup(Roles.PARSER);
                    SAXConfigurationHandler builder = new SAXConfigurationHandler();
                    InputSource inputStream = new InputSource(resource.openStream());

                    parser.setContentHandler(builder);
                    inputStream.setSystemId(resource.toExternalForm());
                    parser.parse(inputStream);

                    conf = builder.getConfiguration();
                } catch (Exception e) {
                    getLogger().error("Could not configure Database mapping environment", e);
                    throw new ConfigurationException("Error trying to load configurations for resource: " + resource.toExternalForm());
                } finally {
                    if (urlFactory != null) this.manager.release((Component) urlFactory);
                    if (parser != null) this.manager.release((Component) parser);
                }

                this.cacheConfiguration(descriptor, conf);
            }
        }

        return conf;
    }

    /**
     * Cache the configuration so that we can use it later.
     */
    private void cacheConfiguration(String descriptor, Configuration conf) {
        synchronized (AbstractComplimentaryConfigurableAction.configurations) {
            AbstractComplimentaryConfigurableAction.configurations.put(descriptor, conf);
        }
    }
}
