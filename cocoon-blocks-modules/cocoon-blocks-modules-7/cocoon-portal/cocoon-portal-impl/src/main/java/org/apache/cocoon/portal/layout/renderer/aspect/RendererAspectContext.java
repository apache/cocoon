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

import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutException;
import org.apache.cocoon.portal.services.aspects.BasicAspectContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * The renderer aspect context is passed to every renderer aspect.
 * Using this context, a renderer aspect can get it's configuration
 * and it can invoke (if wanted) the next aspect in the aspect chain.
 *
 * @version $Id$
 */
public interface RendererAspectContext extends BasicAspectContext {

    /**
     * Stream out raw layout 
     */
    void invokeNext(Layout layout, ContentHandler handler)
    throws SAXException, LayoutException;

    /** 
     * Get the "compiled" configuration of the aspect.
     */
    Object getAspectConfiguration();
}
