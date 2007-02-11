/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.util;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.excalibur.source.SourceResolver;

/**
 * A source cache caches Objects that are created from a source. It handles
 * transparently rereading sources and recreation of objects if the source
 * validity has expired or the object has been cleaned from the cache. For 
 * this, a reloader callback needs to be registered that actually does the
 * recreation of the cached object.
 * 
 * <p>Example:</p>
 * <pre>
 * 
 *   public void service(ServiceManager manager) {
 *      ...
 *      // obtain source cache on startup / service / initialize
 *      SourceCache cache = (SourceCache) manager.lookup(SourceCache.ROLE);
 *      // register reloader
 *      // with anonymous class handling the callback
 *      cache.register( new SourceReloader() { 
 *                 public Object reload(Source src, Object param) {
 *                     return refresh(src, (String) param[0], (Integer) param[1] );
 *                 });
 *   }
 *  
 *   // have callback method. Private is OK because its used from a anonymous
 *   // nested class.
 *   private CreatedObject refresh(Source src, String param1, Integer param2) {
 *      ...
 *   }
 *
 * 
 *   public void foo() {
 *      ...
 *      // use cache
 *      CreatedObject foo = (CreatedObject) cache.getObject(resolver, key, uri, 
 *      ...                                                    Object[] { param1, new Integer(param2) });
 *   }
 * 
 *   public void dispose() {
 *      // release source cache on dispose
 *      manager.release(cache);
 *   }
 * 
 * </pre>
 * 
 * @since 2.1.4
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: SourceCache.java,v 1.3 2004/03/05 10:07:26 bdelacretaz Exp $
 */
public interface SourceCache {
	
	public static final String ROLE = SourceCache.class.getName();
	
	/**
	 * Register a source reloader that will recreate cached objects.
	 * Often, this will be done using an anonymous nested class.
	 *  
	 * @param reloader
	 */
	void register(SourceReloader reloader);
	
	/**
	 * Retrieve an object from the cache. Transparently reloads and 
	 * recreates objects using the registered source reloader if the
	 * objects identified by the key hasn't been cached or has been
	 * cleared from the cache, or source has changed.
	 * 
	 * @param resolver A source resolver to use.
	 * @param key An object used as a key to the cached object.
	 * @param uri A string holding the URI.
	 * @param parameter Parameters to pass to the source reloader.
	 * @return Cached object.
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	Object getObject(SourceResolver resolver, Object key, String uri, Object parameter)
		throws MalformedURLException, IOException;
}