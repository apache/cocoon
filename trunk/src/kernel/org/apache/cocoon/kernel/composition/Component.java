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

import java.net.URL;

/**
 * <p>The {@link Component} interface identifies a component aware of its
 * deployment in a block-enabled environment.</p>
 *
 * <p>Any component implementing this interface <b>must be aware</b> that
 * the {@link Wire} they will be given will resolve {@link URL}s in the context
 * of the <b>calling</b> block and not in the context of the block they are
 * defined in.</p>
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
 * <p>Additionally, a {@link Component} can have control over its own
 * {@link Wire}d visibilty from requesting blocks. For example, JDBC
 * {@link java.sql.Connection} implementation implementing also this interface,
 * could call the {@link Wire#release() release()} method when its {@link
 * java.sql.Connection#close() close()} method is invoked, to make sure that
 * it is completely disconnected from the requestor and that its instance
 * is returned to the original {@link Composer}.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public interface Component {

    /**
     * <p>Contextualize this {@link Component} component instance with the
     * {@link Wire} through which its caller is accessing it.</p>
     *
     * @param wire the {@link Wire} instance associated with this instance.
     */
    public void contextualize(Wire wire);
}
