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
package org.apache.cocoon.forms.datatype.convertor;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.jxpath.JXPathContext;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.Locale;
import java.util.Map;

/**
 * Converts String representation of beans to bean instances and vice versa.
 * 
 * <p>
 * Sometimes the toString() method doesn't give a good representation of a
 * Java Bean suited for selection list IDs. For this an optional  
 * &lt;fd:id-path&gt;jx-path&lt;/fd:id-path&gt; attribute can be specified to 
 * have this convertor to use a different string representation.
 * </p>
 *
 * @version $Id$
 */
public class BeanConvertor
    implements Convertor
{
    //~ Instance fields --------------------------------------------------------

    private Class m_class;

    private Map m_objects = new ReferenceMap();

    private String m_idPath;

    //~ Constructors -----------------------------------------------------------

    /**
     * Construct a new BeanConvertor for a class
     *
     * @param className The package-qualified name of the class implementing
     *        the typesafe enum pattern.
     * @param idPath Path to the identity field of the bean
     *
     * @throws CascadingRuntimeException If the class cannot be found
     */
    public BeanConvertor( final String className,
                          final String idPath )
    {
        try
        {
            m_class = Class.forName( className );
        }
        catch( ClassNotFoundException e )
        {
            throw new CascadingRuntimeException( "Class " + className +
                                                 " not found", e );
        }

        m_idPath = idPath;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @see org.apache.cocoon.forms.datatype.convertor.Convertor#getTypeClass()
     */
    public Class getTypeClass(  )
    {
        return m_class;
    }

    /**
     * @see org.apache.cocoon.forms.datatype.convertor.Convertor#convertFromString(java.lang.String,
     *      java.util.Locale,
     *      org.apache.cocoon.forms.datatype.convertor.Convertor.FormatCache)
     */
    public ConversionResult convertFromString( final String value,
                                               final Locale locale,
                                               final FormatCache formatCache )
    {
        return new ConversionResult( m_objects.get( value ) );
    }

    /**
     * @see org.apache.cocoon.forms.datatype.convertor.Convertor#convertToString(java.lang.Object,
     *      java.util.Locale,
     *      org.apache.cocoon.forms.datatype.convertor.Convertor.FormatCache)
     */
    public String convertToString( final Object value,
                                   final Locale locale,
                                   final FormatCache formatCache )
    {
        String idValue = "";

        if( null != value )
        {
            if( m_idPath != null )
            {
                final JXPathContext ctx = JXPathContext.newContext( value );
                idValue = ctx.getValue( m_idPath ).toString(  );
            }
            else
            {
                idValue = value.toString(  );
            }
        }

        m_objects.put( idValue, value );

        return idValue;
    }

    /**
     * We do not enerate any SAX events
     *
     * @param contentHandler The contentHandler
     * @param locale The locale
     *
     * @throws SAXException Just in case of failure that could never happen
     */
    public void generateSaxFragment( final ContentHandler contentHandler,
                                     final Locale locale )
        throws SAXException
    {
        // intentionally empty
    }
}
