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

import org.apache.cocoon.portal.layout.Parameters;

/**
 * Field handler for parameters.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id: ParameterFieldHandler.java,v 1.6 2004/03/05 13:02:17 bdelacretaz Exp $
 */
public class ParameterFieldHandler extends AbstractFieldHandler {
    
    public Object getValue(Object object) {
        HashMap map = new HashMap();
        Iterator iterator =
            ((Parameters) object).getParameters().entrySet().iterator();
        Map.Entry entry;
        Object key;
        while (iterator.hasNext()) {
            entry = (Map.Entry) iterator.next();
            key = entry.getKey();
            map.put(key, new AttributedMapItem(key, entry.getValue()));
        }
        return map;
    }

    public Object newInstance(Object parent) {
        return new AttributedMapItem();
    }

    public void resetValue(Object object) {
        ((Parameters) object).getParameters().clear();
    }

    public void setValue(Object object, Object value) {
        AttributedMapItem item = (AttributedMapItem) value;
        ((Parameters) object).getParameters().put(
            item.getKey(),
            item.getValue());
    }
}
