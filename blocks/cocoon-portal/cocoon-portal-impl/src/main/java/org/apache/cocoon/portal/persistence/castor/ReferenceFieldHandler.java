/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.portal.persistence.castor;

import java.util.Map;

import org.apache.cocoon.portal.coplet.CopletBaseData;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
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
        if ( object instanceof CopletData ) {
            return ((CopletData)object).getCopletBaseData().getId();
        }
        if ( object instanceof CopletInstanceData ) {
            return ((CopletInstanceData)object).getCopletData().getId();
        }
        if ( object instanceof CopletLayout ) {
            return ((CopletLayout)object).getCopletInstanceData().getId();
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
        if ( object instanceof CopletData ) {
            ((CopletData)object).setCopletBaseData(null);
        }
        if ( object instanceof CopletInstanceData ) {
            ((CopletInstanceData)object).setCopletData(null);
        }
        if ( object instanceof CopletLayout ) {
            ((CopletLayout)object).setCopletInstanceData(null);
        }
    }

    /**
     * @see org.exolab.castor.mapping.FieldHandler#setValue(java.lang.Object, java.lang.Object)
     */
    public void setValue(Object object, Object value) {
        final Map references = (Map)CastorSourceConverter.threadLocalMap.get();
        final Object reference = (references != null ? references.get(value) : null);
        if ( object instanceof CopletData ) {
            ((CopletData)object).setCopletBaseData((CopletBaseData)reference);
        }
        if ( object instanceof CopletInstanceData ) {
            ((CopletInstanceData)object).setCopletData((CopletData)reference);
        }
        if ( object instanceof CopletLayout ) {
            ((CopletLayout)object).setCopletInstanceData((CopletInstanceData)reference);
        }
    }
}
