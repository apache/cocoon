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

import org.apache.cocoon.portal.coplets.basket.ContentStore;
import org.apache.cocoon.portal.event.Event;

/**
 * This is the base class for all content store events
 *
 * @version CVS $Id: BasketEvent.java 30941 2004-07-29 19:56:58Z vgritsenko $
 */
public abstract class ContentStoreEvent implements Event {
    
    protected final ContentStore store;
    
    /**
     * this is a general event for a set of content stores
     */
    public ContentStoreEvent() {
        this.store = null; 
    }
    
    /**
     * This event is for a defined content store
     */
    public ContentStoreEvent(ContentStore store) {
        this.store = store;
    }
    
    public ContentStore getContentStore() {
        return this.store;
    }
}
