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
 * @version CVS $Id: SourceCache.java,v 1.1 2003/11/09 13:33:24 haul Exp $
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
	 * @param A source resolver to use.
	 * @param An object used as a key to the cached object.
	 * @param A string holding the URI.
	 * @param Parameters to pass to the source reloader.
	 * @return Cached object.
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	Object getObject(SourceResolver resolver, Object key, String uri, Object parameter)
		throws MalformedURLException, IOException;
}