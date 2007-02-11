/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.precept.acting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.ComponentException;
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
 * @version CVS $Id: AbstractPreceptorAction.java,v 1.2 2003/03/16 17:49:04 vgritsenko Exp $
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


    final protected Instance createInstance(String id) throws ComponentException {
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

