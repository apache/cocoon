/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.portal.aspect.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.aspect.AspectDataStore;
import org.apache.cocoon.portal.aspect.Aspectalizable;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.impl.ChangeAspectDataEvent;
import org.apache.cocoon.portal.event.impl.ChangeCopletInstanceAspectDataEvent;

/**
 * An aspect data store is a component that manages aspect data objects.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: RequestAspectDataStore.java,v 1.4 2003/05/28 13:47:29 cziegeler Exp $
 */
public class RequestAspectDataStore 
    extends AbstractLogEnabled
    implements Component, Composable, ThreadSafe, AspectDataStore, Contextualizable, Parameterizable {
    
    protected Context context;
    
    protected String requestParameterName;
    
    protected ComponentManager manager;
    
    protected Map getMap(Aspectalizable owner) {
        final Request request = ContextHelper.getRequest(this.context);
        Map componentMap = (Map)request.getAttribute(this.getClass().getName());
        if ( componentMap == null) {
            componentMap = new HashMap(3);
            request.setAttribute(this.getClass().getName(), componentMap);
        }
        Map ownerMap = (Map)componentMap.get( owner );
        if ( ownerMap == null ) {
            ownerMap = new HashMap(3);
            componentMap.put( owner, ownerMap );
        }
        return ownerMap;
    }
    
    public Object getAspectData(Aspectalizable owner, String aspectName) {
        return this.getMap(owner).get( aspectName );
    }
    
    public void setAspectData(Aspectalizable owner, String aspectName, Object data) {
        this.getMap(owner).put(aspectName, data);
        // create persistence
        ChangeAspectDataEvent e;
        if ( owner instanceof CopletInstanceData) {
            e = new ChangeCopletInstanceAspectDataEvent((CopletInstanceData)owner, aspectName, data);
        } else {
            e = new ChangeAspectDataEvent( owner, aspectName, data );
        }
        if ( this.requestParameterName != null ) {
            e.setRequestParameterName( this.requestParameterName );
        }
        LinkService service = null;
        try {
            service = (LinkService)this.manager.lookup(LinkService.ROLE);
            service.addEventToLink( e );
        } catch (ComponentException ce) {
            throw new CascadingRuntimeException("Unable to lookup link service.", ce);
        } finally {
            this.manager.release( service );
        }
        
    }

    public boolean isPersistent() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;

    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters pars) throws ParameterException {
        requestParameterName = pars.getParameter("parameter-name", null);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.component.Composable#compose(org.apache.avalon.framework.component.ComponentManager)
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }

}
