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
package org.apache.cocoon.forms.formmodel;


/**
 * A container {@link Widget} which can hold zero or more child widgets.
 *
 * @version $Id: Struct.java,v 1.4 2004/04/20 22:19:27 mpo Exp $
 */
public class Struct extends AbstractContainerWidget {
    private static final String STRUCT_EL = "struct";
    
    private final StructDefinition definition; 

    public Struct(StructDefinition definition) {
        this.definition = definition;
    }
    
    protected WidgetDefinition getDefinition() {
        return this.definition;
    }
    
    /**
     * @return "struct"
     */
    public String getXMLElementName() {
        return STRUCT_EL;
    }
    
}
