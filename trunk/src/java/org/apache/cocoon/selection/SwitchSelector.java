/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.selection;

import org.apache.avalon.framework.parameters.Parameters;

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
 * @version CVS $Id: SwitchSelector.java,v 1.2 2003/12/29 15:24:35 unico Exp $
 */
public interface SwitchSelector extends Selector {

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


