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
package org.apache.cocoon.portal.profile.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutFactory;

/**
 * The profile manager using the authentication framework
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id: AbstractUserProfileManager.java,v 1.6 2004/02/23 14:52:50 cziegeler Exp $
 */
public abstract class AbstractUserProfileManager 
    extends AbstractProfileManager { 

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#login()
     */
    public void login() {
        super.login();
        // TODO - we should move most of the stuff from getPortalLayout to here
        // for now we use a hack :)
        this.getPortalLayout(null, null);
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#logout()
     */
    public void logout() {
        final String layoutKey = this.getDefaultLayoutKey();
        PortalService service = null;
        ServiceSelector adapterSelector = null;
        try {
            adapterSelector = (ServiceSelector)this.manager.lookup(CopletAdapter.ROLE+"Selector");
            service = (PortalService)this.manager.lookup(PortalService.ROLE);

            CopletInstanceDataManager copletInstanceDataManager = (CopletInstanceDataManager)service.getAttribute("CopletInstanceData:"+layoutKey);
            if ( copletInstanceDataManager != null ) {
                Iterator iter = copletInstanceDataManager.getCopletInstanceData().values().iterator();
                while ( iter.hasNext() ) {
                    CopletInstanceData cid = (CopletInstanceData) iter.next();
                    CopletAdapter adapter = null;
                    try {
                        adapter = (CopletAdapter)adapterSelector.select(cid.getCopletData().getCopletBaseData().getCopletAdapterName());
                        adapter.logout( cid );
                    } finally {
                        adapterSelector.release( adapter );
                    }
                }
            }
            
            service.removeAttribute("CopletData:"+layoutKey);
            service.removeAttribute("CopletInstanceData:"+layoutKey);
            service.removeAttribute("Layout:"+layoutKey);
        } catch (ServiceException e) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(service);
            this.manager.release(adapterSelector);
        }
        super.logout();
    }
       
    /**
     * @param layoutMap
     * @param layout
     */
    protected void cacheLayouts(Map layoutMap, Layout layout) {
        if ( layout != null ) {
            if ( layout.getId() != null ) {
                layoutMap.put( layout.getId(), layout );
            }
            if ( layout instanceof CompositeLayout ) {
                CompositeLayout cl = (CompositeLayout)layout;
                Iterator i = cl.getItems().iterator();
                while ( i.hasNext() ) {
                    Item current = (Item)i.next();
                    this.cacheLayouts( layoutMap, current.getLayout() );
                }
            }
        }
        
    }

	/**
	 * Prepares the object by using the specified factory.
	 */
    protected void prepareObject(Object object, Object factory)
	throws ProcessingException {
		if (factory != null && object != null) {
			if (object instanceof Layout) {
				((LayoutFactory)factory).prepareLayout((Layout)object);
			} else if (object instanceof CopletDataManager) {
				CopletFactory copletFactory = (CopletFactory)factory;
				Iterator iterator = ((CopletDataManager)object).getCopletData().values().iterator();
				while (iterator.hasNext()) {
					CopletData cd = (CopletData)iterator.next();
					copletFactory.prepare(cd);
				}
			} else if (object instanceof CopletInstanceDataManager) {
				CopletFactory copletFactory = (CopletFactory)factory;
				Iterator iterator = ((CopletInstanceDataManager)object).getCopletInstanceData().values().iterator();
				while (iterator.hasNext()) {
					CopletInstanceData cid = (CopletInstanceData)iterator.next();
					copletFactory.prepare(cid);
				}
			}
		}
	}
	
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstanceData(java.lang.String)
     */
    public CopletInstanceData getCopletInstanceData(String copletID) {
        String layoutKey = this.getDefaultLayoutKey();
        PortalService service = null;
        String attribute = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

			attribute = "CopletInstanceData:"+layoutKey;
			CopletInstanceDataManager copletInstanceDataManager = (CopletInstanceDataManager)service.getAttribute(attribute);

            return copletInstanceDataManager.getCopletInstanceData(copletID);
        } catch (ServiceException e) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(service);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletData(java.lang.String)
     */
    public CopletData getCopletData(String copletDataId) {
        String layoutKey = this.getDefaultLayoutKey();
        PortalService service = null;
        String attribute = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

            attribute = "CopletInstanceData:"+layoutKey;
            CopletInstanceDataManager copletInstanceDataManager = (CopletInstanceDataManager)service.getAttribute(attribute);

            Iterator i = copletInstanceDataManager.getCopletInstanceData().values().iterator();
            boolean found = false;
            CopletInstanceData current = null;
            while ( !found && i.hasNext() ) {
                current = (CopletInstanceData)i.next();
                found = current.getCopletData().getId().equals(copletDataId);
            }
            if ( found ) {
                return current.getCopletData();
            }
            return null;
        } catch (ServiceException e) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(service);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstanceData(org.apache.cocoon.portal.coplet.CopletData)
     */
    public List getCopletInstanceData(CopletData data) {
        String layoutKey = this.getDefaultLayoutKey();
        List coplets = new ArrayList();
        PortalService service = null;
        String attribute = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

            attribute = "CopletInstanceData:" + layoutKey;
            CopletInstanceDataManager copletInstanceDataManager = (CopletInstanceDataManager)service.getAttribute(attribute);

            Iterator iter = copletInstanceDataManager.getCopletInstanceData().values().iterator();
            while ( iter.hasNext() ) {
                CopletInstanceData current = (CopletInstanceData)iter.next();
                if ( current.getCopletData().equals(data) ) {
                    coplets.add( current );
                }
            }
            return coplets;
        } catch (ServiceException e) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(service);
        }
    }

    public void register(CopletInstanceData coplet) {
        String layoutKey = this.getDefaultLayoutKey();
        PortalService service = null;
        String attribute = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

            attribute = "CopletInstanceData:" + layoutKey;
            CopletInstanceDataManager copletInstanceDataManager = (CopletInstanceDataManager)service.getAttribute(attribute);
            
            copletInstanceDataManager.putCopletInstanceData( coplet );
            
        } catch (ServiceException e) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(service);
        }
    }
    
    public void unregister(CopletInstanceData coplet) {
        String layoutKey = this.getDefaultLayoutKey();
        PortalService service = null;
        String attribute = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

            attribute = "CopletInstanceData:" + layoutKey;
            CopletInstanceDataManager copletInstanceDataManager = (CopletInstanceDataManager)service.getAttribute(attribute);
            
            copletInstanceDataManager.getCopletInstanceData().remove(coplet.getId());
            
        } catch (ServiceException e) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(service);
        }
    }

    public void register(Layout layout) {
        String layoutKey = this.getDefaultLayoutKey();
        PortalService service = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

            Map layoutMap = (Map)service.getAttribute("Layout-Map:" + layoutKey);
            if ( layoutMap == null ) {
                layout = (Layout)service.getAttribute("Layout:" + layoutKey);
                if (layout != null) {
                    layoutMap = new HashMap();
                    this.cacheLayouts(layoutMap, layout);
                    service.setAttribute("Layout-Map:" + layoutKey, layoutMap);
                }
            }
            
            if ( layoutMap != null) {
                layoutMap.put(layout.getId(), layout);
            }
            
        } catch (ServiceException e) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(service);
        }
    }
    
    public void unregister(Layout layout) {
        String layoutKey = this.getDefaultLayoutKey();
        PortalService service = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

            Map layoutMap = (Map)service.getAttribute("Layout-Map:" + layoutKey);
            
            if ( layoutMap != null) {
                layoutMap.remove(layout.getId());
            }
            
        } catch (ServiceException e) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(service);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#getPortalLayout(java.lang.String, java.lang.String)
     */
    public Layout getPortalLayout(String layoutKey, String layoutID) {
        PortalService service = null;
        ServiceSelector adapterSelector = null;
        
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            LayoutFactory factory = service.getComponentManager().getLayoutFactory();
            CopletFactory copletFactory = service.getComponentManager().getCopletFactory();
            
            adapterSelector = (ServiceSelector)this.manager.lookup(CopletAdapter.ROLE+"Selector");
            
            if ( null == layoutKey ) {
                layoutKey = this.getDefaultLayoutKey();
            }
            // FIXME actually this is a hack for full screen
            Layout l = (Layout) service.getTemporaryAttribute("DEFAULT_LAYOUT:" + layoutKey);
            if ( null != l) {
                return l;
            }
            
            final String layoutAttributeKey = "Layout:" + layoutKey;
            final String layoutObjectsAttributeKey = "Layout-Map:" + layoutKey;
            
            Layout layout = (Layout)service.getAttribute(layoutAttributeKey);
            if (layout == null) {
                layout = this.loadProfile(layoutKey, service, copletFactory, factory, adapterSelector);
            }
            
            if ( layoutID != null ) {
                // now search for a layout
                Map layoutMap = (Map)service.getAttribute(layoutObjectsAttributeKey);
                if ( layoutMap == null ) {
                    layoutMap = new HashMap();
                    this.cacheLayouts(layoutMap, layout);
                    service.setAttribute(layoutObjectsAttributeKey, layoutMap);
                }
                if ( layoutMap != null) {
                    return (Layout) layoutMap.get( layoutID );
                }
            }
            
            return layout;
        } catch (Exception ce) {
            throw new CascadingRuntimeException("Exception during loading of profile.", ce);
        } finally {
            this.manager.release(service);
            this.manager.release(adapterSelector);
        }
    }
    
    /**
     * This loads a new profile
     */
    protected abstract Layout loadProfile(String layoutKey, 
                                            PortalService service,
                                            CopletFactory copletFactory,
                                            LayoutFactory layoutFactory,
                                            ServiceSelector adapterSelector) 
    throws Exception;
    
}
