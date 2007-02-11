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
package org.apache.cocoon.generation;

/**
 * A generator factory is the factory of {@link Generator}s.
 *
 * <p>Regular GeneratorFactory implementation should be
 * {@link org.apache.avalon.framework.thread.ThreadSafe} component
 * serving as a factory of lightweight {@link Generator} objects.</p>
 *
 * <p>GeneratorFactory can implement any number of Avalon lifecycle interfaces
 * and perform any initializations necessary. Ligtweight Generator instances
 * created by {@link #getInstance()} method will only need to parse
 * additional parameters passed on sitemap component invocation via
 * {@link org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, String, org.apache.avalon.framework.parameters.Parameters)}
 * method and can access global configuration of GeneratorFactory.</p>
 *
 * <p><strong>NOTE:</strong> Only Disposable interface is applicable to
 * the Generator instance returned by the {@link #getInstance()}.</p>
 *
 * @since 2.2
 * @version $Id$
 */
public interface GeneratorFactory {

    String ROLE = Generator.ROLE;

    /**
     * Instance of the Generator created by the GeneratorFactory
     * 
     * @cocoon.sitemap.component.documentation.disabled
     */
    interface Instance extends Generator {

        /**
         * @return GeneratorFactory which created this Generator instance
         */
        GeneratorFactory getFactory();
    }

    /**
     * Create an instance of the Generator
     */
    Instance getInstance();
}
