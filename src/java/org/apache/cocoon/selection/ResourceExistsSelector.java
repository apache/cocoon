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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;


/**
 * Selects the first of a set of Resources (usually files) that exists in the
 * context.
 * <p>
 * The 'test' expression is interpreted as a context-rooted ('/' = context)
 * path, resolved by the servlet container, <em>not</em> a Source.
 * <p>
 * A parameter, 
 * <pre>
 * &lt;map:parameter src="prefix" value="<code>/</code>"/>
 * </pre>
 * may be supplied to the selector instance.  This prefix is prepended to all
 * test expressions before evaluation.  The default prefix is '<code>/</code>',
 * meaning that all expressions are context root-relative, unless explicitly
 * overridden.
 * <p>
 * For example, we could define a ResourceExistsSelector with:
 * <pre>
 * &lt;map:selector name="resource-exists"
 *               logger="sitemap.selector.resource-exists"
 *               src="org.apache.cocoon.selection.ResourceExistsSelector" />
 * </pre>
 * And use it to build a PDF from XSL:FO or a higher-level XML format with:
 *
 * <pre>
 *  &lt;map:match pattern="**.pdf">
 *    &lt;map:select type="resource-exists">
 *       &lt;map:when test="context/xdocs/{1}.fo">
 *          &lt;map:generate src="content/xdocs/{1}.fo" />
 *       &lt;/map:when>
 *       &lt;map:otherwise>
 *         &lt;map:generate src="content/xdocs/{1}.xml" />
 *         &lt;map:transform src="stylesheets/document2fo.xsl" />
 *       &lt;/map:otherwise>
 *    &lt;/map:select>
 *    &lt;map:serialize type="fo2pdf" />
 * </pre>
 *
 * @author <a href="mailto:jefft@apache.org">Jeff Turner</a>
 * @version CVS $Id: ResourceExistsSelector.java,v 1.2 2003/03/20 12:32:18 cziegeler Exp $
 */
public class ResourceExistsSelector extends AbstractLogEnabled
  implements ThreadSafe, Selector {

    public boolean select(String expression, Map objectModel, Parameters parameters) {

        Context context = ObjectModelHelper.getContext(objectModel);
        URL url = null;
        expression = parameters.getParameter("prefix", "/") + expression;
        try {
            url = context.getResource(expression);
            if (url == null) {
                return false;
            } else { return true; }
        } catch (MalformedURLException e) {
            getLogger().warn("Selector expression '"+expression+"' is not a valid URL");
            return false;
        }

        /* While the servlet Javadocs state that getResource should return null
         * if a resource doesn't exist, early versions of Tomcat didn't respect
         * this.  If this turns to be an issue with other containers, remove
         * the 'else return true' above and uncomment this code.  (JT)
        InputStream is = null;
        try {
           is = url.openStream();
           return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
            if (is != null) is.close();
            } catch (IOException e) {}
        }
        */
    }
}
