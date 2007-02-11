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
 * Represents a Schematron phase element.
 *
 * Example:
 * <phase id="basicValidation">
 *   <active pattern="text" />
 *   <active pattern="tables" />
 *   <active pattern="attributePresence" />
 * </phase>
 *
 * @author  Ivelin Ivanov, ivelin@acm.org, ivelin@iname.com
 * @version CVS $Id: Phase.java,v 1.2 2004/03/05 13:01:58 bdelacretaz Exp $
 */
public class Phase {

    private String id_;
    private ArrayList active_ = new ArrayList();

    /**
     * Returns the id of the phase.
     */
    public String getId() {
        return id_;
    }

    /**
     * Sets the id of the phase.
     */
    public void setId(String newId) {
        id_ = newId;
    }

    /**
     * Returns the list of active patterns.
     */
    public List getActive() {
        return active_;
    }

    /**
     * Sets the list of active patterns.
     */
    public void setActive(Collection newActivePatterns) {
        active_ = new ArrayList();
        active_.addAll(newActivePatterns);
    }

    /**
     * Add a pattern to the list of active patterns.
     */
    public void addActive(ActivePattern p) {
        active_.add(p);
    }
}
