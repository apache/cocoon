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

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.aspect.AspectStatus;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.status.SizeableStatus;
import org.apache.cocoon.portal.layout.AbstractLayout;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutFactory;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.cocoon.portal.util.DeltaApplicable;
import org.apache.cocoon.webapps.authentication.user.RequestState;
import org.apache.cocoon.webapps.authentication.user.UserHandler;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;
import org.exolab.castor.mapping.Mapping;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Björn Lütkemeier</a>
 * 
 * @version CVS $Id: SimpleProfileManager.java,v 1.3 2003/05/19 12:51:00 cziegeler Exp $
 */
public class SimpleProfileManager 
    extends AbstractLogEnabled 
    implements Composable, ProfileManager, ThreadSafe {

    protected ComponentManager manager;

    private Mapping layoutMapping;

    private Map layoutStati = new HashMap(100);
    
    /**
     * @see org.apache.avalon.framework.component.Composable#compose(ComponentManager)
     */
    public void compose(ComponentManager componentManager) throws ComponentException {
        this.manager = componentManager;
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getPortalLayout(Object)
     */
    public Layout getPortalLayout(String key) {
        PortalService service = null;
        LayoutFactory factory = null;
        
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            factory = (LayoutFactory) this.manager.lookup(LayoutFactory.ROLE);
            
            if ( null == key ) {
                Layout l = (Layout) service.getTemporaryAttribute("DEFAULT_LAYOUT");
                if ( null != l) {
                    return l;
                }
            }
            
            String portalPrefix = "/"+service.getPortalName();

			// TODO Change to KeyManager usage
			UserHandler handler = RequestState.getState().getHandler();
			HashMap map = new HashMap();
			map.put("portalname", service.getPortalName());
			map.put("user", handler.getUserId());
			map.put("role", handler.getContext().getContextInfo().get("role"));

			// load coplet base data
			map.put("profile", "copletbasedata");
			map.put("objectmap", null);
			Object[] result = this.getProfile(map, portalPrefix+"/CopletBaseData", service);
			if (result[0] == null) {
				throw new SourceNotFoundException("Could not find coplet base data profile.");
			}
			CopletBaseDataManager copletBaseDataManager = (CopletBaseDataManager)result[0];
			boolean lastLoaded = ((Boolean)result[1]).booleanValue();

			// load coplet data
			map.put("profile", "copletdata");
			map.put("objectmap", copletBaseDataManager.getCopletBaseData());
			result = this.getDeltaProfile(map, portalPrefix+"/CopletData", service);
			if (result[0] == null) {
				throw new SourceNotFoundException("Could not find coplet data profile.");
			}
			CopletDataManager copletDataManager = (CopletDataManager)result[0];
			boolean loaded = ((Boolean)result[1]).booleanValue();
			if (lastLoaded && !loaded) {
				copletDataManager.update(copletBaseDataManager);
			}
			lastLoaded = loaded;

			// load coplet instance data
			map.put("profile", "copletinstancedata");
			map.put("objectmap", copletDataManager.getCopletData());
			result = this.getOrCreateProfile(map, portalPrefix+"/CopletInstanceData", service);
			CopletInstanceDataManager copletInstanceDataManager = (CopletInstanceDataManager)result[0];
			loaded = ((Boolean)result[1]).booleanValue();
			if (lastLoaded && !loaded) {
				copletInstanceDataManager.update(copletDataManager);
			}
			lastLoaded = loaded;

			// load layout
			map.put("profile", "layout");
			map.put("objectmap", ((CopletInstanceDataManager)result[0]).getCopletInstanceData());
			result = this.getOrCreateProfile(map, portalPrefix+"/Layout", service);
			Layout layout = (Layout)result[0];
			loaded = ((Boolean)result[1]).booleanValue();
			if (lastLoaded && !loaded) {
				resolveParents(layout, null, copletInstanceDataManager);
			} else {
				resolveParents(layout, null, null);
			}
            factory.prepareLayout( layout );
             
            return layout;
        } catch (Exception ce) {
            // TODO
            throw new CascadingRuntimeException("Arg", ce);
        } finally {
            this.manager.release(service);
            this.manager.release((Component)factory);
        }
    }
    
    /**
     * Gets a profile and applies possible user and role deltas to it.
     * @return result[0] is the profile, result[1] is a Boolean, 
     * which signals whether the profile has been loaded or reused.
     */
    private Object[] getDeltaProfile(Object key, String location, PortalService service) 
    throws Exception {
    	Object[] result;
    	
		// TODO Change key access to KeyManager usage
		Map map = (Map)key;
		
		// check validities
		map.remove("type");
		Object[] globalValidity = this.getValidity(map, location+"-global", service);
		map.put("type", "role");
		Object[] roleValidity = this.getValidity(map, location+"-role-"+map.get("role"), service);
		map.put("type", "user");
		Object[] userValidity = this.getValidity(map, location+"-user-"+map.get("user"), service);
		boolean isValid
			= ((Boolean)globalValidity[0]).booleanValue()
			  &&((Boolean)roleValidity[0]).booleanValue()
			  &&((Boolean)userValidity[0]).booleanValue();
			  
		if (isValid) {
			/* The objects of the global profile have been modified by deltas
			   during the last load and therefore represent the current profile. 
			   So reuse them. */
			Object[] objects = (Object[]) service.getAttribute(SimpleProfileManager.class.getName()+location+"-global");

			result = new Object[] {objects[0], Boolean.FALSE};
		} else {
			// load global profile
			map.remove("type");
			/* It must be loaded and cannot be reused since the objects are modified by deltas
			   so they do NOT represent the global profile any more. */
			DeltaApplicable object = (DeltaApplicable)this.loadProfile(map, location+"-global", (SourceValidity)globalValidity[1], service);
			result = new Object[] {object, Boolean.TRUE};
		
			// load role delta
			map.put("type", "role");
			result = this.getProfile(map, location+"-role-"+map.get("role"), roleValidity, service);
			if (((Boolean)result[1]).booleanValue())
				object.applyDelta(result[0]); 		

			// load user delta
			map.put("type", "user");
			result = this.getProfile(map, location+"-user-"+map.get("user"), userValidity, service);
			if (((Boolean)result[1]).booleanValue())
				object.applyDelta(result[0]);
			
			result = new Object[] {object, Boolean.TRUE}; 		
		}

		// clean up for reuse
		map.remove("type");

    	return result;
    }

	/**
	 * Gets a user profile and creates it by copying the role or the global profile.
	 * @return result[0] is the profile, result[1] is a Boolean, 
	 * which signals whether the profile has been loaded or reused.
	 */
	private Object[] getOrCreateProfile(Object key, String location, PortalService service) 
	throws Exception {
		Object[] result;
    	
		// TODO Change key access to KeyManager usage
		Map map = (Map)key;
		
		// load user profile
		map.put("type", "user");
		result = this.getProfile(key, location+"-user-"+map.get("user"), service);

		if (result[0] == null) {
			// load role profile
			map.put("type", "role");
			result = this.getProfile(key, location+"-role-"+map.get("role"), service);

			if (result[0] == null) {
				// load global profile
				map.remove("type");
				result = this.getProfile(key, location+"-global", service);

				if (result[0] == null) {
					throw new SourceNotFoundException("Could not find global or role profile to create user profile.");
				}
			}
			
			// save profile as user profile
			MapSourceAdapter adapter = null;
			try {
				// TODO could one perhaps simply copy the file to increase performance??
				adapter = (MapSourceAdapter) this.manager.lookup(MapSourceAdapter.ROLE);
				map.put("type", "user");
				
                // FIXME - disabled saving for testing
                // adapter.saveProfile(key, result[0]);

				// set validity for created user profile
				SourceValidity newValidity = adapter.getValidity(key);
				if (newValidity != null) {
					Object[] objects = new Object[] { result[0], newValidity };
					service.setAttribute(SimpleProfileManager.class.getName()+location+"-user-"+map.get("user"), objects);
				} else {
                    Object[] objects = new Object[] { result[0], null };
                    service.setAttribute(SimpleProfileManager.class.getName()+location+"-user-"+map.get("user"), objects);
				}
			} finally {
				this.manager.release(adapter);
			}
		}
		
		// clean up for reuse
		map.remove("type");

		return result;
	}

	/**
	 * Gets a profile.
	 * @return result[0] is the profile, result[1] is a Boolean, 
	 * which signals whether the profile has been loaded or reused.
	 */
	private Object[] getProfile(Object key, String location, PortalService service) 
	throws Exception {
		MapSourceAdapter adapter = null;
		try {
			adapter = (MapSourceAdapter) this.manager.lookup(MapSourceAdapter.ROLE);

			Object[] objects = (Object[]) service.getAttribute(SimpleProfileManager.class.getName() + location);
			
			// check whether still valid
			SourceValidity sourceValidity = null;
			if (objects != null)
				sourceValidity = (SourceValidity)objects[1];
			Object[] validity = this.getValidity(key, location, sourceValidity, adapter);
			if (((Boolean)validity[0]).booleanValue()) {
				if (objects == null) {
					return new Object[]{null, Boolean.FALSE};
				} else {
					return new Object[]{objects[0], Boolean.FALSE};
				}
			}
			
			// load profile
			SourceValidity newValidity = (SourceValidity)validity[1];
			Object object = adapter.loadProfile(key);
			if (newValidity != null) {
				objects = new Object[] { object, newValidity };
				service.setAttribute(SimpleProfileManager.class.getName() + location, objects);
			}

			return new Object[]{object, Boolean.TRUE};
		} finally {
			this.manager.release(adapter);
		}
	}
	
	/**
	 * Gets a profile by using the specified validity information.
	 * @return result[0] is the profile, result[1] is a Boolean, 
	 * which signals whether the profile has been loaded or reused.
	 */
	private Object[] getProfile(Object key, String location, Object[] validity, PortalService service) 
	throws Exception {
		MapSourceAdapter adapter = null;
		try {
			adapter = (MapSourceAdapter) this.manager.lookup(MapSourceAdapter.ROLE);

			// check whether still valid
			Object[] objects = (Object[]) service.getAttribute(SimpleProfileManager.class.getName() + location);
			if (((Boolean)validity[0]).booleanValue()) {
				if (objects == null) {
					return new Object[]{null, Boolean.FALSE};
				} else {
					return new Object[]{objects[0], Boolean.FALSE};
				}
			}

			// load profile
			SourceValidity newValidity = (SourceValidity)validity[1];
			Object object = adapter.loadProfile(key);
			if (newValidity != null) {
				objects = new Object[] { object, newValidity };
				service.setAttribute(SimpleProfileManager.class.getName() + location, objects);
			}

			return new Object[]{object, Boolean.TRUE};
		} finally {
			this.manager.release(adapter);
		}
	}

	/**
	 * Loads a profile and reuses the specified validity for storing if it is not null.
	 */
	private Object loadProfile(Object key, String location, SourceValidity newValidity, PortalService service) 
	throws Exception {
		MapSourceAdapter adapter = null;
		try {
			adapter = (MapSourceAdapter) this.manager.lookup(MapSourceAdapter.ROLE);

			if (newValidity == null)
				newValidity = adapter.getValidity(key);
			Object object = adapter.loadProfile(key);
			if (newValidity != null) {
				Object[] objects = new Object[] { object, newValidity };
				service.setAttribute(SimpleProfileManager.class.getName() + location, objects);
			}

			return object;
		} finally {
			this.manager.release(adapter);
		}
	}

	/**
	 * Checks the validity.
	 * @return result[0] is a Boolean, which signals whether it is valid, 
	 * result[1] may contain a newly created validity or be null if it could be reused.
	 */
	private Object[] getValidity(Object key, String location, PortalService service)
	throws Exception { 
		MapSourceAdapter adapter = null;
		try {
			adapter = (MapSourceAdapter) this.manager.lookup(MapSourceAdapter.ROLE);

			Object[] objects = (Object[]) service.getAttribute(SimpleProfileManager.class.getName() + location);
			SourceValidity sourceValidity = null;
			if (objects != null)
				sourceValidity = (SourceValidity)objects[1];
			
			return this.getValidity(key, location, sourceValidity, adapter);
		} finally {
			this.manager.release(adapter);
		}
	}

	/**
	 * Checks the specified validity.
	 * @return result[0] is a Boolean, which signals whether it is valid, 
	 * result[1] may contain a newly created validity or be null if it could be reused.
	 */
	private Object[] getValidity(Object key, String location, SourceValidity sourceValidity, MapSourceAdapter adapter) 
	throws Exception {
		int valid = SourceValidity.INVALID;

		if (sourceValidity != null) {
			valid = sourceValidity.isValid();
			if (valid == SourceValidity.VALID)
				return new Object[]{Boolean.TRUE, null};
		}

		SourceValidity newValidity = adapter.getValidity(key);
		
		// source does not exist so it is valid
		if (newValidity == null)
			return new Object[]{Boolean.TRUE, null};
		
		if (valid == SourceValidity.UNKNWON) {
			if (sourceValidity.isValid(newValidity) == SourceValidity.VALID)
				return new Object[]{Boolean.TRUE, newValidity};
		}

		return new Object[]{Boolean.FALSE, newValidity};
	}
	
    public CopletInstanceData getCopletInstanceData(String copletID) {
        PortalService service = null;
        String attribute = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

			// TODO Change to KeyManager usage
			UserHandler handler = RequestState.getState().getHandler();
			attribute = SimpleProfileManager.class.getName()+"/"+service.getPortalName()+"/CopletInstanceData-user-"+handler.getUserId();

/* 			TODO Must be changed for dynamic coplet creation.           
 
 			if (null == coplets) {
                coplets = new HashMap();
                service.setAttribute(attribute, coplets);
            }*/
			CopletInstanceDataManager copletInstanceDataManager = (CopletInstanceDataManager)((Object[])service.getAttribute(attribute))[0];

            CopletInstanceData cid = copletInstanceDataManager.getCopletInstanceData(copletID);
            if (null == cid) {
/* 				TODO Must be changed for dynamic coplet creation. 

                CopletBaseData base = new CopletBaseData();
                base.setName("URICoplet");
                base.setCopletAdapterName("uri");
                base.setDefaultRendererName("window");
                cid = new CopletInstanceData();
                CopletData cd = new CopletData();
                cd.setName(copletID);
                cid.setCopletData(cd);
                cid.setCopletId(copletID); // TODO generate unique copletID
                cid.getCopletData().setCopletBaseData(base);
                cid.getCopletData().setAttribute("uri", copletID);
                cid.getCopletData().setTitle(copletID);
                coplets.put(key, cid);

                Marshaller marshaller;
                try {
                    marshaller = new Marshaller(new PrintWriter(System.out));
                    marshaller.setSuppressXSIType(true);
                    marshaller.setMapping(layoutMapping);
                    marshaller.marshal(cid);
                } catch (Exception e) {
                    //e.printStackTrace();
                }*/
            }
            return cid;
        } catch (ComponentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new CascadingRuntimeException("CE", e);
        } finally {
            this.manager.release(service);
        }
    }

    public void setDefaultLayout(Layout object) {
        PortalService service = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            service.setTemporaryAttribute("DEFAULT_LAYOUT", object);
        } catch (ComponentException e) {
            throw new CascadingRuntimeException("Unable to lookup service manager.", e);
        } finally {
            this.manager.release(service);
        }
    }

    private void resolveParents(final Layout layout, final Item item, CopletInstanceDataManager manager)
    throws ProcessingException {
        String id = layout.getId();
        if ( id == null ) {
            id = Integer.toString(layout.hashCode());
            ((AbstractLayout)layout).setId(id);
        }
        if (layout instanceof CompositeLayout) {

            final CompositeLayout compositeLayout = (CompositeLayout) layout;

            for (int j = 0; j < compositeLayout.getSize(); j++) {
                final Item layoutItem = (Item) compositeLayout.getItem(j);
                layoutItem.setParent(compositeLayout);
                this.resolveParents(layoutItem.getLayout(), layoutItem, manager);
            }
        }
        if (layout instanceof CopletLayout) {
			CopletLayout copletLayout = (CopletLayout)layout;

			if (manager != null) {
				String copletId = copletLayout.getCopletInstanceData().getCopletId();
				copletLayout.setCopletInstanceData(manager.getCopletInstanceData(copletId)); 
			}

            // FIXME - move this simple test at a better place
            if ( copletLayout.getCopletInstanceData() == null ) {
                throw new ProcessingException("Layout " + copletLayout.getId() + " has no coplet instance data.");
            } else {
                if ( copletLayout.getCopletInstanceData().getCopletData() == null ) {
                    throw new ProcessingException("CopletInstanceData " + copletLayout.getCopletInstanceData().getCopletId() + " has no coplet data.");
                }
            }
            this.setAspectStatus(ProfileManager.SESSION_STATUS, copletLayout.getCopletInstanceData().getCopletData().getName(), new SizeableStatus());
        }
        layout.setParent(item);
    }
	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.profile.ProfileManager#getLayoutStatus(java.lang.String)
	 */
	public AspectStatus getAspectStatus(Class type, String mode, String key) {
        if ( ProfileManager.REQUEST_STATUS.equals( mode )) {
            PortalService service = null;
            try {
                service = (PortalService) this.manager.lookup(PortalService.ROLE);
                return (AspectStatus)service.getTemporaryAttribute(type.getName()+"."+key);
            } catch (ComponentException ce) {
                // ignore
                return null;
            } finally {
                this.manager.release( service );
            }
        } else {
            // FIXME implement session mode
            Map stati = (Map) this.layoutStati.get( type.getName() );
            return (stati == null ? null : (AspectStatus)stati.get(key));
        }
	}

	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.profile.ProfileManager#setLayoutStatus(java.lang.String, org.apache.cocoon.portal.layout.LayoutStatus)
	 */
	public void setAspectStatus( String mode, String key, AspectStatus status) {
        if ( ProfileManager.REQUEST_STATUS.equals( mode )) {
            PortalService service = null;
            try {
                service = (PortalService) this.manager.lookup(PortalService.ROLE);
                final String attribute = status.getClass().getName() + "." + key;
                if (null == status) {
                    service.removeTemporaryAttribute(attribute);
                } else {
                    service.setTemporaryAttribute(attribute, status);
                }
            } catch (ComponentException ce) {
                // ignore
            } finally {
                this.manager.release( service );
            }
        } else {
            // FIXME implement session mode
            Map stati = (Map) this.layoutStati.get( status.getClass().getName() );
            if ( stati == null ) {
                stati = new HashMap(5);
                this.layoutStati.put( status.getClass().getName(), stati );
            }
            stati.put(key, status);
        }
	}

}