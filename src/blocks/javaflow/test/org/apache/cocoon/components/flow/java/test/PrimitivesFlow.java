/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.cocoon.components.flow.java.test;

import junit.framework.TestCase;

import org.apache.cocoon.components.flow.java.*;

public class PrimitivesFlow extends TestCase implements Continuable {

    public PrimitivesFlow(String s) {
        super(s);
    }    

    public void testPrimitives() {
        boolean bool = true;
        byte b = (byte)0xab;
        double d = 123456789.123456789d;
        float f = 987654321.987654321f;
        int i = 9876598;
        long l = 1234512345l;
        Object n = null;
        Object o = new Integer(12345);
        short s = 12;
                                                                                                                                                             
        Continuation.suspend();

        assertEquals(bool, true);
        assertEquals(b, 0xab);
        assertEquals(d, 123456789.123456789d, 0d);
        assertEquals(f, 987654321.987654321f, 0f);
        assertEquals(i, 9876598);
        assertEquals(l, 1234512345l);
        assertEquals(n, null);
        assertEquals(o, new Integer(12345));
        assertEquals(s, 12);
    }
}
