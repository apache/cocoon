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
package org.apache.cocoon.environment;

import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.xml.sax.SAXException;

/**
 * Base interface for resolving a source by system identifiers. This 
 * component is a special extension of the Avalon Excalibur 
 * {@link org.apache.excalibur.source.SourceResolver} that is only
 * used for Cocoon sitemap components.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: SourceResolver.java,v 1.3 2004/03/05 13:02:54 bdelacretaz Exp $
 */

public interface SourceResolver
extends org.apache.excalibur.source.SourceResolver {

    /**
     * Resolve the source.
     * @param systemID This is either a system identifier
     * (<code>java.net.URL</code> or a local file.
     * @deprecated Use the resolveURI methods instead
     */
    Source resolve(String systemID)
    throws ProcessingException, SAXException, IOException;

}

