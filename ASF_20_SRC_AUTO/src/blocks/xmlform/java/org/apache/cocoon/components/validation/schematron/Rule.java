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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a Schematron rule element.
 *
 * From the Schematron specification:
 *
 * example:
 * <rule context="dog">
 *   <assert test="count(ear) = 2">A 'dog' element should contain two 'ear' elements.</assert>
 *   <report test="bone">This dog has a bone.</report>
 * </rule>
 *
 * @author  Ivelin Ivanov, ivelin@acm.org, ivelin@iname.com
 * @version CVS $Id: Rule.java,v 1.3 2004/03/05 13:02:37 bdelacretaz Exp $
 */
public class Rule {

    private String context_;
    private ArrayList asserts_ = new ArrayList();
    private ArrayList reports_ = new ArrayList();

    /**
     * Returns the context of the pattern.
     */
    public String getContext() {
        return context_;
    }

    /**
     * Sets the context of the pattern.
     */
    public void setContext(String newContext) {
        context_ = newContext;
    }

    /**
     * Returns the list of the assertion rules.
     */
    public List getAssert() {
        return asserts_;
    }

    /**
     * Sets the the list of the assertion rules.
     */
    public void setAssert(Collection newAsserts) {
        asserts_ = new ArrayList();
        asserts_.addAll(newAsserts);
    }

    /**
     * Add an assert rule.
     */
    public void addAssert(Assert a) {
        asserts_.add(a);
    }

    /**
     * Returns the list of the report rules.
     */
    public List getReport() {
        return reports_;
    }

    /**
     * Sets the list of the report rules.
     */
    public void setReport(Collection newReports) {
        reports_ = new ArrayList();
        reports_.addAll(newReports);
    }

    /**
     * Add a report rule.
     */
    public void addReport(Report r) {
        reports_.add(r);
    }
}
