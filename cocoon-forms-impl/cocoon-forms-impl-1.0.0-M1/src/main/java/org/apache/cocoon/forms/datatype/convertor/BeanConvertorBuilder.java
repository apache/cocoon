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
package org.apache.cocoon.forms.datatype.convertor;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.util.DomHelper;

import org.w3c.dom.Element;


/**
 * Creates {@link BeanConvertor}s
 * 
 * <p>
 * The optional &lt;fd:bean&gt;FQCN&lt;/fd:bean&gt; attribute is used to give 
 * this convertor a hint of which concrete bean class we are going to work with.
 * If this attribute is not specified java.lang.Object is used.
 * <p>
 * Sometimes the toString() method doesn't give a good representation of a
 * Java Bean suited for selection list IDs. For this an optional  
 * &lt;fd:id-path&gt;jx-path&lt;/fd:id-path&gt; attribute can be specified to 
 * have this convertor to use a different string representation.
 * </p>
 *
 * @version $Id$
 */
public class BeanConvertorBuilder
    implements ConvertorBuilder
{
    //~ Methods ----------------------------------------------------------------

    /**
     * Build a {@link BeanConvertor}
     *
     * @param configElement The configuration element
     *
     * @return An initialized {@link Convertor}
     *
     * @throws Exception In case of failure
     */
    public Convertor build( final Element configElement )
        throws Exception
    {
        if( configElement == null )
        {
            return null;
        }

        final Element beanEl =
            DomHelper.getChildElement( configElement, FormsConstants.DEFINITION_NS,
                                       "bean", false );
        final String clazz =
            ( ( beanEl == null ) ? Object.class.getName(  )
              : beanEl.getFirstChild(  ).getNodeValue(  ) );
        final Element idPathEl =
            DomHelper.getChildElement( configElement, FormsConstants.DEFINITION_NS,
                                       "id-path", false );
        final String idPath =
            ( ( idPathEl != null )
              ? idPathEl.getFirstChild(  ).getNodeValue(  ) : null );
        final BeanConvertor convertor = new BeanConvertor( clazz, idPath );

        return convertor;
    }
}
