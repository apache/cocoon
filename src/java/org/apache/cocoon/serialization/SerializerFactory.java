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
package org.apache.cocoon.serialization;

import org.apache.avalon.framework.component.Component;

/**
 * A serializer factory is the factory of {@link Serializer}s.
 *
 * <p>Regular SerializerFactory implementation should be
 * {@link org.apache.avalon.framework.thread.ThreadSafe} component
 * serving as a factory of lightweight {@link Serializer} objects.</p>
 *
 * <p><strong>NOTE:</strong> Only Disposable interface is applicable to
 * the Serializer instance returned by the {@link #getInstance()}.</p>
 *
 * @since 2.2
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id$
 */
public interface SerializerFactory extends Component {

    String ROLE = Serializer.ROLE;

    /**
     * Instance of the Serializer created by the SerializerFactory
     */
    interface Instance extends Serializer {

        /**
         * @return SerializerFactory which created this Serializer instance
         */
        SerializerFactory getFactory();
    }

    /**
     * Create an instance of the Serializer
     */
    Instance getInstance();
}
