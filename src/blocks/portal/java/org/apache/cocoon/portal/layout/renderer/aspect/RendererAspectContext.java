/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.layout.renderer.aspect;

import java.util.Map;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.layout.Layout;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * The renderer aspect context is passed to every renderer aspect.
 * Using this context, a renderer aspect can get it's configuration
 * and it can invoke (if wanted) the next aspect in the aspect chain.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: RendererAspectContext.java,v 1.5 2004/03/05 13:02:13 bdelacretaz Exp $
 */
public interface RendererAspectContext {
    
    /**
     * Stream out raw layout 
     */
    void invokeNext(Layout layout, PortalService service, ContentHandler handler)
    throws SAXException;

    /** 
     * Get the "compiled" configuration of the aspect.
     */
    Object getAspectConfiguration();
    
    /**
     * Set an attribute
     */
    void setAttribute(String key, Object attribute);

    /**
     * Get an attribute
     */
    Object getAttribute(String key);

    /**
     * Remove an attribute
     */
    void removeAttribute(String key);
    
    /**
     * Get the object model
     */
    Map getObjectModel();
}
