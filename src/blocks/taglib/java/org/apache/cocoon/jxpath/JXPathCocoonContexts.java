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

package org.apache.cocoon.jxpath;

import java.util.Map;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.http.HttpContext;
import org.apache.cocoon.environment.http.HttpRequest;
import org.apache.cocoon.environment.http.HttpSession;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.servlet.Constants;
import org.apache.commons.jxpath.servlet.KeywordVariables;

/**
 * Component that allocate and cache JXPathContexts bound to VariableContext,
 * Cocoon Request, Cocoon Session and Cocoon Context.
 * <p>
 * If you need to limit the attibute lookup to just one scope, you can use the
 * pre-definded variables "request", "session" and "application".
 * For example, the expression "$session/foo" extracts the value of the
 * session attribute named "foo".
 * <p>
 * Following are some implementation details.
 * There is a separate JXPathContext for each of the four scopes. These contexts are chained
 * according to the nesting of the scopes.  So, the parent of the "variable"
 * JXPathContext is a "request" JXPathContext, whose parent is a "session"
 * JXPathContext (that is if there is a session), whose parent is an "application"
 * context.
 * <p>
 * Since JXPath chains lookups for variables and extension functions, variables
 * and extension function declared in the outer scopes are also available in
 * the inner scopes.
 * <p>
 * The "session" variable will be undefined if there is no session for this servlet.
 * JXPath does not automatically create sessions.
 *
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id: JXPathCocoonContexts.java,v 1.4 2004/02/05 10:47:24 cziegeler Exp $
 */
public final class JXPathCocoonContexts implements Component, Contextualizable, Recyclable {
    
    public static final String ROLE = JXPathCocoonContexts.class.getName();
    private static JXPathContextFactory factory;
    private JXPathContext variableContext;
    private Context context;
    
    static {
        factory = JXPathContextFactory.newInstance();
        JXPathIntrospector.registerDynamicClass(HttpRequest.class, CocoonRequestHandler.class);
        JXPathIntrospector.registerDynamicClass(HttpSession.class, CocoonSessionHandler.class);
        JXPathIntrospector.registerDynamicClass(HttpContext.class, CocoonContextHandler.class);
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context)
    throws ContextException {
        this.context = context;
    }

    public final JXPathContext getVariableContext() {
        if (variableContext == null) {
            JXPathContext parentContext = getRequestContext();
            variableContext = factory.newContext(parentContext, null);
        }
        return variableContext;
    }

    /**
     * Returns a JXPathContext bound to the "request" scope. Caches that context
     * within the request itself.
     */
    public final JXPathContext getRequestContext() {
        Map objectModel = ContextHelper.getObjectModel(this.context);
        Request request = ObjectModelHelper.getRequest(objectModel);
        JXPathContext context = (JXPathContext) request.getAttribute(Constants.JXPATH_CONTEXT);
        if (context == null) {
            org.apache.cocoon.environment.Context cocoonContext = ObjectModelHelper.getContext(objectModel);
            JXPathContext parentContext = null;
            Session session = request.getSession(false);
            if (session != null) {
                parentContext = getSessionContext(session, cocoonContext);
            } else {
                parentContext = getApplicationContext(cocoonContext);
            }
            if (request.getClass() != HttpRequest.class)
                JXPathIntrospector.registerDynamicClass(request.getClass(), CocoonRequestHandler.class);
            context = factory.newContext(parentContext, request);
            context.setVariables(new KeywordVariables(Constants.REQUEST_SCOPE, request));
            request.setAttribute(Constants.JXPATH_CONTEXT, context);
        }
        return context;
    }

    /**
     * Returns a JXPathContext bound to the "session" scope. Caches that context
     * within the session itself.
     */
    public final JXPathContext getSessionContext(Session session, org.apache.cocoon.environment.Context cocoonContext) {
        JXPathContext context = (JXPathContext) session.getAttribute(Constants.JXPATH_CONTEXT);
        if (context == null) {
            if (session.getClass() != HttpSession.class)
                JXPathIntrospector.registerDynamicClass(session.getClass(), CocoonSessionHandler.class);
            JXPathContext parentContext = getApplicationContext(cocoonContext);
            context = factory.newContext(parentContext, session);
            context.setVariables(new KeywordVariables(Constants.SESSION_SCOPE, session));
            session.setAttribute(Constants.JXPATH_CONTEXT, context);
        }
        return context;
    }

    /**
     * Returns a JXPathContext bound to the "application" scope. Caches that context
     * within the servlet context itself.
     */
    public final JXPathContext getApplicationContext(org.apache.cocoon.environment.Context cocoonContext) {
        JXPathContext context = (JXPathContext) cocoonContext.getAttribute(Constants.JXPATH_CONTEXT);
        if (context == null) {
            if (cocoonContext.getClass() != HttpContext.class)
                JXPathIntrospector.registerDynamicClass(cocoonContext.getClass(), CocoonContextHandler.class);
            context = factory.newContext(null, cocoonContext);
            context.setVariables(new KeywordVariables(Constants.APPLICATION_SCOPE, cocoonContext));
            cocoonContext.setAttribute(Constants.JXPATH_CONTEXT, context);
        }
        return context;
    }
    
    /*
     * @see Recyclable#recycle()
     */
    public void recycle() {
        this.variableContext = null;
    }
}
