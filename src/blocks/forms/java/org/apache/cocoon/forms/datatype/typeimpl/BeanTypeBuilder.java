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
package org.apache.cocoon.forms.datatype.typeimpl;

import org.apache.cocoon.forms.datatype.Datatype;
import org.apache.cocoon.forms.datatype.DatatypeManager;

import org.w3c.dom.Element;


/**
 * Builder for {@link BeanType}
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version $Id: BeanTypeBuilder.java,v 1.1 2004/12/21 14:37:32 giacomo Exp $
 */
public class BeanTypeBuilder
    extends AbstractDatatypeBuilder
{
    //~ Methods ----------------------------------------------------------------

    /**
     * @see org.apache.cocoon.forms.datatype.DatatypeBuilder#build(org.w3c.dom.Element,
     *      boolean, org.apache.cocoon.forms.datatype.DatatypeManager)
     */
    public Datatype build( final Element datatypeElement,
                           final boolean arrayType,
                           final DatatypeManager datatypeManager )
        throws Exception
    {
        final BeanType type = new BeanType( arrayType, this );
        buildValidationRules( datatypeElement, type, datatypeManager );
        buildConvertor( datatypeElement, type );
        return type;
    }
}
