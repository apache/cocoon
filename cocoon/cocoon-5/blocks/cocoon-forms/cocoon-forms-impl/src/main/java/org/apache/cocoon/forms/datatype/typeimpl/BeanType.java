/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * @version $Id$
 */
public class BeanType
    extends AbstractDatatype {

    /**
     * Creates a new BeanType object.
     *
     * @param arrayType whether it's an array or not
     * @param builder The {@link BeanTypeBuilder}
     */
    public BeanType( final boolean arrayType,
                     final BeanTypeBuilder builder ) {
        setArrayType( arrayType );
        setBuilder( builder );
    }

    /**
     * @see org.apache.cocoon.forms.datatype.Datatype#getDescriptiveName()
     */
    public String getDescriptiveName() {
        return this.getConvertor().getTypeClass().getName();
    }

    /**
     * We make sure the plain Convertor is the same
     *
     * @return The convertor
     */
    public Convertor getPlainConvertor() {
        return getConvertor();
    }

    /**
     * @see org.apache.cocoon.forms.datatype.Datatype#getTypeClass()
     */
    public Class getTypeClass() {
        return this.getConvertor().getTypeClass();
    }
}
