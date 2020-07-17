/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.FormsException;
import org.apache.cocoon.forms.event.RepeaterListener;
import org.apache.cocoon.forms.util.DomHelper;

import org.w3c.dom.Element;

/**
 * Builds {@link RepeaterDefinition}s.
 *
 * @version $Id$
 */
public final class RepeaterDefinitionBuilder extends AbstractContainerDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element repeaterElement, WidgetDefinitionBuilderContext context)
    throws Exception {

        int initialSize = DomHelper.getAttributeAsInteger(repeaterElement, "initial-size", 0);
        int minSize = DomHelper.getAttributeAsInteger(repeaterElement, "min-size", 0);
        int maxSize = DomHelper.getAttributeAsInteger(repeaterElement, "max-size", Integer.MAX_VALUE);

        // should throw error on negative values ? Just correct them for now.
        if (minSize < 0) {
            throw new FormsException("min-size should be positive.",
                                     DomHelper.getLocationObject(repeaterElement));
        }

        if (maxSize < 0) {
            throw new FormsException("max-size should be positive.",
                                     DomHelper.getLocationObject(repeaterElement));
        }

        if (maxSize < minSize) {
            throw new FormsException("max-size should be greater than or equal to min-size.",
                                     DomHelper.getLocationObject(repeaterElement));
        }

        // initial size is at least the min size
        initialSize = minSize > initialSize ? minSize : initialSize;

        boolean orderable = DomHelper.getAttributeAsBoolean(repeaterElement, "orderable", false);
        boolean selectable = DomHelper.getAttributeAsBoolean(repeaterElement, "selectable", false);
        boolean enhanced = DomHelper.getAttributeAsBoolean(repeaterElement, "enhanced", false);

        int initialPage = 0;
        int pageSize = Integer.MAX_VALUE;
        String customPageId = null;

        Element pagesElement = DomHelper.getChildElement(repeaterElement, FormsConstants.DEFINITION_NS, "pages");
        if (pagesElement!=null) {
            enhanced = true;
            initialPage = DomHelper.getAttributeAsInteger(pagesElement, "initial", 1) - 1;
            pageSize = DomHelper.getAttributeAsInteger(pagesElement, "size", 0);
            customPageId = DomHelper.getAttribute(pagesElement, "page-field", null);
        }

        RepeaterDefinition repeaterDefinition = new RepeaterDefinition(initialSize, minSize, maxSize, selectable, orderable, enhanced, initialPage, pageSize, customPageId);
        setupDefinition(repeaterElement, repeaterDefinition, context);
        setDisplayData(repeaterElement, repeaterDefinition);

        // parse "on-repeater-modified"
        Iterator i = buildEventListeners(repeaterElement, "on-repeater-modified", RepeaterListener.class).iterator();
        while (i.hasNext()) {
            repeaterDefinition.addRepeaterListener((RepeaterListener) i.next());
        }

        setupContainer(repeaterElement, "widgets", repeaterDefinition, context);

        repeaterDefinition.makeImmutable();
        return repeaterDefinition;
    }
}
