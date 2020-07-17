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
package org.apache.cocoon.sitemap;

import org.apache.avalon.framework.component.Component;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This interface marks a component as a sitemap component that produces
 * a response, like a serializer or a reader.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id$
 */
public interface SitemapOutputComponent extends Component {

    /**
     * Set the {@link OutputStream} where the requested resource should
     * be serialized.
     */
    void setOutputStream(OutputStream out) throws IOException;

    /**
     * Get the media type of the output of this <code>Component</code>
     * to be used in <code>Content-Type</code> header in the response.
     * <br>
     * Example content type value: <code>text/html; charset=utf-8</code>.
     * <br>
     * The returned value is used if no <code>mime-type</code> attribute is set
     * in the sitemap.
     *
     * @see org.apache.cocoon.components.pipeline.AbstractProcessingPipeline#setMimeTypeForSerializer
     * @see org.apache.cocoon.components.pipeline.AbstractProcessingPipeline#setMimeTypeForReader(org.apache.cocoon.environment.Environment) 
     */
    String getMimeType();

    /**
     * Test if the component wants to set the content length
     */
    boolean shouldSetContentLength();
}
