/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
 * @version CVS $Id: AbstractUserProfileManager.java,v 1.7 2004/03/05 13:02:16 bdelacretaz Exp $
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
