/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: ResourceExistsSelector.java,v 1.3 2003/12/12 15:15:19 vgritsenko Exp $
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
