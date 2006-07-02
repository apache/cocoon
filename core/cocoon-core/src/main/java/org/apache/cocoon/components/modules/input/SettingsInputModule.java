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
package org.apache.cocoon.components.modules.input;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.commons.collections.IteratorUtils;

/**
 * This module allows access to properties defined in the settings.
 *
 * @version $Id$
 */
public final class SettingsInputModule
    extends AbstractLogEnabled
    implements InputModule, ThreadSafe {

    /**
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttribute(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    /**
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttribute(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Object getAttribute(String name, Configuration modeConf, Map objectModel)
    throws ConfigurationException {
        final Settings settings = (Settings)EnvironmentHelper.getCurrentProcessor().getBeanFactory().getBean(Settings.ROLE);
        if ( settings != null ) {
            return settings.getProperty(name);
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttributeNames(org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Iterator getAttributeNames(Configuration modeConf, Map objectModel)
    throws ConfigurationException {
        return IteratorUtils.EMPTY_ITERATOR;
    }

    /**
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttributeValues(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Object[] getAttributeValues(String name, Configuration modeConf, Map objectModel)
    throws ConfigurationException {
        Object o = this.getAttribute(name, modeConf, objectModel);
        if (o != null) {
            return new Object[] {o};
        }
        return null;
    }
}
