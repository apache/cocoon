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

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.parameters.Parameters;

import java.util.Map;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:leo.sutic">Leo Sutic</a>
 * @version CVS $Id: Selector.java,v 1.2 2004/03/05 13:02:57 bdelacretaz Exp $
 */
public interface Selector extends Component {

    String ROLE = Selector.class.getName();
    /**
     * Selectors test pattern against some objects in a <code>Map</code>
     * model and signals success with the returned boolean value
     * @param expression  The expression to test.
     * @param objectModel The <code>Map</code> containing object of the
     *                    calling environment which may be used
     *                    to select values to test the expression.
     * @param parameters  The sitemap parameters, as specified by &lt;parameter/&gt; tags.
     * @return boolean    Signals successfull test.
     */
    boolean select (String expression, Map objectModel, Parameters parameters);
}


