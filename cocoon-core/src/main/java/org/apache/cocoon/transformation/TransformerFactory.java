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
package org.apache.cocoon.transformation;

/**
 * A transformer factory is the factory of {@link Transformer}s.
 *
 * <p>Regular TransformerFactory implementation should be
 * {@link org.apache.avalon.framework.thread.ThreadSafe} component
 * serving as a factory of lightweight {@link Transformer} objects.</p>
 *
 * <p><strong>NOTE:</strong> Only Disposable interface is applicable to
 * the Transformer instance returned by the {@link #getInstance()}.</p>

 * @since 2.2
 * @version $Id$
 */
public interface TransformerFactory {

    String ROLE = Transformer.ROLE;

    /**
     * Instance of the Transformer created by the TransformerFactory
     */
    interface Instance extends Transformer {

        /**
         * @return TransformerFactory which created this Transformer instance
         */
        TransformerFactory getFactory();
    }

    /**
     * Create an instance of the Transformer
     */
    Instance getInstance();
}
