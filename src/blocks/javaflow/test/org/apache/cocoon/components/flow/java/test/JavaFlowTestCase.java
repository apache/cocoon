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

import java.util.ArrayList;

import org.apache.cocoon.SitemapComponentTestCase;
import org.apache.cocoon.components.flow.java.VarMap;
import org.apache.commons.jxpath.JXPathContext;

/**
 *
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels </a>
 * @version CVS $Id: JavaFlowTestCase.java,v 1.1 2004/06/14 14:53:56 stephan Exp $
 */
public class JavaFlowTestCase extends SitemapComponentTestCase {

    public JavaFlowTestCase(String name) {
        super(name);
    }
    
    public void testSimple() throws Exception {
        String source = "org.apache.cocoon.components.flow.java.test.SimpleFlow";
        String id = callFunction("java", source, "simpleTest", new ArrayList());
        
        getRequest().addParameter("a", "2.3");
        getRedirector().reset();
        
        callContinuation("java", source, id, new ArrayList());

        VarMap map = (VarMap)getFlowContextObject();
        
        assertEquals(((Float)map.getMap().get("result")).floatValue(), 3.3f, 0.1f);

        JXPathContext jxcontext = JXPathContext.newContext(getFlowContextObject());
        Float result = (Float)jxcontext.getValue("result");

        assertEquals(result.floatValue(), 3.3f, 0.1f);
    }
    
    public void testNew() throws Exception {
        String source = "org.apache.cocoon.components.flow.java.test.SimpleFlow";
        String id = callFunction("java", source, "newTest", new ArrayList());
    }
    
    public void testCatch() throws Exception {
        
        String source = "org.apache.cocoon.components.flow.java.test.SimpleFlow";
        String id = callFunction("java", source, "catchTest", new ArrayList());
        
        assertEquals(getRedirector().getRedirect(), "cocoon:/getNumberA");
        
        getRequest().addParameter("a", "bla");
        getRedirector().reset();
        
        id = callContinuation("java", source, id, new ArrayList());
        
        assertEquals(getRedirector().getRedirect(), "cocoon:/error");
        
        getRedirector().reset();
        
        id = callContinuation("java", source, id, new ArrayList());
        
        assertEquals(getRedirector().getRedirect(), "cocoon:/result");
        
    }
    
    public void testAbstract() throws Exception {
        
        String source = "org.apache.cocoon.components.flow.java.test.SimpleFlow";
        String id = callFunction("java", source, "abstractTest", new ArrayList());

        assertEquals(getRedirector().getRedirect(), "cocoon:/parent");
    }
    
    public void testDelegate() throws Exception {

        String source = "org.apache.cocoon.components.flow.java.test.SimpleFlow";
        String id = callFunction("java", source, "delegateTest", new ArrayList());

        assertEquals(getRedirector().getRedirect(), "cocoon:/page/getNumberA");

        getRequest().addParameter("a", "2");
        getRedirector().reset();

        id = callContinuation("java", source, id, new ArrayList());
        
        assertEquals(getRedirector().getRedirect(), "cocoon:/page/getNumberB");

        getRequest().addParameter("b", "2");
        getRedirector().reset();
        
        id = callContinuation("java", source, id, new ArrayList());

        assertEquals(getRedirector().getRedirect(), "cocoon:/page/getOperator");
        
        getRequest().addParameter("operator", "plus");
        getRedirector().reset();
        
        id = callContinuation("java", source, id, new ArrayList());

        assertEquals(getRedirector().getRedirect(), "cocoon:/page/displayResult");
    }

    public void testException() throws Exception {
        String source = "org.apache.cocoon.components.flow.java.test.SimpleFlow";
        String id = callFunction("java", source, "exceptionTest", new ArrayList());
        
        assertEquals(getRedirector().getRedirect(), "cocoon:/test.jxt");
        
        try {
            callContinuation("java", source, id, new ArrayList());
            fail("Excepting a FooException");
        } catch (FooException e) {}
    }
    
    public void testSimpleContinuable() throws Exception {
        String source = "org.apache.cocoon.components.flow.java.test.SimpleContinuable";
        String id = callFunction("java", source, "suspendTest", new ArrayList());
        
        id = callContinuation("java", source, id, new ArrayList());
    }

    public void testWrapperContinuable() throws Exception {
        String source = "org.apache.cocoon.components.flow.java.test.WrapperContinuable";
        String id = callFunction("java", source, "wrapperTest", new ArrayList());
        
        id = callContinuation("java", source, id, new ArrayList());
    }

    public void testExtendedContinuable() throws Exception {
        String source = "org.apache.cocoon.components.flow.java.test.ExtendedContinuable";
        String id = callFunction("java", source, "extendedTest", new ArrayList());
        
        id = callContinuation("java", source, id, new ArrayList());
    }
}
