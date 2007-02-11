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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a Schematron pattern.
 *
 * @author  Ivelin Ivanov, ivelin@acm.org, ivelin@iname.com
 * @version CVS $Id: Pattern.java,v 1.2 2004/03/05 13:01:58 bdelacretaz Exp $
 */
public class Pattern {

    private String name_;
    private String id_;
    private ArrayList rules_ = new ArrayList();

    /**
     * Returns the id of the pattern.
     */
    public String getId() {
        return id_;
    }

    /**
     * Sets the id of the pattern.
     */
    public void setId(String newId) {
        id_ = newId;
    }

    /**
     * Returns the name of the pattern.
     */
    public String getName() {
        return name_;
    }

    /**
     * Sets the name of the pattern.
     */
    public void setName(String newName) {
        name_ = newName;
    }

    /**
     * Returns the list of rules.
     */
    public List getRule() {
        return rules_;
    }

    /**
     * Sets the list of rules.
     */
    public void setRule(Collection newRules) {
        rules_ = new ArrayList();
        rules_.addAll(newRules);
    }

    /**
     * Add a rule to the list.
     */
    public void addRule(Rule r) {
        rules_.add(r);
    }
}
