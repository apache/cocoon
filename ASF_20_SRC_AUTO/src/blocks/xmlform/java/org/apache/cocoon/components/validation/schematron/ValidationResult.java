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
 * @version CVS $Id: ValidationResult.java,v 1.3 2004/03/05 13:02:37 bdelacretaz Exp $
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
