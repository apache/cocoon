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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.PortalComponentManager;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.layout.LayoutFactory;
import org.apache.cocoon.portal.layout.renderer.Renderer;
import org.apache.cocoon.portal.profile.ProfileManager;

/**
 * Default {@link PortalComponentManager} implementation
 * 
 * @see org.apache.cocoon.portal.PortalComponentManager
 * 
 * TODO Handle non ThreadSafe components
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: DefaultPortalComponentManager.java,v 1.1 2003/07/18 14:41:46 cziegeler Exp $
 */
public class DefaultPortalComponentManager
    extends AbstractLogEnabled
    implements PortalComponentManager, Composable, Disposable, ThreadSafe, Configurable {

    protected ComponentManager manager;

    protected LinkService linkService;

    protected ProfileManager profileManager;

    protected String profileManagerRole;
    
    protected String linkServiceRole;
    
    protected String rendererSelectorRole;
    
    protected ComponentSelector rendererSelector;
    
    protected Map renderers;
    
    protected String copletFactoryRole;
    
    protected String layoutFactoryRole;
    
    protected CopletFactory copletFactory;
    
    protected LayoutFactory layoutFactory;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.component.Composable#compose(org.apache.avalon.framework.component.ComponentManager)
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.PortalComponentManager#getLinkService()
     */
    public LinkService getLinkService() {
        if ( null == this.linkService ) {
            try {
                this.linkService = (LinkService)this.manager.lookup( this.linkServiceRole );
            } catch (ComponentException e) {
                throw new CascadingRuntimeException("Unable to lookup link service with role " + this.linkServiceRole, e);
            }
        }
        return this.linkService;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.PortalComponentManager#getProfileManager()
     */
    public ProfileManager getProfileManager() {
        if ( null == this.profileManager ) {
            try {
                this.profileManager = (ProfileManager)this.manager.lookup( this.profileManagerRole );
            } catch (ComponentException e) {
                throw new CascadingRuntimeException("Unable to lookup profile manager with role " + this.profileManagerRole, e);
            }
        }
        return this.profileManager;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            if ( this.rendererSelector != null) {
                Iterator i = this.renderers.values().iterator();
                while ( i.hasNext() ) {
                    this.rendererSelector.release( (Component) i.next());
                }
                this.manager.release( this.rendererSelector );
                this.rendererSelector = null;
                this.renderers = null;
            }
            this.manager.release( this.profileManager );
            this.manager.release( this.linkService );
            this.manager = null;
            this.profileManager = null;
            this.linkService = null;
            this.manager.release( (Component)this.copletFactory );
            this.manager.release( (Component)this.layoutFactory );
            this.copletFactory = null;
            this.layoutFactory = null;
        }

    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        this.profileManagerRole = config.getChild("profile-manager").getValue(ProfileManager.ROLE);
        this.linkServiceRole = config.getChild("link-service").getValue(LinkService.ROLE);
        this.rendererSelectorRole = config.getChild("renderer-selector").getValue(Renderer.ROLE+"Selector");
        this.copletFactoryRole = config.getChild("coplet-factory").getValue(CopletFactory.ROLE);
        this.layoutFactoryRole = config.getChild("layout-factory").getValue(LayoutFactory.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.PortalComponentManager#getRenderer(java.lang.String)
     */
    public Renderer getRenderer(String hint) {
        if ( rendererSelector == null ) {
            try {
                this.rendererSelector = (ComponentSelector)this.manager.lookup( this.rendererSelectorRole );
            } catch (ComponentException e) {
                throw new CascadingRuntimeException("Unable to lookup renderer selector with role " + this.rendererSelectorRole, e);
            }
            this.renderers = new HashMap();
        }
        Renderer o = (Renderer) this.renderers.get( hint );
        if ( o == null ) {
            try {
                o = (Renderer) this.rendererSelector.select( hint );
                this.renderers.put( hint, o );
            } catch (ComponentException e) {
                throw new CascadingRuntimeException("Unable to lookup renderer with hint " + hint, e);
            }
        }
        return o;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.PortalComponentManager#getCopletFactory()
     */
    public CopletFactory getCopletFactory() {
        if ( null == this.copletFactory ) {
            try {
                this.copletFactory = (CopletFactory)this.manager.lookup( this.copletFactoryRole);
            } catch (ComponentException e) {
                throw new CascadingRuntimeException("Unable to lookup coplet factory with role " + this.copletFactoryRole, e);
            }
        }
        return this.copletFactory;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.PortalComponentManager#getLayoutFactory()
     */
    public LayoutFactory getLayoutFactory() {
        if ( null == this.layoutFactory ) {
            try {
                this.layoutFactory = (LayoutFactory)this.manager.lookup( this.layoutFactoryRole);
            } catch (ComponentException e) {
                throw new CascadingRuntimeException("Unable to lookup layout factory with role " + this.copletFactoryRole, e);
            }
        }
        return this.layoutFactory;
    }

}
