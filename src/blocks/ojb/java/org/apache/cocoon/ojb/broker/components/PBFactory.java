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
package org.apache.cocoon.ojb.broker.components;

import org.apache.avalon.framework.component.Component;
import org.apache.ojb.broker.PBFactoryException;
import org.apache.ojb.broker.PBKey;
import org.apache.ojb.broker.PersistenceBroker;

/**
 * Interface of the Persistence Broker Factory.
 * It is used to get the Persistence Manager to interact with Persistence Broker using OJB.
 * The PersistenceBroker API provides the lowest level access to OJB's persistence engine.
 *
 * @author <a href="mailto:antonio@apache.org">Antonio Gallardo</a>
 * @version CVS $Id: PBFactory.java,v 1.2 2004/03/05 13:02:01 bdelacretaz Exp $
*/
public interface PBFactory extends Component
{
    /**
     * The <code>ROLE</code>
     */
    String ROLE = PBFactory.class.getName();
	
    /**
     * Get the default Persistence BrokerManager.
     * @return a PersistenceBroker Object
     * @throws PBFactoryException - If the operation failed.
     */
    public PersistenceBroker defaultPersistenceBroker()
        throws PBFactoryException;
    
    /**
     * Create a new PersistenceBroker with the given parameters.
     * Using this method we can access diferents datasources.
     * 
     * @param jcdAlias - name of the jdbc connection descriptor to be used.
     * @param user - Datasource user's name
     * @param password - Datasource user's password
     * @return a PersistenceBroker Object
     * @throws PBFactoryException - If the operation failed.
     */
    public PersistenceBroker createPersistenceBroker(String jcdAlias,
            String user, String password) throws PBFactoryException;
    
    /**
     * Create a new PersistenceBroker with the given parameters.
     * @param key - A immutable key that identify the PB instance in pools
     * @return a PersistenceBroker Object
     * @throws PBFactoryException - If the operation failed.
     */
    public PersistenceBroker createPersistenceBroker(PBKey key)
        throws PBFactoryException;
}
