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

import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.profile.impl.CopletInstanceDataManager;

/**
 * Field handler for CopletInstanceData instances.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id: CopletInstanceDataFieldHandler.java,v 1.5 2004/03/05 13:02:17 bdelacretaz Exp $
 */
public class CopletInstanceDataFieldHandler extends AbstractFieldHandler {

    public Object getValue(Object object) {
        Map map = ((CopletInstanceDataManager) object).getCopletInstanceData();
        Vector result = new Vector(map.size());

        Iterator iterator = map.values().iterator();
        while (iterator.hasNext())
            result.addElement(iterator.next());

        return result;
    }

    public Object newInstance(Object parent) {
        return new CopletInstanceData();
    }

    public void resetValue(Object object) {
        ((CopletInstanceDataManager) object).getCopletInstanceData().clear();
    }

    public void setValue(Object object, Object value) {
        CopletInstanceData data = (CopletInstanceData) value;
        ((CopletInstanceDataManager) object).getCopletInstanceData().put(
            data.getId(),
            data);
    }
}
