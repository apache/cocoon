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

import org.apache.cocoon.kernel.resolution.Resolver;


/**
 * <p>An abstract implementation of the {@link Component} interface.</p> 
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.2 $)
 */
public class AbstractComponent implements Component {
 
    /** <p>The {@link Wire} instance contextualized in this instance.</p> */
    protected Wire wire = null;

    /** <p>The {@link Resolver} instance contextualized in this instance.</p> */
    protected Resolver resolver = null;

    /**
     * <p>Create a new {@link AbstractComposer} instance.</p>
     */
    public AbstractComponent() {
        super();
    }

    /**
     * <p>Contextualize this {@link Component} instance.</p>
     *
     * <p>The contextualized {@link Wire} and {@link Resolver} instances will
     * be available in the {@link #wire} and {@link #resolver} fields
     * respectively.</p>
     *
     * @param wire the {@link Wire} instance through which the block requesting
     *             this instance is accessing it.
     * @param resolver the {@link Resolver} instance resolving resources in the
     *                 calling block instance.
     */
    public void contextualize(Wire wire, Resolver resolver) {
        this.resolver = resolver;
        this.wire = wire;
    }
}
