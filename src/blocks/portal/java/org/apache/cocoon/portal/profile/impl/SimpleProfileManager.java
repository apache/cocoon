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
import java.util.Iterator;
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
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutFactory;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.cocoon.portal.util.DeltaApplicableReferencesAdjustable;
import org.apache.cocoon.webapps.authentication.AuthenticationManager;
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
 * @version CVS $Id: SimpleProfileManager.java,v 1.10 2003/05/23 14:20:09 cziegeler Exp $
 */
public class SimpleProfileManager 
    extends AbstractLogEnabled 
    implements Composable, ProfileManager, ThreadSafe {

    protected ComponentManager manager;

    private Mapping layoutMapping;

    private Map layoutStati = new HashMap(100);
    
    private Map attributes = new HashMap();
    
    /**
     * @see org.apache.avalon.framework.component.Composable#compose(ComponentManager)
     */
    public void compose(ComponentManager componentManager) throws ComponentException {
        this.manager = componentManager;
    }

    public RequestState getRequestState() {
        AuthenticationManager authManager = null;
        try {
            authManager = (AuthenticationManager)this.manager.lookup(AuthenticationManager.ROLE);
            return authManager.getState();    
        } catch (ComponentException ce) {
            // ignore this here
            return null;
        } finally {
            this.manager.release( (Component)authManager );
        }
    }
    
    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getPortalLayout(Object)
     */
    public Layout getPortalLayout(String key) {
        PortalService service = null;
        LayoutFactory factory = null;
        CopletFactory copletFactory = null;
        
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            factory = (LayoutFactory) this.manager.lookup(LayoutFactory.ROLE);
            copletFactory = (CopletFactory) this.manager.lookup(CopletFactory.ROLE);
            
            if ( null == key ) {
                Layout l = (Layout) service.getTemporaryAttribute("DEFAULT_LAYOUT");
                if ( null != l) {
                    return l;
                }
            }
            
            String portalPrefix = SimpleProfileManager.class.getName()+"/"+service.getPortalName();
            Layout layout = null;

            if ( key != null ) {
                // now search for a layout
                Map layoutMap = (Map)service.getAttribute("layout-map");
                if ( layoutMap == null ) {
                    Object[] objects = (Object[])service.getAttribute(portalPrefix+"/Layout");
                    if (objects != null && objects[0] != null) {
                        layoutMap = new HashMap();
                        this.cacheLayouts(layoutMap, (Layout)objects[0]);
                        service.setAttribute("layout-map", layoutMap);
                    }
                }
                if ( layoutMap != null) {
                    layout = (Layout) layoutMap.get( key );
                    if ( layout != null) {
                        return layout;
                    }
                }
            }
            

			Object[] objects = (Object[])service.getAttribute(portalPrefix+"/Layout");
			if (objects != null)
				layout = (Layout)objects[0];
				
			if (layout == null) {
				HashMap map = new HashMap();
				map.put("portalname", service.getPortalName());
				
				// TODO Change to KeyManager usage
                RequestState state = this.getRequestState();
				UserHandler handler = state.getHandler();
				HashMap keyMap = new HashMap();
				keyMap.put("user", handler.getUserId());
				keyMap.put("role", handler.getContext().getContextInfo().get("role"));
				keyMap.put("config", state.getApplicationConfiguration().getConfiguration("portal"));
	
				// load coplet base data
				map.put("profile", "copletbasedata");
				map.put("objectmap", null);
				Object[] result = this.getProfile(keyMap, map, portalPrefix+"/CopletBaseData", null, null);
				if (result[0] == null) {
					throw new SourceNotFoundException("Could not find coplet base data profile.");
				}
				CopletBaseDataManager copletBaseDataManager = (CopletBaseDataManager)result[0];
				boolean lastLoaded = ((Boolean)result[1]).booleanValue();
	
				// load coplet data
				map.put("profile", "copletdata");
				map.put("objectmap", copletBaseDataManager.getCopletBaseData());
				result = this.getDeltaProfile(keyMap, map, portalPrefix+"/CopletData", service, copletFactory, lastLoaded);
				if (result[0] == null) {
					throw new SourceNotFoundException("Could not find coplet data profile.");
				}
				CopletDataManager copletDataManager = (CopletDataManager)result[0];
				lastLoaded = ((Boolean)result[1]).booleanValue();
				// updating
				Iterator i = copletDataManager.getCopletData().values().iterator();
				while ( i.hasNext()) {
					CopletData cd = (CopletData)i.next();
					copletFactory.prepare(cd);
				}
	
				// load coplet instance data
				map.put("profile", "copletinstancedata");
				map.put("objectmap", copletDataManager.getCopletData());
				result = this.getOrCreateProfile(keyMap, map, portalPrefix+"/CopletInstanceData", service, copletFactory);
				CopletInstanceDataManager copletInstanceDataManager = (CopletInstanceDataManager)result[0];
				boolean loaded = ((Boolean)result[1]).booleanValue();
				if (lastLoaded && !loaded) {
					copletInstanceDataManager.update(copletDataManager);
				}
				lastLoaded = loaded;
				// updating
				i = copletInstanceDataManager.getCopletInstanceData().values().iterator();
				while ( i.hasNext()) {
					CopletInstanceData cid = (CopletInstanceData)i.next();
					copletFactory.prepare(cid);
				}
	
				// load layout
				map.put("profile", "layout");
				map.put("objectmap", ((CopletInstanceDataManager)result[0]).getCopletInstanceData());
				result = this.getOrCreateProfile(keyMap, map, portalPrefix+"/Layout", service, factory);
				layout = (Layout)result[0];
				loaded = ((Boolean)result[1]).booleanValue();
				if (lastLoaded && !loaded) {
					updateLayout(layout, copletInstanceDataManager);
				}
	            factory.prepareLayout( layout );
			}

            return layout;
        } catch (Exception ce) {
            // TODO
            throw new CascadingRuntimeException("Arg", ce);
        } finally {
            this.manager.release(service);
            this.manager.release((Component)factory);
            this.manager.release((Component)copletFactory);
        }
    }
    
    /**
     * @param layoutMap
     * @param layout
     */
    private void cacheLayouts(Map layoutMap, Layout layout) {
        if ( layout != null ) {
            layoutMap.put( layout.getId(), layout );
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

    public void saveUserProfiles() {
		MapSourceAdapter adapter = null;
		PortalService service = null;
		try {
			adapter = (MapSourceAdapter) this.manager.lookup(MapSourceAdapter.ROLE);
			service = (PortalService) this.manager.lookup(PortalService.ROLE);
            
			String portalPrefix = SimpleProfileManager.class.getName()+"/"+service.getPortalName();

			HashMap map = new HashMap();
			map.put("portalname", service.getPortalName());
			map.put("type", "user");
				
			// TODO Change to KeyManager usage
            RequestState state = this.getRequestState();
			UserHandler handler = state.getHandler();
			HashMap key = new HashMap();
			key.put("user", handler.getUserId());
			key.put("role", handler.getContext().getContextInfo().get("role"));
			key.put("config", state.getApplicationConfiguration().getConfiguration("portal"));
	
			// save coplet instance data
			map.put("profile", "copletinstancedata");
			Object profile = ((Object[])service.getAttribute(portalPrefix+"/CopletInstanceData"))[0];
			adapter.saveProfile(key, map, profile);

			// save coplet instance data
			map.put("profile", "layout");
			profile = ((Object[])service.getAttribute(portalPrefix+"/Layout"))[0];
			adapter.saveProfile(key, map, profile);
		} catch (Exception e) {
			// TODO
			throw new CascadingRuntimeException("Arg", e);
		} finally {
			this.manager.release(adapter);
			this.manager.release(service);
		}
    }
    
    /**
     * Gets a profile and applies possible user and role deltas to it.
     * @return result[0] is the profile, result[1] is a Boolean, 
     * which signals whether the profile has been loaded or reused.
     */
    private Object[] getDeltaProfile(Object key, Map map, String location, PortalService service, Object factory, boolean forcedLoad) 
    throws Exception {
    	Object[] result;
    	
		// TODO Change to KeyManager usage
		Map keyMap = (Map)key;

		// check validities
		map.put("type", "global");
		Object[] globalValidity = this.getValidity(key, map, location, null);
		map.put("type", "role");
		Object[] roleValidity = this.getValidity(key, map, location+"-role-"+keyMap.get("role"), null);
		map.put("type", "user");
		Object[] userValidity = this.getValidity(key, map, location+"-user", service);
		boolean isValid
			= ((Boolean)globalValidity[0]).booleanValue()
			  &&((Boolean)roleValidity[0]).booleanValue()
			  &&((Boolean)userValidity[0]).booleanValue();
			  
		if (isValid && !forcedLoad) {
			Object[] objects = (Object[]) this.attributes.get(location);
			result = new Object[] {objects[0], Boolean.FALSE};
		} else {
			// load global profile
			map.put("type", "global");
			Object global = this.getProfile(key, map, location, globalValidity, null, factory, forcedLoad)[0];
			DeltaApplicableReferencesAdjustable object = (DeltaApplicableReferencesAdjustable)this.loadProfile(key, map, location, (SourceValidity)globalValidity[1], service, factory);
			result = new Object[] {object, Boolean.TRUE};
		
			// load role delta
			map.put("type", "role");
			result = this.getProfile(key, map, location+"-role-"+keyMap.get("role"), roleValidity, null, factory, forcedLoad);
			if (((Boolean)result[1]).booleanValue())
				object.applyDelta(result[0]); 		

			// load user delta
			map.put("type", "user");
			result = this.getProfile(key, map, location+"-user", userValidity, service, factory, forcedLoad);
			if (((Boolean)result[1]).booleanValue())
				object.applyDelta(result[0]);
				
			// change references to objects where no delta has been applied
			object.adjustReferences(global);
			
			result = new Object[] {object, Boolean.TRUE}; 		
		}

    	return result;
    }

	/**
	 * Gets a user profile and creates it by copying the role or the global profile.
	 * @return result[0] is the profile, result[1] is a Boolean, 
	 * which signals whether the profile has been loaded or reused.
	 */
	private Object[] getOrCreateProfile(Object key, Map map, String location, PortalService service, Object factory) 
	throws Exception {
		Object[] result;
    	
		// TODO Change to KeyManager usage
		Map keyMap = (Map)key;

		// load user profile
		map.put("type", "user");
		result = this.getProfile(key, map, location, service, factory);

		if (result[0] == null) {
			// load role profile
			map.put("type", "role");
			result = this.getProfile(key, map, location+"-role-"+keyMap.get("role"), service, factory);

			if (result[0] == null) {
				// load global profile
				map.put("type", "global");
				result = this.getProfile(key, map, location+"-global", service, factory);

				if (result[0] == null) {
					throw new SourceNotFoundException("Could not find global or role profile to create user profile.");
				}
			}
			
			// save profile as user profile
			MapSourceAdapter adapter = null;
			try {
				adapter = (MapSourceAdapter) this.manager.lookup(MapSourceAdapter.ROLE);
				map.put("type", "user");
				
                // FIXME - disabled saving for testing
                // adapter.saveProfile(key, map, result[0]);

				// set validity for created user profile
				SourceValidity newValidity = adapter.getValidity(key, map);
				if (newValidity != null) {
					Object[] objects = new Object[] { result[0], newValidity };
					this.setAttribute(location, objects, service);
				} else {
                    Object[] objects = new Object[] { result[0], null };
                    this.setAttribute(location, objects, service);
				}
			} finally {
				this.manager.release(adapter);
			}
		}
		
		return result;
	}

	/**
	 * Gets a profile.
	 * @return result[0] is the profile, result[1] is a Boolean, 
	 * which signals whether the profile has been loaded or reused.
	 */
	private Object[] getProfile(Object key, Map map, String location, PortalService service, Object factory) 
	throws Exception {
		MapSourceAdapter adapter = null;
		try {
			adapter = (MapSourceAdapter) this.manager.lookup(MapSourceAdapter.ROLE);

			Object[] objects = (Object[]) this.getAttribute(location, service);
			
			// check whether still valid
			SourceValidity sourceValidity = null;
			if (objects != null)
				sourceValidity = (SourceValidity)objects[1];
			Object[] validity = this.getValidity(key, map, location, sourceValidity, adapter);
			if (((Boolean)validity[0]).booleanValue()) {
				if (objects == null) {
					return new Object[]{null, Boolean.FALSE};
				} else {
					return new Object[]{objects[0], Boolean.FALSE};
				}
			}
			
			// load profile
			SourceValidity newValidity = (SourceValidity)validity[1];
			Object object = adapter.loadProfile(key, map);
			if (object != null)
				this.prepareObject(object, factory);
			if (newValidity != null) {
				objects = new Object[] { object, newValidity };
				this.setAttribute(location, objects, service);
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
	private Object[] getProfile(Object key, Map map, String location, Object[] validity, PortalService service, Object factory, boolean forcedLoad) 
	throws Exception {
		if (forcedLoad) {
			try {
				return new Object[] {this.loadProfile(key, map, location, (SourceValidity)validity[1], service, factory), Boolean.TRUE};
			} catch (SourceNotFoundException e) {
				return new Object[] {null, Boolean.FALSE};
			}
		} else {
			MapSourceAdapter adapter = null;
			try {
				adapter = (MapSourceAdapter) this.manager.lookup(MapSourceAdapter.ROLE);

				// check whether still valid
				Object[] objects = (Object[]) this.getAttribute(location, service);
				if (((Boolean)validity[0]).booleanValue()) {
					if (objects == null) {
						return new Object[]{null, Boolean.FALSE};
					} else {
						return new Object[]{objects[0], Boolean.FALSE};
					}
				}

				// load profile
				SourceValidity newValidity = (SourceValidity)validity[1];
				Object object = adapter.loadProfile(key, map);
				if (object != null)
					this.prepareObject(object, factory);
				if (newValidity != null) {
					objects = new Object[] { object, newValidity };
					this.setAttribute(location, objects, service);
				}

				return new Object[]{object, Boolean.TRUE};
			} finally {
				this.manager.release(adapter);
			}
		}
	}

	/**
	 * Loads a profile and reuses the specified validity for storing if it is not null.
	 */
	private Object loadProfile(Object key, Map map, String location, SourceValidity newValidity, PortalService service, Object factory) 
	throws Exception {
		MapSourceAdapter adapter = null;
		try {
			adapter = (MapSourceAdapter) this.manager.lookup(MapSourceAdapter.ROLE);

			if (newValidity == null)
				newValidity = adapter.getValidity(key, map);
			Object object = adapter.loadProfile(key, map);
			if (object != null)
				this.prepareObject(object, factory);
			if (newValidity != null) {
				Object[] objects = new Object[] { object, newValidity };
				this.setAttribute(location, objects, service);
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
	private Object[] getValidity(Object key, Map map, String location, PortalService service)
	throws Exception { 
		MapSourceAdapter adapter = null;
		try {
			adapter = (MapSourceAdapter) this.manager.lookup(MapSourceAdapter.ROLE);

			Object[] objects = (Object[]) this.getAttribute(location, service);
			SourceValidity sourceValidity = null;
			if (objects != null)
				sourceValidity = (SourceValidity)objects[1];
			
			return this.getValidity(key, map, location, sourceValidity, adapter);
		} finally {
			this.manager.release(adapter);
		}
	}

	/**
	 * Checks the specified validity.
	 * @return result[0] is a Boolean, which signals whether it is valid, 
	 * result[1] may contain a newly created validity or be null if it could be reused.
	 */
	private Object[] getValidity(Object key, Map map, String location, SourceValidity sourceValidity, MapSourceAdapter adapter) 
	throws Exception {
		int valid = SourceValidity.INVALID;

		if (sourceValidity != null) {
			valid = sourceValidity.isValid();
			if (valid == SourceValidity.VALID)
				return new Object[]{Boolean.TRUE, null};
		}

		SourceValidity newValidity = adapter.getValidity(key, map);
		
		// source does not exist so it is valid
		if (newValidity == null)
			return new Object[]{Boolean.TRUE, null};
		
		if (valid == SourceValidity.UNKNWON) {
			if (sourceValidity.isValid(newValidity) == SourceValidity.VALID)
				return new Object[]{Boolean.TRUE, newValidity};
		}

		return new Object[]{Boolean.FALSE, newValidity};
	}
	
	/**
	 * Prepares the object by using the specified factory.
	 */
	private void prepareObject(Object object, Object factory)
	throws ProcessingException {
		if (factory != null) {
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
	
	/**
	 * If service is null the value is stored in this.attributes otherwise it is stored via the service.
	 */
	private void setAttribute(String key, Object value, PortalService service) {
		if (service == null) {
			this.attributes.put(key, value);
		} else {
			service.setAttribute(key, value);
		}
	}
	
	/**
	 * If service is null the value is requested from this.attributes otherwise it is stored via the service.
	 */
	private Object getAttribute(String key, PortalService service) {
		if (service == null) {
			return this.attributes.get(key);
		} else {
			return service.getAttribute(key);
		}
	}

    public CopletInstanceData getCopletInstanceData(String copletID) {
        PortalService service = null;
        String attribute = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

			attribute = SimpleProfileManager.class.getName()+"/"+service.getPortalName()+"/CopletInstanceData";
			CopletInstanceDataManager copletInstanceDataManager = (CopletInstanceDataManager)((Object[])service.getAttribute(attribute))[0];

            return copletInstanceDataManager.getCopletInstanceData(copletID);
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

    private void updateLayout(final Layout layout, final CopletInstanceDataManager manager)
    throws ProcessingException {
        if (layout instanceof CompositeLayout) {
            final CompositeLayout compositeLayout = (CompositeLayout)layout;
            for (int j = 0; j < compositeLayout.getSize(); j++) {
                final Item layoutItem = (Item) compositeLayout.getItem(j);
                this.updateLayout(layoutItem.getLayout(), manager);
            }
        } else if (layout instanceof CopletLayout) {
			final CopletLayout copletLayout = (CopletLayout)layout;
			if (manager != null) {
				String copletId = copletLayout.getCopletInstanceData().getId();
				copletLayout.setCopletInstanceData(manager.getCopletInstanceData(copletId)); 
			}
        }
    }

}
