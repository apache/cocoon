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


/**
 * <p>The {@link Singleton} class represents a simple {@link Composer}
 * returning its own instance in the {@link #acquire()} method.</p> 
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public abstract class Singleton extends AbstractComposer {

    /**
     * <p>Create a new {@link Singleton} instance.</p>
     */
    protected Singleton() {
        super();
    }

    /**
     * <p>Acquire this {@link Singleton} instance.</p>
     *
     * @return <b>this</b> {@link Singleton} instance.
     */
    public final Object acquire()
    throws Throwable {
        return(this);
    }

    /**
     * <p>This method will not perform any operation.</p>
     */
    public final void release(Object object) {
        /* Nothing to do */
    }

    /**
     * <p>This method will not perform any operation.</p>
     */
    public final void dispose(Object object) {
        /* Nothing to do */
    }
}
