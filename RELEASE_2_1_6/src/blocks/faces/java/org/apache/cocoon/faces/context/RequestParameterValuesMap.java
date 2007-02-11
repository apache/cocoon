/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.faces.context;

import org.apache.cocoon.environment.Request;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Request parameter values map
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id$
 */
class RequestParameterValuesMap extends BaseMap {

    private Request request;


    RequestParameterValuesMap(Request request) {
        this.request = request;
    }

    public Object get(Object key) {
        return request.getParameterValues(key.toString());
    }

    public Set entrySet() {
        Set entries = new HashSet();
        for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            entries.add(new BaseMap.Entry(name, request.getParameterValues(name)));
        }

        return entries;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof RequestParameterValuesMap)) {
            return false;
        }

        return super.equals(obj);
    }
}
