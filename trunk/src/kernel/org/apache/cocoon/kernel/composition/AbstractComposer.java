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

import org.apache.cocoon.kernel.configuration.Parameters;


/**
 * <p>An abstract implementation of the {@link Composer} interface.</p> 
 *
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public abstract class AbstractComposer implements Composer {

    /** <p>The {@link Wirings} instance contextualized in this instance.</p> */
    protected Wirings wirings = null;

    /** <p>The {@link Parameters} instance configured in this instance.</p> */
    protected Parameters parameters = null;

    /**
     * <p>Create a new {@link AbstractComposer} instance.</p>
     */
    public AbstractComposer() {
        super();
    }

    /**
     * <p>This method will simply invoke {@link #dispose(Object)}. </p>
     */
    public void release(Object object) {
        this.dispose(object);
    }

    /**
     * <p>Contextualize this instance and store the supplied {@link Wirings}
     * instance.</p>
     * 
     * <p>The contextualized {@link Wirings} instance will be available in the
     * protected {@link #wirings} field.</p>
     * 
     * @param wirings the {@link Wirings} contextualizing this instance.
     * @throws Exception if an error occurred contextualizing this instance.
     */
    public void contextualize(Wirings wirings)
    throws Exception {
        this.wirings = wirings;
    }

    /**
     * <p>Configure this instance and store the supplied {@link Parameters}
     * instance.</p>
     * 
     * <p>The configure {@link Parameters} instance will be available in the
     * protected {@link #parameters} field.</p>
     * 
     * @param parameters the {@link Parameters} configuring this instance.
     * @throws Exception if an error occurred configuring this instance.
     */
    public void configure(Parameters parameters)
    throws Exception {
        this.parameters = parameters;
    }
}
