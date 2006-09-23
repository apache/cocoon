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
package org.apache.cocoon.webapps.session.selection;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.webapps.session.MediaManager;

/**
 *  This selector uses the media management.
 *
 * @deprecated This block is deprecated and will be removed in future versions.
 * @version $Id$
 */
public final class MediaSelector
implements Serviceable, Selector, ThreadSafe {

    private ServiceManager manager;

    /**
     * Selector
     */
    public boolean select (String expression, Map objectModel, Parameters parameters) {
        MediaManager mediaManager = null;
        boolean result;
        try {
            mediaManager = (MediaManager) this.manager.lookup( MediaManager.ROLE );
            result = mediaManager.testMedia(expression);
        } catch (Exception local) {
            // ignore me
            result = false;
        } finally {
            this.manager.release(mediaManager );
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

}


