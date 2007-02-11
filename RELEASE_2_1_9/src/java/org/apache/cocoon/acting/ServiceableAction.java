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

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

/**
 * The <code>ServiceableAction</code> will allow any <code>Action</code>
 * that extends this to access SitemapComponents.
 *
 * @author <a href="mailto:cziegeler@pwr.ch">Carsten Ziegeler</a>
 * @version CVS $Id: ServiceableAction.java,v 1.2 2004/03/05 13:02:43 bdelacretaz Exp $
 */
public abstract class ServiceableAction 
    extends AbstractAction implements Serviceable {

    /** The service manager instance */
    protected ServiceManager manager;

    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }
}
