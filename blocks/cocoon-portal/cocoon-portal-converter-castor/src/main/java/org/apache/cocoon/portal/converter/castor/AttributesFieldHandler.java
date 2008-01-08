/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.converter.castor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.portal.om.CopletDefinition;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.LayoutInstance;
import org.exolab.castor.mapping.AbstractFieldHandler;
import org.exolab.castor.mapping.MapItem;

/**
 * Field handler for attributes of a CopletDefinition and a CopletInstanceData object.
 *
 * @version $Id$
 */
public class AttributesFieldHandler extends AbstractFieldHandler {

    protected Iterator getAttributesIterator(Object object) {
        if (object instanceof CopletDefinition) {
            return ((CopletDefinition) object).getAttributes().entrySet().iterator();
        } else if ( object instanceof LayoutInstance ) {
            return ((LayoutInstance) object).getAttributes().entrySet().iterator();
        } else {
            return ((CopletInstance) object).getAttributes().entrySet().iterator();
        }        
    }
    /**
     * @see org.exolab.castor.mapping.FieldHandler#getValue(java.lang.Object)
     */
    public Object getValue(Object object) {
        final Map map = new HashMap();
        final Iterator iterator = this.getAttributesIterator(object);
        while (iterator.hasNext()) {
            final Map.Entry entry = (Map.Entry) iterator.next();
            final Object key = entry.getKey();
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
        final Iterator iterator = this.getAttributesIterator(object);
        while ( iterator.hasNext() ) {
            final String key = (String)iterator.next();
            if ( object instanceof CopletDefinition ) {
                ((CopletDefinition)object).removeAttribute(key);
            } else if ( object instanceof LayoutInstance ) {
                ((LayoutInstance)object).removeAttribute(key);
            } else {
                ((CopletInstance)object).removeAttribute(key);
            }
        }
    }

    /**
     * @see org.exolab.castor.mapping.FieldHandler#setValue(java.lang.Object, java.lang.Object)
     */
    public void setValue(Object object, Object value) {
        final MapItem item = (MapItem) value;
        final String key = item.getKey().toString();
        if (object instanceof CopletDefinition) {
            ((CopletDefinition)object).setAttribute(key, item.getValue());
        } else if ( object instanceof LayoutInstance ) {
            ((LayoutInstance)object).setAttribute(key, item.getValue());
        } else {
            ((CopletInstance)object).setAttribute(key, item.getValue());
        }
    }
}
