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

*/
package org.apache.cocoon.components.flow;

import org.apache.cocoon.environment.Redirector;

import java.util.List;

/**
 * The interface to the flow scripting languages. This interface is
 * for a component, which implements the appropriate language to be
 * used for describing the flow. A system could have multiple
 * components that implement this interface, each of them for a
 * different scripting language.
 *
 * <p>A flow script defines what is the page flow in an interactive
 * Web application. Usually the flow is defined in a high level
 * programming language which provides the notion of continuations,
 * which allows for the flow of the application to be described as a
 * simple procedural program, without having to think about the
 * application as a finite state machine which changes its internal
 * state on each HTTP request from the client browser.
 *
 * <p>However an implementation may choose to use its own
 * representation of an application, which may include XML
 * representations of finite state machines. Note: this API has no
 * provision for such implementations.
 *
 * <p>The component represented by this interface is called in three
 * situations:
 *
 * <ul>
 *
 *  <li>
 *
 *    <p>From the sitemap, to invoke a top level function defined in a
 *    * given implementation language of the flow. This is done from
 *    the * sitemap using the construction:
 *
 *    <pre>
 *      &lt;map:call function="..." language="..."/&gt;
 *    </pre>
 *
 *    <p>The <code>language</code> attribute can be ignored if the *
 *    default language is used.
 *
 *  <li>
 *
 *    <p>From the sitemap, to continue a previously started
 *    computation. A previously started computation is saved in the
 *    form of a continuation inside the flow implementation language.
 *
 *    <p>This case is similar with the above one, but the function
 *    invoked has a special name, specific to each language
 *    implementation. See the language implementation for more
 *    information on the function name and the arguments it receives.
 *
 *  <li>
 *
 *    <p>From a program in the flow layer. This is done to invoke a
 *    pipeline defined in the sitemap, to generate the response of the
 *    request.
 *
 * </ul>
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @since March 11, 2002
 * @see InterpreterSelector
 * @version CVS $Id: Interpreter.java,v 1.6 2004/02/20 20:45:17 cziegeler Exp $
 */
public interface Interpreter
{


  public static class Argument
  {
    public String name;
    public String value;

    public Argument(String name, String value)
    {
      this.name = name;
      this.value = value;
    }

    public String toString()
    {
      return name + ": " + value;
    }
  }

  public static String ROLE = Interpreter.class.getName();

  /**
   * This method is called from the sitemap, using the syntax
   *
   * <pre>
   *   &lt;map:call function="..."/&gt;
   * </pre>
   *
   * The method will execute the named function, which must be defined
   * in the given language. There is no assumption made on how various
   * arguments are passed to the function.
   *
   * <p>The <code>params</code> argument is a <code>List</code> object
   * that contains <code>Interpreter.Argument</code> instances,
   * representing the parameters to be passed to the called
   * function. An <code>Argument</code> instance is a key-value pair,
   * where the key is the name of the parameter, and the value is its
   * desired value. Most languages will ignore the name value and
   * simply pass to the function, in a positional order, the values of
   * the argument. Some languages however can pass the arguments in a
   * different order than the original prototype of the function. For
   * these languages the ability to associate the actual argument with
   * a formal parameter using its name is essential.
   *
   * <p>A particular language implementation may decide to put the
   * environment, request, response etc. objects in the dynamic scope
   * available to the function at the time of the call. Other
   * implementations may decide to pass these as arguments to the
   * called function.
   *
   * <p>The current implementation assumes the sitemap implementation
   * is TreeProcessor.
   *
   * @param funName a <code>String</code> value, the name of the
   * function to call
   * @param params a <code>List</code> object whose components are
   * CallFunctionNode.Argument instances. The interpretation of the
   * parameters is left to the actual implementation of the
   * interpreter.
   * @param redirector a <code>Redirector</code> used to call views
   */
  void callFunction(String funName, List params, Redirector redirector)
    throws Exception;

  /**
   * Forward the request to a Cocoon pipeline.
   *
   * @param uri a <code>String</code>, the URI of the forwarded request
   * @param bizData an <code>Object</code>, the business data object
   * to be made available to the forwarded pipeline
   * @param continuation a <code>WebContinuation</code>, the
   * continuation to be called to resume the processing
   * @param redirector a <code>Redirector</code> used to call views
   * @exception Exception if an error occurs
   */
  void forwardTo(String uri, Object bizData, WebContinuation continuation,
                 Redirector redirector)
    throws Exception;

  /**
   * Continues a previously started processing. The continuation
   * object where the processing should start from is indicated by the
   * <code>continuationId</code> string.
   *
   * @param continuationId a <code>String</code> value
   *
   * @param params a <code>List</code> value, containing the
   * parameters to be passed when invoking the continuation. As
   * opposed to the parameters passed by <code>callFunction</code>,
   * these parameters will only become available in the language's
   * environment, if at all.
   *
   * @param redirector a <code>Redirector</code> used to call views
   * @exception Exception if an error occurs
   */
  void handleContinuation(String continuationId, List params,
                          Redirector redirector)
    throws Exception;
}
