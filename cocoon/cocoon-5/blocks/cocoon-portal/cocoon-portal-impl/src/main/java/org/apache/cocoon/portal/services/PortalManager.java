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
package org.apache.cocoon.portal.services;

import java.util.Properties;

import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.services.aspects.AspectChain;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This is the main component that has currently two actions:
 * Invoke the request processing using the {@link #process()} method,
 * this starts the event handling for the current request (evaluating
 * request parameters etc.)
 * The section method {@link #render(ContentHandler, Properties)}
 * starts rendering the portal.
 *
 * @version $Id$
 */
public interface PortalManager {

    /** This property containing a layout id can be passed to the
     * {@link #render(ContentHandler, Properties)} method. In this
     * case only the tree starting with this layout object is rendered.
     */
    String PROPERTY_RENDER_LAYOUT = "render-layout";

    /** This property containing a coplet instance id can be passed to the
     * {@link #render(ContentHandler, Properties)} method. In this
     * case only the coplet with the surrounding layout is rendered.
     */
    String PROPERTY_RENDER_COPLET = "render-coplet";

    /**
     * Start the first phase of the request handling.
     * In this phase all events are fired and processed.
     * @throws PortalException
     */
    void process()
    throws PortalException;

    /**
     * Render the portal.
     * @param ch         The content handler receiving the sax events.
     * @param properties A properties object (can be null)
     * @throws SAXException
     */
    void render(ContentHandler ch,
                Properties     properties)
    throws SAXException;

    AspectChain getResponseProcessorAspectChain();

    AspectChain getRequestProcessorAspectChain();
}
