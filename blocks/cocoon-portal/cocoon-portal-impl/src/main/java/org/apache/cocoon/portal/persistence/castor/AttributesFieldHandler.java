/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.persistence.castor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.portal.coplet.CopletDefinition;
import org.apache.cocoon.portal.coplet.CopletInstance;
import org.exolab.castor.mapping.AbstractFieldHandler;
import org.exolab.castor.mapping.MapItem;

/**
 * Field handler for attributes of a CopletDefinition and a CopletInstanceData object.
 *
 * @version $Id$
 */
public class AttributesFieldHandler extends AbstractFieldHandler {

    protected Map getAttributes(Object object) {
        if (object instanceof CopletDefinition) {
            return ((CopletDefinition) object).getAttributes();
        }
        return ((CopletInstance) object).getAttributes();
    }

    /**
     * @see org.exolab.castor.mapping.FieldHandler#getValue(java.lang.Object)
     */
    public Object getValue(Object object) {
        HashMap map = new HashMap();
        Iterator iterator = this.getAttributes(object).entrySet().iterator();
        Map.Entry entry;
        Object key;
        while (iterator.hasNext()) {
            entry = (Map.Entry) iterator.next();
            key = entry.getKey();
            map.put(key, new MapItem(key, entry.getValue()));
        }
        return map;
    }

    /**
     * @see org.exolab.castor.mapping.FieldHandler#newInstance(java.lang.Object)
     */
    public Object newInstance(Object parent) {
        return new MapItem();
    }

    /**
     * @see org.exolab.castor.mapping.ExtendedFieldHandler#newInstance(java.lang.Object, java.lang.Object[])
     */
    public Object newInstance(Object arg0, Object[] arg1) {
        if ( arg1 == null ) {
            return this.newInstance(arg0);
        }
        throw new IllegalStateException("Constructor is not supported.");
    }

    /**
     * @see org.exolab.castor.mapping.FieldHandler#resetValue(java.lang.Object)
     */
    public void resetValue(Object object) {
        this.getAttributes(object).clear();
    }

    /**
     * @see org.exolab.castor.mapping.FieldHandler#setValue(java.lang.Object, java.lang.Object)
     */
    public void setValue(Object object, Object value) {
        MapItem item = (MapItem) value;
        this.getAttributes(object).put(item.getKey(), item.getValue());
    }
}
