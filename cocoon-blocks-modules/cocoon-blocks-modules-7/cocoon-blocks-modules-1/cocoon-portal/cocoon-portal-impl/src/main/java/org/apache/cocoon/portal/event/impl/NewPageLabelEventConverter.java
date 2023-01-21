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
package org.apache.cocoon.portal.event.impl;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventConverter;
import org.apache.cocoon.portal.event.layout.ChangeTabEvent;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.NamedItem;

/**
 * This page label event converter converts only {@link ChangeTabEvent} events.
 *
 * @version $Id$
 * @since 2.2
 */
public class NewPageLabelEventConverter
    extends AbstractLogEnabled
    implements EventConverter, ThreadSafe {

    /**
     * @see org.apache.cocoon.portal.event.EventConverter#encode(org.apache.cocoon.portal.event.Event)
     */
    public String encode(Event event) {
        if ( event instanceof ChangeTabEvent ) {
            final Item item = ((ChangeTabEvent)event).getItem();
            final boolean useName = ((ChangeTabEvent)event).isUseName();
            if ( useName ) {
                return this.getPageLabel((NamedItem)item);
            } else {
                return String.valueOf(item.getParent().getItems().indexOf(item));
            }
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.event.EventConverter#decode(java.lang.String)
     */
    public Event decode(String value) {
        if ( value != null ) {
            // TODO
        }
        return null;
    }

    protected String getPageLabel(NamedItem item) {
        // first search parent
        Layout layout = item.getParent();
        NamedItem parent = null;
        while ( parent == null && layout != null ) {
            if ( layout.getParent() != null ) {
                if ( layout.getParent() instanceof NamedItem ) {
                    parent = (NamedItem)layout.getParent();
                } else {
                    layout = layout.getParent().getParent();
                }
            } else {
                layout = null;
            }
        }
        if ( parent != null ) {
            return this.getPageLabel(parent) + '.' + item.getName();
        }
        return item.getName();
    }
}