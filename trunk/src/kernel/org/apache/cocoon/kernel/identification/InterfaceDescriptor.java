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
package org.apache.cocoon.kernel.identification;

import org.apache.cocoon.kernel.configuration.Configuration;
import org.apache.cocoon.kernel.configuration.ConfigurationException;

/**
 * <p>The {@link InterfaceDescriptor} class defines an interface block
 * {@link Descriptor}.</p>
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.4 $)
 */
public class InterfaceDescriptor extends AbstractDescriptor {

    /** <p>The {@link Map} of all our exposures.</p> */
    private String exposition = null;

    /**
     * <p>Create a new {@link InterfaceDescriptor} instance.</p>
     *
     * @param configuration a {@link Configuration} element with details of
     *                      this {@link Descriptor} characteristics.
     * @throws ConfigurationException if the specified {@link Configuration}
     *                                did not represent a valid descriptor.
     * @throws IdentificationException if the block identifier gathered from
     *                                 the configuration was not valid.
     * @throws NullPointerException if the {@link Configuration} was <b>null</b>.
     */
    public InterfaceDescriptor(Configuration configuration)
    throws ConfigurationException, IdentificationException {
        super(configuration);

        /* Process interface block exposure */
        Configuration current = configuration.child(NAMESPACE, "exposes");
        this.exposition = current.getStringAttribute("interface", null);

        /* Check that we don't implement anything */
        if (this.implemented.size() > 0) {
            throw new ConfigurationException("Interface descriptor claims"
                                             + "implementation of other "
                                             + "interfaces", configuration);
        }

        /* Check that we don't require anything */
        if (this.required.size() > 0) {
            throw new ConfigurationException("Interface descriptor requires "
                                             + "other blocks", configuration);
        }
    }

    /**
     * <p>Return the {@link String} name of the interface that the block
     * described by this {@link InterfaceDescriptor} descriptor exposes.</p>
     *
     * @return an {@link String} or <b>null</b> if this {@link Descriptor}
     *         descriptor doesn't expose any interface.
     */
    public String exposedInterface() {
        return this.exposition;
    }
    
    /**
     * <p>Check whether this {@link Descriptor} describes an interface
     * block or not.</p>
     *
     * @return <b>true</b> always.
     */
    public boolean isInterface() {
        return(true);
    }
}
