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

import java.util.HashMap;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.core.container.ContainerTestCase;

public class ContextStackMapTestCase extends ContainerTestCase {
    Logger logger = new ConsoleLogger(ConsoleLogger.LEVEL_WARN);

    public Logger getLogger() {
        return logger;
    }

    ContextStackMap map;

    public void setUp() throws Exception {
        super.setUp();
        map = new ContextStackMap();
    }

    public void testPutInRootContext() {
        assertNull(map.put("key", "value1"));
        assertEquals("value1", map.get("key"));
        assertEquals("value1", map.put("key", "value2"));
    }

    public void testRemoveInRootContext() {
        assertNull(map.remove("blah"));

        map.put("key", "value");
        assertEquals("value", map.remove("key"));
        assertNull(map.remove("key"));
    }

    public void testPutInAddedContext1() {
        map.pushContext();

        assertNull(map.put("key2", "value2"));
        assertEquals("value2", map.get("key2"));
    }

    public void testPutInAddedContext2() {
        map.put("key1", "value1.1");
        map.pushContext();

        assertEquals("value1.1", map.put("key1", "value1.2"));
        assertEquals("value1.2", map.get("key1"));
        assertEquals("value1.2", map.put("key1", "value1.3"));
    }

    public void testRemoveInAddedContext1() {
        map.pushContext();
        map.put("key", "value");
        assertEquals("value", map.remove("key"));
        assertNull(map.remove("key"));
    }

    public void testRemoveInAddedContext2() {
        map.put("key", "value1");
        map.pushContext();
        map.put("key", "value2");
        assertEquals("value2", map.remove("key"));
        assertNull(map.remove("key"));
        map.popContext();
        assertEquals("value1", map.get("key"));
    }

    public void testPopContext1() {
        map.pushContext();
        map.put("key", "value");
        map.popContext();

        assertNull(map.get("key"));
    }

    public void testPopContext2() {
        map.put("key", "value1");
        map.pushContext();

        map.put("key", "value2");
        map.popContext();

        assertEquals("value1", map.get("key"));
    }

    public void testClear() {
        map.put("key1", "value1");
        map.pushContext();
        map.put("key2", "value2");
        map.clear();
        assertNull(map.get("key1"));
        assertNull(map.get("key2"));
    }

    public void testPutAll1() {
        HashMap m = new HashMap();
        m.put("key", "value");
        map.putAll(m);
        assertEquals("value", map.get("key"));
    }

    public void testPutAll2() {
        HashMap m = new HashMap();
        map.pushContext();
        try {
            map.putAll(m);
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }
}

