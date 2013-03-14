/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.forms.datatype.validationruleimpl;

import junit.framework.TestCase;


/**
 * Description of EmailValidationRuleTest.
 * 
 * @version $Id$
 */
public class EmailValidationRuleTest extends TestCase {

    public EmailValidationRuleTest(String name) {
        super(name);
    }
    
    /**
     * Test some patterns that should be accepted.
     */
    public void testAccept() {
        EmailValidationRule rule = new EmailValidationRule();
        assertTrue("dev@cocoon.apache.org", rule.isEmail("dev@cocoon.apache.org"));
        assertTrue("\"Arc, Joan\"@ird.govt.nz", rule.isEmail("\"Arc, Joan\"@ird.govt.nz"));
    }
    
    /**
     * Test some patterns that should be rejected.
     */
    public void testReject() {
        EmailValidationRule rule = new EmailValidationRule();
        assertTrue("x@....", ! rule.isEmail("x@...."));
        assertTrue("neville.@hotmail.com", ! rule.isEmail("neville.@hotmail.com"));
    }
}
