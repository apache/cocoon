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

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.store.Store;

import java.util.Map;

/**
 * Simple action which ensures the persistent store is cleared.
 *
 * @cocoon.sitemap.component.documentation
 * Simple action which ensures the persistent store is cleared.
 *
 * @version $Id$
 */
public class ClearPersistentStoreAction extends ServiceableAction implements ThreadSafe {

    public Map act(Redirector redirector,
                    SourceResolver resolver,
                    Map objectModel,
                    String src,
                    Parameters par
    ) throws Exception {
        if ( this.manager.hasService( Store.PERSISTENT_STORE) ) {
            final Store store_persistent = (Store)this.manager.lookup(Store.ROLE);

            try {
                store_persistent.clear();
                return EMPTY_MAP;
            } catch (Exception ex) {
                getLogger().debug("Exception while trying to Clearing the Store", ex);
                return null;
            } finally {
                this.manager.release( store_persistent );
            }
        } else {
            getLogger().info("Unable to clear persistent store as no persistent store is configured.");
            return null;
        }
    }
}
