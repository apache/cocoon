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

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;

import java.util.Map;

/**
 * Abstract SwitchSelector class.
 *
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: AbstractSwitchSelector.java,v 1.2 2004/03/05 13:02:57 bdelacretaz Exp $
 */
public abstract class AbstractSwitchSelector extends AbstractLogEnabled
    implements SwitchSelector {

    /**
     * Method to create a selector context.
     *
     * @param objectModel The <code>Map</code> containing object of the
     *                    calling environment which may be used
     *                    to select values to test the expression.
     * @param parameters  The sitemap parameters, as specified by
     *                    &lt;parameter/&gt; tags.
     * @return selector context
     */
    public abstract Object getSelectorContext(Map objectModel, Parameters parameters);

    /**
     * Selectors test pattern against some objects in a <code>Map</code>
     * model and signals success with the returned boolean value
     * @param expression  The expression to test.
     * @return boolean    Signals successful test.
     */
    public abstract boolean select(String expression, Object selectorContext);

    /**
     * Selectors test pattern against some objects in a <code>Map</code>
     * model and signals success with the returned boolean value
     * @param expr        The expression to test.
     * @return Signals successful test.
     */
    public boolean select(String expr, Map objectModel, Parameters params) {
        return select(expr, getSelectorContext(objectModel, params));
    }
}


