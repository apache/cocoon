/*
 * Copyright 2004, Ugo Cei.
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
package org.apache.butterfly.sitemap;

import java.io.OutputStream;

/**
 * This interface marks a component as a sitemap component that produces
 * a response, like a serializer or a reader.
 * 
 * @version CVS $Id: SitemapOutputComponent.java,v 1.1 2004/07/23 08:47:20 ugo Exp $
 */
public interface SitemapOutputComponent {

    /**
     * Set the {@link OutputStream} where the requested resource should
     * be serialized.
     */
    void setOutputStream(OutputStream out);

    /**
     * Get the mime-type of the output of this <code>Component</code>.
     */
    String getMimeType();

    /**
     * Test if the component wants to set the content length
     */
    boolean shouldSetContentLength();
}
