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
package org.apache.cocoon.caching;

import java.io.Serializable;

/**
 * This is the cache key for one pipeline (or the first part of a pipeline).
 * It consists of one or more {@link ComponentCacheKey}s.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: SimpleCacheKey.java,v 1.1 2003/09/01 09:20:59 cziegeler Exp $
 * @since 2.1.1
 */
public class SimpleCacheKey
        implements Serializable {

    /** The key */
    final protected String key;

    /** the hash code */
    final protected boolean complete;

    /** cache key */
    final protected String cacheKey;
    
    /** cache toString() */
    protected String toString;
    
    /**
     * Constructor
     */
    public SimpleCacheKey(String key, boolean complete) {
        this.key = key;
        this.complete = complete;
        final StringBuffer buf = new StringBuffer();
        buf.append(complete).append(':').append(this.key);
        this.cacheKey = buf.toString();
    }

    /**
     * Compare
     */
    public boolean equals(Object object) {
        if (object instanceof SimpleCacheKey) {
            SimpleCacheKey pck = (SimpleCacheKey)object;
            return ( this.cacheKey.equals( pck.cacheKey ) );
        }
        return false;
    }

    /**
     * Generate a hash code
     */
    public int hashCode() {
        return this.cacheKey.hashCode();
    }

    /**
     * toString
     * The FilesystemStore uses toString!
     */
    public String toString() {
        if (this.toString == null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("SCK:");
            buffer.append(this.cacheKey);
            this.toString = buffer.toString();
        }
        return toString;
    }
    
    /**
     * The cache key
     */
    public String getKey() {
        return this.key;
    }
}
