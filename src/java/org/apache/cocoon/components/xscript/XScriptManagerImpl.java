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
package org.apache.cocoon.components.xscript;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;

import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.Constants;

import java.util.Map;

/**
 * The actual implementation of the <code>XScriptManager</code> interface.
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @version CVS $Id: XScriptManagerImpl.java,v 1.3 2004/03/05 13:02:54 bdelacretaz Exp $
 * @since August  4, 2001
 */
public class XScriptManagerImpl
        extends AbstractLogEnabled
        implements XScriptManager, Serviceable, Component, Parameterizable, Contextualizable, ThreadSafe
{
    public static final String CONTEXT = "org.apache.cocoon.components.xscript.scope";

    /**
     * The <code>ServiceManager</code> instance.
     */
    protected ServiceManager manager = null;

    /**
     * The <code>Context</code> instance.
     */
    protected Context context = null;


    public void contextualize(org.apache.avalon.framework.context.Context context)
            throws ContextException
    {
        this.context = (Context)context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }

    public void service(ServiceManager manager) throws ServiceException
    {
        this.manager = manager;
    }

    public void register(XScriptObject object) {
        try {
            object.service(manager);
        } catch (ServiceException ignored) { }
    }

    public void parameterize(Parameters params)
            throws ParameterException
    {
        String[] names = params.getNames();

        XScriptVariableScope s = new XScriptVariableScope();
        context.setAttribute(CONTEXT, s);

        for (int i = 0; i < names.length; i++) {
            String resourceString = params.getParameter(names[i]);
            XScriptObject resource = new XScriptObjectFromURL(this, resourceString);
            s.put(names[i], resource);
        }
    }

    private IllegalArgumentException
            createAccessException(String msg, String name, int scope)
    {
        StringBuffer message = new StringBuffer("Cannot ").append(msg)
                .append(" variable named '").append(name)
                .append("' in ");

        if (scope == XScriptManager.GLOBAL_SCOPE)
            message.append("global scope");
        else if (scope == XScriptManager.SESSION_SCOPE)
            message.append("session scope");
        else if (scope == XScriptManager.PAGE_SCOPE)
            message.append("page scope");
        else if (scope == XScriptManager.REQUEST_SCOPE)
            message.append("request scope");
        else if (scope == XScriptManager.ALL_SCOPES)
            message.append("any scope");
        else
            message.append("unknown scope (").append(scope).append(")");

        return new IllegalArgumentException(message.toString());
    }

    public XScriptObject get(XScriptVariableScope pageScope,
                             Map objectModel,
                             String name,
                             int scope)
            throws IllegalArgumentException
    {
        XScriptObject o;
        XScriptVariableScope s = null;

        if (scope == XScriptManager.GLOBAL_SCOPE) {
            s = (XScriptVariableScope) ObjectModelHelper.getContext(objectModel).getAttribute(CONTEXT);
        } else if (scope == XScriptManager.SESSION_SCOPE) {
            Request request = ObjectModelHelper.getRequest(objectModel);
            s = (XScriptVariableScope) request.getSession().getAttribute(CONTEXT);
        } else if (scope == XScriptManager.REQUEST_SCOPE) {
            Request request = ObjectModelHelper.getRequest(objectModel);
            s = (XScriptVariableScope) request.getAttribute(CONTEXT);
        } else if (scope == XScriptManager.PAGE_SCOPE) {
            s = pageScope;
        } else if (scope == XScriptManager.ALL_SCOPES) {
            Request request = ObjectModelHelper.getRequest(objectModel);

            // Lookup in the request scope first.
            s = (XScriptVariableScope) request.getAttribute(CONTEXT);
            if (s != null) {
                o = s.get(name);
                if (o != null) {
                    return o;
                }
            }
            // No luck finding `name' in request scope, try in session scope.
            s = (XScriptVariableScope) request.getSession().getAttribute(CONTEXT);
            if (s != null) {
                o = s.get(name);
                if (o != null) {
                    return o;
                }
            }
            // No luck finding `name' in session scope, try in page scope.
            o = pageScope.get(name);
            if (o != null) {
                return o;
            }
            // No luck finding `name' in the page scope, try the global scope.
            s = (XScriptVariableScope) ObjectModelHelper.getContext(objectModel).getAttribute(CONTEXT);
            if (s != null) {
                o = s.get(name);
                if (o != null) {
                    return o;
                }
            }
            // Not found, throw exception
            s = null;
        }

        if (s != null) {
            o = s.get(name);
            if (o != null) {
                return o;
            }
        }

        throw createAccessException("find", name, scope);
    }

    public XScriptObject getFirst(XScriptVariableScope pageScope,
                                  Map objectModel,
                                  String name)
            throws IllegalArgumentException
    {
        return get(pageScope, objectModel, name, ALL_SCOPES);
    }

    public void put(XScriptVariableScope pageScope,
                    Map objectModel,
                    String name,
                    XScriptObject value,
                    int scope)
    {
        XScriptVariableScope s;

        if (scope == XScriptManager.GLOBAL_SCOPE) {
            Context context = ObjectModelHelper.getContext(objectModel);
            synchronized (context) {
                s = (XScriptVariableScope) context.getAttribute(CONTEXT);
                if (s == null) {
                    context.setAttribute(CONTEXT, s = new XScriptVariableScope());
                }
            }
        } else if (scope == XScriptManager.SESSION_SCOPE) {
            Session session = ObjectModelHelper.getRequest(objectModel).getSession();
            synchronized (session) {
                s = (XScriptVariableScope) session.getAttribute(CONTEXT);
                if (s == null) {
                    session.setAttribute(CONTEXT, s = new XScriptVariableScope());
                }
            }
        } else if (scope == XScriptManager.REQUEST_SCOPE) {
            Request request = ObjectModelHelper.getRequest(objectModel);
            synchronized (request) {
                s = (XScriptVariableScope) request.getAttribute(CONTEXT);
                if (s == null) {
                    request.setAttribute(CONTEXT, s = new XScriptVariableScope());
                }
            }
        } else if (scope == XScriptManager.PAGE_SCOPE) {
            s = pageScope;
        } else {
            throw createAccessException("create", name, scope);
        }

        s.put(name, value);
    }

    public XScriptObject remove(XScriptVariableScope pageScope,
                                Map objectModel,
                                String name,
                                int scope)
            throws IllegalArgumentException
    {
        XScriptObject o;
        XScriptVariableScope s = null;

        if (scope == XScriptManager.GLOBAL_SCOPE) {
            s = (XScriptVariableScope) ObjectModelHelper.getContext(objectModel).getAttribute(CONTEXT);
        } else if (scope == XScriptManager.SESSION_SCOPE) {
            Request request = ObjectModelHelper.getRequest(objectModel);
            s = (XScriptVariableScope) request.getSession().getAttribute(CONTEXT);
        } else if (scope == XScriptManager.REQUEST_SCOPE) {
            Request request = ObjectModelHelper.getRequest(objectModel);
            s = (XScriptVariableScope) request.getAttribute(CONTEXT);
        } else if (scope == XScriptManager.PAGE_SCOPE) {
            s = pageScope;
        } else if (scope == XScriptManager.ALL_SCOPES) {
            Request request = ObjectModelHelper.getRequest(objectModel);

            // Lookup in the request scope first.
            s = (XScriptVariableScope) request.getAttribute(CONTEXT);
            if (s != null) {
                o = s.remove(name);
                if (o != null) {
                    return o;
                }
            }
            // No luck finding `name' in request scope, try in session scope.
            s = (XScriptVariableScope) request.getSession().getAttribute(CONTEXT);
            if (s != null) {
                o = s.remove(name);
                if (o != null) {
                    return o;
                }
            }
            // No luck finding `name' in session scope, try in page scope.
            o = pageScope.remove(name);
            if (o != null) {
                return o;
            }
            // No luck finding `name' in the page scope, try the global scope.
            s = (XScriptVariableScope) ObjectModelHelper.getContext(objectModel).getAttribute(CONTEXT);
            if (s != null) {
                o = s.remove(name);
                if (o != null) {
                    return o;
                }
            }
            // Not found, throw exception
            s = null;
        }

        if (s != null) {
            o = s.remove(name);
            if (o != null) {
                return o;
            }
        }

        throw createAccessException("remove", name, scope);
    }

    public XScriptObject removeFirst(XScriptVariableScope pageScope, Map objectModel,
                                     String name)
            throws IllegalArgumentException
    {
        return remove(pageScope, objectModel, name, ALL_SCOPES);
    }
}
