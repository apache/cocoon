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
package org.apache.cocoon.samples.xmlform;

import org.apache.cocoon.acting.AbstractControllerAction;
import org.apache.cocoon.components.validation.Violation;
import org.apache.cocoon.components.xmlform.Form;

import java.util.ArrayList;

/**
 * Controller action for the calculator example.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: CalculatorAction.java,v 1.2 2004/03/05 13:02:38 bdelacretaz Exp $
 */
public class CalculatorAction extends AbstractControllerAction {

    public String doCalculate(String command, Form form) {

        CalculatorBean bean = (CalculatorBean) form.getModel();

        if (command.equals("next")) {
            if ((bean.getOperator().equals("divide")) &&
                (bean.getNumberB()==0)) {
                ArrayList list = new ArrayList();

                list.add(new Violation("operator", "Can not divide by zero"));
                form.addViolations(list);
                return null;
            } else {

                if (bean.getOperator().equals("plus")) {
                    bean.setResult(bean.getNumberA()+bean.getNumberB());
                } else if (bean.getOperator().equals("minus")) {
                    bean.setResult(bean.getNumberA()-bean.getNumberB());
                } else if (bean.getOperator().equals("multiply")) {
                    bean.setResult(bean.getNumberA()*bean.getNumberB());
                } else if (bean.getOperator().equals("divide")) {
                    bean.setResult(bean.getNumberA()/bean.getNumberB());
                }
            }
            return "next";
        }

        // execute default transition
        return null;
    }

    public String doReset(String command, Form form) {

        if (command.equals("next")) {
            form.setModel(new CalculatorBean());
        }

        return command;
    }
}

