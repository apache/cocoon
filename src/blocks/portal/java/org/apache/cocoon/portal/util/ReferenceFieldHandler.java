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
import java.util.Map;

/**
 * Field handler superclass for external references.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id: ReferenceFieldHandler.java,v 1.5 2004/03/05 13:02:17 bdelacretaz Exp $
 */
public abstract class ReferenceFieldHandler extends AbstractFieldHandler {
    /**
     * Used to pass resolvable objects to the field handler.
     */
    private static ThreadLocal threadLocalMap = new InheritableThreadLocal();

    /**
     * Gets the map used to pass resolvable objects to the field handler.
     */
    public static Map getObjectMap() {
        Map map = (Map) threadLocalMap.get();

        if (map == null) {
            map = new HashMap();
            threadLocalMap.set(map);
        }

        return map;
    }

    /**
     * Sets the map used to pass resolvable objects to the field handler.
     */
    public static void setObjectMap(Map objectMap) {
        if (objectMap == null) {
            threadLocalMap.set(new HashMap());
        } else {
            threadLocalMap.set(objectMap);
        }
    }
}
