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

import org.apache.cocoon.components.flow.java.*;
import org.apache.cocoon.forms.FormContext;
import java.util.Locale;

public class SimpleFlow extends AbstractSimpleFlow {

    public SimpleFlow() {
        if (Continuation.currentContinuation() != null)
            throw new RuntimeException("Conitnuation should not exist");
        //sendPageAndWait("should not stop");
    }

    public boolean run() {
        System.out.println("start of flow");
        float a = 1;
        sendPageAndWait("getNumberA");
        a = Float.parseFloat(getRequest().getParameter("a"));
        System.out.println("a=" + a);
        sendPage("result", new VarMap().add("result", a + 1));
        System.out.println("end of flow");
        return true;
    }

    public void testNew(Locale locale) {
        FormContext formContext = new FormContext(getRequest(), locale);
    }

    public void testCatch() {
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

    public void testAbstract() {
        super.parent();
    }

    public void testDelegate() {
        CalculatorFlow flow = new CalculatorFlow();
        flow.run();
    }
}
