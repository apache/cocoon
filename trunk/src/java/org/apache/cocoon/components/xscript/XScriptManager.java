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
package org.apache.cocoon.components.xscript;

import java.util.Map;

/**
 * <code>XScriptManager</code> is the public interface used to
 * interact with the XScript component, which implements the
 * supporting code for the XScript language.
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: XScriptManager.java,v 1.1 2003/03/09 00:09:27 pier Exp $
 * @since August  4, 2001
 */
public interface XScriptManager
{
    String ROLE = "org.apache.cocoon.components.xscript.XScriptManager";

    String XSCRIPT_NS = "http://apache.org/xsp/xscript/1.0";

    /**
     * The variable's global scope. Each Cocoon instance has exactly one
     * global scope for variables.
     */
    int GLOBAL_SCOPE = 1;

    /**
     * The session scope. This scope is specific to a particular
     * activation of an XSP page, which is usually identified by the
     * session id of the user that sent the HTTP request. From XScript's
     * point of view however, the session identifier is not interpreted
     * in any way, so a client can define its own notion of "session".
     */
    int SESSION_SCOPE = 2;

    /**
     * The page scope. This is scope very specific to an XSP page, and
     * defines variables visible only within the context of that
     * page. No other XSP page can access these variables, unless it
     * knows the identifier used by that page to store its
     * variables. This identifier is not necessarily the URL or full
     * path of that page; it is up to the page to define it.
     */
    int PAGE_SCOPE = 3;

    /**
     * The request scope. This is scope specific to request, and
     * defines variables visible only within the context of that
     * request. Once request is processed, these variables are lost.
     */
    int REQUEST_SCOPE = 5;

    /**
     * Search for a variable in all the accessible scopes. The variable
     * is first searched in the current session scope. If no variable is
     * found here, the current page scope is searched next. If nothing
     * is found either, the global scope is searched.
     */
    int ALL_SCOPES = 4;

    /**
     * Obtains the object value of the
     * <code>name</code> variable in <code>scope</code>. The
     * <code>context</code> parameter is interpreted differently
     * depending on the value of <code>scope</code>, as follows:
     *
     * <ul>
     *
     *  <li>if <code>scope</code> is <code>{@link #GLOBAL_SCOPE}</code>, the
     *  value of <code>context is ignored.
     *
     *  <li>if <code>scope</code> is <code>{@link
     *  #SESSION_SCOPE}</code>, the value of <code>context</code> is
     *  interpreted as the session identifier.
     *
     *  <li>if <code>scope</code> is <code>{@link #PAGE_SCOPE}</code>, the value
     *  of <code>context</code> is interpreted as an identifier of the
     *  page. This could be the URL of the page or the path of the file
     *  name in the file system.
     *
     * </ul>
     *
     * @param objectModel an instance of Cocoon object model used to obtain context
     * @param name a <code>String</code> value
     * @param scope an <code>int</code> value
     * @return a <code>{@link XScriptObject}</code> value
     */
    XScriptObject get(XScriptVariableScope pageScope,
                      Map objectModel,
                      String name,
                      int scope) throws IllegalArgumentException;

    /**
     * Search for the first occurence of the variable
     * <code>name</code>.
     *
     * <p>The search happens first in the session scope
     * identified by <code>sessionContext</code>. If no variable is
     * found here, the search continues in the page scope identified by
     * <code>pageContext</code>. If no variable is found here, it's
     * finally searched in the global scope.
     *
     * <p>The <code>XScriptObject</code> value of the variable is
     * returned if a variable is found in one of the scopes, otherwise
     * an exception is thrown.
     *
     * @param objectModel an instance of Cocoon object model used to obtain context
     * @param name a <code>String</code> value
     * @return a <code>XScriptObject</code> value
     * @exception IllegalArgumentException if an error occurs
     */
    XScriptObject getFirst(XScriptVariableScope pageScope,
                           Map objectModel,
                           String name) throws IllegalArgumentException;

    /**
     * Defines or overwrites the value of variable
     * <code>name</code> in <code>scope</code>. The <code>context</code>
     * argument is interpreted as described in
     * {@link #get(XScriptVariableScope, Map, String, int)}.
     *
     * @param objectModel an instance of Cocoon object model used to obtain context
     * @param name a <code>String</code> value
     * @param value a <code>XScriptObject</code> value
     * @param scope an <code>int</code> value
     */
    void put(XScriptVariableScope pageScope,
             Map objectModel,
             String name,
             XScriptObject value,
             int scope) throws IllegalArgumentException;

    /**
     * Removes a variable previously declared in <code>scope</code>
     * within <code>context</code>. Such a variable could be declared
     * using the {@link #put(XScriptVariableScope, Map, String, XScriptObject, int)}
     * method.
     *
     * @param objectModel an instance of Cocoon object model used to obtain context
     * @param name a <code>String</code> value
     * @param scope an <code>int</code> value
     * @exception IllegalArgumentException if an error occurs
     */
    XScriptObject remove(XScriptVariableScope pageScope,
                         Map objectModel,
                         String name,
                         int scope) throws IllegalArgumentException;

    /**
     * Remove the first appearance of <code>name</code> in the all the
     * currently accessible scopes. The search happens as described in
     * {@link #getFirst(XScriptVariableScope, Map, String)}.
     *
     * @param objectModel an instance of Cocoon object model used to obtain context
     * @param name a <code>String</code> value
     * @exception IllegalArgumentException if an error occurs
     */
    XScriptObject removeFirst(XScriptVariableScope pageScope,
                              Map objectModel,
                              String name) throws IllegalArgumentException;
}
