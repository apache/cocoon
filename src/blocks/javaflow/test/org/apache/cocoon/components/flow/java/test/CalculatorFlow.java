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

import org.apache.cocoon.components.flow.java.*;

public class CalculatorFlow extends AbstractContinuable {

    public void run() {

        float a, b;
        String op;
        String uri = "page/";

        sendPageAndWait(uri + "getNumberA");
        a = Float.parseFloat(getRequest().getParameter("a"));
        System.out.println("a=" + a);

        sendPageAndWait(uri + "getNumberB", new VarMap().add("a", a));
        b = Float.parseFloat(getRequest().getParameter("b"));
        System.out.println("b=" + b);

        sendPageAndWait(uri + "getOperator", new VarMap().add("a", a).add("b", b));
        op = getRequest().getParameter("operator");
        System.out.println("operator=" + op);

        if ("plus".equals(op)) {
            System.out.println("result=" + (a + b));
            sendPage(uri + "displayResult", new VarMap().add("a", a).add("b", b).add("operator", op).add("result", a + b));
        }
        else if ("minus".equals(op)) {
            System.out.println("result=" + (a - b));
            sendPage(uri + "displayResult", new VarMap().add("a", a).add("b", b).add("operator", op).add("result", a - b));
        }
        else if ("multiply".equals(op)) {
            System.out.println("result=" + (a * b));
            sendPage(uri + "displayResult", new VarMap().add("a", a).add("b", b).add("operator", op).add("result", a * b));
        }
        else if ("divide".equals(op)) {
            if (b == 0) {
                //sendPage("Error: Division by zero!");
            }
            else {
                System.out.println("result=" + (a / b));
                sendPage(uri + "displayResult", new VarMap().add("a", a).add("b", b).add("operator", op).add("result", a / b));
            }
        }
        else {
            //sendPage("Error: Unkown operator!");
        }
    }
}
