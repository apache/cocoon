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
import java.util.ArrayList;
import java.util.List;

/**
 * This is the cache key for one pipeline (or the first part of a pipeline).
 * It consists of one or more {@link ComponentCacheKey}s.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: PipelineCacheKey.java,v 1.1 2003/03/09 00:08:45 pier Exp $
 */
public final class PipelineCacheKey
        implements Serializable {

    /** The keys */
    private List keys;

    /** the hash code */
    private int hashCode = 0;

    /**
     * Constructor
     */
    public PipelineCacheKey() {
        this.keys = new ArrayList(6);
    }

    /**
     * Constructor
     */
    public PipelineCacheKey(int size) {
        this.keys = new ArrayList(size);
    }

    /**
     * Add a key
     */
    public void addKey(ComponentCacheKey key) {
        this.keys.add(key);
        this.hashCode = 0;
        this.toString = null;
    }

    /**
     * Remove the last key
     */
    public void removeLastKey() {
        this.keys.remove(this.keys.size()-1);
        this.hashCode = 0;
        this.toString = null;
    }

    /**
     * Remove unitl cachepoint (including cachePoint) 
     */
    public void removeUntilCachePoint() {
        this.hashCode = 0;
        this.toString = null;
        int keyCount = this.keys.size();

        while (keyCount > 0) {
            if (((ComponentCacheKey)this.keys.get(keyCount-1)).isCachePoint()) {
                this.keys.remove(keyCount-1);
                return;
            }
            this.keys.remove(keyCount-1);
            keyCount--;
        }
    }

    /**
     * Return the number of keys
     */
    public int size() {
        return this.keys.size();
    }

    /**
     * Compare
     */
    public boolean equals(Object object) {
        if (object instanceof PipelineCacheKey) {
            PipelineCacheKey pck = (PipelineCacheKey)object;
            final int len = this.keys.size();
            if (pck.keys.size() == len) {
                boolean cont = true;
                int i = 0;
                while (i < len && cont) {
                    cont = this.keys.get(i).equals(pck.keys.get(i));
                    i++;
                }
                return cont;
            }
        }
        return false;
    }

    /**
     * Generate a hash code
     */
    public int hashCode() {
        // FIXME - this is not very safe
        if (this.hashCode == 0) {
            final int len = this.keys.size();
            for(int i=0; i < len; i++) {
                this.hashCode += this.keys.get(i).hashCode();
            }
            if (len % 2 == 0) this.hashCode++;
        }
        return this.hashCode;
    }

    /**
     * Clone the object (but not the component keys)
     */
    public PipelineCacheKey copy() {
        final int len = this.keys.size();
        PipelineCacheKey pck = new PipelineCacheKey(len);
        for(int i=0; i < len; i++) {
            pck.keys.add(this.keys.get(i));
        }
        return pck;
    }

    private String toString;

    /**
     * toString
     * The FilesystemStore uses toString!
     */
    public String toString() {
        if (this.toString == null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("PK");
            final int len = this.keys.size();
            for(int i=0; i < len; i++) {
                buffer.append('_').append(this.keys.get(i).toString());
            }
            this.toString = buffer.toString();
        }
        return toString;
    }
}
