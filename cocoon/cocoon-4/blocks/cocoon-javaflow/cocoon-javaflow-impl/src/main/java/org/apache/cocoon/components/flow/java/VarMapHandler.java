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
package org.apache.cocoon.components.flow.java;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.jxpath.DynamicPropertyHandler;

/**
 * JXPath handler for VarMap.
 *
 * @version $Id$
 */
public final class VarMapHandler implements DynamicPropertyHandler {

    public String[] getPropertyNames(final Object object){

        Map map = ((VarMap)object).getMap();
        ArrayList list = new ArrayList();
        for(Iterator i=map.keySet().iterator(); i.hasNext();)
            list.add(i.next());
        String[] array = new String[list.size()];
        list.toArray(array);
        return array;
    }

    public Object getProperty(final Object object, final String property){

        Map map = ((VarMap)object).getMap();
        return map.get(property);
    }

    public void setProperty(final Object object, final String property, final Object value){

        Map map = ((VarMap)object).getMap();
        map.put(property, value);
    }
}
