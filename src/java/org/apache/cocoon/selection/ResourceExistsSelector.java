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
package org.apache.cocoon.selection;

import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceResolver;

/**
 * Selects the first of a set of Resources (usually files) that exists.
 * 
 * <p>
 * A parameter 'prefix', 
 * <pre>
 *   &lt;map:parameter src="prefix" value="<code>some/path</code>"/&lt;
 * </pre>
 * may be supplied to the selector instance.  This prefix is prepended to all
 * test expressions before evaluation.  The default prefix is '' (empty string),
 * meaning that all expressions are relative to the current sitemap, unless
 * explicitly overridden.
 * 
 * <p><b>NOTE:</b>
 * Provided resource URI is resolved as Source, relative to the current
 * sitemap, which differs from behavior of selector in previous versions.
 * To resolve resource paths relative to the context root, provide prefix
 * parameter:
 * <pre>
 *   &lt;map:parameter name="prefix" value="context://"/&lt;
 * </pre>
 * 
 * <p>
 * For example, we could define a ResourceExistsSelector with:
 * <pre>
 * &lt;map:selector name="resource-exists"
 *               logger="sitemap.selector.resource-exists"
 *               src="org.apache.cocoon.selection.ResourceExistsSelector" /&lt;
 * </pre>
 * And use it to build a PDF from XSL:FO or a higher-level XML format with:
 *
 * <pre>
 *  &lt;map:match pattern="**.pdf"&lt;
 *    &lt;map:select type="resource-exists"&lt;
 *       &lt;map:when test="context/xdocs/{1}.fo"&lt;
 *          &lt;map:generate src="content/xdocs/{1}.fo" /&lt;
 *       &lt;/map:when&lt;
 *       &lt;map:otherwise&lt;
 *         &lt;map:generate src="content/xdocs/{1}.xml" /&lt;
 *         &lt;map:transform src="stylesheets/document2fo.xsl" /&lt;
 *       &lt;/map:otherwise&lt;
 *    &lt;/map:select&lt;
 *    &lt;map:serialize type="fo2pdf" /&lt;
 * </pre>
 *
 * @author <a href="mailto:jefft@apache.org">Jeff Turner</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: ResourceExistsSelector.java,v 1.4 2004/03/05 13:02:57 bdelacretaz Exp $
 */
public class ResourceExistsSelector extends AbstractLogEnabled
                                    implements ThreadSafe, Serviceable, Disposable, Selector {

    private ServiceManager manager;
    private SourceResolver resolver;
    
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver)manager.lookup(SourceResolver.ROLE);
    }

    public void dispose() {
        this.manager.release(this.resolver);
        this.resolver = null;
        this.manager = null;
    }
    
    public boolean select(String expression, Map objectModel, Parameters parameters) {
        String resourceURI = parameters.getParameter("prefix", "") + expression;
        Source source = null;
        try {
            source = resolver.resolveURI(resourceURI);
            return source.exists();
        } catch (SourceNotFoundException e) {
            return false;
        } catch (Exception e) {
            getLogger().warn("Exception resolving resource " + resourceURI, e);
            return false;
        } finally {
            if (source != null) {
                resolver.release(source);
            }
        }
    }
}
