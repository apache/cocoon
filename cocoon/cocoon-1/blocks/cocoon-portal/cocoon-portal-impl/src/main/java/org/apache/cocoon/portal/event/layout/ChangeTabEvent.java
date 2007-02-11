/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.portal.event.layout;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.LayoutFeatures;
import org.apache.cocoon.portal.layout.NamedItem;

/**
 *
 * @version $Id$
 * @since 2.2
 */
public class ChangeTabEvent
    extends LayoutChangeParameterEvent {

    public static final String TAB_TEMPORARY_ATTRIBUTE_NAME = "tab";

    protected Item item;

    protected boolean useName;

    public ChangeTabEvent(PortalService service, String eventData) {
        super(service, eventData);
    }

    public ChangeTabEvent(Item target, boolean useName) {
        super(target.getParent(), LayoutFeatures.ATTRIBUTE_TAB, (useName ? ((NamedItem)target).getName(): String.valueOf(target.getParent().getItems().indexOf(target))), true);
        this.item = target;
        this.useName = useName;
    }

    public Item getItem() {
        return item;
    }

    public boolean isUseName() {
        return useName;
    }
}
