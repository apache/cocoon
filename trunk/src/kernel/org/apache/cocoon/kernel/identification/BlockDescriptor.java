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
package org.apache.cocoon.kernel.identification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.cocoon.kernel.configuration.Configuration;
import org.apache.cocoon.kernel.configuration.ConfigurationException;

/**
 * <p>The {@link BlockDescriptor} class extends a {@link Descriptor} to
 * describe a &quot;solid&quot; implementation block descriptor.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.2 $)
 */
public class BlockDescriptor extends AbstractDescriptor {

    /** <p>The {@link String} class name of our provision.</p> */
    private String provision = null;
    
    /** <p>The {@link String} class name of our composer.</p> */
    private String composer = null;
    
    /** <p>The {@link Map} of all our required parameters.</p> */
    private Map parameters = new HashMap();

    /**
     * <p>Create a new {@link BlockDescriptor} instance.</p>
     *
     * @param configuration a {@link Configuration} element with details of
     *                      this {@link Descriptor} characteristics.
     * @throws ConfigurationException if the specified {@link Configuration}
     *                                did not represent a valid descriptor.
     * @throws NullPointerException if the {@link Descriptor} was <b>null</b>.
     */
    public BlockDescriptor(Configuration configuration)
    throws ConfigurationException, IdentificationException {
        super(configuration);

        /* The parameters to associate with this block */
        Configuration current = configuration.child(NAMESPACE, "parameters");
        Iterator iterator = current.children(NAMESPACE, "parameter");
        while (iterator.hasNext()) {
            current = (Configuration)iterator.next();
            String para = current.getStringAttribute("name");
            String type = current.getStringAttribute("type");
            if (this.parameters.put(para, type) == null) continue;
            
            /* Whops... same parameter declared twice */
            throw new ConfigurationException("Descriptor reqires parameter "
                                             + "\"" + para + "\" twice",
                                             current);
        }

        /* The provision we declare */
        current = configuration.child(NAMESPACE, "provides");
        this.provision = current.getStringAttribute("class", null);
        this.composer = current.getStringAttribute("composer", null);
        if ((this.provision == null) && (this.composer == null)) {
            throw new ConfigurationException("Descriptor does not provide "
                                             + "composer or component class "
                                             + "name", configuration);
        }
    }

    /* ====================================================================== */

    /**
     * <p>Return the {@link String} name of the class that the block described
     * by this {@link BlockDescriptor} descriptor provides.</p>
     *
     * @return a {@link String} with the class name or <b>null</b> if this
     *         {@link BlockDescriptor} doesn't specify any class.
     */
    public String providedClass() {
        return this.provision;
    }

    /**
     * <p>Return the {@link String} name of the composer class associated with
     * the provided component of this block.</p>
     *
     * @return a {@link String} with the class name or <b>null</b> if this
     *         {@link BlockDescriptor} doesn't specify any composer.
     */
    public String providedComposer() {
        return this.composer;
    }

    /* ====================================================================== */

    /**
     * <p>Return an {@link Iterator} over all parameter names required by a
     * provision declared by this {@link Descriptor}.</p>
     *
     * <p>Note that the returned parameter names will assume the format of
     * <code>provisionName:parameterName</code>, so that a simple iteration
     * will notify of all parameter requirements of the entire block.</p>
     *
     * @return an {@link Iterator} iterating over {@link String} instances.
     */
    public Iterator parameters() {
        return(new ArrayList(this.parameters.keySet()).iterator());
    }
    
    /**
     * <p>Return a {@link String} identifying the type of a parameter reqired
     * by the block described by this {@link Descriptor}.</p>
     *
     * @param name the unique parameter name in the format returned by the
     *             {@link #parameters()} method.
     * @return a {@link String} or <b>null</b> if the parameter was not required.
     */
    public String parameterType(String name) {
        return((String)this.parameters.get(name));
    }

    /* ====================================================================== */

    /**
     * <p>Return an {@link Iterator} over all aliases of the blocks that this
     * {@link Descriptor} declares to be requisites.</p>
     *
     * @return an {@link Iterator} iterating over {@link String} instances.
     */
    public Iterator requirements() {
        return(new ArrayList(this.required.keySet()).iterator());
    }

    /**
     * <p>Return the {@link Identifier} of the block reqired by this, mapped
     * with the specified name.</p>
     *
     * @return a {@link String} or <b>null</b> if the name was not required.
     */
    public Identifier requiredBlock(String name) {
        return((Identifier)this.required.get(name));
    }

    /* ====================================================================== */
    
    /**
     * <p>Check whether this {@link Descriptor} describes an interface
     * block or not.</p>
     *
     * @return <b>false</b> always.
     */
    public boolean isInterface() {
        return(false);
    }
}
