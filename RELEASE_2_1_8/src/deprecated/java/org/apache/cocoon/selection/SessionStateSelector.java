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


import org.apache.avalon.framework.configuration.Configuration;

/**
 * This class generates source code to implement a selector that
 * matches a string against an arbitrary session attribute.
 *
 * <p><b>Global and local configuration</b></p>
 * <table border="1">
 * <tr><td><code>org.apache.cocoon.SessionState</code></td><td>String identifying the session attribute.</td></tr>
 * </table>
 *
 * @deprecated use SessionAttributeSelector instead
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: SessionStateSelector.java,v 1.4 2004/03/05 13:02:42 bdelacretaz Exp $
 */
public class SessionStateSelector extends SessionAttributeSelector {

    public static final String SESSION_STATE_ATTRIBUTE = "org.apache.cocoon.SessionState";

    public SessionStateSelector() {
        this.defaultName = SESSION_STATE_ATTRIBUTE;
    }

    public void configure(Configuration config)
    {
        // ignore
    }
}
