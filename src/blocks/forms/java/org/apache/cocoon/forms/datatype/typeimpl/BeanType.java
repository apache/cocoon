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

import org.apache.cocoon.forms.datatype.convertor.Convertor;


/**
 * The CForm type of a bean
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version $Id: BeanType.java,v 1.1 2004/12/21 14:37:32 giacomo Exp $
 */
public class BeanType
    extends AbstractDatatype
{
    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new BeanType object.
     *
     * @param arrayType whether it's an array or not
     * @param builder The {@link BeanTypeBuilder}
     */
    public BeanType( final boolean arrayType,
                     final BeanTypeBuilder builder )
    {
        super(  );
        setArrayType( arrayType );
        setBuilder( builder );
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @see org.apache.cocoon.forms.datatype.Datatype#getDescriptiveName()
     */
    public String getDescriptiveName(  )
    {
        final Class c1 = this.getConvertor(  ).getTypeClass(  );

        return this.getConvertor(  ).getTypeClass(  ).getName(  );
    }

    /**
     * We make sure the plain Convertor is the same
     *
     * @return The convertor
     */
    public Convertor getPlainConvertor(  )
    {
        return getConvertor(  );
    }

    /**
     * @see org.apache.cocoon.forms.datatype.Datatype#getTypeClass()
     */
    public Class getTypeClass(  )
    {
        return this.getConvertor(  ).getTypeClass(  );
    }
}
