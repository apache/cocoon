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
 * <p>The {@link Wire} interface defines a component as returned by a
 * {@link Wirings} instance.</p>
 *
 * <p>Note that this framework's components <b>must never</b> implement this
 * interface, as unpredictable results might happen in doing so.</p>
 *
 * <p>It is not required to implement this interface because the framework's
 * contract with the {@link Composer} interface already guarantees that all
 * instances will always be returned to the {@link Composer} from which they
 * were acquired, and therefore any destruction or recycling operation should
 * be performed there.</p>
 *
 * <p>Furthermore, if a component <b>needs</b> to have access to its associated
 * {@link Wire} it <b>must</b> implement the {@link Component} interface.</p>
 *
 * <p>The underlying framework will <b>automatically wrap</b> any object 
 * {@link Composer#acquire() acquired} from a {@link Composer} into a
 * {@link Wirings} instance (preserving the requested role interface and all
 * other interfaces the original component might implement), before it is
 * returned by the {@link Wirings#lookup(Class,String)} method.</p>
 *
 * <p>At any time a {@link Wirings} can be &quot;unwired&quot; by simply calling
 * the {@link #release()} or {@link #dispose()} methods. After this call, any
 * call to any method in any other interface implemented by the original
 * will throw an {@link IllegalStateException}.</p>
 *
 * <p>Users of resource intensive components are encuraged to manually call
 * the {@link #release()} method after utilization to allow the original
 * {@link Composer}s to reclaim component instances allow them to be garbage
 * collected by the underlying Java&trade; Virtual Machine.</p>
 *
 * <p>A {@link Wire} can also be disconnected by the framework itself if (for
 * example) the original block of this component was reloaded or reconfigured,
 * and the administrator forced the destruction of the block creating this
 * {@link Wire} instance.</p>
 *
 * <p>To check whether the {@link Wire} is still active, or it should be
 * recreated, users should call the {@link #wired()} method.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public interface Wire extends Resolver {

    /**
     * <p>Resolve a resource visible from a {@link Component} into a
     * {@link Resource}.</p>
     *
     * <p>This method will resolve resources local to the block requesting
     * this wire.</p>
     *
     * <p>If the specified {@link String} assumes a format like
     * <code>wiring:/resource/path</code> where <code>wiring</code> is the
     * wiring name as required in the block's deployment descriptor, the
     * resolution of the {@link Resource} will be performed relative to the
     * block target of the wiring.</p>
     *
     * <p>{@link Component}s using a {@link Wire} for resource resolution
     * <b>must</b> be aware that resolution will happen in the context of the
     * requesting block, rather than in the context of the block composing the
     * {@link Component}.</p>
     *
     * <p>This is done because resource management is decoupled entirely from
     * component management, and because in block-enabled environments, it will
     * be possible to have components provided by wired blocks to operate on
     * resources available from other wired blocks.</p>
     * 
     * <p>Let's examine a simple example based (for example) on Apache Cocoon:
     * the <i>ForrestPipeline</i> block (a block providing a <code>Pipeline</code>
     * component for Apache Forrest) is wired to two other blocks: the first is
     * the <i>XSLTransformer</i> block (providing <code>Transformer</code>
     * component instances) and the second one is an instance of the
     * <i>ForrestSkin</i> block (a block providing XSLT stylesheets to be used
     * while processing the forrest pipeline).</p>
     *
     * <p>Being an implementation of {@link Component}, the <code>Transformer</code>
     * component instantiated by the <i>ForrestPipeline</i> block will have direct
     * access (and will be able to resolve) a resource in the form of
     * <code>skin:/document2xhtml.xsl</code>, which represents a resource provided
     * by any <i>ForrestSkin</i> implementation, wired to the <i>ForrestPipeline</i>
     * block, but completely separated from the original<i>XSLTransformer</i> block
     * which provides the <code>Transformer</code> instance.</p>
     * 
     * @param name the local (or wired) resource name.
     * @return a {@link Resource} instance, or <b>null</b> if the resource was
     *         not found or was not accessible.
     */
    public Resource resolve(String name);

    /**
     * <p>Check wether the wire between this {@link Wirings} instance and the
     * original component is still active.</p>
     *
     * <p>When this method returns <b>true</b> all methods called on this
     * instance will be forwarded to the original component instance associated
     * with this wire.</p>
     *
     * <p>When this method returns <b>false</b> the original block composing
     * the component was reconfigured and/or redeployed, and therefore there
     * is no guarantee that methods will be successfully invoked.</p>
     *
     * <p>That said, the framework will never completely deactivate (unless
     * forced to do so) a block when wires are still active, anyhow this method
     * should be invoked to ensure whether a new instance must be looked up,
     * or this one is still valid.</p>
     *
     * @return <b>true</b> if the wiring of this instance is still active,
     *         <b>false</b> otherwise.
     */
    public boolean wired();

    /**
     * <p>Release the wire identified by this instance, allowing the
     * framework to release of the original component in the remote block.</p>
     *
     * <p>If this {@link Wirings} was already unwired, this method will simply
     * be ignored by the underlying framework.</p>
     *
     * @see Composer#release(Object)
     */
    public void release();

    /**
     * <p>Dispose the wire identified by this instance, allowing the
     * framework to release of the original component in the remote block.</p>
     *
     * <p>If this {@link Wirings} was already unwired, this method will simply
     * be ignored by the underlying framework.</p>
     *
     * @see Composer#dispose(Object)
     */
    public void dispose();
}
