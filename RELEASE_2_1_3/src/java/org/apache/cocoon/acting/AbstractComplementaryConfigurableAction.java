/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.acting;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;

import java.util.HashMap;
import java.util.Map;

/**
 * Set up environment for configurable form handling data.  This group
 * of actions are unique in that they employ a terciary mapping.
 *
 * Each configuration file must use the same format in order to be
 * effective.  The name of the root configuration element is irrelevant.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Id: AbstractComplementaryConfigurableAction.java,v 1.3 2003/10/25 18:06:19 joerg Exp $
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
     * @deprecated please use the getConfiguration(String, SourceResolver, boolean)
     *             version of this method instead.
     */
    protected Configuration getConfiguration(String descriptor, boolean reloadable) throws ConfigurationException {
        return this.getConfiguration( descriptor, null, reloadable );
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
