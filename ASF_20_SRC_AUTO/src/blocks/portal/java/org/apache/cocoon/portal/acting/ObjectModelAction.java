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
package org.apache.cocoon.portal.acting;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

/**
 * Stores all parameters in the object model adding prefix "cocoon-portal-".
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @version CVS $Id: ObjectModelAction.java,v 1.4 2004/03/05 13:02:09 bdelacretaz Exp $
 */
public class ObjectModelAction 
extends AbstractAction {

	public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) 
    throws Exception {
        String[] names = parameters.getNames();
        String name;
        for(int i = 0; i < names.length; i++) {
            name = names[i];
            objectModel.put("cocoon-portal-" + name, parameters.getParameter(name));
        }
		return EMPTY_MAP;
	}
}
