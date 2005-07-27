/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;

/**
 * A factory for 'create-document:' sources (see {@link CreateDocumentSource}).
 * 
 * @version $Id:$
 * @since 2.1.8
 */
public class CreateDocumentSourceFactory 
    extends AbstractLogEnabled
    implements SourceFactory, ThreadSafe {
    
    /**
     * Get a {@link CreateDocumentSource} object.
     * 
     * @param location   The URI to resolve - this URI includes the scheme.
     * @param parameters this is optional and not used here
     *
     * @see org.apache.excalibur.source.SourceFactory#getSource(java.lang.String, java.util.Map)
     */
    public Source getSource( String location, Map parameters )
    throws IOException, MalformedURLException {
        return new CreateDocumentSource(location);
    }
    
    /**
     * @see org.apache.excalibur.source.SourceFactory#release(org.apache.excalibur.source.Source)
     */
    public void release( Source source ) {
        // Do nothing here
    }

}
