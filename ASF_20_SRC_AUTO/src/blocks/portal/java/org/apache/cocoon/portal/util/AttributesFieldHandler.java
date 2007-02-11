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
package org.apache.cocoon.portal.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.exolab.castor.mapping.MapItem;

/**
 * Field handler for attributes of a CopletData object.
 *
 * FIXME This is a little bit hacky and should be changed by using
 * reflection
 * 
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id: AttributesFieldHandler.java,v 1.7 2004/03/05 13:02:17 bdelacretaz Exp $
 */
public class AttributesFieldHandler extends AbstractFieldHandler {

    protected Map getAttributes(Object object) {
        if (object instanceof CopletData) {
            return ((CopletData) object).getAttributes();
        } else {
            return ((CopletInstanceData) object).getAttributes();
        }
    }

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

    public Object newInstance(Object parent) {
        return new MapItem();
    }

    public void resetValue(Object object) {
        this.getAttributes(object).clear();
    }

    public void setValue(Object object, Object value) {
        MapItem item = (MapItem) value;
        this.getAttributes(object).put(item.getKey(), item.getValue());
    }
}
