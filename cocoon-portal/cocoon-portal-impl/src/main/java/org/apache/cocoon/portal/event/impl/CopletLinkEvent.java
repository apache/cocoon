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
package org.apache.cocoon.portal.event.impl;

import org.apache.cocoon.portal.event.coplet.AbstractCopletInstanceEvent;
import org.apache.cocoon.portal.om.CopletInstance;

/**
 * This class realizes a link event created by the EventLinkTransformer.
 *
 * @version $Id$
 */
public class CopletLinkEvent
    extends AbstractCopletInstanceEvent {

    /**
     * The link to be handled by this event.
     */
    protected String link;

    /**
     * Creates a new LinkEvent.
     */
    public CopletLinkEvent(CopletInstance target, String link) {
        super(target);
        this.link = link;
    }

    /**
     * Gets this event's link.
     */
    public String getLink() {
        return this.link;
    }
}
