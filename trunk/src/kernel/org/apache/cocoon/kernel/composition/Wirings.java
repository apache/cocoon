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
package org.apache.cocoon.kernel.composition;

import org.apache.cocoon.kernel.resolution.Resolver;
import org.apache.cocoon.kernel.resolution.Resource;

/**
 * <p>The {@link Wirings} interface defines a simple object performing
 * lookup on component instances wired to the block associated with it.</p>
 *
 * <p>Each {@link Wirings} instance is associated with a specific
 * block instance, and will provide access to the the components provided by
 * all blocks wired to it.</p>
 *
 * <p>As each component is associated with a {@link Composer}, and having its
 * creating composer access to the {@link Wirings} associated with the
 * block where the component is defined, it should be the responsibility of
 * the original {@link Composer} to look up for wired components and pass them
 * to its composed object instances.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.2 $)
 */
public interface Wirings extends Resolver {

    /**
     * <p>Lookup a {@link Wirings} instance associated with the specified
     * {@link Class} role, and a name as requested by the block descriptor.</p>
     *
     * <p>Note that although objects acquired from a {@link Composer} <b>must
     * not</b> implement the {@link Wirings} interface, the framework will
     * always automatically wrap their instance into a unique {@link Wirings}
     * instance.</p>
     *
     * <p>The specified role {@link Class} <b>must</b> be an interface (a call
     * to {@link Class#isInterface()} <b>must</b> return <b>true</b>)
     * implemented by the wired component, and the framework will guarantee
     * that the returned instance will be &quot;castable&quot; to it. For
     * example:</p>
     *
     * <pre>
     * MyInterface myObject = manager.lookup(MyInterface.class, "mywiring");
     * <small><i>[... use the object instance ...]</i></small>
     * ((Wiring)myObject).release();</pre>
     *
     * <p>Note that although <b>it is not required</b> to release the
     * {@link Wirings} associated with component instance, it is common practice
     * to do so for resource intensive components.</p>
     *
     * <p>That said, even if an object is not released by the caller, the
     * framework will make sure that its instance is released upon garbage
     * collection of the associated {@link Wirings} instance.</p>
     *
     * <p>{@link Wirings}s can also be invalidated by the framework itself when
     * (for example) the remote block is reloaded, reconfigured or redeployed.
     * For this reason {@link Wirings}s returned by this method could be checked
     * at any time for the availability of the connection to the wired component
     * instance.</p>
     *
     * <p>Common practice dictates that such checks sould be performed before
     * operating on the returned {@link Wirings} if they are (for example)
     * kept as local references and not released after use. For example:</p>
     *
     * <pre>
     * // myObject is a wired component that we looked up at initialization
     * if (!((Wiring)myObject).wired()) {
     *   manager.lookup(MyInterface.class, "mywiring");
     * }
     * <small><i>[... use the object instance ...]</i></small></pre>
     *
     * <p>That said, it is not guaranteed for how long the returned instance
     * will be available for, as this is dependant on factors external to
     * the block itself.</p>
     *
     * @param role the interface {@link Class} instance to which the returned
     *             {@link Wirings} must be castable to.
     * @param name the block's wiring name as required in the block's
     *               descriptor.
     * @return a <b>non null</b> {@link Wire} implementing the specified role.
     * @throws WiringException if an error occurred creating the instance.
     */
    public Wire lookup(Class role, String name)
    throws WiringException;

    /**
    * <p>Resolve a resource visible from a {@link Composer} into a
     * {@link Resource}.</p>
     *
     * <p>This method will resolve resources local to the block containing
     * the {@link Composer}.</p>
     *
     * <p>If the specified {@link String} assumes a format like
     * <code>wiring:/resource/path</code> where <code>wiring</code> is the
     * wiring name as required in the block's deployment descriptor, the
     * resolution of the {@link Resource} will be performed relative to the
     * block target of the wiring.</p>
     *
     * @param name the local (or wired) resource path.
     * @return a {@link Resource} instance, or <b>null</b> if the resource was
     *         not found or was not accessible.
     */
    public Resource resolve(String name);
}
