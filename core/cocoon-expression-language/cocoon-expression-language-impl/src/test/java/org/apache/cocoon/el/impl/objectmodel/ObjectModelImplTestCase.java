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
package org.apache.cocoon.el.impl.objectmodel;

import java.util.Collection;
import java.util.Map;

import org.apache.cocoon.el.impl.objectmodel.ObjectModelImpl;
import org.apache.cocoon.el.objectmodel.ObjectModel;

import junit.framework.TestCase;

public class ObjectModelImplTestCase extends TestCase {
    
    private ObjectModel objectModel;
    
    protected void setUp() throws Exception {
        super.setUp();
        this.objectModel = new ObjectModelImpl();
    }
    
    public void testMap() {
        objectModel.put("foo1", "bar1");
        assertEquals("bar2", objectModel.put("foo2", "bar2")); 
        assertEquals(true, objectModel.containsKey("foo1"));
    }
    
    public void testMultiValue() {
        objectModel.markLocalContext();
        objectModel.put("foo", "bar1");
        
        objectModel.markLocalContext();
        objectModel.put("foo", "bar2");
        assertEquals(2, ((Collection)objectModel.getAll().get("foo")).size());
        
        objectModel.cleanupLocalContext();
        assertEquals(1, ((Collection)objectModel.getAll().get("foo")).size());
        assertEquals("bar1", objectModel.get("foo"));
        
        objectModel.cleanupLocalContext();
        assertEquals(null, objectModel.get("foo"));
        assertEquals(null, objectModel.getAll().get("foo"));
    }
    
    public void testValues() {
        objectModel.put("foo", "bar1");
        Collection values = objectModel.values();
        assertEquals(true, values.contains("bar1"));
        
        objectModel.markLocalContext();
        objectModel.put("foo", "bar2");
        values = objectModel.values();
        assertEquals(false, values.contains("bar1"));
        assertEquals(true, values.contains("bar2"));
        
        objectModel.cleanupLocalContext();
        values = objectModel.values();
        assertEquals(false, values.contains("bar2"));
    }
    
    public void testLocalContext() {
        objectModel.put("foo", "bar1");
        objectModel.markLocalContext();
        objectModel.put("foo", "bar2");
        objectModel.put("abc", "xyz");
        
        assertEquals(true, objectModel.getAll().values().contains("bar2"));
        assertEquals(true, objectModel.getAll().values().contains("bar1"));
        assertEquals(true, objectModel.containsKey("abc"));
        
        objectModel.cleanupLocalContext();
        
        assertEquals(false, objectModel.getAll().values().contains("bar2"));
        assertEquals(true, objectModel.getAll().values().contains("bar1"));
        assertEquals(false, objectModel.containsKey("abc"));
    }
    
    public void testNull() {
        objectModel.markLocalContext();
        objectModel.put("foo", "bar");
        
        objectModel.markLocalContext();
        objectModel.put("foo", null);
        objectModel.put("foo", null);
        assertEquals(null, objectModel.get("foo"));
        objectModel.cleanupLocalContext();
        
        assertEquals("bar", objectModel.get("foo"));
        objectModel.cleanupLocalContext();
        
        assertTrue(objectModel.isEmpty());
        assertTrue(objectModel.getAll().isEmpty());
    }
    
    public void testThis() {
        assertEquals(objectModel, objectModel.get("this"));
    }

    public void testKeyAsPath() {        
        objectModel.putAt("foo", "bar");
        assertEquals("bar", objectModel.get("foo"));
    }

    public void testPutAt() {
        objectModel.putAt("foo/bar", "xyz");
        assertTrue(objectModel.containsKey("foo"));
        assertTrue(objectModel.get("foo") instanceof Map);
        assertEquals(((Map)objectModel.get("foo")).get("bar"), "xyz");
    }

    public void testPathInLocalContext() {
        objectModel.markLocalContext();
        objectModel.putAt("foo/bar", "xyz");
        
        objectModel.markLocalContext();
        objectModel.putAt("foo2/bar", "abc");
        assertEquals(((Map)objectModel.get("foo")).get("bar"), "xyz");
        assertEquals(((Map)objectModel.get("foo2")).get("bar"), "abc");
        objectModel.cleanupLocalContext();
        
        assertEquals(((Map)objectModel.get("foo")).get("bar"), "xyz");
        assertTrue(!objectModel.containsKey("foo2"));
        objectModel.cleanupLocalContext();
        
        assertTrue(objectModel.isEmpty());
    }
    
    public void testTrivialPath() {
        objectModel.markLocalContext();
        objectModel.putAt("foo", "bar");
        
        assertTrue(objectModel.containsKey("foo"));
        assertEquals("bar", objectModel.get("foo"));
        
        objectModel.cleanupLocalContext();
        
        assertTrue(objectModel.isEmpty());
    }

    public void testIfMapIsCreated() {
        objectModel.putAt("foo/bar/xyz", "abc");
        assertTrue(((Map)objectModel.get("foo")).get("bar") instanceof Map);
    }
    
    public void testParent() {
        ObjectModel parentObjectModel = new ObjectModelImpl();
        
        parentObjectModel.put("foo", "bar");
        parentObjectModel.put("foo2", "xyz");
        objectModel.setParent(parentObjectModel);
        
        assertTrue(objectModel.containsKey("foo"));
        assertEquals("xyz", objectModel.get("foo2"));
        
        objectModel.markLocalContext();
        objectModel.put("foo", "abc");
        assertEquals("abc", objectModel.get("foo"));
        assertEquals(2, ((Collection)objectModel.getAll().get("foo")).size());
        objectModel.cleanupLocalContext();
        
        assertEquals("bar", objectModel.get("foo"));
    }
}
