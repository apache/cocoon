/* ========================================================================== *
 * Copyright (C) 1996-2004 VNU Business Publications LTD. All rights reserved *
 * ========================================================================== */
package org.apache.cocoon.kernel;

import org.apache.cocoon.kernel.composition.Wire;
import org.apache.cocoon.kernel.composition.WiringException;
import org.apache.cocoon.kernel.composition.Wirings;
import org.apache.cocoon.kernel.resolution.Resource;

/**
 * <p></p> 
 *
 * @version CVS $Revision: 1.1 $
 * @author <a href="mailto:pier_fumagalli@vnu.co.uk">Pier Fumagalli</a>
 * @author Copyright &copy; 1996-2004 <a href="http://www.vnunet.com/">VNU
 *         Business Publications LTD.</a> All rights reserved
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

	public Wire lookup(Class role, String name) throws WiringException {
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
