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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
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
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.cocoon.portal.util.DeltaApplicableReferencesAdjustable;
import org.apache.cocoon.webapps.authentication.AuthenticationManager;
import org.apache.cocoon.webapps.authentication.user.RequestState;
import org.apache.cocoon.webapps.authentication.user.UserHandler;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;

/**
 * The profile manager using the authentication framework
 * 
 * FIXME - create abstract base class
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Björn Lütkemeier</a>
 * 
 * @version CVS $Id: AuthenticationProfileManager.java,v 1.7 2003/07/03 08:27:47 cziegeler Exp $
 */
public class AuthenticationProfileManager 
    extends AbstractLogEnabled 
    implements Composable, ProfileManager, ThreadSafe {

    protected ComponentManager manager;

    private Map attributes = new HashMap();
    
    private ReadWriteLock lock = new ReadWriteLock();
    
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
    
    public void login() {
        // TODO - we should move most of the stuff from getPortalLayout to here
        // for now we use a hack :)
        this.getPortalLayout(null);
    }
    
    public void logout() {
        PortalService service = null;
        String attribute = null;
        ComponentSelector adapterSelector = null;
        try {
            adapterSelector = (ComponentSelector)this.manager.lookup(CopletAdapter.ROLE+"Selector");
            service = (PortalService)this.manager.lookup(PortalService.ROLE);

            String portalPrefix = AuthenticationProfileManager.class.getName()+"/"+service.getPortalName();

            CopletInstanceDataManager copletInstanceDataManager = (CopletInstanceDataManager)service.getAttribute(portalPrefix+"/CopletInstanceData");
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

            service.removeAttribute(portalPrefix+"/CopletData");
            service.removeAttribute(portalPrefix+"/CopletInstanceData");
            service.removeAttribute(portalPrefix+"/Layout");
        } catch (ComponentException e) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(service);
            this.manager.release(adapterSelector);
        }
    }
    
    /**
     * @see ProfileManager#getPortalLayout(String)
     */
    public Layout getPortalLayout(String key) {
        PortalService service = null;
        LayoutFactory factory = null;
        CopletFactory copletFactory = null;
        ComponentSelector adapterSelector = null;
        
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            factory = (LayoutFactory) this.manager.lookup(LayoutFactory.ROLE);
            copletFactory = (CopletFactory) this.manager.lookup(CopletFactory.ROLE);
            adapterSelector = (ComponentSelector)this.manager.lookup(CopletAdapter.ROLE+"Selector");
            
            if ( null == key ) {
                Layout l = (Layout) service.getTemporaryAttribute("DEFAULT_LAYOUT");
                if ( null != l) {
                    return l;
                }
            }
            
            String portalPrefix = AuthenticationProfileManager.class.getName()+"/"+service.getPortalName();
            Layout layout = null;

            if ( key != null ) {
                // now search for a layout
                Map layoutMap = (Map)service.getAttribute("layout-map");
                if ( layoutMap == null ) {
                    layout = (Layout)service.getAttribute(portalPrefix+"/Layout");
                    if (layout != null) {
                        layoutMap = new HashMap();
                        this.cacheLayouts(layoutMap, layout);
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
            

			layout = (Layout)service.getAttribute(portalPrefix+"/Layout");
			if (layout == null) {
				this.lock.readLock();
                HashMap map = new HashMap();
                HashMap keyMap = new HashMap();
                CopletDataManager copletDataManager = null;
				try {
					map.put("portalname", service.getPortalName());
					
					// TODO Change to KeyManager usage
					RequestState state = this.getRequestState();
					UserHandler handler = state.getHandler();
					keyMap.put("user", handler.getUserId());
					keyMap.put("role", handler.getContext().getContextInfo().get("role"));
					keyMap.put("config", state.getApplicationConfiguration().getConfiguration("portal"));
					
					// load coplet base data
					map.put("profile", "copletbasedata");
					map.put("objectmap", null);
					Object[] result = this.getProfile(keyMap, map, portalPrefix+"/CopletBaseData", null);
					CopletBaseDataManager copletBaseDataManager = (CopletBaseDataManager)result[0];
					
					// load coplet data
					map.put("profile", "copletdata");
					map.put("objectmap", copletBaseDataManager.getCopletBaseData());
					copletDataManager = (CopletDataManager)this.getDeltaProfile(keyMap, map, portalPrefix+"/CopletData", service, copletFactory, ((Boolean)result[1]).booleanValue());
					
                } finally {
                    this.lock.releaseLocks();
                }
				// load coplet instance data
				map.put("profile", "copletinstancedata");
				map.put("objectmap", copletDataManager.getCopletData());
				CopletInstanceDataManager copletInstanceDataManager = (CopletInstanceDataManager)this.getOrCreateProfile(keyMap, map, portalPrefix+"/CopletInstanceData", service, copletFactory);
				
				// load layout
				map.put("profile", "layout");
				map.put("objectmap", copletInstanceDataManager.getCopletInstanceData());
				layout = (Layout)this.getOrCreateProfile(keyMap, map, portalPrefix+"/Layout", service, factory);
                
                // now invoke login on each instance
                Iterator iter =  copletInstanceDataManager.getCopletInstanceData().values().iterator();
                while ( iter.hasNext() ) {
                    CopletInstanceData cid = (CopletInstanceData) iter.next();
                    CopletAdapter adapter = null;
                    try {
                        adapter = (CopletAdapter) adapterSelector.select(cid.getCopletData().getCopletBaseData().getCopletAdapterName());
                        adapter.login( cid );
                    } finally {
                        adapterSelector.release( adapter );
                    }
                }
			}
			
            return layout;
        } catch (Exception ce) {
            // TODO
            throw new CascadingRuntimeException("Arg", ce);
        } finally {
            this.manager.release(service);
            this.manager.release((Component)factory);
            this.manager.release((Component)copletFactory);
            this.manager.release(adapterSelector);
        }
    }
    
    /**
     * @param layoutMap
     * @param layout
     */
    private void cacheLayouts(Map layoutMap, Layout layout) {
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

    public void saveUserProfiles() {
		MapSourceAdapter adapter = null;
		PortalService service = null;
		try {
			adapter = (MapSourceAdapter) this.manager.lookup(MapSourceAdapter.ROLE);
			service = (PortalService) this.manager.lookup(PortalService.ROLE);
            
			String portalPrefix = AuthenticationProfileManager.class.getName()+"/"+service.getPortalName();

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
	 * Gets a profile.
	 * @return result[0] is the profile, result[1] is a Boolean, 
	 * which signals whether the profile has been loaded or reused.
	 */
	private Object[] getProfile(Object key, Map map, String location, Object factory)
	throws Exception {
		return this.getProfile(key, map, location, factory, false);
	}

	/**
	 * Gets a profile and applies possible user and role deltas to it.
	 */
	private Object getDeltaProfile(Object key, Map map, String location, PortalService service, Object factory, boolean forcedLoad) 
	throws Exception {
		DeltaApplicableReferencesAdjustable result;
		Object object;
    	
		// TODO Change to KeyManager usage
		Map keyMap = (Map)key;

		// load global profile
		map.put("type", "global");
		Object global = this.getProfile(key, map, location, factory, forcedLoad)[0];
		result = (DeltaApplicableReferencesAdjustable)this.loadProfile(key, map, factory);
	
		// load role delta
		map.put("type", "role");
		try {
			object = this.getProfile(key, map, location+"-role-"+keyMap.get("role"), factory, forcedLoad)[0];
			if (object != null)
				result.applyDelta(object); 		
		} catch (Exception e) {
			if (!isSourceNotFoundException(e))
				throw e;
		}

		// load user delta
		map.put("type", "user");
		try {
			object = this.loadProfile(key, map, factory);
			if (object != null)
				result.applyDelta(object);
		} catch (Exception e) {
			if (!isSourceNotFoundException(e))
				throw e;
		}
		
		if (result == null)
			throw new SourceNotFoundException("Global "+keyMap.get("profile")+" does not exist.");
		
		// change references to objects where no delta has been applied
		result.adjustReferences(global);
		
		service.setAttribute(location, result);
		
		return result;
	}

	/**
	 * Gets a user profile and creates it by copying the role or the global profile.
	 */
	private Object getOrCreateProfile(Object key, Map map, String location, PortalService service, Object factory) 
	throws Exception {
		Object result;
    	
		// TODO Change to KeyManager usage
		Map keyMap = (Map)key;

		// load user profile
		map.put("type", "user");
		try {
			result = this.loadProfile(key, map, factory);
		} catch (Exception e1) {
			if (!isSourceNotFoundException(e1))
				throw e1;

			// load role profile
			map.put("type", "role");
			try {
				result = this.loadProfile(key, map, factory);
			} catch (Exception e2) {
				if (!isSourceNotFoundException(e2))
					throw e2;

				// load global profile
				map.put("type", "global");
				result = this.loadProfile(key, map, factory);
			}
			
			// save profile as user profile
			MapSourceAdapter adapter = null;
			try {
				adapter = (MapSourceAdapter) this.manager.lookup(MapSourceAdapter.ROLE);
				map.put("type", "user");
				
                adapter.saveProfile(key, map, result);
			} finally {
				this.manager.release(adapter);
			}
		}
		
		service.setAttribute(location, result);

		return result;
	}

	/**
	 * Gets a profile.
	 * @return result[0] is the profile, result[1] is a Boolean, 
	 * which signals whether the profile has been loaded or reused.
	 */
	private Object[] getProfile(Object key, Map map, String location, Object factory, boolean forcedLoad) 
	throws Exception {
		MapSourceAdapter adapter = null;
		try {
			adapter = (MapSourceAdapter)this.manager.lookup(MapSourceAdapter.ROLE);

			Object result = this.checkValidity(key, map, location, forcedLoad, adapter);
			if (!(result instanceof SourceValidity))
				return new Object[]{result, Boolean.FALSE};
			SourceValidity newValidity = (SourceValidity)result; 

			this.lock.releaseReadLock();
			this.lock.writeLock();
			
			// check validity again in case of another thread has already loaded
			result = this.checkValidity(key, map, location, forcedLoad, adapter);
			if (!(result instanceof SourceValidity))
				return new Object[]{result, Boolean.FALSE};
			newValidity = (SourceValidity)result; 

			Object object = adapter.loadProfile(key, map);
			this.prepareObject(object, factory);
			if (newValidity != null) {
				this.attributes.put(location, new Object[] {object, newValidity});
			}

			return new Object[]{object, Boolean.TRUE};
		} finally {
			this.manager.release(adapter);
		}
	}
	
	/**
	 * If the profile is valid itself is returned, otherwise a newly created SourceValidity object is returned.
	 */
	private Object checkValidity(Object key, Map map, String location, boolean forcedLoad, MapSourceAdapter adapter) {
		Object[] objects = (Object[])this.attributes.get(location);

		SourceValidity sourceValidity = null;
		int valid = SourceValidity.INVALID;
		if (objects != null) {
			sourceValidity = (SourceValidity) objects[1];
			valid = sourceValidity.isValid();
			if (!forcedLoad && valid == SourceValidity.VALID)
				return objects[0];
		}

		SourceValidity newValidity = adapter.getValidity(key, map);
		if (!forcedLoad && valid == SourceValidity.UNKNWON) {
			if (sourceValidity.isValid(newValidity) == SourceValidity.VALID)
				return objects[0];
		}
		
		return newValidity;
	}

	/**
	 * Loads a profile.
	 */
	private Object loadProfile(Object key, Map map, Object factory)
	throws Exception {
		MapSourceAdapter adapter = null;
		try {
			adapter = (MapSourceAdapter) this.manager.lookup(MapSourceAdapter.ROLE);

			Object object = adapter.loadProfile(key, map);
			this.prepareObject(object, factory);

			return object;
		} finally {
			this.manager.release(adapter);
		}
	}

	private boolean isSourceNotFoundException(Throwable t) {
		while (t != null) {
			if (t instanceof SourceNotFoundException) {
                return true;
			}
            t = ExceptionUtils.getCause(t);
		}
		return false;
	}
	
	/**
	 * Prepares the object by using the specified factory.
	 */
	private void prepareObject(Object object, Object factory)
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
	
    public CopletInstanceData getCopletInstanceData(String copletID) {
        PortalService service = null;
        String attribute = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

			attribute = AuthenticationProfileManager.class.getName()+"/"+service.getPortalName()+"/CopletInstanceData";
			CopletInstanceDataManager copletInstanceDataManager = (CopletInstanceDataManager)service.getAttribute(attribute);

            return copletInstanceDataManager.getCopletInstanceData(copletID);
        } catch (ComponentException e) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(service);
        }
    }

    public List getCopletInstanceData(CopletData data) {
        List coplets = new ArrayList();
        PortalService service = null;
        String attribute = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

            attribute = AuthenticationProfileManager.class.getName()+"/"+service.getPortalName()+"/CopletInstanceData";
            CopletInstanceDataManager copletInstanceDataManager = (CopletInstanceDataManager)service.getAttribute(attribute);

            Iterator iter = copletInstanceDataManager.getCopletInstanceData().values().iterator();
            while ( iter.hasNext() ) {
                CopletInstanceData current = (CopletInstanceData)iter.next();
                if ( current.getCopletData().equals(data) ) {
                    coplets.add( current );
                }
            }
            return coplets;
        } catch (ComponentException e) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(service);
        }
    }

    public void register(CopletInstanceData coplet) {
        PortalService service = null;
        String attribute = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

            attribute = AuthenticationProfileManager.class.getName()+"/"+service.getPortalName()+"/CopletInstanceData";
            CopletInstanceDataManager copletInstanceDataManager = (CopletInstanceDataManager)service.getAttribute(attribute);
            
            copletInstanceDataManager.putCopletInstanceData( coplet );
            
        } catch (ComponentException e) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(service);
        }
    }
    
    public void unregister(CopletInstanceData coplet) {
        PortalService service = null;
        String attribute = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

            attribute = AuthenticationProfileManager.class.getName()+"/"+service.getPortalName()+"/CopletInstanceData";
            CopletInstanceDataManager copletInstanceDataManager = (CopletInstanceDataManager)service.getAttribute(attribute);
            
            copletInstanceDataManager.getCopletInstanceData().remove(coplet.getId());
            
        } catch (ComponentException e) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(service);
        }
    }

    public void register(Layout layout) {
        PortalService service = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            String portalPrefix = AuthenticationProfileManager.class.getName()+"/"+service.getPortalName();

            Map layoutMap = (Map)service.getAttribute("layout-map");
            if ( layoutMap == null ) {
                layout = (Layout)service.getAttribute(portalPrefix+"/Layout");
                if (layout != null) {
                    layoutMap = new HashMap();
                    this.cacheLayouts(layoutMap, layout);
                    service.setAttribute("layout-map", layoutMap);
                }
            }
            
            if ( layoutMap != null) {
                layoutMap.put(layout.getId(), layout);
            }
            
        } catch (ComponentException e) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(service);
        }
    }
    
    public void unregister(Layout layout) {
        PortalService service = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            String portalPrefix = AuthenticationProfileManager.class.getName()+"/"+service.getPortalName();

            Map layoutMap = (Map)service.getAttribute("layout-map");
            
            if ( layoutMap != null) {
                layoutMap.remove(layout.getId());
            }
            
        } catch (ComponentException e) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
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
    
    class ReadWriteLock {
    	private Thread activeWriter = null;
    	private HashSet activeReaders = new HashSet();
    	private int waitingWriters = 0;
    	
    	public void readLock() 
    	throws InterruptedException {
    		synchronized (ReadWriteLock.this) {
				while (this.activeWriter != null || this.waitingWriters != 0) {
					ReadWriteLock.this.wait();
				}
				this.activeReaders.add(Thread.currentThread());
    		}
    	}
    	
    	public void writeLock()
    	throws InterruptedException {
    		synchronized (ReadWriteLock.this) {
				Thread current = Thread.currentThread();

				if (this.activeWriter != current) {
					this.waitingWriters++;
					while (this.activeWriter != null || this.activeReaders.size() != 0) {
						ReadWriteLock.this.wait();
					}
					this.waitingWriters--;
					this.activeWriter = current;
				}
    		}
    	}
    	
		public void releaseReadLock() {
			synchronized (ReadWriteLock.this) {
				Thread current = Thread.currentThread();

				this.activeReaders.remove(current);
				if (this.activeReaders.size() == 0 && this.waitingWriters > 0) {
					ReadWriteLock.this.notifyAll();
				}
			}
		}

    	public void releaseLocks() {
			synchronized (ReadWriteLock.this) {
				Thread current = Thread.currentThread();
				boolean notify = false;
				
	    		if (this.activeWriter == current) {
	    			this.activeWriter = null;
	    			notify = true;
	    		} 

				this.activeReaders.remove(current);
				if (this.activeReaders.size() == 0 && this.waitingWriters > 0) {
					notify = true;
				}

				if (notify) {
					ReadWriteLock.this.notifyAll();
				}
			}
       	}
    }
}