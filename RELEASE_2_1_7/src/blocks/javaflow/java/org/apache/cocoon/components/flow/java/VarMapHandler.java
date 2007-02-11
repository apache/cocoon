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
package org.apache.cocoon.components.flow.java;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.jxpath.DynamicPropertyHandler;

/**
 * JXPath handler for VarMap.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: VarMapHandler.java,v 1.1 2004/03/29 17:47:21 stephan Exp $
 */
public class VarMapHandler implements DynamicPropertyHandler {

    public String[] getPropertyNames(Object object){

        Map map = ((VarMap)object).getMap();
        ArrayList list = new ArrayList();
        for(Iterator i=map.keySet().iterator(); i.hasNext();)
            list.add(i.next());
        String[] array = new String[list.size()];
        list.toArray(array);
        return array;
    }

    public Object getProperty(Object object, String property){

        Map map = ((VarMap)object).getMap();
        return map.get(property);
    }

    public void setProperty(Object object, String property, Object value){

        Map map = ((VarMap)object).getMap();
        map.put(property, value);
    }
}
