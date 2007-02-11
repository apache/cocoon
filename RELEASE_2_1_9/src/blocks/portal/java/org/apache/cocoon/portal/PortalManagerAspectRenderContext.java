/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * @since 2.1.8
 * @version SVN $Id$
 */
public interface PortalManagerAspectRenderContext {

    /**
     * Invoke next aspect 
     */
    void invokeNext(ContentHandler ch,
                    Parameters     parameters)
    throws SAXException;

    /** 
     * Get the {@link Parameters} of the aspect.
     */
    Parameters getAspectParameters();

    /**
     * Get the object model.
     */
    Map getObjectModel();
}
