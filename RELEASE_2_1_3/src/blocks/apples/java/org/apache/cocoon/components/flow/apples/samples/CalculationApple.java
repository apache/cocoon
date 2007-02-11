/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

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
