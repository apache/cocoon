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
package org.apache.cocoon.kernel.deployment;

import java.util.Iterator;
import org.apache.cocoon.kernel.configuration.Configurable;
import org.apache.cocoon.kernel.configuration.Parameters;

/**
 * <p>The {@link Instance} interface represents a deployable or deployed block
 * instance.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.3 $)
 */
public interface Instance extends Configurable {

    /**
     * <p>Return the {@link Block} associated with this deployable block
     * {@link Instance}.</p>
     */
    public Block block();

    /**
     * <p>Return the informational {@link String} name with wich this instance
     * was created.</p>
     */
    public String name();

    /**
     * <p>Return the {@link Parameters} configured in this instance.</p>
     *
     * @return a <b>non null</b> {@link Parameters} instance.
     */
    public Parameters configuration();

    /**
     * <p>Wire this {@link Instance} to another.</p>
     *
     * <p>This method can be called multiple times on each {@link Instance}.</p>
     *
     * @param name the wiring name as specified by the block requirements.
     * @param target the target {@link Instance} of the wiring operation.
     */
    public void wire(String name, Instance target);

    /**
     * <p>Return an {@link Iterator} over all configured wiring names.</p>
     *
     * @return a <b>non null</b> {@link Iterator} over {@link String} instances.
     */
    public Iterator wirings();

    /**
     * <p>Return the {@link Instance} wired to this one with the specified
     * name.</p>
     *
     * @return an {@link Instance} or <b>null</b> if the wiring was not found.
     */
    public Instance wiring(String name);

    /**
     * <p>Return whether this instance was deployed or not.</p>
     *
     * @return always false.
     */
    public boolean deployed();
}
