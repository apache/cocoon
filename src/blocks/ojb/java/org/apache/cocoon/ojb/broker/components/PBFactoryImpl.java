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

import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ojb.components.AbstractOjbImpl;
import org.apache.ojb.broker.PBFactoryException;
import org.apache.ojb.broker.PBKey;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerFactory;

/**
* Implementation of the JdoPMF. Create one PMF and store it for future use
 *
 * @author <a href="mailto:antonio@apache.org">Antonio Gallardo</a>
 * @version CVS $Id: PBFactoryImpl.java,v 1.4 2004/06/25 14:49:56 cziegeler Exp $
*/
public class PBFactoryImpl 
extends AbstractOjbImpl 
implements PBFactory, ThreadSafe {
	
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        super.dispose();
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("OJB-PB: Disposed OK!");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
          super.initialize();
          try {
              if (this.getLogger().isDebugEnabled()) {
                  this.getLogger().debug("OJB-PB: Started OK!");
              }
          } catch (Throwable t) {
              if (this.getLogger().isFatalErrorEnabled()) {
                  this.getLogger().fatalError("OJB-PB: Started failed: Cannot create a Persistence Broker Factory.",t);
              }
          }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.broker.components.PBFactory#defaultPersistenceBroker()
     */
    public PersistenceBroker defaultPersistenceBroker() throws PBFactoryException {
        return PersistenceBrokerFactory.defaultPersistenceBroker();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.broker.components.PBFactory#createPersistenceBroker(java.lang.String, java.lang.String, java.lang.String)
     */
    public PersistenceBroker createPersistenceBroker(String jcdAlias, String user, String password) throws PBFactoryException {
        return PersistenceBrokerFactory.createPersistenceBroker(jcdAlias, user, password);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.broker.components.PBFactory#createPersistenceBroker(org.apache.ojb.broker.PBKey)
     */
    public PersistenceBroker createPersistenceBroker(PBKey key) throws PBFactoryException {
        return PersistenceBrokerFactory.createPersistenceBroker(key);
    }
}
