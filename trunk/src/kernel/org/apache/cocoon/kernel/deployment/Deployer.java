/* ========================================================================== *
 *                                                                            *
 * Copyright 2004 The Apache Software Foundation.                             *
 *                                                                            *
 * Licensed  under the Apache License,  Version 2.0 (the "License");  you may *
 * not use this file except in compliance with the License.  You may obtain a *
 * copy of the License at                                                     *
 *                                                                            *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 *                                                                            *
 * Unless  required  by  applicable law or  agreed  to in  writing,  software *
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT *
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.           *
 *                                                                            *
 * See  the  License for  the  specific language  governing  permissions  and *
 * limitations under the License.                                             *
 *                                                                            *
 * ========================================================================== */
package org.apache.cocoon.kernel.deployment;

import org.apache.cocoon.kernel.identification.Identifier;
import org.apache.cocoon.kernel.archival.Library;

/**
 * <p>A {@link Deployer} describes a simple container where block instances
 * can be deployed.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public interface Deployer {

    /**
     * <p>Create a new {@link Instance} of a block specifying its
     * {@link Identifier}.</p>
     *
     * <p>Note that <b>only</b> non-interface blocks can be actively deployed.
     * Implemented interface blocks must be available for successful deployment,
     * but those will never be &quot;instantiated&quot; (like in Java &trade;
     * it is impossible to create new instances of interface classes).</p>
     *
     * @param identifier the {@link Identifier} of the block to deploy.
     * @param name an optional (possibly unique) {@link String} instance name.
     * @return a <b>non null</b> block {@link Instance}.
     * @throws DeploymentException if the block identified by the specified
     *                             {@link Identifier} could not be instantiated.
     */
    public Instance instantiate(Identifier identifier, String name)
    throws DeploymentException;
    
    /**
     * <p>Deploy the specified block {@link Instance}.</p>
     *
     * @param instance the {@link Instance} instance to deploy.
     * @throws DeploymentException if the specified {@link Instance} could
     *                             not be deployed for any reason.
     */
    public void deploy(Instance instance)
    throws DeploymentException;
}
