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
 * SwitchSelector is an enhanced Selector interface that allows a
 * context object to be created to optimize selector conditional testing.
 *
 * <p>
 * The original Selector interface supports an <code>if-then-else</code> style
 * of conditional testing depending on whether a particular expression is true.
 * This causes Selector.select() to be invoked for each &lt;map:when&gt;
 * statement which may be undesirable due to performance or logic reasons.
 * </p>
 *
 * <p>
 *  <pre>
 *  Example, the following sitemap snippet:
 *
 *  &lt;map:select type="aSelector"&gt;
 *   &lt;map:when test="test-expr1"&gt;...&lt;/map:when&gt;
 *   &lt;map:when test="test-expr2"&gt;...&lt;/map:when&gt;
 *  &lt;/map:select&gt;
 *
 *  is interpreted as (pseudo-code):
 *
 *  if (aSelector.select("test-expr1", objectModel, params)) {
 *   ...
 *  } else if (aSelector.select("test-expr2", objectModel, params)) {
 *   ...
 *  }
 *
 *  ie. aSelector.select(...) is called once for each &lt;map:when&gt;
 *  statement.
 *  </pre>
 * </p>
 *
 * <p>
 * SwitchSelector allows the developer to first create a
 * context object which is passed with each call to select(). This context
 * object is created before any conditional tests are made, and hence can be
 * used to optimize conditional testing.
 * </p>
 *
 * <p>
 *  <pre>
 *  The above example implemented as a SwitchSelector would be
 *  interpreted as (psuedo-code):
 *
 *  Object selectorContext = aSelector.getSelectorContext(objectModel, params);
 *  
 *  if (aSelector.select("test-expr1", selectorContext)) {
 *   ...
 *  else if (aSelector.select("test-expr2", selectorContext)) {
 *   ...
 *  }
 *
 *  ie. the bulk of the selector's work is done in getSelectorContext(),
 *  select() simply compares whether the expression should be considered true. 
 *  </pre>
 * </p>
 *
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: SwitchSelector.java,v 1.2 2004/03/05 13:02:57 bdelacretaz Exp $
 */
public interface SwitchSelector extends Selector, ThreadSafe {

    String ROLE = SwitchSelector.class.getName();

    /**
     * Method to create a selector context.
     *
     * @param objectModel The <code>Map</code> containing object of the
     *                    calling environment which may be used
     *                    to select values to test the expression.
     * @param parameters  The sitemap parameters, as specified by
     *                    &lt;parameter/&gt; tags.
     * @return            Selector context
     */
    Object getSelectorContext(Map objectModel, Parameters parameters);

    /**
     * Switch Selectors test patterns against a context object
     * and signal success with the returned boolean value
     * @param expression  The expression to test.
     * @param selectorContext The context this test should be performed in.
     * @return            true if the test was successful.
     */
    boolean select(String expression, Object selectorContext);
}


