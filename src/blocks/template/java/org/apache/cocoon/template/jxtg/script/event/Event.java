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
package org.apache.cocoon.template.jxtg.script.event;

import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

public class Event {
    protected final Locator location;
    protected Event next; // in document order

    public static final Locator NULL_LOCATOR = new LocatorImpl();

    public Event(Locator locator) {
        this.location = locator != null ? new LocatorImpl(locator) : NULL_LOCATOR;
    }

    public final Locator getLocation() {
        return location;
    }

    public Event getNext() {
        return next;
    }

    public void setNext(Event next) {
        this.next = next;
    }

    public String locationString() {
        StringBuffer buf = new StringBuffer();
        buf.append(location.getSystemId());
        if (buf.length() > 0) {
            buf.append(", ");
        }
        buf.append("Line " + location.getLineNumber());
        int col = location.getColumnNumber();
        if (col > 0) {
            buf.append("." + col);
        }
        return buf.toString();
    }
}
