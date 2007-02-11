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

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.cocoon.portal.coplet.CopletBaseData;
import org.apache.cocoon.portal.profile.impl.CopletBaseDataManager;

/**
 * Field handler for CopletBaseData instances.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id: CopletBaseDataFieldHandler.java,v 1.5 2004/03/05 13:02:17 bdelacretaz Exp $
 */
public class CopletBaseDataFieldHandler extends AbstractFieldHandler {

    public Object getValue(Object object) {
        Map map = ((CopletBaseDataManager) object).getCopletBaseData();
        Vector result = new Vector(map.size());

        Iterator iterator = map.values().iterator();
        while (iterator.hasNext())
            result.addElement(iterator.next());

        return result;
    }

    public Object newInstance(Object parent) {
        return new CopletBaseData();
    }

    public void resetValue(Object object) {
        ((CopletBaseDataManager) object).getCopletBaseData().clear();
    }

    public void setValue(Object object, Object value) {
        CopletBaseData data = (CopletBaseData) value;
        ((CopletBaseDataManager) object).getCopletBaseData().put(
            data.getId(),
            data);
    }
}
