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

import org.apache.excalibur.source.SourceValidity;

/**
 * This is a cached response. This can either contain a byte array with
 * the complete character response or a byte array with compiled SAX events.
 *
 * This class replaces the <code>CachedEventObject</code> and the
 * <code>CachedStreamObject</code>.
 *
 * @since 2.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CachedResponse.java,v 1.1 2003/03/09 00:08:44 pier Exp $
 */
public final class CachedResponse
        implements Serializable {

    private SourceValidity[] validityObjects;
    private byte[]           response;
    private Long             expires;

    /**
     * Create a new entry for the cache.
     *
     * @param validityObjects The SourceValidity objects in the order
     *                        they occured in the pipeline
     * @param response        The cached sax stream or character stream
     */
    public CachedResponse(SourceValidity[] validityObjects,
                          byte[]           response) {
        this.validityObjects = validityObjects;
        this.response = response;
        this.expires = null;
    }

    /**
     * Create a new entry for the cache.
     *
     * @param validityObjects The SourceValidity objects in the order
     *                        they occured in the pipeline
     * @param response        The cached sax stream or character stream
     * @param expires         The configured expires, or null if no
     *                        expires was defined.
     */
    public CachedResponse(SourceValidity[] validityObjects,
                          byte[]           response,
                          Long expires) {
        this.validityObjects = validityObjects;
        this.response = response;
        this.expires = expires;
    }

    /**
     * Get the validity objects
     */
    public SourceValidity[] getValidityObjects() {
        return this.validityObjects;
    }

    /**
     * Get the cached response.
     *
     * @return The sax stream or character stream
     */
    public byte[] getResponse() {
        return this.response;
    }

    /**
     * Get the configured expires.
     *
     * @return The configured expires, or null if no expires was defined
     */
    public Long getExpires() {
        return this.expires;
    }
    
    /**
     * Set the (newly) configured expires.
     * 
     */
    public void setExpires(Long newExpires) {
        this.expires = newExpires;    
    }
}
