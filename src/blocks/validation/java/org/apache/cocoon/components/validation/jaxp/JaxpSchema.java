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

import javax.xml.validation.Schema;
import javax.xml.validation.ValidatorHandler;

import org.apache.cocoon.components.validation.ValidationHandler;
import org.apache.cocoon.components.validation.impl.AbstractSchema;
import org.apache.cocoon.components.validation.impl.DefaultValidationHandler;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * <p>TODO: ...</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class JaxpSchema extends AbstractSchema {
    
    private final Schema schema;
    
    public JaxpSchema(Schema schema, SourceValidity validity) {
        super(validity);
        this.schema = schema;
    }

    public ValidationHandler createValidator(ErrorHandler handler)
    throws NullPointerException, SAXException {
        ValidatorHandler validator = this.schema.newValidatorHandler();
        validator.setErrorHandler(handler);
        return new DefaultValidationHandler(this.getValidity(), validator);
    }

}
