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
package org.apache.cocoon.sitemap;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;

/**
 * This interface marks a component as a sitemap component that is used
 * in the processing phase, like a generator, a transformer, a serializer
 * or a reader.
 * 
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @version CVS $Id: SitemapModelComponent.java,v 1.4 2004/03/08 14:03:30 cziegeler Exp $
 */
public interface SitemapModelComponent {
    /**
     * Set the <code>SourceResolver</code>, objectModel <code>Map</code>,
     * the source and sitemap <code>Parameters</code> used to process the request.
     */
    void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) 
    throws ProcessingException, SAXException, IOException;
}
