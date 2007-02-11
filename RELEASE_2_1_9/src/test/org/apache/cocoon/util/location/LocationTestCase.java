/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.util.location;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

public class LocationTestCase extends TestCase {
    
    public LocationTestCase(String name) {
        super(name);
    }
    
    static final String str = "path/to/file.xml:1:40";

    public void testParse() throws Exception {
        String str = "<map:generate> - path/to/file.xml:1:40";
        Location loc = LocationUtils.parse(str);
        
        assertEquals("<map:generate>", loc.getDescription());
        assertEquals("URI", "path/to/file.xml", loc.getURI());
        assertEquals("line", 1, loc.getLineNumber());
        assertEquals("column", 40, loc.getColumnNumber());
        assertEquals("string representation", str, loc.toString());
    }
    
    public void testEquals() throws Exception {
        Location loc1 = LocationUtils.parse(str);
        Location loc2 = new LocationImpl(null, "path/to/file.xml", 1, 40);
        
        assertEquals("locations", loc1, loc2);
        assertEquals("hashcode", loc1.hashCode(), loc2.hashCode());
        assertEquals("string representation", loc1.toString(), loc2.toString());
    }
    
    /**
     * Test that Location.UNKNOWN is kept identical on deserialization
     */
    public void testSerializeUnknown() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        
        oos.writeObject(Location.UNKNOWN);
        oos.close();
        bos.close();
        
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        
        Object obj = ois.readObject();
        
        assertSame("unknown location", Location.UNKNOWN, obj);
    }
}
