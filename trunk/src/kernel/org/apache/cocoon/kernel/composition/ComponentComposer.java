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
package org.apache.cocoon.kernel.composition;

import org.apache.cocoon.kernel.configuration.ConfigurationException;
import org.apache.cocoon.kernel.configuration.Parameters;


/**
 * <p>The {@link ComponentComposer} class represents a simple {@link Composer}
 * returning its own instance in the {@link #acquire()} method.</p> 
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public abstract class ComponentComposer implements Composer {

    /** <p>The contextualized {@link Wirings} instance.</p> */
    protected Wirings wirings = null;

    /** <p>The configured {@link Parameters} instance.</p> */
    protected Parameters parameters = null;

    /**
     * <p>Create a new {@link ComponentComposer} instance.</p>
     */
    protected ComponentComposer() {
        super();
    }

    /**
     * <p>Acquire this {@link ComponentComposer} instance.</p>
     *
     * @return this {@link ComponentComposer} instance.
     */
    public Object acquire()
    throws Throwable {
        return(this);
    }

    /**
     * <p>Release a previously acquired {@link ComponentComposer} instance.</p>
     * 
     * <p>This method does not do anything.</p>
     *
     * @return this {@link ComponentComposer} instance.
     */
    public void release(Object object) {
        /* Nothing to do here */
    }

    /**
     * <p>Dispose a previously acquired {@link ComponentComposer} instance.</p>
     * 
     * <p>This method simply invokes the {@link #release()} method.</p>
     *
     * @return this {@link ComponentComposer} instance.
     */
    public void dispose(Object object) {
        this.release(object);
    }

    /**
     * <p>Contextualize this {@link ComponentComposer} with the {@link Wirings}
     * associated to the block where it resides.</p>
     *
     * @param wirings the {@link Wirings} instance associated with the block in
     *                which this {@link ComponentComposer} was deployed.
     * @throws WiringException if there was an error performing operations
     *                         on the supplied {@link Wirings} instance.
     */
    public void contextualize(Wirings wirings)
    throws WiringException {
        this.wirings = wirings;
     }

    /**
     * <p>Configure this {@link ComponentComposer} instance with the specified
     * {@link Parameters}.</p>
     *
     * @param parameters the {@link Parameters} configuring the instance.
     * @throws ConfigurationException if this instance could not be configured.
     */
    public void configure(Parameters parameters)
    throws ConfigurationException {
        this.parameters = parameters;
    }
}
