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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutFactory;
import org.apache.cocoon.portal.profile.ProfileLS;
import org.apache.cocoon.portal.util.DeltaApplicableReferencesAdjustable;
import org.apache.cocoon.webapps.authentication.AuthenticationManager;
import org.apache.cocoon.webapps.authentication.user.RequestState;
import org.apache.cocoon.webapps.authentication.user.UserHandler;
import org.apache.commons.collections.SequencedHashMap;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;

/**
 * The profile manager using the authentication framework
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id: AuthenticationProfileManager.java,v 1.10 2003/08/04 03:06:30 joerg Exp $
 */
public class AuthenticationProfileManager 
    extends AbstractUserProfileManager { 

    protected ReadWriteLock lock = new ReadWriteLock();
    
    protected Map attributes = new HashMap();
    
    /**
     * Get the current authentication state of the user
     * @return the current authentication state of the user
     */
    protected RequestState getRequestState() {
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
     * This loads a new profile
     */
    protected Layout loadProfile(String layoutKey, 
                                PortalService service,
                                CopletFactory copletFactory,
                                LayoutFactory layoutFactory,
                                ComponentSelector adapterSelector) 
    throws Exception {
        final RequestState state = this.getRequestState();
        final UserHandler handler = state.getHandler();
        final Configuration config = state.getApplicationConfiguration().getConfiguration("portal").getChild("profiles");

        HashMap parameters = new HashMap();
        parameters.put("config", config);
        parameters.put("handler", handler);
        CopletDataManager copletDataManager = null;
        try {
            this.lock.readLock();

            // load coplet base data
            parameters.put("profiletype", "copletbasedata");
            parameters.put("objectmap", null);

            Object[] result = this.getProfile(layoutKey, parameters, null, false, service);
            CopletBaseDataManager copletBaseDataManager = (CopletBaseDataManager)result[0];
                    
            // load coplet data
            parameters.put("profiletype", "copletdata");
            parameters.put("objectmap", copletBaseDataManager.getCopletBaseData());
            copletDataManager = (CopletDataManager)this.getDeltaProfile(layoutKey, parameters, service, copletFactory, ((Boolean)result[1]).booleanValue());
                    
        } finally {
            this.lock.releaseLocks();
        }
        // load coplet instance data
        parameters.put("profiletype", "copletinstancedata");
        parameters.put("objectmap", copletDataManager.getCopletData());
        CopletInstanceDataManager copletInstanceDataManager = (CopletInstanceDataManager)this.getOrCreateProfile(layoutKey, parameters, service, copletFactory);
        service.setAttribute("CopletInstanceData:" + layoutKey, copletInstanceDataManager);
                
        // load layout
        parameters.put("profiletype", "layout");
        parameters.put("objectmap", copletInstanceDataManager.getCopletInstanceData());
        Layout layout = (Layout)this.getOrCreateProfile(layoutKey, parameters, service, layoutFactory);
        service.setAttribute("Layout:" + layoutKey, layout);
                
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
        
        return layout;
    }
    
    public void saveUserProfiles() {
        final String layoutKey = this.getDefaultLayoutKey();
		ProfileLS adapter = null;
		PortalService service = null;
		try {
			adapter = (ProfileLS) this.manager.lookup(ProfileLS.ROLE);
			service = (PortalService) this.manager.lookup(PortalService.ROLE);
            
            RequestState state = this.getRequestState();
            UserHandler handler = state.getHandler();

			HashMap parameters = new HashMap();
			parameters.put("type", "user");
            parameters.put("config", state.getApplicationConfiguration().getConfiguration("portal"));
            parameters.put("handler", handler);
            parameters.put("profiletype", "copletinstancedata");

			Map key = this.buildKey(service, parameters, layoutKey, false);
	
			// save coplet instance data
			Object profile = ((Object[])service.getAttribute("CopletInstanceData:" + layoutKey))[0];
			adapter.saveProfile(key, parameters, profile);

			// save coplet instance data
			parameters.put("profiletype", "layout");
            key = this.buildKey(service, parameters, layoutKey, false);
			profile = ((Object[])service.getAttribute("Layout:" + layoutKey))[0];
			adapter.saveProfile(key, parameters, profile);
            
		} catch (Exception e) {
			// TODO
			throw new CascadingRuntimeException("Exception during save profile", e);
		} finally {
			this.manager.release((Component)adapter);
			this.manager.release(service);
		}
    }
    
	/**
	 * Gets a profile and applies possible user and role deltas to it.
	 */
    protected Object getDeltaProfile(String layoutKey, 
                                    Map parameters, 
                                    PortalService service, 
                                    Object factory, 
                                    boolean forcedLoad) 
	throws Exception {
		DeltaApplicableReferencesAdjustable result;
		Object object;

        parameters.put("type", "global");
		Object global = this.getProfile(layoutKey, parameters, factory, forcedLoad, service)[0];
        Object key = this.buildKey(service, parameters, layoutKey, true);
		result = (DeltaApplicableReferencesAdjustable)this.loadProfile(key, parameters, factory);
	
		// load role delta
        parameters.put("type", "role");
		try {
			object = this.getProfile(layoutKey, parameters, factory, forcedLoad, service)[0];
			if (object != null)
				result.applyDelta(object); 		
		} catch (Exception e) {
			if (!isSourceNotFoundException(e))
				throw e;
		}

		// load user delta
        parameters.put("type", "user");
		try {
            key = this.buildKey(service, parameters, layoutKey, true);
			object = this.loadProfile(key, parameters, factory);
			if (object != null)
				result.applyDelta(object);
		} catch (Exception e) {
			if (!isSourceNotFoundException(e))
				throw e;
		}
		
		if (result == null)
			throw new SourceNotFoundException("Global profile does not exist.");
		
		// change references to objects where no delta has been applied
		result.adjustReferences(global);
		
        // FIXME
		this.attributes.put(key, result);
		
		return result;
	}

	/**
	 * Gets a user profile and creates it by copying the role or the global profile.
	 */
    protected Object getOrCreateProfile(String layoutKey, Map parameters, PortalService service, Object factory) 
	throws Exception {
		Object result;
    	
		// load user profile
		parameters.put("type", "user");
        Map keyMap = this.buildKey(service, parameters, layoutKey, true);
		try {
			result = this.loadProfile(keyMap, parameters, factory);
		} catch (Exception e1) {
			if (!isSourceNotFoundException(e1))
				throw e1;

			// load role profile
			parameters.put("type", "role");
            keyMap = this.buildKey(service, parameters, layoutKey, true);
			try {
				result = this.loadProfile(keyMap, parameters, factory);
			} catch (Exception e2) {
				if (!isSourceNotFoundException(e2))
					throw e2;

				// load global profile
				parameters.put("type", "global");
                keyMap = this.buildKey(service, parameters, layoutKey, true);
				result = this.loadProfile(keyMap, parameters, factory);
			}
			
			// save profile as user profile
			ProfileLS adapter = null;
			try {
				adapter = (ProfileLS) this.manager.lookup(ProfileLS.ROLE);
                parameters.put("type", "user");
                keyMap = this.buildKey(service, parameters, layoutKey, false);
				
                //adapter.saveProfile(keyMap, parameters, result);
			} finally {
				this.manager.release((Component)adapter);
			}
		}
		
        // FIXME
        this.attributes.put(keyMap, result);

		return result;
	}

	/**
	 * Gets a profile.
	 * @return result[0] is the profile, result[1] is a Boolean, 
	 * which signals whether the profile has been loaded or reused.
	 */
    protected Object[] getProfile(String layoutKey, 
                                 Map parameters, 
                                 Object factory, 
                                 boolean forcedLoad, 
                                 PortalService service) 
	throws Exception {
        final Map key = this.buildKey(service, parameters, layoutKey, true);

        ProfileLS adapter = null;
		try {
			adapter = (ProfileLS)this.manager.lookup(ProfileLS.ROLE);

			Object result = this.checkValidity(key, parameters, forcedLoad, adapter, service);
            
			if (!(result instanceof SourceValidity))
				return new Object[]{result, Boolean.FALSE};
			SourceValidity newValidity = (SourceValidity)result; 

			this.lock.releaseReadLock();
			this.lock.writeLock();
			
			// check validity again in case of another thread has already loaded
			result = this.checkValidity(key, parameters, forcedLoad, adapter, service);
            
			if (!(result instanceof SourceValidity))
				return new Object[]{result, Boolean.FALSE};
			newValidity = (SourceValidity)result; 

			Object object = adapter.loadProfile(key, parameters);
			this.prepareObject(object, factory);
			if (newValidity != null) {
                this.attributes.put(key, new Object[] {object, newValidity});
			}

			return new Object[]{object, Boolean.TRUE};
		} finally {
			this.manager.release((Component) adapter);
		}
	}
	
	/**
	 * If the profile is valid itself is returned, otherwise a newly created SourceValidity object is returned.
	 */
    protected Object checkValidity(Object key, 
                                  Map parameters, 
                                  boolean forcedLoad, 
                                  ProfileLS adapter, 
                                  PortalService service) {
		Object[] objects = (Object[])this.attributes.get(key);

		SourceValidity sourceValidity = null;
		int valid = SourceValidity.INVALID;
		if (objects != null) {
			sourceValidity = (SourceValidity) objects[1];
			valid = sourceValidity.isValid();
			if (!forcedLoad && valid == SourceValidity.VALID)
				return objects[0];
		}

		SourceValidity newValidity = adapter.getValidity(key, parameters);
		if (!forcedLoad && valid == SourceValidity.UNKNOWN) {
			if (sourceValidity.isValid(newValidity) == SourceValidity.VALID)
				return objects[0];
		}
		
		return newValidity;
	}

	/**
	 * Loads a profile.
	 */
    protected Object loadProfile(Object key, Map map, Object factory)
	throws Exception {
        ProfileLS adapter = null;
		try {
			adapter = (ProfileLS) this.manager.lookup(ProfileLS.ROLE);

			Object object = adapter.loadProfile(key, map);
			this.prepareObject(object, factory);

			return object;
		} finally {
			this.manager.release((Component) adapter);
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
	
    protected Map buildKey(PortalService service, 
                            Map           parameters,
                            String        layoutKey, 
                            boolean      load) 
    throws ProcessingException, ConfigurationException {
        
        // TODO Change to KeyManager usage
        final String type = (String)parameters.get("type");
        final Configuration config = (Configuration) parameters.get("config");
        final String profileType = (String)parameters.get("profiletype");
        final String postFix = (load ? "load" : "save");
        final UserHandler handler = (UserHandler)parameters.get("handler");
        
        String uri = null;
        if (type == null) {
            uri = config.getChild(profileType + "-" + postFix).getAttribute("uri");
        } else if (type.equals("global")) {
            uri = config.getChild(profileType + "-global-" + postFix).getAttribute("uri");
        } else if (type.equals("role")) {
            uri = config.getChild(profileType + "-role-" + postFix).getAttribute("uri");
        } else if (type.equals("user")) {
            uri = config.getChild(profileType + "-user-" + postFix).getAttribute("uri");
        }

        Map key = new SequencedHashMap();
        key.put("baseuri", uri);
        key.put("separator", "?");
        key.put("portal", service.getPortalName());
        key.put("layout", layoutKey);
        if ( type != null ) {
            key.put("type", type);
            if ( "role".equals(type) || "user".equals(type)) {
                key.put("role", handler.getContext().getContextInfo().get("role"));
            }
            if ( "user".equals(type) ) {
                key.put("user", handler.getUserId());
            }
        }
        return key;
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
