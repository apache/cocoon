/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.ojb.components;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;


/**
 * Base class for different DB-OBJ wrapper implementations. This base class is responsible to initialize and dispose of
 * the helper class <code>ConnectionFactoryAvalonDataSource</code> which can be used as a ConnectionFactory class in
 * the OJB Configuration
 *
 * @author giacomo
 * @version $Id: AbstractOjbImpl.java,v 1.2 2004/03/05 13:02:01 bdelacretaz Exp $
 */
public class AbstractOjbImpl
    extends AbstractLogEnabled
    implements Initializable, Disposable, Serviceable {
    /** The <code>ServiceManager</code> instance */
    protected ServiceManager manager;

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize()
        throws Exception {
        ConnectionFactoryAvalonDataSource.initialize(this.manager);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        ConnectionFactoryAvalonDataSource.dispose();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(final ServiceManager manager)
        throws ServiceException {
        this.manager = manager;
    }
}
