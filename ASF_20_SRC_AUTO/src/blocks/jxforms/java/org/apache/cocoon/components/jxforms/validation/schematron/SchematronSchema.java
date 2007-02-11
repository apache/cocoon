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

import org.apache.cocoon.components.jxforms.validation.Schema;
import org.apache.cocoon.components.jxforms.validation.Validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a Schematron Schema.
 *
 * Specification:
 * http://www.ascc.net/xml/resource/schematron/Schematron2000.html
 *
 * @author  Ivelin Ivanov, ivelin@acm.org, ivelin@iname.com
 * @version CVS $Id: SchematronSchema.java,v 1.2 2004/03/05 13:01:58 bdelacretaz Exp $
 */
public class SchematronSchema implements Schema {

    private String title_;
    private ArrayList patterns_ = new ArrayList();
    private ArrayList phases_ = new ArrayList();

    /**
     * Returns the message for to the element.
     */
    public String getTitle() {
        return title_;
    }

    /**
     * Sets the message for to the element.
     */
    public void setTitle(String newTitle) {
        title_ = newTitle;
    }

    /**
     * Returns a list of the patterns which
     * contain messages that failed during validation.
     */
    public List getPattern() {
        return patterns_;
    }

    /**
     * Sets the list of the patterns which
     * contain messages that failed during validation.
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

    /**
     * Returns the list of schema phases.
     */
    public List getPhase() {
        return phases_;
    }

    /**
     * Sets the list of schema phases.
     */
    public void setPhase(Collection newPhases) {
        phases_ = new ArrayList();
        phases_.addAll(newPhases);
    }

    /**
     * Add a pattern to the list.
     */
    public void addPhase(Phase p) {
        phases_.add(p);
    }

    public Validator newValidator() throws InstantiationException {
        return new SchematronValidator(this);
    }
}
