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

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import java.util.Map;

/**
 * A <code>Selector</code> that matches a string in the parameters object passed to it.
 *
 * <pre>
 *  &lt;map:selector name="parameter" factory="org.apache.cocoon.selection.ParameterSelector"/&gt;
 *
 *   &lt;map:select type="parameter"&gt;
 *      &lt;map:parameter name="parameter-selector-test" value="{mySitemapParameter}"/&gt;
 *
 *      &lt;map:when test="myParameterValue"&gt;
 *         &lt;!-- executes iff {mySitemapParameter} == "myParameterValue" --&gt;
 *         &lt;map:transform src="stylesheets/page/uk.xsl"/&gt;
 *      &lt;/map:when&gt;
 *      &lt;map:otherwise&gt;
 *         &lt;map:transform src="stylesheets/page/us.xsl"/&gt;
 *      &lt;/map:otherwise&gt;
 *   &lt;/map:select&gt;
 * </pre>
 *
 * The purpose of this selector is to allow an action to set parameters
 * and to be able to select between different pipeline configurations
 * depending on those parameters.
 *
 * @author <a href="mailto:leo.sutic@inspireinfrastructure.com">Leo Sutic</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ParameterSelector.java,v 1.2 2004/03/05 13:02:57 bdelacretaz Exp $
 */
public class ParameterSelector implements ThreadSafe, Selector {

    public boolean select(String expression, Map objectModel, Parameters parameters) {
        String compareToString = parameters.getParameter("parameter-selector-test", null);
        return compareToString != null && compareToString.equals(expression);
    }
}
