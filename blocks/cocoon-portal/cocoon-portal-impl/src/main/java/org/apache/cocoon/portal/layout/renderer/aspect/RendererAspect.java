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
package org.apache.cocoon.portal.layout.renderer.aspect;

import java.util.Properties;

import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A renderer aspect extends a renderer with a distinct functionality.
 * It can add a particular decoration like a border or buttons for example.
 * A renderer aspect has to be thread safe!
 *
 * @version $Id$
 */
public interface RendererAspect {

    String ROLE = RendererAspect.class.getName();

    /**
     * Stream out raw layout 
     */
    void toSAX(RendererAspectContext context,
               Layout                layout, 
               ContentHandler        handler)
    throws SAXException, LayoutException;

    /**
     * Compile the configuration.
     * A renderer aspect can "compile" the configuration in
     * order to increase performance.
     * If the renderer does not want to compile it should
     * simply return the configuration.
     * The "compiled" configuration is available during streaming via the context object.
     * This method can also be used for validating the configuration.
     */
    Object prepareConfiguration(Properties configuration)
    throws PortalException;
}
