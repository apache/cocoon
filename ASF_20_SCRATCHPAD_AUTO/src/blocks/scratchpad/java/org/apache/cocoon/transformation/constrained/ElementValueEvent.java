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
package org.apache.cocoon.transformation.constrained;

import org.xml.sax.Attributes;

/**
 *
 * @author <a href="mailto:nicolaken@apache.org">Nicola Ken Barozzi</a> 
 * @version CVS $Id: ElementValueEvent.java,v 1.3 2004/03/05 10:07:26 bdelacretaz Exp $
 */
public class ElementValueEvent extends ContainerElementEndEvent {

    private String     elementValue;
    private Attributes elementAttributes;

    public ElementValueEvent(
            Object source, String elementName, String elementValue,
            Attributes elementAttributes) {

        super(source, elementName);

        this.elementValue      = elementValue;
        this.elementAttributes = elementAttributes;
    }



    public String getElementValue() {
        return elementValue;
    }

    public Attributes getAttributes() {
        return elementAttributes;
    }

    public String getAttribute(String attributeName) {
        return elementAttributes.getValue(attributeName).toString();
    }
}
