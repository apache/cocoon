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
package org.apache.cocoon.portal.util;

import java.util.Collections;
import java.util.Map;

/**
 * Field handler superclass for external references.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id$
 */
public abstract class ReferenceFieldHandler extends AbstractFieldHandler {
    /**
     * Used to pass resolvable objects to the field handler.
     */
    private static ThreadLocal threadLocalMap = new ThreadLocal();

    /**
     * Gets the map used to pass resolvable objects to the field handler.
     */
    public static Map getObjectMap() {
        Map map = (Map) threadLocalMap.get();
        if (map == null) {
            map = Collections.EMPTY_MAP;
        }
        return map;
    }

    /**
     * Sets the map used to pass resolvable objects to the field handler.
     */
    public static void setObjectMap(Map objectMap) {
        threadLocalMap.set(objectMap);
    }

    public static void clearObjectMap() {
        threadLocalMap.set(null);
    }
}
