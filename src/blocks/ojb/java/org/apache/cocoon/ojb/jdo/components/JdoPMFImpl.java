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
package org.apache.cocoon.ojb.jdo.components;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ojb.components.AbstractOjbImpl;
import org.apache.ojb.jdori.sql.OjbStorePMF;

/**
* Implementation of the JdoPMF. Create one PMF and store it for future use
 *
 * @author <a href="mailto:antonio@apache.org">Antonio Gallardo</a>
 * @version CVS $Id: JdoPMFImpl.java,v 1.6 2004/06/25 14:49:56 cziegeler Exp $
*/
public class JdoPMFImpl 
    extends AbstractOjbImpl 
    implements JdoPMF, ThreadSafe {
    
	protected PersistenceManagerFactory factory;
	
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        super.dispose();
		if (this.getLogger().isDebugEnabled()) {
			this.getLogger().debug("OJB-JDO: Disposed OK!");
        }
    }

	/* (non-Javadoc)
	 * @see org.apache.cocoon.ojb.jdori.components.JdoPMF#getPersistenceManager()
	 */
	public PersistenceManager getPersistenceManager() {
		return factory.getPersistenceManager();
	}
    
	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.activity.Initializable#initialize()
	 */
	public void initialize() throws Exception {
        super.initialize();
		try	{
			// Create the factory
			factory = new OjbStorePMF();
			if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("OJB-JDO: Started OK!");
            }
		} catch (Throwable t){
			if (this.getLogger().isFatalErrorEnabled()) {
				this.getLogger().fatalError("OJB-JDO: Started failed: Cannot create a Persistence Manager Factory.",t);
			}
		}
	}
}
