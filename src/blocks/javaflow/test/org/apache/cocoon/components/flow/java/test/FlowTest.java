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
package org.apache.cocoon.components.flow.java.test;

import java.lang.reflect.Method;
import java.util.HashMap;

import junit.framework.TestCase;

import org.apache.cocoon.components.flow.java.Continuable;
import org.apache.cocoon.components.flow.java.Continuation;
import org.apache.cocoon.components.flow.java.ContinuationClassLoader;
import org.apache.cocoon.components.flow.java.ContinuationContext;
import org.apache.cocoon.components.flow.java.VarMap;
import org.apache.cocoon.components.flow.java.VarMapHandler;
import org.apache.cocoon.environment.mock.MockRequest;
import org.apache.cocoon.environment.mock.MockRedirector;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.cocoon.components.ContextHelper;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.environment.ObjectModelHelper;

public class FlowTest extends TestCase {
    public FlowTest(String s) {
        super(s);
    }

    static {
        JXPathIntrospector.registerDynamicClass(VarMap.class, VarMapHandler.class);
    }

    private static ClassLoader loader = new ContinuationClassLoader(FlowTest.class.getClassLoader());
    private ContinuationContext context;
    private MockRequest request;
    private MockRedirector redirector;
    private HashMap objectmodel;

    public void setUp() {

        context = new ContinuationContext();
      
        DefaultContext avalonContext = new DefaultContext();

        request = new MockRequest();
        avalonContext.put(ContextHelper.CONTEXT_REQUEST_OBJECT, request);
        objectmodel = new HashMap();
        objectmodel.put(ObjectModelHelper.REQUEST_OBJECT, request);
        avalonContext.put(ContextHelper.CONTEXT_OBJECT_MODEL, objectmodel);
        redirector = new MockRedirector();

        context.setAvalonContext(avalonContext);
        context.setRedirector(redirector);
    }

    public void testSimple() throws Exception {

/*        ClassLoader cl = getClass().getClassLoader();
        while (cl != null) {
            System.out.println(cl);
            cl = cl.getParent();
        }
        try {
            System.out.println(
                    getClass().
                    getProtectionDomain().
                    getCodeSource().
                    getLocation());
        }
        catch (Exception e) {
        }*/

        Class clazz = loader.loadClass("org.apache.cocoon.components.flow.java.test.SimpleFlow");
        Continuable flow = (Continuable) clazz.newInstance();

        Method method = clazz.getMethod("run", new Class[0]);

        Continuation c = new Continuation(context);
        assertTrue(!c.isRestoring());
        assertTrue(!c.isCapturing());

        System.out.println("*** start flow");
        c.registerThread();
        method.invoke(flow, new Object[0]);
        if (c.isCapturing())
            c.getStack().popReference();
        c.deregisterThread();
        System.out.println("*** return from flow");

        assertTrue(!c.isRestoring());
        assertTrue(c.isCapturing());

        //System.out.println("request=" + request);
        request.addParameter("a", "2.3");
        redirector.reset();
        c = new Continuation(c, context);

        assertTrue(c.isRestoring());
        assertTrue(!c.isCapturing());

        System.out.println("*** resume flow");
        c.registerThread();
        method.invoke(flow, new Object[0]);
        if (c.isCapturing())
            c.getStack().popReference();
        c.deregisterThread();
        System.out.println("*** return from flow");

        assertTrue(!c.isRestoring());
        assertTrue(!c.isCapturing());

        VarMap map = (VarMap)FlowHelper.getContextObject(objectmodel);
        
        assertEquals(((Float)map.getMap().get("result")).floatValue(), 3.3f, 0.1f);

        JXPathContext jxcontext = JXPathContext.newContext(FlowHelper.getContextObject(objectmodel));
        Float result = (Float)jxcontext.getValue("result");

        assertEquals(result.floatValue(), 3.3f, 0.1f);
    }

    public void testCatch() throws Exception {

        Class clazz = loader.loadClass("org.apache.cocoon.components.flow.java.test.SimpleFlow");
        Continuable flow = (Continuable) clazz.newInstance();

        Method method = clazz.getMethod("testCatch", new Class[0]);

        Continuation c = new Continuation(context);
        assertTrue(!c.isRestoring());
        assertTrue(!c.isCapturing());

        System.out.println("*** start flow");
        c.registerThread();
        method.invoke(flow, new Object[0]);
        if (c.isCapturing())
            c.getStack().popReference();
        c.deregisterThread();
        System.out.println("*** return from flow");

        assertTrue(!c.isRestoring());
        assertTrue(c.isCapturing());

        assertEquals(redirector.getRedirect(), "cocoon:/getNumberA");

        request.addParameter("a", "bla");
        redirector.reset();
        c = new Continuation(c, context);

        assertTrue(c.isRestoring());
        assertTrue(!c.isCapturing());

        System.out.println("*** resume flow");
        c.registerThread();
        method.invoke(flow, new Object[0]);
        if (c.isCapturing())
            c.getStack().popReference();
        c.deregisterThread();
        System.out.println("*** return from flow");

        assertTrue(!c.isRestoring());
        assertTrue(c.isCapturing());

        assertEquals(redirector.getRedirect(), "cocoon:/error");

        redirector.reset();
        c = new Continuation(c, context);

        assertTrue(c.isRestoring());
        assertTrue(!c.isCapturing());

        System.out.println("*** resume flow");
        c.registerThread();
        method.invoke(flow, new Object[0]);
        if (c.isCapturing())
            c.getStack().popReference();
        c.deregisterThread();
        System.out.println("*** return from flow");

        assertTrue(!c.isRestoring());
        assertTrue(!c.isCapturing());

        assertEquals(redirector.getRedirect(), "cocoon:/result");
    }

/*    public void testFinally() throws Exception {

        Class clazz = loader.loadClass("org.apache.cocoon.components.flow.java.test.SimpleFlow");
        Continuable flow = (Continuable) clazz.newInstance();

        Method method = clazz.getMethod("testFinally", new Class[0]);

        Continuation c = new Continuation(context);
        assertTrue(!c.isRestoring());
        assertTrue(!c.isCapturing());

        System.out.println("*** start flow");
        c.registerThread();
        method.invoke(flow, new Object[0]);
        c.deregisterThread();
        System.out.println("*** return from flow");

        assertTrue(!c.isRestoring());
        assertTrue(c.isCapturing());

        assertEquals(context.getRedirectedURI(), "cocoon:/getNumberA");

        request.addParameter("a", "bla");
        redirector.reset();
        c = new Continuation(c, context);

        assertTrue(c.isRestoring());
        assertTrue(!c.isCapturing());

        System.out.println("*** resume flow");
        c.registerThread();
        method.invoke(flow, new Object[0]);
        c.deregisterThread();
        System.out.println("*** return from flow");

        assertTrue(!c.isRestoring());
        assertTrue(c.isCapturing());

        assertEquals(context.getRedirectedURI(), "cocoon:/result");

        redirector.reset();
        c = new Continuation(c, context);

        assertTrue(c.isRestoring());
        assertTrue(!c.isCapturing());

        try {

            System.out.println("*** resume flow");
            c.registerThread();
            method.invoke(flow, new Object[0]);
            c.deregisterThread();
            System.out.println("*** return from flow");

            fail("NumberFormatException should be thrown");
        } catch (NumberFormatException nfe) {
            // sucessful
        }
    }*/

    public void testFormFlow() throws Exception {
        Class clazz = loader.loadClass("org.apache.cocoon.samples.flow.java.FormFlow");
        Continuable flow = (Continuable) clazz.newInstance();
        
        assertNotNull(flow);
    }

/*    public static void testOJBFlow() throws Exception {
        ClassLoader loader = new ContinuationClassLoader(Thread.currentThread().getContextClassLoader());
        Class clazz = loader.loadClass("org.apache.cocoon.samples.flow.java.PersistenceFlow");
        //Class clazz = Class.forName("org.apache.cocoon.samples.flow.java.PersistenceFlow");
        Continuable flow = (Continuable) clazz.newInstance();
    }*/


    public void testAbstract() throws Exception {

        Class clazz = loader.loadClass("org.apache.cocoon.components.flow.java.test.SimpleFlow");
        Continuable flow = (Continuable) clazz.newInstance();

        Method method = clazz.getMethod("testAbstract", new Class[0]);

        Continuation c = new Continuation(context);
        assertTrue(!c.isRestoring());
        assertTrue(!c.isCapturing());

        System.out.println("*** start flow");
        c.registerThread();
        method.invoke(flow, new Object[0]);
        if (c.isCapturing())
            c.getStack().popReference();
        c.deregisterThread();
        System.out.println("*** return from flow");

        assertTrue(!c.isRestoring());
        assertTrue(c.isCapturing());

        assertEquals(redirector.getRedirect(), "cocoon:/parent");
    }

    public void testDelegate() throws Exception {

        ClassLoader loader = new ContinuationClassLoader(getClass().getClassLoader());
        Class clazz = loader.loadClass("org.apache.cocoon.components.flow.java.test.SimpleFlow");
        Continuable flow = (Continuable) clazz.newInstance();

        Method method = clazz.getMethod("testDelegate", new Class[0]);

        Continuation c = new Continuation(context);
        assertTrue(!c.isRestoring());
        assertTrue(!c.isCapturing());

        System.out.println("*** start flow");
        c.registerThread();
        method.invoke(flow, new Object[0]);
        if (c.isCapturing())
            c.getStack().popReference();
        c.deregisterThread();
        System.out.println("*** return from flow");
        System.out.println();

        assertTrue(!c.isRestoring());
        assertTrue(c.isCapturing());

        assertEquals(redirector.getRedirect(), "cocoon:/page/getNumberA");

        request.addParameter("a", "2");
        redirector.reset();
        c = new Continuation(c, context);

        assertTrue(c.isRestoring());
        assertTrue(!c.isCapturing());

        System.out.println();
        System.out.println("*** resume flow");
        c.registerThread();
        method.invoke(flow, new Object[0]);
        if (c.isCapturing())
            c.getStack().popReference();
        c.deregisterThread();
        System.out.println("*** return from flow");
        System.out.println();

        assertTrue(!c.isRestoring());
        assertTrue(c.isCapturing());

        assertEquals(redirector.getRedirect(), "cocoon:/page/getNumberB");

        request.addParameter("b", "2");
        redirector.reset();
        c = new Continuation(c, context);

        assertTrue(c.isRestoring());
        assertTrue(!c.isCapturing());

        System.out.println();
        System.out.println("*** resume flow");
        c.registerThread();
        method.invoke(flow, new Object[0]);
        if (c.isCapturing())
            c.getStack().popReference();
        c.deregisterThread();
        System.out.println("*** return from flow");
        System.out.println();

        assertTrue(!c.isRestoring());
        assertTrue(c.isCapturing());

        assertEquals(redirector.getRedirect(), "cocoon:/page/getOperator");
        
        request.addParameter("operator", "plus");
        redirector.reset();
        c = new Continuation(c, context);

        assertTrue(c.isRestoring());
        assertTrue(!c.isCapturing());

        System.out.println();
        System.out.println("*** resume flow");
        c.registerThread();
        method.invoke(flow, new Object[0]);
        if (c.isCapturing())
            c.getStack().popReference();
        c.deregisterThread();
        System.out.println("*** return from flow");
        System.out.println();

        assertTrue(!c.isRestoring());
        assertTrue(!c.isCapturing());

        assertEquals(redirector.getRedirect(), "cocoon:/page/displayResult");
    }

    public static void main(String[] args) throws Exception {
        new FlowTest("test").testDelegate();
    }
}
