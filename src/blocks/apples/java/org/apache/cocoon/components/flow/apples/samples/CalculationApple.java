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
package org.apache.cocoon.components.flow.apples.samples;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.flow.apples.AppleController;
import org.apache.cocoon.components.flow.apples.AppleRequest;
import org.apache.cocoon.components.flow.apples.AppleResponse;

/**
 * CalculationApple shows an easy Apple example implementation for a Calculator.
 * <p>
 * It is explicitely designed to show the difference with flowscript by 
 * remembering the 'lookahead' information from the previous path that entered 
 * already the other data.
 * <p>
 * In other words this shows that Apples are not building a complete tree of 
 * continuations like flowscript is doing.  But the initial argument of course was
 * that some cases simply don't need it. 
 */
public class CalculationApple extends AbstractLogEnabled implements AppleController {

    BigDecimal inputA;
    BigDecimal inputB;
    String inputOp;
    BigDecimal output;


    public String toString() {
        return "CalculationApple[ a=" + this.inputA + " | b=" + this.inputB
                + " | op = " + this.inputOp + " | result = " + this.output + "]";
    }

    public void process(AppleRequest req, AppleResponse res) throws ProcessingException {
        String changeTo = processRequest(req);
        getLogger().debug(toString());
        showNextState(res, changeTo);
    }

    private String processRequest(AppleRequest req) {
        String changeRequest = req.getCocoonRequest().getParameter("change");

        String newA = req.getCocoonRequest().getParameter("a");
        if (newA != null) {
            this.inputA = new BigDecimal(newA);
            // explicitely do not set inputB and inputOp to null !
        }
        String newB = req.getCocoonRequest().getParameter("b");
        if (newB != null) {
            this.inputB = new BigDecimal(newB);
            // explicitely do not set inputOp to null !
        }
        String newOp = req.getCocoonRequest().getParameter("operator");
        if (newOp != null) {
            this.inputOp = newOp;
        }        
        //explicitely always do the calculation
        calculate();

        return changeRequest;
    }


    private void calculate() {
        if (this.inputA == null || this.inputB == null) {
            this.output = null;
        } else if("plus".equals(this.inputOp)) {
            this.output = this.inputA.add(this.inputB);            
        } else if("minus".equals(this.inputOp)) {
            this.output = this.inputA.add(this.inputB.negate());            
        } else if("multiply".equals(this.inputOp)) {
            this.output = this.inputA.multiply(this.inputB);            
        } else if("divide".equals(this.inputOp)) {
            this.output = this.inputA.divide(this.inputB, BigDecimal.ROUND_HALF_EVEN);            
        } else { //not a valid operator
            this.output = null;
        }        
    }

    private void showNextState(AppleResponse res, String changeTo) {
        Object bizdata = buildBizData();
        
        if (changeTo != null) {
            res.sendPage("calc/get" + changeTo, bizdata);            
        } else if (this.inputA == null) {
            res.sendPage("calc/getNumberA", null);
        } else if (this.inputB == null) {
            res.sendPage("calc/getNumberB", bizdata);
        } else if (this.inputOp == null) {
            res.sendPage("calc/getOperator", bizdata);
        } else {
            res.sendPage("calc/displayResult", bizdata);
        }
    }

    private Object buildBizData() {
        Map bizdata = new HashMap();
        bizdata.put("a", this.inputA);
        bizdata.put("b", this.inputB);
        bizdata.put("operator", this.inputOp);
        bizdata.put("result", this.output);
        return bizdata;
    }

}
