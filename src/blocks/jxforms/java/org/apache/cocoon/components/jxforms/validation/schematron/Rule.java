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
package org.apache.cocoon.components.jxforms.validation.schematron;

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
 * @version CVS $Id: Rule.java,v 1.1 2003/07/12 19:22:30 coliver Exp $
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
