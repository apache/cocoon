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
package org.apache.cocoon.transformation.helpers;

import java.io.IOException;
import java.io.Serializable;

import org.apache.avalon.framework.logger.Logger;
import org.apache.excalibur.store.Store;

/**
 * This is the interface between the {@link IncludeCacheManager} and the usual
 * store.
 * 
 *  @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 *  @version CVS $Id: StoreIncludeCacheStorageProxy.java,v 1.2 2003/03/11 16:33:37 vgritsenko Exp $
 *  @since   2.1
 */
public final class StoreIncludeCacheStorageProxy
    implements IncludeCacheStorageProxy {

    private Store  store;
    
    private Logger logger;
    
    /**
     * Constructor
     * @param store  The store for the cached content
     * @param logger A logger for debugging
     */
    public StoreIncludeCacheStorageProxy(Store store, Logger logger) {
        this.store = store;
        this.logger = logger;
    }
    
    /** A string representation for a key */
    private String getKey(String uri) {
        return "DCS:" + uri;
    }
    
    /**
     * @see IncludeCacheStorageProxy#get(java.lang.String)
     */
    public Serializable get(String uri) {
        if (logger.isDebugEnabled()) {
            logger.debug("StoreProxy: Getting content for " + uri);
        }

        Serializable result = (Serializable)this.store.get(this.getKey(uri));

        if (logger.isDebugEnabled()) {
            logger.debug("StoreProxy: Result for " + uri + " : " + (result == null ? "Not in cache" : "Found"));
        }
        return result;
    }

    /**
     * @see IncludeCacheStorageProxy#put(java.lang.String, java.io.Serializable)
     */
    public void put(String uri, Serializable object) 
    throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("StoreProxy: Storing content for " + uri);
        }
        this.store.store(this.getKey(uri), object);
    }

    /**
     * @see IncludeCacheStorageProxy#remove(java.lang.String)
     */
    public void remove(String uri) {
        if (logger.isDebugEnabled()) {
            logger.debug("StoreProxy: Removing content for " + uri);
        }
        this.store.remove(this.getKey(uri));
    }
}
