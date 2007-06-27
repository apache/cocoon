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
package org.apache.cocoon.environment.impl;

import org.apache.cocoon.environment.Request;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Request headers map
 *
 * @version $Id$
 */
public class RequestHeaderMap extends BaseMap {

    private Request request;


    public RequestHeaderMap(Request request) {
        this.request = request;
    }

    public Object get(Object key) {
        return request.getHeader(key.toString());
    }

    public Set entrySet() {
        Set entries = new HashSet();
        for (Enumeration e = request.getHeaderNames(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            entries.add(new BaseMap.Entry(name, request.getHeader(name)));
        }

        return entries;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof RequestHeaderMap)) {
            return false;
        }

        return super.equals(obj);
    }
}
