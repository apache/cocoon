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

import java.util.Locale;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A container {@link Widget} which can hold zero or more child widgets.
 *
 * @version $Id: Struct.java,v 1.3 2004/04/12 14:05:09 tim Exp $
 */
public class Struct extends AbstractContainerWidget {
    private static final String ELEMENT = "struct";
    
    private final StructDefinition definition; 

    public Struct(StructDefinition definition) {
        this.definition = definition;
    }
    
    protected WidgetDefinition getDefinition() {
        return this.definition;
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        generateSaxFragment(contentHandler, locale, ELEMENT);
    }
}
