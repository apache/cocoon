/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.coplets.basket;

import java.io.Serializable;

import org.apache.cocoon.portal.coplet.CopletInstanceData;


/**
 * This is an item that contains a link or a content.
 * The item can either reference a coplet or an URL.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: ContentItem.java,v 1.2 2004/03/05 13:02:11 bdelacretaz Exp $
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
