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
package org.apache.cocoon.portal.util;

import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;

/**
 * Field handler for external CopletData references.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id$
 */
public class CopletDataReferenceFieldHandler extends ReferenceFieldHandler {

    public Object getValue(Object object) {
        CopletData copletData = ((CopletInstanceData) object).getCopletData();
        if (copletData != null) {
            return copletData.getId();
        }
        return null;
    }

    public Object newInstance(Object parent) {
        return new CopletData();
    }

    public void resetValue(Object object) {
        ((CopletInstanceData) object).setCopletData(null);
    }

    public void setValue(Object object, Object value) {
        CopletData copletData = (CopletData) getObjectMap().get(value);
        if (copletData == null) {
            throw new ProfileException(
                "Referenced Coplet Data " + value + " does not exist.");
        }
        ((CopletInstanceData) object).setCopletData(copletData);
    }
}
