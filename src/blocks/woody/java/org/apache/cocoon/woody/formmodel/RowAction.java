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
package org.apache.cocoon.woody.formmodel;

import java.util.Locale;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: RowAction.java,v 1.4 2004/03/09 13:53:56 reinhard Exp $
 */
public class RowAction extends Action {
    public RowAction(RowActionDefinition definition) {
        super(definition);
    }
    
    public static class MoveUpAction extends RowAction {
        public MoveUpAction(RowActionDefinition.MoveUpDefinition definition) {
            super(definition);
        }

        public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
            
            // Only generate if we're not at the top
            Repeater.RepeaterRow row = Repeater.getParentRow(this);
            if (((Repeater)row.getParent()).indexOf(row) > 0) {
                super.generateSaxFragment(contentHandler, locale);
            }
        }
    }

    public static class MoveDownAction extends RowAction {
        public MoveDownAction(RowActionDefinition.MoveDownDefinition definition) {
            super(definition);
        }

        public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
            
            // Only generate if we're not at the bottom
            Repeater.RepeaterRow row = Repeater.getParentRow(this);
            Repeater repeater = (Repeater)row.getParent();
            
            if (repeater.indexOf(row) < repeater.getSize() - 1) {
                super.generateSaxFragment(contentHandler, locale);
            }
        }
    }
}

