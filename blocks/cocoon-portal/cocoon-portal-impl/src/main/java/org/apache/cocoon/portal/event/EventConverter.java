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
 * This component converts {@link Event} objects from and into strings.
 * If the event is a {@link ConvertableEvent} the event itself does the
 * converting, if not it's up to the implementation of this component
 * to do the conversion.
 *
 * @version $Id$
 */
public interface EventConverter {

    /**
     * Encode an event.
     * This is used to "activate" events using a link
     * @param event The event to encode
     * @return A unique string representation for this event
     */
    String encode(Event event);

    /**
     * Decode an event.
     * This is used to "activate" events using a link
     * @param value The string representation created using {@link #encode(Event)}
     * @return The event or null 
     */
    Event decode(String value);
}
