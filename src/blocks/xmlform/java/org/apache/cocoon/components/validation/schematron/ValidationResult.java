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

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components.validation.schematron;

import org.apache.cocoon.components.validation.Violation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the result of a Schematron validation process.
 *
 * <validationResult>
 *   list of <pattern> elements with <rule> subelements
 * </validationResult>
 *
 * @author  Ivelin Ivanov, ivelin@acm.org, ivelin@iname.com
 * @version CVS $Id: ValidationResult.java,v 1.2 2003/04/26 12:10:44 stephan Exp $
 */
public class ValidationResult {

    private ArrayList patterns_ = new ArrayList();

    /**
     * Returns a list of the patterns which
     * contain rules that failed during validation.
     */
    public List getPattern() {
        return patterns_;
    }

    /**
     * Sets the list of the patterns which
     * contain rules that failed during validation.
     */
    public void setPattern(Collection newPatterns) {
        patterns_ = new ArrayList();
        patterns_.addAll(newPatterns);
    }

    /**
     * Add a pattern to the list.
     */
    public void addPattern(Pattern p) {
        patterns_.add(p);
    }

    public boolean isEmpty() {
        return patterns_.isEmpty();
    }

    /**
     * Adds all errors to a sorted list.
     * Key is XPath of each error location
     * @return SortedSet
     */
    public List toList() {

        if (isEmpty()) {
            return null;
        }

        List violations = new LinkedList();

        Iterator piter = getPattern().iterator();

        while (piter.hasNext()) {
            Pattern pattern = (Pattern) piter.next();
            // System.out.println("Pattern name: " + pattern.getName() + ", id: " + pattern.getId() );
            Iterator ruleIter = pattern.getRule().iterator();

            while (ruleIter.hasNext()) {
                Rule rule = (Rule) ruleIter.next();
                // System.out.println("    Rule name: " + rule.getContext() );

                Iterator assertIter = rule.getAssert().iterator();

                while (assertIter.hasNext()) {
                    Assert assertion = (Assert) assertIter.next();

                    // add the next assert to the violations set
                    Violation v = new Violation();

                    v.setPath(rule.getContext());
                    v.setMessage(assertion.getMessage());
                    violations.add(v);
                    // System.out.println("        Assert test: " + assertion.getTest() + ", message: " + assertion.getMessage() );
                }

                Iterator reportIter = rule.getReport().iterator();

                while (reportIter.hasNext()) {
                    Report report = (Report) reportIter.next();

                    // add the next report to the violations set
                    Violation v = new Violation();

                    v.setPath(rule.getContext());
                    v.setMessage(report.getMessage());
                    violations.add(v);
                    // System.out.println("        Report test: " + report.getTest() + ", message: " + report.getMessage() );
                }
            }
        }
        return violations;
    }
}
