/*

 ============================================================================
				   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.ReferenceMap;
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
 * @version CVS $Id: SoftSourceCache.java,v 1.2 2003/12/12 02:01:16 crossley Exp $
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
