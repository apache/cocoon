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
package org.apache.cocoon.kernel;

import org.apache.cocoon.kernel.composition.Composer;
import org.apache.cocoon.kernel.composition.WiringException;
import org.apache.cocoon.kernel.composition.Wirings;
import org.apache.cocoon.kernel.configuration.ConfigurationException;
import org.apache.cocoon.kernel.configuration.Parameters;
import org.apache.cocoon.kernel.deployment.DeploymentException;

/**
 * <p>A {@link SimpleComposer} is a very simple implementation of the
 * {@link Composer} interface creating new instances each time one is
 * requested.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.5 $)
 */
public class SimpleComposer implements Composer {

    /** <p>Our instantiable {@link Class}.</p> */
    private Class clazz = null;
    
    /**
     * <p>Create a new instance of this {@link SimpleComposer}.</p>
     *
     * <p>This class will always throw a {@link DeploymentException}.</p>
     *
     * @throws UnsupportedOperationException always.
     */
    public SimpleComposer()
    throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Component class name must"
                                                + " be provided");
    }

    /**
     * <p>Create a new instance of this {@link SimpleComposer} creating
     * instances of the specified {@link Class}.</p>
     *
     * @param clazz the {@link Class} to use for instances.
     * @throws NullPointerException if the {@link Class} was <b>null</b>.
     * @throws IllegalArgumentException if the {@link Class} did not provide
     *                                  an accessible void constructor.
     */
    public SimpleComposer(Class clazz)
    throws NullPointerException, IllegalArgumentException {
        if (clazz == null)
            throw new NullPointerException("Null component class specified");
        try {
            clazz.getConstructor(new Class[0]);
        } catch (Exception e) {
            String message = "Component class \"" + clazz.getName() + "\" does"
                             + " not provide void constructor";
            IllegalArgumentException x = new IllegalArgumentException(message);
            throw ((IllegalArgumentException)x.initCause(e));
        }
        this.clazz = clazz;
    }

    /**
     * <p>Acquire an instance of an {@link Object} created (or recycled) by
     * this {@link Composer}.</p>
     *
     * <p>This implementation will simply create a new instance of the
     * {@link Class} specified at construction time.</p>
     *
     * @return a <b>non null</b> {@link Object} instance.
     * @throws Throwable if the {@link Object} can not be (for example)
     *                   instantiated or returned for whatever reason.
     */
    public Object acquire()
    throws Throwable {
        return(this.clazz.newInstance());
    }

    /**
     * <p>Normally release an {@link Object} instance previously acquired from
     * this {@link Composer}.</p>
     *
     * <p>This implementation will ignore this call.</b>
     *
     * @param object a <b>non null</b> {@link Object} instance as previously
     *               acquired from this {@link Composer} to release.
     */
    public void release(Object object) {
        /* Don't do anything. */
    }
    
    /**
     * <p>Abnormally dispose of an {@link Object} instance previously acquired
     * from this {@link Composer}.</p>
     *
     * <p>This implementation will ignore this call.</b>
     *
     * @param object a <b>non null</b> {@link Object} instance as previously
     *               acquired from this {@link Composer} to dispose.
     */
    public void dispose(Object object) {
        /* Don't do anything. */
    }

    /**
     * <p>Contextualize this {@link Composer} with the {@link Wirings}
     * associated to the block where it resides.</p>
     *
     * <p>This implementation will ignore this call.</b>
     *
     * @param wirings the {@link Wirings} instance associated with
     *                this {@link Composer}'s block.
     * @throws WiringException if there was an error performing operations
     *                              on the supplied {@link Wirings} instance.
     */
    public void contextualize(Wirings wirings)
    throws WiringException {
        /* Don't do anything. */
    }

    /**
     * <p>Configure this instance with the specified {@link Parameters}.</p>
     *
     * <p>This implementation will ignore this call.</b>
     *
     * @param parameters the {@link Parameters} configuring the instance.
     * @throws ConfigurationException if this instance could not be configured.
     */
    public void configure(Parameters parameters)
    throws ConfigurationException {
        /* Don't do anything. */
    }
}
