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
package org.apache.cocoon.components.jxforms.validation.schematron;

/**
 * Represents a Schematron assert element.
 *
 * example:
 * <assert test="count(ear)=2">A <name/> element should contain two <emph>ear</emph> elements.</assert>
 *
 * @author  Ivelin Ivanov, ivelin@acm.org, ivelin@iname.com
 * @version CVS $Id: Assert.java,v 1.2 2004/03/05 13:01:58 bdelacretaz Exp $
 */
public class Assert {

    private String test_;
    private String message_;
    private String diagnostics_;

    /**
     * Returns the test attribute
     */
    public String getTest() {
        return test_;
    }

    /**
     * Sets the test attribute
     */
    public void setTest(String newTest) {
        test_ = newTest;
    }

    /**
     * Returns the message for to the element
     */
    public String getMessage() {
        return message_;
    }

    /**
     * Sets the message for to the element
     */
    public void setMessage(String newMessage) {
        message_ = newMessage;
    }

    /**
     * Returns the diagnostics list
     */
    public String getDiagnostics() {
        return diagnostics_;
    }

    /**
     * Sets the diagnostics list
     */
    public void setDiagnostics(String newDiagnostics) {
        diagnostics_ = newDiagnostics;
    }
}
