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
package org.apache.cocoon.woody.samples.bindings;

/**
 * LenientOKBean
 * @author Marc Portier
 * @version $Id: LenientOKBean.java,v 1.4 2004/03/05 13:02:34 bdelacretaz Exp $
 */
public class LenientOKBean extends LenientBaseBean{

    public LenientOKBean(String initVal) {
        super(initVal);
    }

    /**
     * @return Returns the breakingField.
     */
    public String getBreakingField() {
        return breakingField;
    }

    /**
     * @param breakingField The breakingField to set.
     */
    public void setBreakingField(String breakingField) {
        this.breakingField = breakingField;
    }
}
