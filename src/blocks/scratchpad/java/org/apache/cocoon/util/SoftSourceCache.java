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
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;

/**
 * Cache for objects created from a source. Objects and keys
 * are held using {@link java.lang.ref.SoftReference} and are
 * thus cleanable by the garbage collector if the VM is low on
 * memory. If an object cannot be found in the cache or the 
 * source's validity has expired, the source is reread and the
 * object is recreated using the registered 
 * {@link org.apache.cocoon.util.SourceReloader}.
 * 
 * @since 2.1.4
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: SoftSourceCache.java,v 1.4 2004/03/05 10:07:26 bdelacretaz Exp $
 */
public class SoftSourceCache implements SourceCache {

	/** Object cache. */
	Map objectCache = null;
	/** Callback for object reloading. */
	SourceReloader reloader = null;

	/**
	 * Initialize this instance. Create a new cache.
	 */
	private synchronized void init() {
		if (this.objectCache == null)
			this.objectCache =
				Collections.synchronizedMap(
					new ReferenceMap(ReferenceMap.SOFT, ReferenceMap.SOFT));
	}

	/*
	 *  (non-Javadoc)
	 * @see org.apache.cocoon.util.SourceCache#register(org.apache.cocoon.util.SourceReloader)
	 */
	public void register(SourceReloader reloader) {
		this.reloader = reloader;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.apache.cocoon.util.SourceCache#getObject(org.apache.excalibur.source.SourceResolver, java.lang.String, java.lang.Object)
	 */
	public Object getObject(SourceResolver resolver, Object key, String uri, Object parameter)
		throws MalformedURLException, IOException {

		Object result = null;
		if (this.objectCache == null)
			init();
		CacheEntry cacheEntry = (CacheEntry) this.objectCache.get(key);
		if (cacheEntry != null) {
			SourceValidity previous = cacheEntry.validity;
			Source source = null;
			switch (previous.isValid()) {
				case SourceValidity.VALID:
					result = cacheEntry.object;
					break;
				case SourceValidity.UNKNOWN:
					source = resolver.resolveURI(uri);
					SourceValidity fresh = source.getValidity();
					switch (previous.isValid(fresh)){
						case SourceValidity.VALID:
							result = cacheEntry.object;
							break;
						case SourceValidity.UNKNOWN:
						case SourceValidity.INVALID:
							result = this.reloader.reload(source, parameter);
							this.objectCache.put(key, new CacheEntry(fresh, result));
							break;
					}
					resolver.release(source); 
					break;
				case SourceValidity.INVALID:
					source = resolver.resolveURI(uri);
					result = this.reloader.reload(source, parameter);
					this.objectCache.put(uri, new CacheEntry(source.getValidity(), result));
					resolver.release(source);
					break;
			}
		}
		return result;
	}

	/**
	 * Private helper to hold source validities and cached objects.
	 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
	 */
	private class CacheEntry {
		public SourceValidity validity = null;
		public Object object = null;

		public CacheEntry(SourceValidity val, Object obj) {
			this.validity = val;
			this.object = obj;
		}

	}

}
