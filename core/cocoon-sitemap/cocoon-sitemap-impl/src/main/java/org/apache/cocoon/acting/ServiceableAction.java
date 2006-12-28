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
package org.apache.cocoon.acting;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

/**
 * The <code>ServiceableAction</code> will allow any <code>Action</code>
 * that extends this to access SitemapComponents.
 *
 * @version $Id$
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
