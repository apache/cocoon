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
package org.apache.cocoon.components.resolver;

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
 * @version CVS $Id: DefaultResolver.java,v 1.6 2004/03/05 13:02:50 bdelacretaz Exp $
 * @since 2.1
 */
public class DefaultResolver
  extends DefaultEntityResolver {

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
