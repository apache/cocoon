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

import org.apache.cocoon.kernel.resolution.Resource;


/**
 * <p>The {@link WiringsWrapper} class is a simple wrapper around the
 * {@link Wirings} interface.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public class WiringsWrapper implements Wirings {

    /** <p>The wrapped {@link Wirings} instance. */
    protected Wirings instance = null;

    /**
     * <p>Create a new {@link WiringsWrapper} instance around a specified
     * {@link Wirings} instance.</p>
     * 
     * @param wirings the @{link Wirings} instance to wrap.
     * @throws NullPointerException if the specified instance was null.
     */
    public WiringsWrapper(Wirings wirings) {
        if (wirings == null) throw new NullPointerException("Null wirings");
        this.instance = wirings;
    }

    public Wire lookup(Class role, String name)
    throws WiringException {
        return(this.instance.lookup(role, name));
    }

    public Resource resolve(String name) {
        return(this.instance.resolve(name));
    }
}
