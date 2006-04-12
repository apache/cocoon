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
package org.apache.cocoon.portal.coplet.adapter;

import java.util.Collections;
import java.util.List;

import org.apache.cocoon.portal.event.Event;

/**
 * @version $Id$
 */
public final class DecorationAction {

    protected String name;
    protected List   events;

    public DecorationAction(String name, List events) {
        this.name = name;
        this.events = events;
    }

    public DecorationAction(String name, Event event) {
        this.name = name;
        this.events = Collections.singletonList(event);
    }

    public String getName() {
        return this.name;
    }

    public List getEvents() {
        return this.events;
    }
}