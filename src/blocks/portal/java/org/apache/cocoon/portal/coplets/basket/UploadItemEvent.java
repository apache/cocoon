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

import java.util.List;


/**
 * This event adds upload files to the basket
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: UploadItemEvent.java,v 1.2 2004/03/05 13:02:11 bdelacretaz Exp $
 */
public class UploadItemEvent extends BasketEvent {
    
    /** List of parameter names containing uploaded files */
    protected List itemNames;
    
    /** 
     * Constructor
     * @param itemNames List of parameter names with uploaded files
     */
    public UploadItemEvent(List itemNames) {
        this.itemNames = itemNames;
    }
    
    /**
     * Return the list of parameter names
     */
    public List getItemNames() {
        return this.itemNames;
    }
}
