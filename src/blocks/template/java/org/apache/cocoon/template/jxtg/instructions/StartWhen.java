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
package org.apache.cocoon.template.jxtg.instructions;

import java.util.Stack;

import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.cocoon.template.jxtg.script.event.StartElement;
import org.apache.cocoon.template.jxtg.script.event.StartInstruction;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class StartWhen extends StartInstruction {
    private final JXTExpression test;
    private StartWhen nextChoice;

    public StartWhen(StartElement raw, Attributes attrs, Stack stack) 
        throws SAXException {

        super(raw);

        Locator locator = getLocation();

        if (stack.size() == 0 || !(stack.peek() instanceof StartChoose)) {
            throw new SAXParseException("<when> must be within <choose>", locator, null);
        }
        String test = attrs.getValue("test");
        if (test != null) {
            this.test = JXTExpression.compileExpr(test, "when: \"test\": ", locator);
            // Why is test lenient?
            this.test.setLenient(Boolean.TRUE);
            
            StartChoose startChoose = (StartChoose) stack.peek();
            if (startChoose.getFirstChoice() != null) {
                StartWhen w = startChoose.getFirstChoice();
                while (w.getNextChoice() != null) {
                    w = w.getNextChoice();
                }
                w.setNextChoice(this);
            } else {
                startChoose.setFirstChoice(this);
            }
        } else {
            throw new SAXParseException("when: \"test\" is required", locator, null);
        }
    }

    public JXTExpression getTest() {
        return test;
    }

    public void setNextChoice(StartWhen nextChoice) {
        this.nextChoice = nextChoice;
    }

    public StartWhen getNextChoice() {
        return nextChoice;
    }
}
