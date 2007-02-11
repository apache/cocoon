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
package org.apache.cocoon.portal.util;

/**
 * Interface for functionality of objects to be updated by a delta object 
 * and where references to contained DeltaApplicable objects can be adjusted 
 * if no delta has been applied to them.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id: DeltaApplicableReferencesAdjustable.java,v 1.4 2004/03/05 13:02:17 bdelacretaz Exp $
 */
public interface DeltaApplicableReferencesAdjustable 
extends DeltaApplicable {
	/**
	 * Updates the references to contained DeltaApplicable objects  
     * if no delta has been applied to them.
	 * @throws ClassCastException If the object is not of the expected type.
	 */
	void adjustReferences(Object object);
}
