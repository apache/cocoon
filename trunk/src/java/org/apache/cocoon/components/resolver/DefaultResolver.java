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
package org.apache.cocoon.components.resolver;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.excalibur.xml.DefaultEntityResolver;


/**
 * A component that uses catalogs for resolving entities.
 * This component simply inherits from the excalibur implementation and
 * adds the context: protocol to each relative uri.
 *
 * The catalog is by default loaded from "WEB-INF/entities/catalog".
 * This can be configured by the "catalog" parameter in the cocoon.xconf:
 * &lt;entity-resolver&gt;
 *   &lt;parameter name="catalog" value="mycatalog"/&gt;
 * &lt;/entity-resolver&gt;
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: DefaultResolver.java,v 1.7 2003/12/28 20:54:24 unico Exp $
 * @since 2.1
 * 
 * @avalon.component
 * @avalon.service type=org.apache.excalibur.xml.EntityResolver
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=entity-resolver
 */
public class DefaultResolver extends DefaultEntityResolver {

    /**
     * @avalon.dependency type=org.apache.excalibur.source.SourceResolver
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
    }

    /**
     * Parse a catalog
     */
    protected void parseCatalog(String uri) {
        // check for relative URIs
        //   if the URI has ':/' then it's a URI
        //   if the URI starts with '/ it's an absolute (UNIX) path
        //   if the URI has a ':' at position 1, it's an absolute windows path
        // otherwise we have a relative URI, that is resolved relative
        // to the context
        if (uri.indexOf(":/") == -1 
            && !uri.startsWith("/") 
            && !(uri.length() > 1 && uri.charAt(1) == ':')) {
                uri = "context://" + uri;
        }
        super.parseCatalog( uri );
    }
    
    /**
     * Default catalog path
     */
    protected String defaultCatalog() {
        return "WEB-INF/entities/catalog";
    }

}
