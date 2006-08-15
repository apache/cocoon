/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.layout.renderer;

import org.apache.cocoon.portal.LayoutException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.om.Layout;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A renderer is responsible for rendering a layout object.
 *
 * @version $Id$
 */
public interface Renderer {

    String ROLE = Renderer.class.getName();

    /**
     * Stream out raw layout 
     */
    void toSAX(Layout layout, PortalService service, ContentHandler handler)
    throws SAXException, LayoutException;
}
