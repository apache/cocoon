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
package org.apache.cocoon.webapps.session.context;

import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.webapps.session.SessionConstants;

/**
 *  Context provider for the temporarily context, the request and the
 *  response context.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: StandardSessionContextProvider.java,v 1.4 2003/07/04 10:44:17 cziegeler Exp $
*/
public final class StandardSessionContextProvider
extends AbstractLogEnabled
implements SessionContextProvider, ThreadSafe, Contextualizable, Composable, Component {

    protected Context context;
    
    protected ComponentManager manager;
    
    /**
     * Get the context
     * @param name The name of the context
     * @return The context
     * @throws ProcessingException If the context is not available.
     */
    public SessionContext getSessionContext(String name)
    throws ProcessingException {
        final Map objectModel = ContextHelper.getObjectModel( this.context );
        
        // get the context from the object model
        SessionContext context = this.getContext( objectModel, name );
        if ( context == null ) {
            if ( name.equals(SessionConstants.TEMPORARY_CONTEXT) ) {
                context = new SimpleSessionContext();
                context.setup(name, null, null);
            } else if ( name.equals(SessionConstants.REQUEST_CONTEXT) ) {
                context = new RequestSessionContext();
                context.setup(name, null, null);
                ((RequestSessionContext)context).setup( objectModel, this.manager );
            }
            objectModel.put(this.getClass().getName()+name, context);
        }
        return context;
    }

    /**
     * Does the context exist?
     */
    public boolean existsSessionContext(String name)
    throws ProcessingException {
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        return (this.getContext( objectModel, name) != null);
    }

    private SessionContext getContext(Map objectModel, String name) {
        SessionContext context = (SessionContext) objectModel.get(this.getClass().getName()+name);
        if ( context != null && !name.equals(SessionConstants.TEMPORARY_CONTEXT)) {
            if ( name.equals(SessionConstants.REQUEST_CONTEXT)) {
                RequestSessionContext r = (RequestSessionContext)context;
                if (!(r.getRequest() == ObjectModelHelper.getRequest( objectModel))) {
                    context = null;
                    objectModel.remove(this.getClass().getName()+name);
                }
            } else {
                ResponseSessionContext r = (ResponseSessionContext)context;
                if (!(r.getResponse() == ObjectModelHelper.getResponse( objectModel))) {
                    context = null;
                    objectModel.remove(this.getClass().getName()+name);
                }
            }
        }
        return context; 
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.component.Composable#compose(org.apache.avalon.framework.component.ComponentManager)
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }

}
