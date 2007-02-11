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
package org.apache.cocoon.portal.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.RequestLifecycleComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.PortalService;
import org.xml.sax.SAXException;

/**
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: PortalServiceImpl.java,v 1.4 2003/06/17 19:59:31 cziegeler Exp $
 */
public class PortalServiceImpl
    extends AbstractLogEnabled
    implements Composable,
                RequestLifecycleComponent, 
                PortalService, 
                Recyclable {

    protected Map objectModel;
    protected Map temporaryAttributes = new HashMap();
    protected LinkService linkService;
    
    protected ComponentManager manager;

    protected String portalName;

    public void compose(ComponentManager componentManager) throws ComponentException {
        this.manager = componentManager;
    }


    public void setup(SourceResolver resolver, Map objectModel) throws ProcessingException, SAXException, IOException {
        this.objectModel = objectModel;

		Map context = (Map)objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
		if (context != null) {
			String portalName = (String)context.get(Constants.PORTAL_NAME_KEY);
			if (portalName != null)
				this.setPortalName(portalName);
		}
    }

    public String getPortalName() {
        return this.portalName;
    }

    public void setPortalName(String value) {
        this.portalName = value;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        this.portalName = null;
        this.temporaryAttributes.clear();
        this.manager.release( this.linkService );
        this.linkService = null;
    }

    public Object getAttribute(String key) {
        Session session = ObjectModelHelper.getRequest(this.objectModel).getSession(false);
        if (session == null) {
            return null;
        }
        Map map = (Map) session.getAttribute(this.portalName);
        if (null != map) {
            return map.get(key);
        }
        return null;
    }

    public void setAttribute(String key, Object value) {
        Session session = ObjectModelHelper.getRequest(this.objectModel).getSession();
        Map map = (Map) session.getAttribute(this.portalName);
        if (null == map) {
            map = new HashMap();
            session.setAttribute(this.portalName, map);
        }
        map.put(key, value);
    }

    public void removeAttribute(String key) {
        Session session = ObjectModelHelper.getRequest(this.objectModel).getSession();
        Map map = (Map) session.getAttribute(this.portalName);
        if (null != map) {
            map.remove(key);
        }
    }

    public Iterator getAttributeNames() {
        Session session = ObjectModelHelper.getRequest(this.objectModel).getSession();
        Map map = (Map) session.getAttribute(this.portalName);
        if (null != map) {
            return map.keySet().iterator();
        }
        return Collections.EMPTY_MAP.keySet().iterator();
    }

    public Object getTemporaryAttribute(String key) {
        return this.temporaryAttributes.get( key );
    }
    
    public void setTemporaryAttribute(String key, Object value) {
        this.temporaryAttributes.put( key, value );
    }
    
    public void removeTemporaryAttribute(String key) {
        this.temporaryAttributes.remove( key );
    }
    
    public Iterator getTemporaryAttributeNames() {
        return this.temporaryAttributes.keySet().iterator();
    }

    /**
     * Get the link service
     */
    public LinkService getLinkService() {
        if ( null == this.linkService ) {
            try {
                this.linkService = (LinkService)this.manager.lookup( LinkService.ROLE);
            } catch (ComponentException e) {
                throw new CascadingRuntimeException("Unable to lookup link service.", e);
            }
        }
        return this.linkService;
    }

}
