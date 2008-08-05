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
package org.apache.cocoon.portal.converter.castor;

import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.portal.om.CopletDefinition;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.CopletType;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutType;
import org.apache.cocoon.portal.om.Renderer;
import org.apache.cocoon.portal.profile.PersistenceType;
import org.exolab.castor.mapping.AbstractFieldHandler;

/**
 * Field handler for references.
 *
 * @version $Id$
 */
public class ReferenceFieldHandler extends AbstractFieldHandler {

    /**
     * @see org.exolab.castor.mapping.FieldHandler#getValue(java.lang.Object)
     */
    public Object getValue(Object object) {
        if ( this.getFieldDescriptor().getFieldName().equals("copletType") ) {
            return ((CopletDefinition)object).getCopletType().getId();
        }
        if ( this.getFieldDescriptor().getFieldName().equals("copletDefinition") ) {
            return ((CopletInstance)object).getCopletDefinition().getId();
        }
        if ( this.getFieldDescriptor().getFieldName().equals("customRenderer") ) {
            final Object renderer = ((Layout)object).getCustomRenderer();
            final PersistenceType type = (PersistenceType)CastorSourceConverter.threadLocalMap.get();
            final Map references = type.getReferences(this.getFieldDescriptor().getFieldName());
            final Iterator i = references.entrySet().iterator();
            while ( i.hasNext() ) {
                final Map.Entry current = (Map.Entry)i.next();
                if ( current.getValue() == renderer ) {
                    return current.getKey();
                }
            }
        }
        if ( this.getFieldDescriptor().getFieldName().equals("layoutType") ) {
            return ((Layout)object).getLayoutType().getId();
        }
        return null;
    }

    /**
     * @see org.exolab.castor.mapping.FieldHandler#newInstance(java.lang.Object)
     */
    public Object newInstance(Object parent) {
        return "";
    }

    /**
     * @see org.exolab.castor.mapping.ExtendedFieldHandler#newInstance(java.lang.Object, java.lang.Object[])
     */
    public Object newInstance(Object arg0, Object[] arg1) {
        return "";
    }

    /**
     * @see org.exolab.castor.mapping.FieldHandler#resetValue(java.lang.Object)
     */
    public void resetValue(Object object) {
        if ( this.getFieldDescriptor().getFieldName().equals("copletType") ) {
            ((CopletDefinition)object).setCopletType(null);
        }
        if ( this.getFieldDescriptor().getFieldName().equals("copletDefinition") ) {
            ((CopletInstance)object).setCopletDefinition(null);
        }
        if ( this.getFieldDescriptor().getFieldName().equals("layoutType") ) {
            ((Layout)object).setLayoutType(null);
        }
        if ( this.getFieldDescriptor().getFieldName().equals("customRenderer") ) {
            ((Layout)object).setCustomRenderer(null);
        }
    }

    /**
     * @see org.exolab.castor.mapping.FieldHandler#setValue(java.lang.Object, java.lang.Object)
     */
    public void setValue(Object object, Object value) {
        final PersistenceType type = (PersistenceType)CastorSourceConverter.threadLocalMap.get();
        final Map references = type.getReferences(this.getFieldDescriptor().getFieldName());
        final Object reference = (references != null ? references.get(value) : null);
        if ( this.getFieldDescriptor().getFieldName().equals("copletType") ) {
            ((CopletDefinition)object).setCopletType((CopletType)reference);
        }
        if ( this.getFieldDescriptor().getFieldName().equals("copletDefinition") ) {
            ((CopletInstance)object).setCopletDefinition((CopletDefinition)reference);
        }
        if ( this.getFieldDescriptor().getFieldName().equals("layoutType") ) {
            ((Layout)object).setLayoutType((LayoutType)reference);
        }
        if ( this.getFieldDescriptor().getFieldName().equals("customRenderer") ) {
            ((Layout)object).setCustomRenderer((Renderer)reference);
        }
    }
}
