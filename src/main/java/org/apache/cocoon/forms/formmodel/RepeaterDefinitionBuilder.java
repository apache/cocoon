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

import java.util.Iterator;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.forms.event.RepeaterListener;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Builds {@link RepeaterDefinition}s.
 * 
 * @version $Id$
 */
public final class RepeaterDefinitionBuilder extends AbstractContainerDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element repeaterElement) throws Exception {
        
        int initialSize = DomHelper.getAttributeAsInteger(repeaterElement, "initial-size", 0);
        int minSize = DomHelper.getAttributeAsInteger(repeaterElement, "min-size", 0);
        int maxSize = DomHelper.getAttributeAsInteger(repeaterElement, "max-size", Integer.MAX_VALUE);

        // should throw error on negative values ? Just correct them for now. 
        if (minSize < 0) {
            throw new ConfigurationException("min-size should be positive, at " + DomHelper.getLocationObject(repeaterElement));
        }
        
        if (maxSize < 0) {
            throw new ConfigurationException("max-size should be positive, at " + DomHelper.getLocationObject(repeaterElement));
        }
        
        if (maxSize < minSize) {
            throw new ConfigurationException("max-size should be greater that or equal to min-size, at " + DomHelper.getLocationObject(repeaterElement));
        }

        // initial size is at least the min size
        initialSize = minSize > initialSize ? minSize : initialSize;
        
        boolean orderable = DomHelper.getAttributeAsBoolean(repeaterElement, "orderable", false);
        boolean selectable = DomHelper.getAttributeAsBoolean(repeaterElement, "selectable", false);

        RepeaterDefinition repeaterDefinition = new RepeaterDefinition(initialSize, minSize, maxSize, selectable, orderable);
        super.setupDefinition(repeaterElement, repeaterDefinition);
        setDisplayData(repeaterElement, repeaterDefinition);

        // parse "on-repeater-modified"
        Iterator iter = buildEventListeners(repeaterElement, "on-repeater-modified", RepeaterListener.class).iterator();
        while (iter.hasNext()) {
            repeaterDefinition.addRepeaterListener((RepeaterListener)iter.next());
        }        
        
        setupContainer(repeaterElement,"widgets",repeaterDefinition);

        repeaterDefinition.makeImmutable();
        return repeaterDefinition;
    }
}
