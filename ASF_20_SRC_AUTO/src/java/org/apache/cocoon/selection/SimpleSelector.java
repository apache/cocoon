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
package org.apache.cocoon.selection;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * A very simple selector that operates on string literals, useful especially 
 * in conjunction with input modules. Usage example:
 * <pre>
 *    &lt;map:selector name="simple" src="org.apache.cocoon.selection.SimpleSelector"/&gt;
 * 
 *    &lt;map:select type="simple"&gt;
 *       &lt;map:parameter name="value" value="{request:method}"/&gt;
 *       &lt;map:when test="GET"&gt;
 *           ...
 *       &lt;/map:when&gt;
 *       &lt;map:when test="POST"&gt;
 *           ...
 *       &lt;/map:when&gt;
 *       &lt;map:when test="PUT"&gt;
 *           ...
 *       &lt;/map:when&gt;
 *       &lt;map:otherwise&gt;
 *           ...
 *       &lt;/map:otherwise&gt;
 *    &lt;/map:select&gt;
 * </pre>
 * 
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: SimpleSelector.java,v 1.3 2004/03/05 13:02:57 bdelacretaz Exp $
 * @since 2.1
 */
public class SimpleSelector extends AbstractSwitchSelector implements ThreadSafe {

    public Object getSelectorContext(Map objectModel, Parameters parameters) {
        return parameters.getParameter("value", "");
    }

    public boolean select(String expression, Object selectorContext) {
        if (selectorContext == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("Value not set -- failing.");
            return false;
        }

        return selectorContext.equals(expression);
    }

}
