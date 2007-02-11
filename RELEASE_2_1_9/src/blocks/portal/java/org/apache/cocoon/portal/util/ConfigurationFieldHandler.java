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

import org.apache.cocoon.portal.coplet.CopletBaseData;
import org.exolab.castor.mapping.MapItem;

/**
 * Field handler for attributes of a CopletBaseData object.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id: ConfigurationFieldHandler.java,v 1.5 2004/03/05 13:02:17 bdelacretaz Exp $
 */
public class ConfigurationFieldHandler extends AbstractFieldHandler {
    
    public Object getValue(Object object) {
        HashMap map = new HashMap();
        Iterator iterator =
            ((CopletBaseData) object).getCopletConfig().entrySet().iterator();
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
        ((CopletBaseData) object).getCopletConfig().clear();
    }

    public void setValue(Object object, Object value) {
        MapItem item = (MapItem) value;
        ((CopletBaseData) object).setCopletConfig(
            (String) item.getKey(),
            item.getValue());
    }
}
