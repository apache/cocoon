/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

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

*/
package org.apache.cocoon.portal.coplets.basket;

import java.io.Serializable;

import org.apache.cocoon.portal.coplet.CopletInstanceData;


/**
 * This is an item that contains a link or a content.
 * The item can either reference a coplet or an URL.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: ContentItem.java,v 1.1 2004/02/23 14:52:49 cziegeler Exp $
 */
public class ContentItem extends AbstractItem implements Serializable {
    
    /** The id of the referenced coplet */
    protected String copletId;
    /** Do we store the content or just the link? */
    protected boolean storesContent;
    /** The referenced url */
    protected String url;
    /** The cached string rep */
    protected String stringRep;
    /** The content */
    protected byte[] content;
    
    /**
     * Create a new item referencing a coplet instance data
     * @param cid     The coplet
     * @param content Do we store the content (false: a link)
     */
    public ContentItem(CopletInstanceData cid, boolean content) {
        this.copletId = cid.getId();
        this.storesContent = content;
    }

    /**
     * Create a new item referencing to a url
     * @param url     The url
     * @param content Do we store the content (false: a link)
     */
    public ContentItem(String url, boolean content) {
        this.url = url;
        this.storesContent = content;
    }
    
    /**
     * Return the url of null for a coplet
     */
    public String getURL() {
        return this.url;
    }
    
    /**
     * Return the referenced coplet or null for a url
     */
    public String getCopletId() {
        return this.copletId;
    }
    
    /**
     * Do we store the content? (or just the link)
     */
    public boolean isContent() {
        return this.storesContent;
    }
    
    /**
     * Set the content
     */
    public void setContent(byte[] c) {
        this.storesContent = true;
        this.content = c;
    }
    
    /**
     * Get the content or null
     */
    public byte[] getContent() {
        return this.content;
    }
    
    /**
     * Return the size if content is stored
     * Otherwise -1 is returned
     */
    public int size() {
        if ( this.content != null ) {
            return this.content.length;
        }
        return -1;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if ( this.stringRep == null ) {
            if ( this.copletId != null ) {
                this.stringRep = "Coplet:" + this.copletId + "(" + this.storesContent + ")";
            } else {
                this.stringRep = "URL:" + this.url + "(" + this.storesContent + ")";
            }
        }
        return this.stringRep;
    }
    
    /**
     * Compare one item with another
     */
    public boolean equalsItem(ContentItem ci) {
        if ( ci != null && ci.storesContent == this.storesContent ) {
            if ( ci.url != null && ci.url.equals(this.url)) {
                return true;
            }
            if ( ci.copletId != null 
                 && this.copletId != null 
                 && ci.copletId.equals(this.copletId)) {
                return true;
            }
        }
        return false;
    }
}
