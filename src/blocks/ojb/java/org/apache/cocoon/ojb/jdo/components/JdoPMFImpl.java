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

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.ojb.jdori.sql.OjbStorePMF;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

/**
 * Implementation of the JdoPMF. Create one PMF and store it for future use
 *
 * @author <a href="mailto:antonio@apache.org">Antonio Gallardo</a>
 * @version CVS $Id$
*/
public class JdoPMFImpl extends AbstractLogEnabled
                        implements JdoPMF, ThreadSafe, Initializable {

    protected PersistenceManagerFactory factory;

    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.jdori.components.JdoPMF#getPersistenceManager()
     */
    public PersistenceManager getPersistenceManager() {
        return this.factory.getPersistenceManager();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        this.factory = new OjbStorePMF();
    }
}
