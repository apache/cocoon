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

import java.util.Iterator;
import org.apache.cocoon.kernel.identification.Identifier;

/**
 * <p>A {@link Deployer} describes a simple container where block instances
 * can be deployed.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.4 $)
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
     * <p>The returned {@link Instance} will not be deployed until the
     * {@link #deploy(Instance)} method is called, but it will be returned by
     * the {@link #instances()} method and its {@link Instance#deployed()}
     * will return <b>false</b>.</p>
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

    /**
     * <p>Replace the specified block {@link Instance} with another one.</p>
     *
     * <p>This method will replace a deployed {@link Instance} with another,
     * rewiring all wired blocks to the new {@link Instance}.</p>
     *
     * <p>The old {@link Instance} will not be destroyed, but still returned
     * by the {@link #instances()} method, but its {@link Instance#deployed()}
     * will return <b>false</b>.</p>
     *
     * @param old the old {@link Instance} instance to replace.
     * @param instance the {@link Instance} replacing the old {@link Instance}.
     * @throws DeploymentException if the specified {@link Instance} could
     *                             not be replaced for any reason.
     */
    public void replace(Instance old, Instance instance)
    throws DeploymentException;

    /**
     * <p>Destroy the specified deployed block {@link Instance}.</p>
     *
     * <p>If the specified {@link Instance} is deployed <b>and</b> is required
     * by any other block (there is an active wiring), this method will throw
     * a {@link DeploymentException}.</p>
     *
     * <p>If this {@link Instance} has been replaced by another or has not been
     * wired to any other block, all the eventual wirings to it will be cut and
     * it will be destroyed.</p>
     *
     * @param instance the {@link Instance} instance to deploy.
     * @throws DeploymentException if the specified {@link Instance} could
     *                             not be deployed for any reason.
     */
    public void destroy(Instance instance)
    throws DeploymentException;

    /**
     * <p>Return an {@link Iterator} over all {@link Instance}s known by this
     * {@link Deployer}.</p>
     *
     * <p>This method will return all instances known by this {@link Deployer},
     * whether they have been simply installed, deployed, or have been replaced
     * by other instances. To check if an {@link Instance} is actively deployed
     * the {@link Instance#deployed()} method can be called.</p>
     */
    public Iterator instances();
}
