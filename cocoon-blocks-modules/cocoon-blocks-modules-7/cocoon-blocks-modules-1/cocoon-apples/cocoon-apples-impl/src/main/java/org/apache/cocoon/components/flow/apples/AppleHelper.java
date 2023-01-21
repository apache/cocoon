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
package org.apache.cocoon.components.flow.apples;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.components.flow.Interpreter.Argument;

/**
 * AppleHelper holds some static utility classes used in the Apples flow
 * implementation.
 */
public class AppleHelper {

    /** 
     * Translates a List of Interpreter.Argument objects into a classic 
     * {@link java.util.Map}
     */
    public static Map makeMapFromArguments(List params) {
        Map retMap = new HashMap();
        for (Iterator iter = params.iterator(); iter.hasNext();) {
            Argument element = (Argument) iter.next();
            retMap.put(element.name, element.value);
        }
        return retMap;
    }
}
