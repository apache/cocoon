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
package org.apache.cocoon.precept.acting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.precept.Instance;
import org.apache.cocoon.precept.InstanceFactory;
import org.apache.cocoon.precept.InvalidXPathSyntaxException;
import org.apache.cocoon.precept.NoSuchNodeException;
import org.apache.cocoon.precept.Preceptor;
import org.apache.cocoon.precept.PreceptorViolationException;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Feb 25, 2002
 * @version CVS $Id: AbstractPreceptorAction.java,v 1.4 2004/03/05 13:02:19 bdelacretaz Exp $
 */
public abstract class AbstractPreceptorAction extends AbstractMethodAction implements ThreadSafe {
    public final static String PRECEPTORVIOLATIONS = "preceptorViolations";

    final protected Session createSession(Map objectModel) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        return (request.getSession(true));
    }


    final protected Instance getInstance(Map objectModel, String instanceId) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        Session session = request.getSession(false);
        return ((Instance) session.getAttribute(instanceId));
    }


    final protected Instance createInstance(String id) throws ServiceException {
        InstanceFactory factory = (InstanceFactory) manager.lookup(InstanceFactory.ROLE);
        Instance instance = factory.createInstance(id);
        manager.release(factory);
        return (instance);
    }


    final protected void populate(Map objectModel, String instanceId, String xpath) throws PreceptorViolationException, InvalidXPathSyntaxException {
        Request request = ObjectModelHelper.getRequest(objectModel);
        Session session = request.getSession(false);
        if (session != null) {
            Instance instance = (Instance) session.getAttribute(instanceId);
            if (instance != null) {
                String value = request.getParameter(xpath);
                //String[] values = request.getParameterValues(xpath);

                if (value == null) value = "false";

                getLogger().debug("populating into " + String.valueOf(xpath) + " = " + String.valueOf(value));

                instance.setValue(xpath, value);
            }
        }
    }

    final protected void populate(Map objectModel, String instanceId, String[] xpaths) throws PreceptorViolationException, InvalidXPathSyntaxException {
        for (int i = 0; i < xpaths.length; i++) {
            populate(objectModel, instanceId, xpaths[i]);
        }
    }


    final protected Collection validate(Map objectModel, String instanceId) throws InvalidXPathSyntaxException, NoSuchNodeException {
        Instance instance = getInstance(objectModel, instanceId);
        Preceptor preceptor = instance.getPreceptor();
        Collection violations = preceptor.validate(instance, null);
        return (violations);
    }


    final protected Collection validate(Map objectModel, String instanceId, String xpath) throws InvalidXPathSyntaxException, NoSuchNodeException {
        Instance instance = getInstance(objectModel, instanceId);
        Preceptor preceptor = instance.getPreceptor();
        Collection violations = preceptor.validate(instance, xpath, null);
        return (violations);
    }


    final protected void pass(Map objectModel, Collection violations) {
        if (violations != null) {
            Request request = ObjectModelHelper.getRequest(objectModel);
            List currentViolations = (List) request.getAttribute(PRECEPTORVIOLATIONS);
            if (currentViolations == null) {
                request.setAttribute(PRECEPTORVIOLATIONS, violations);
            }
            else {
            }
        }
    }


    final protected Collection validate(Map objectModel, String instanceId, String[] xpaths) throws InvalidXPathSyntaxException, NoSuchNodeException {
        Instance instance = getInstance(objectModel, instanceId);
        Preceptor preceptor = instance.getPreceptor();
        ArrayList allErrors = null;
        for (int i = 0; i < xpaths.length; i++) {
            Collection errors = preceptor.validate(instance, xpaths[i], null);
            if (errors != null) {
                if (allErrors == null) allErrors = new ArrayList(1);
                allErrors.addAll(errors);
            }
        }
        return (allErrors);
    }


    final protected Map page(String id) {
        Map m = new HashMap(1);
        m.put("page", id);
        return (m);
    }
}

