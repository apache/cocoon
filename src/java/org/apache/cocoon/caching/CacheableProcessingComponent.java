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
package org.apache.cocoon.caching;

import org.apache.excalibur.source.SourceValidity;
import java.io.Serializable;

/**
 * This marker interface declares a (sitemap) component as cacheable.
 *
 * This interface deprecates the org.apache.cocoon.caching.Cacheable interface!
 *
 * @since 2.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CacheableProcessingComponent.java,v 1.3 2004/03/08 13:57:40 cziegeler Exp $
 */
public interface CacheableProcessingComponent {

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     * This method must be invoked before the getValidity() method.
     *
     * @return The generated key or <code>null</code> if the component
     *              is currently not cacheable.
     */
    Serializable getKey();

    /**
     * Generate the validity object.
     * Before this method can be invoked the getKey() method
     * must be invoked.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    SourceValidity getValidity();
}
