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

import org.apache.cocoon.kernel.archival.Library;
import org.apache.cocoon.kernel.identification.Descriptor;
import org.apache.cocoon.kernel.identification.Identifier;

/**
 * <p>The {@link Loader} interface describes an object loading {@link Block}
 * instances from their {@link Descriptor}s.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.4 $)
 */
public interface Loader extends Library {
    
    /**
     * <p>Load the instance of the {@link Block} described by the specified
     * {@link String}.</p>
     *
     * @param identifier the {@link String} identifier of the {@link Block}
     * @return a <b>non null</b> {@link Block} instance.
     * @throws DeploymentException if an error occurred loading the instance.
     */
    public Block load(String identifier)
    throws DeploymentException;
    
    /**
     * <p>Load the instance of the {@link Block} described by the specified
     * {@link Identifier}.</p>
     *
     * @param identifier the {@link Identifier} of the {@link Block} to load.
     * @return a <b>non null</b> {@link Block} instance.
     * @throws DeploymentException if an error occurred loading the instance.
     */
    public Block load(Identifier identifier)
    throws DeploymentException;
}
