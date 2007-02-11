/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.webapps.session.context;

import org.apache.cocoon.ProcessingException;

/**
 * Interface for a context provider.
 * Objects of this class provide special context, e.g. authentication or portal.
 * The provider has to take care that the context is instantiated and managed
 * correctly: for example a request context object should only created once
 * per request, an authentication context once per session etc.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: SessionContextProvider.java,v 1.4 2004/03/05 13:02:22 bdelacretaz Exp $
*/
public interface SessionContextProvider {

    String ROLE = SessionContextProvider.class.getName();
    
    /**
     * Get the context
     * @param name The name of the context
     * @return The context
     * @throws ProcessingException If the context is not available.
     */
    SessionContext getSessionContext(String name)
    throws ProcessingException;
    
    /**
     * Does the context exist?
     */
    boolean existsSessionContext(String name)
    throws ProcessingException;

}
