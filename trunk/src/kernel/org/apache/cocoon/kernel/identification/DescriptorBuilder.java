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

import java.io.IOException;
import java.net.URL;

import org.apache.cocoon.kernel.configuration.Configuration;
import org.apache.cocoon.kernel.configuration.ConfigurationBuilder;
import org.apache.cocoon.kernel.configuration.ConfigurationException;

/**
 * <p>A {@link DescriptorBuilder} is a simple utility class converting a
 * {@link Configuration} into a {@link Descriptor}.</p>
 *
 * <p>A {@link Configuration} can be supplied as an instance, or parsed
 * automatically from an original XML file.</p>
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.5 $)
 */
public class DescriptorBuilder {

    /**
     * <p>Deny construction.
     */
    private DescriptorBuilder() {
        super();
    }

    
    /**
     * Create a new {@link Descriptor} instance given a {@link URL} locating
     * a descriptor to parse.</p>
     *
     * @param location the location of the descriptor.
     * @throws ConfigurationException if the specified {@link Configuration}
     *                                did not represent a valid descriptor.
     * @throws IdentificationException if the specified {@link Configuration}
     *                                 specified an invalid block identifier.
     * @throws NullPointerException if the {@link Descriptor} was <b>null</b>.
     */
    public static Descriptor newInstance(URL location)
    throws ConfigurationException, IdentificationException {
        try {
            return newInstance(ConfigurationBuilder.parse(location));
        } catch (IOException exception) {
            throw new ConfigurationException("Unable to parse descriptor \""
                                             + location + "\"", exception);
        }
    }
    
    /**
     * Create a new {@link Descriptor} instance given a {@link String} locating
     * a descriptor to parse.</p>
     *
     * @param location the location of the descriptor.
     * @throws ConfigurationException if the specified {@link Configuration}
     *                                did not represent a valid descriptor.
     * @throws IdentificationException if the specified {@link Configuration}
     *                                 specified an invalid block identifier.
     * @throws NullPointerException if the {@link Descriptor} was <b>null</b>.
     */
    public static Descriptor newInstance(String location)
    throws ConfigurationException, IdentificationException {
        try {
            return newInstance(ConfigurationBuilder.parse(location));
        } catch (IOException exception) {
            throw new ConfigurationException("Unable to parse descriptor \""
                                             + location + "\"", exception);
        }
    }

    /**
     * Create a new {@link Descriptor} instance given a {@link Configuration}
     * element describing a block.</p>
     *
     * @param configuration a {@link Configuration} element with details of
     *                      this {@link Descriptor} characteristics.
     * @throws ConfigurationException if the specified {@link Configuration}
     *                                did not represent a valid descriptor.
     * @throws IdentificationException if the specified {@link Configuration}
     *                                 specified an invalid block identifier.
     * @throws NullPointerException if the {@link Descriptor} was <b>null</b>.
     */
    public static final Descriptor newInstance(Configuration configuration)
    throws ConfigurationException, IdentificationException {
        /* Check namespace */
        if (!Descriptor.NAMESPACE.equals(configuration.namespace())) {
            throw new ConfigurationException("Invalid descriptor namespace \""
                                             + configuration.namespace()
                                             + "\" for block", configuration);
        }
        
        /* Return an instance depending on the root element name */
        if ("interface".equals(configuration.name())) {
            return(new InterfaceDescriptor(configuration));
        } else if ("block".equals(configuration.name())) {
            return(new BlockDescriptor(configuration));
        }

        /* What the heck is this file? */
        throw new ConfigurationException("Invalid root element name \""
                                         + configuration.name() + "\" in "
                                         + "descriptor", configuration);
    }
}
