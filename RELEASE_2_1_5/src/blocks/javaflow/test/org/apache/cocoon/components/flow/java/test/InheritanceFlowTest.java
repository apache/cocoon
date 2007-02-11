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

import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

import org.apache.cocoon.components.flow.java.Continuation;
import org.apache.cocoon.components.flow.java.ContinuationClassLoader;

public class InheritanceFlowTest extends TestCase {
    
    public InheritanceFlowTest(String s) {
        super(s);
    }

    static public void main(String args[]) {
        try {
            testSimpleContinuable();
            System.out.println("SimpleContinuable test done");
            testExtendedContinuable();
            System.out.println("ExtendedContinuable test done");
            testWrapperContinuable();
            System.out.println("Wrapper continuable test done");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void testSimpleContinuable() throws Exception {
        ContinuationClassLoader cl = new ContinuationClassLoader(Thread
                .currentThread().getContextClassLoader());
        Continuation continuation = new Continuation(null);
        continuation.registerThread();
        Class clazz = cl.loadClass("org.apache.cocoon.components.flow.java.test.SimpleContinuable");
        Object object = clazz.newInstance();
        clazz.getMethod("suspend", null).invoke(object, null);
        if (continuation.isCapturing())
            continuation.getStack().popReference();
        continuation = new Continuation(continuation, null);
        continuation.registerThread();
        clazz.getMethod("suspend", null).invoke(object, null);
    }

    public static void testWrapperContinuable() throws Exception {
        ContinuationClassLoader cl = new ContinuationClassLoader(Thread
                .currentThread().getContextClassLoader());
        Continuation continuation = new Continuation(null);
        continuation.registerThread();
        Class clazz = cl.loadClass("org.apache.cocoon.components.flow.java.test.WrapperContinuable");
        Object object = clazz.newInstance();
        clazz.getMethod("test", null).invoke(object, null);
				if (continuation.isCapturing())
            continuation.getStack().popReference();
        continuation = new Continuation(continuation, null);
        continuation.registerThread();
        clazz.getMethod("test", null).invoke(object, null);
    }

    public static void testExtendedContinuable() throws Exception {
        ContinuationClassLoader cl = new ContinuationClassLoader(Thread
                .currentThread().getContextClassLoader());
        Continuation continuation = new Continuation(null);
        continuation.registerThread();
        Class clazz = cl.loadClass("org.apache.cocoon.components.flow.java.test.ExtendedContinuable");
        Object object = clazz.newInstance();
        clazz.getMethod("test", null).invoke(object, null);
				if (continuation.isCapturing())
            continuation.getStack().popReference();
        continuation = new Continuation(continuation, null);
        continuation.registerThread();
        clazz.getMethod("test", null).invoke(object, null);
    }
}
