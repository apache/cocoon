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
package org.apache.cocoon.samples.flow.java;

import org.apache.cocoon.components.flow.java.AbstractContinuable;
import org.apache.cocoon.components.flow.java.VarMap;

public class CalculatorFlow extends AbstractContinuable {

    private int count = 1;

    public void doCalculator() {
        float a = getNumber("a", 0f, 0f);
        float b = getNumber("b", a, 0f);
        String op = getOperator(a, b);

        if (op.equals("plus")) {
            sendResult(a, b, op, a + b);
        } else if (op.equals("minus")) {
            sendResult(a, b, op, a - b);
        } else if (op.equals("multiply")) {
            sendResult(a, b, op, a * b);
        } else if (op.equals("divide")) {
            if (b==0f)
                sendMessage("Error: Cannot divide by zero!");
            sendResult(a, b, op, a / b);
        } else {
            sendMessage("Error: Unkown operator!");
        }

        count++;
    }

    private float getNumber(String name, float a, float b) {
        String uri = "page/calculator-" + name.toLowerCase();
        sendPageAndWait(uri, new VarMap().add("a", a).add("b", b).add("count", count));

        float value = 0f;
        try {
            value = Float.parseFloat(getRequest().getParameter(name));
        } catch (Exception e) {
            sendMessage("Error: \""+getRequest().getParameter(name)+"\" is not a correct number!");
        }
        return value;
    }

    private String getOperator(float a, float b) {
        sendPageAndWait("page/calculator-operator", new VarMap().add("a", a).add("b", b).add("count", count));
        return getRequest().getParameter("operator");
    }

    private void sendResult(float a, float b, String op, float result) {
        sendPage("page/calculator-result", new VarMap().add("a", a).add("b", b).add("operator", op).add("result", result).add("count", count));
    }

    private void sendMessage(String message) {
        sendPageAndWait("page/calculator-message", new VarMap().add("message", message).add("count", count));
    }
}
