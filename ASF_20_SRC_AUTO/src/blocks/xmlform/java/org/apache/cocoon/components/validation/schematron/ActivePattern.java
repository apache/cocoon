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

/**
 * Represents a Schematron phase
 * <active pattern="some"> element.
 *
 * @author  Ivelin Ivanov, ivelin@acm.org, ivelin@iname.com
 * @version CVS $Id: ActivePattern.java,v 1.3 2004/03/05 13:02:37 bdelacretaz Exp $
 */
public class ActivePattern {

    private String pattern_;

    /**
     * Returns the active pattern name
     */
    public String getPattern() {
        return pattern_;
    }

    /**
     * Sets the active pattern name
     */
    public void setPattern(String pattern) {
        pattern_ = pattern;
    }
}
