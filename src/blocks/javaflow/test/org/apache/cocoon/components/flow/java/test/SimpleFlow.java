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

import java.util.Locale;

import junit.framework.Assert;

import org.apache.cocoon.components.flow.java.*;
import org.apache.cocoon.forms.FormContext;
/*import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;*/

public class SimpleFlow extends AbstractSimpleFlow {

    public SimpleFlow() {
        /*if (Continuation.currentContinuation() != null)
            throw new RuntimeException("Conitnuation should not exist");*/
        //sendPageAndWait("should not stop");
    }

    public boolean doSimpleTest() {
        System.out.println("start of flow");
        float a = 1;
        sendPageAndWait("getNumberA");
        a = Float.parseFloat(getRequest().getParameter("a"));
        System.out.println("a=" + a);
        sendPage("result", new VarMap().add("result", a + 1));
        System.out.println("end of flow");
        return true;
    }
				
    public void doNewTest() {
        Locale locale = null;
        FormContext formContext = new FormContext(getRequest(), locale);
    }

    public void doCatchTest() {
        try {
            sendPageAndWait("getNumberA");
            float a = Float.parseFloat(getRequest().getParameter("a"));
        } catch (NumberFormatException nfe) {
            sendPageAndWait("error");
        }
        sendPage("result");
    }

    public void testFinally() {
        try {
            sendPageAndWait("getNumberA");
            float a = Float.parseFloat(getRequest().getParameter("a"));
        } finally {
            sendPageAndWait("result");
        }
    }

    public void testEmpty() {
        //nothing
    }

    public void doAbstractTest() {
        super.parent();
    }

    public void doDelegateTest() {
        CalculatorFlow flow = new CalculatorFlow();
        flow.run();
    }

    public boolean run() {
        System.out.println("start of flow");
        Object res = foo();
        System.out.println("Result of foo is " + res);
        return true;
    }

    public FooInner foo() {
        float a = 1;
        sendPageAndWait("getNumberA");
        System.out.println("old a=" + a);
        a = Float.parseFloat(getRequest().getParameter("a"));
        System.out.println("a=" + a);
        sendPage("result", new VarMap().add("result", a + 1));
        System.out.println("end of flow");
        return new FooInner(12,12);
    }
    
    /*public void doTest() throws Exception {
        
        // This causes an error -> The class can not be registered at first call!!!
        Query query = null;
        Hits hits;
        IndexSearcher searcher = null;
        // end
  
        while(true) {
            
            this.sendPageAndWait("foo");
            
            query = QueryParser.parse("foo", "bar", new StandardAnalyzer());
            // here comes the problem, because searcher is null, which must preserved by the continuation
            hits = searcher.search(query);
        }
    }*/
    
    public void doExceptionTest() throws Exception {
        
         while(true) {
            
             this.sendPageAndWait("test.jxt");
             throw new FooException("test", 123);
         }
     }
    
    public void doParameterTest() throws Exception {
    	Assert.assertEquals("abc", getParameters().getParameter("p1")); 
    	Assert.assertEquals("def", getParameters().getParameter("p2"));
    	Assert.assertEquals(2.3f, getParameters().getParameterAsFloat("p3"), 0.1f);
    }
}

class FooInner {
    public FooInner(int i, int j) {

    }
}
