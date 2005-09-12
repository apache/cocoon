/* ========================================================================== *
 * Copyright (C) 2004-2005 Pier Fumagalli <http://www.betaversion.org/~pier/> *
 *                            All rights reserved.                            *
 * ========================================================================== *
 *                                                                            *
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may *
 * not use this file except in compliance with the License.  You may obtain a *
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.       *
 *                                                                            *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software *
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT *
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the *
 * License for the  specific language  governing permissions  and limitations *
 * under the License.                                                         *
 *                                                                            *
 * ========================================================================== */

package org.apache.cocoon.components.validation.jaxp;


import org.apache.cocoon.components.validation.impl.ValidationResolver;
import org.apache.excalibur.source.SourceResolver;
import org.w3c.dom.DOMError;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class JaxpResolver extends ValidationResolver
implements LSResourceResolver {
    
    public JaxpResolver(SourceResolver sourceResolver,
                        EntityResolver entityResolver) {
        super(sourceResolver, entityResolver);
    }

    public LSInput resolveResource(String type, String namespace, String systemId,
                                   String publicId, String base) {
        try {
            final InputSource source = this.resolveEntity(base, publicId, systemId);
            return new JaxpInput(source);
        } catch (Exception exception) {
            String message = "Exception resolving resource " + systemId;
            Throwable err = new LSException(DOMError.SEVERITY_FATAL_ERROR, message);
            throw (LSException) err.initCause(exception);
        }
    }
}
