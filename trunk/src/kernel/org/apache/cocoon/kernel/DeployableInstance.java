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
package org.apache.cocoon.kernel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.cocoon.kernel.configuration.Parameters;
import org.apache.cocoon.kernel.deployment.Block;
import org.apache.cocoon.kernel.deployment.DeploymentException;
import org.apache.cocoon.kernel.deployment.Instance;

/**
 * <p>The {@link DeployableInstance} class provides a simple implementation of
 * the {@link Instance} interface.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public class DeployableInstance implements Instance {

    /** <p>The {@link Block} associated with this deployable instance.</p> */
    private Block block = null;
    
    /** <p>The identifier for this {@link Instance}.</p> */
    private String name = null;
    
    /** <p>The "toString" representation of this {@link Instance}.</p> */
    private String string = null;
    
    /** <p>The wirings associated with each instance.</p> */
    private Map wirings = new HashMap();
    
    /** <p>Our copy of the parameters.</p> */
    private Parameters parameters = null;
    
    /** <p>A flag identifying whether this instance was deployed or not.</p> */
    private boolean deployed = false;

    /* ====================================================================== */
    
    /**
     * <p>Create a new deployable {@link Instance} specifiying its {@link Block}
     * and an informational {@link String}.
     *
     * @param block the {@link Block} of the resources of this instance.
     * @param name an informational {@link String}.
     * @throws NullPointerException if the {@link Block} was <b>null</b>.
     */
    public DeployableInstance(LoadedBlock block, String name)
    throws DeploymentException {
        if (block == null) throw new NullPointerException();
        if (block.isInterface()) {
            throw new DeploymentException("Unable to create instance of inter"
                                          + "face block \"" + block + "\"");
        }
        this.block = block;
        this.name = name;
        this.string = block.toString() + (name == null? "[null" : "[" + name)
                      + "/" + this.hashCode() + "]";
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the {@link Block} associated with this deployable block
     * {@link Instance}.</p>
     */
    public Block block() {
        return(this.block);
    }
    
    /**
     * <p>Return the informational {@link String} name with wich this instance
     * was created.</p>
     */
    public String name() {
        return(this.name);
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Configure this instance with the specified {@link Parameters}.</p>
     *
     * @param parameters the {@link Parameters} configuring this instance.
     * @throws NullPointerException if the {@link Parameters} were <b>null</b>.
     */
    public void configure(Parameters parameters) {
        if (this.deployed) throw new IllegalStateException();

        if (parameters == null) throw new NullPointerException();
        this.parameters = parameters;
    }
    
    /**
     * <p>Return the {@link Parameters} configured in this instance.</p>
     *
     * @return a <b>non null</b> {@link Parameters} instance.
     */
    public Parameters configuration() {
        if (this.parameters == null) this.parameters = new Parameters();
        return(this.parameters);
    }
    
    /* ====================================================================== */

    /**
     * <p>Wire this {@link Instance} to another.</p>
     *
     * <p>This method can be called multiple times on each {@link Instance}.</p>
     *
     * @param name the wiring name as specified by the block requirements.
     * @param target the target {@link Instance} of the wiring operation.
     */
    public void wire(String name, Instance target) {
        if (this.deployed) throw new IllegalStateException();

        /* Initial checks */
        if (target == null) throw new NullPointerException();
        if (name == null) throw new NullPointerException();
        if (target == this) throw new IllegalArgumentException("Self-wiring");
        
        /* Add wiring */
        this.wirings.put(name, target);
    }
    
    /**
     * <p>Return an {@link Iterator} over all configured wiring names.</p>
     *
     * @return a <b>non null</b> {@link Iterator} over {@link String} instances.
     */
    public Iterator wirings() {
        return(this.wirings.keySet().iterator());
    }
    
    /**
     * <p>Return the {@link Instance} wired to this one with the specified
     * name.</p>
     *
     * @return an {@link Instance} or <b>null</b> if the wiring was not found.
     */
    public Instance wiring(String name) {
        return((Instance)this.wirings.get(name));
    }
    
    /* ====================================================================== */

    /**
     * <p>Return whether this instance was deployed or not.</p>
     */
    public boolean deployed() {
        return(this.deployed);
    }

    /**
     * <p>Specifiy whether this {@link Instance} is deployed or not.</p>
     */
    protected void deployed(boolean flag) {
        this.deployed = flag;
    }
    
    /* ====================================================================== */

    /**
     * <p>Return a human-readable representation of this {@link Instance}.</p>
     */
    public String toString() {
        return(this.string);
    }
}
