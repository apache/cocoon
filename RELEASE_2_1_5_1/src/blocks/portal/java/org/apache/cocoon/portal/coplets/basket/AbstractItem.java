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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This is a possible base class for item implementations.
 * 
 * It just adds attributes (or meta-data) functionality
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: AbstractItem.java,v 1.2 2004/03/05 13:02:11 bdelacretaz Exp $
 */
public class AbstractItem implements Serializable {
    
    /** The attributes */
    protected Map attributes = new HashMap();
    
    /** Return an attribute or null */
    public Object getAttribute(String name) { return this.attributes.get(name); }
    
    /** Set an attribute */
    public void setAttribute(String name, Object value) { this.attributes.put(name, value); }
    
    /** Get all attribute names */
    public Iterator getAttributeNames() { return this.attributes.keySet().iterator(); }
    
    /** Remove one attribute */
    public void removeAttribute(String name) { this.attributes.remove(name); }
    
    /** Check if an attribute is available */
    public boolean hasAttribute(String name) { return this.attributes.containsKey(name); }
}
