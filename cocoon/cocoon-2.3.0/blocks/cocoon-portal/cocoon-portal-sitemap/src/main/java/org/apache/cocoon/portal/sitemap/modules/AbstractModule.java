/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.sitemap.modules;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * @version $Id$
 */
public abstract class AbstractModule
    extends AbstractLogEnabled
    implements InputModule, Serviceable, ThreadSafe, Disposable {

    /** The service manager. */
    protected ServiceManager manager;

    /** The portal service. */
    protected PortalService portalService;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager aManager) throws ServiceException {
        this.manager = aManager;
        this.portalService = (PortalService)this.manager.lookup(PortalService.class.getName());
    }

	/**
	 * @see org.apache.avalon.framework.activity.Disposable#dispose()
	 */
	public void dispose() {
		if ( this.manager != null ) {
			this.manager.release(this.portalService);
			this.portalService = null;
			this.manager = null;
		}
	}

	/**
	 * @see org.apache.cocoon.components.modules.input.InputModule#getAttributeNames(org.apache.avalon.framework.configuration.Configuration, java.util.Map)
	 */
	public Iterator getAttributeNames(Configuration modeConf, Map objectModel) {
        return Collections.EMPTY_LIST.iterator();
	}

	/**
	 * @see org.apache.cocoon.components.modules.input.InputModule#getAttributeValues(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
	 */
	public Object[] getAttributeValues(String name, Configuration modeConf, Map objectModel)
    throws ConfigurationException {
        Object[] result = new Object[1];
        result[0] = this.getAttribute(name, modeConf, objectModel);
        return result;
	}
}
