/*
 * Copyright 2004-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.coplets.basket.events;

import java.util.List;

import org.apache.cocoon.portal.coplets.basket.ContentStore;

/**
 * This event adds uploaded files to the content store
 *
 * @version CVS $Id: UploadItemEvent.java 30941 2004-07-29 19:56:58Z vgritsenko $
 */
public class UploadItemEvent extends ContentStoreEvent {
    
    /** List of parameter names containing uploaded files */
    protected final List itemNames;
    
    /** 
     * Constructor
     * @param itemNames List of parameter names with uploaded files
     */
    public UploadItemEvent(ContentStore store, List itemNames) {
        super(store);
        this.itemNames = itemNames;
    }
    
    /**
     * Return the list of parameter names
     */
    public List getItemNames() {
        return this.itemNames;
    }
}
