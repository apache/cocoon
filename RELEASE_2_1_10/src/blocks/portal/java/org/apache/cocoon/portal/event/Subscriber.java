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
package org.apache.cocoon.portal.event;

/**
 * <tt>Subscriber</tt> registers its interest in a class of events and
 * filters the events of which it should be notified.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @author Mauro Talevi
 * @deprecated Use the {@link org.apache.cocoon.portal.event.Receiver} instead.
 *
 * @version CVS $Id$
 */
public interface Subscriber {

    /**
     *  Returns the event type of the event on which the Subscriber is interested.
     *  The event type is encoded by a <tt>Class</tt>.
     *
     *  @return the <tt>Class</tt> encoding the event type
     */
    Class getEventType();

    /**
     *  Returns the filter used to select the events in which the subscriber is
     *  interested.
     *
     *  @return the <tt>Filter</tt>
     */
    Filter getFilter();

    /**
     * Callback method informing the Subscriber of the occurence of an event.
     *
     * @param event the <tt>Event</tt> of which the <tt>Subscriber</tt> is informed
     */
    void inform( Event event );
}
