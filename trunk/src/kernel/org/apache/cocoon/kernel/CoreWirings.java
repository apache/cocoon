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

import org.apache.cocoon.kernel.composition.Wire;
import org.apache.cocoon.kernel.composition.WiringException;
import org.apache.cocoon.kernel.composition.Wirings;
import org.apache.cocoon.kernel.resolution.Resource;

/**
 * <p>The {@link CoreWirings} provides an implementation of the {@link Wirings}
 * interface wrapping all blocks installed in a {@link KernelDeployer}.</p> 
 *
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.3 $)
 */
public class CoreWirings implements Wirings {
    
    KernelDeployer deployer = null;

    /**
     * <p>Create a new {@link CoreWirings} instance associated with a specified
     * {@link KernelDeployer}.</p>
     * 
     * @param deployer a <b>non null</b> {@link KernelDeployer} instance.
     * @throws NullPointerException if the specified deployer was <b>null</b>.
     */
    public CoreWirings(KernelDeployer deployer) {
        super();
        if (deployer == null) throw new NullPointerException("Null deployer");
        this.deployer = deployer;
    }

    public Wire lookup(Class role, String name)
    throws WiringException {
        /* Fail if either name or role are null */
        if (name == null) throw new WiringException("No name specified");
        if (role == null) throw new WiringException("No role specified");
        
        /* Look in our component wirings for the key matching the name */
        DeployedWirings target = this.deployer.lookup(name);
        if (target != null) return(target.newWire(role, target.getResolver()));

        /* Wrong wiring name specified */
        throw new WiringException("Unknown wiring \"" + name + "\"");
    }

    public Resource resolve(String name) {
        return null;
    }
}
