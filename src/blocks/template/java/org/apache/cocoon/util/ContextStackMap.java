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
package org.apache.cocoon.util;

import java.util.Map;
import java.util.Stack;

import org.apache.commons.collections.map.HashedMap;

public final class ContextStackMap extends HashedMap {

    Stack bkpStack = new Stack();

    Map bkp;

    public void clear() {
        super.clear();
        bkpStack.clear();
        bkp = null;
    }

    public Object put(Object key, Object value) {
        if (bkp == null || bkp.containsKey(key)) {
            return super.put(key, value);
        } else {
            Object previous = super.put(key, value);
            bkp.put(key, previous);
            return previous;
        }
    }

    public void putAll(Map map) {
        if (bkp == null)
            super.putAll(map);
        else
            throw new IllegalStateException(
                    "We don't support putAll() in added contexts yet");
    }

    // NB. remove() only operates on the current context
    public Object remove(Object key) {
        if (bkp == null)
            return super.remove(key);
        else if (bkp.containsKey(key))
            return super.put(key, bkp.remove(key));
        else
            return null;
    }

    public void pushContext() {
        bkp = new HashedMap();
        bkpStack.push(bkp);
    }

    public void popContext() {
        super.putAll((Map) bkpStack.pop());
        bkp = bkpStack.isEmpty() ? null : (Map) bkpStack.peek();
    }

}