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


/**
 * Used by the ParameterFieldHandler for Castor.
 * This MapItem is used by the mapping each time key and value are
 * mapped to attributes in the xml (as opposed to elements)
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id: AttributedMapItem.java,v 1.2 2004/03/05 13:02:17 bdelacretaz Exp $
 */
public class AttributedMapItem
extends org.exolab.castor.mapping.MapItem {

	public AttributedMapItem() {
		super();
	}

	public AttributedMapItem(Object key, Object value) {
		super(key, value);
	}
}
