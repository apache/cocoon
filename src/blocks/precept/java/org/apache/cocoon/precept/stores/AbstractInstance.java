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
package org.apache.cocoon.precept.stores;


import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.precept.Instance;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Mar 18, 2002
 * @version CVS $Id: AbstractInstance.java,v 1.4 2004/03/05 13:02:19 bdelacretaz Exp $
 */
public abstract class AbstractInstance extends AbstractLogEnabled
        implements Instance, Serviceable, Disposable, HttpSessionBindingListener {

    protected ServiceManager manager;

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }


    public void valueBound(HttpSessionBindingEvent event) {
    }


    public void valueUnbound(HttpSessionBindingEvent event) {
        getLogger().debug("releasing instance in session");
        manager.release(this);
    }

    public void dispose() {
    }
}

