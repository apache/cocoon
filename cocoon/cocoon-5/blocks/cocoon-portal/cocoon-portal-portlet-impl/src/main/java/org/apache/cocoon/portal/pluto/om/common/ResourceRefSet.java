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
package org.apache.cocoon.portal.pluto.om.common;

import java.util.Iterator;

/**
 * Resource Reference Set as defined by the web.xml as
 * &lt;resourse-ref&gt;.
 *
 * @version $Id$
 */
public class ResourceRefSet
    extends AbstractSupportSet
    implements java.io.Serializable, Support {

    /**
     *  Retrieve the Resource Reference
     */
    public ResourceRef get(String name) {
        Iterator it = this.iterator();
        while(it.hasNext()) {
            ResourceRef ref = (ResourceRef)it.next();
            if(ref.getName().equals(name)) {
                return ref;
            }
        }
        return null;
    }

    public ResourceRef add(ResourceRef ref) {
        this.add((Object)ref);
        return ref;
    }

    public ResourceRef remove(ResourceRef ref) {
        Iterator it = this.iterator();
        while(it.hasNext()) {
            ResourceRef internal = (ResourceRef)it.next();
            if(internal.equals(ref)) {
                it.remove();
                return internal;
            }
        }
        return null;
    }
}
