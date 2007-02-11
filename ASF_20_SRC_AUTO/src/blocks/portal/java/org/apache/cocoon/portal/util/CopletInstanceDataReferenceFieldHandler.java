/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.util;

import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.impl.CopletLayout;

/**
 * Field handler for external CopletInstanceData references.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id: CopletInstanceDataReferenceFieldHandler.java,v 1.6 2004/03/05 13:02:17 bdelacretaz Exp $
 */
public class CopletInstanceDataReferenceFieldHandler
extends ReferenceFieldHandler {

	public Object getValue(Object object) {
		CopletInstanceData copletInstanceData = ((CopletLayout)object).getCopletInstanceData();
		if (copletInstanceData != null) {
			return copletInstanceData.getId();
		} else {
			return null;
		}
	}

	public Object newInstance(Object parent) {
		return new CopletInstanceData();
	}

	public void resetValue(Object object) {
		((CopletLayout)object).setCopletInstanceData(null);
	}

	public void setValue(Object object, Object value) {
		CopletInstanceData copletInstanceData = (CopletInstanceData)getObjectMap().get(value);
		if (copletInstanceData == null) {
			throw new ProfileException(
                    "Referenced Coplet Instance Data "+value+" does not exist.");
        }
		((CopletLayout)object).setCopletInstanceData(copletInstanceData);
	}
}
