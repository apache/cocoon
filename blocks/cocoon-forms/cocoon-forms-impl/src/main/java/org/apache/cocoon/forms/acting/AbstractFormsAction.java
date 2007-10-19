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
package org.apache.cocoon.forms.acting;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.acting.Action;
import org.apache.cocoon.forms.FormManager;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * Abstract base class for Cocoon Forms actions.
 * 
 * @version $Id$
 */
public abstract class AbstractFormsAction extends AbstractLogEnabled
                                          implements Action, ThreadSafe, Serviceable,
                                                     Disposable {
      
    protected ServiceManager manager;
    
    protected FormManager formManager;


    public void service(ServiceManager serviceManager) throws ServiceException {
        this.manager = serviceManager;
        this.formManager = (FormManager) serviceManager.lookup(FormManager.ROLE);
    }

    public void dispose() {
        if (this.manager != null) {
            this.manager.release(this.formManager);
            this.manager = null;
            this.formManager = null;
        }
    }
}
