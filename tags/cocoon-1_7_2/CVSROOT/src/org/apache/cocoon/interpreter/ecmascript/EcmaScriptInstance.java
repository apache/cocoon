/*-- $Id: EcmaScriptInstance.java,v 1.4 2000-02-13 18:29:26 stefano Exp $ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.
 
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
 
 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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
package org.apache.cocoon.interpreter.ecmascript;

import java.util.*;
import FESI.jslib.*;
import org.w3c.dom.*;
import org.apache.cocoon.interpreter.*;

/**
 * @author <a href="mailto:rrocha@plenix.org">Ricardo Rocha</a>
 * @version $Revision: 1.4 $ $Date: 2000-02-13 18:29:26 $
 */

public class EcmaScriptInstance implements Instance {
  private Document document;
  private EcmaScriptEvaluator evaluator;

  public EcmaScriptInstance(EcmaScriptEvaluator theEvaluator, Document theDocument)
    throws LanguageException
  {
    this.evaluator = theEvaluator;
    this.document = theDocument;
  }

  public Object getInstance() {
    return this.evaluator.getGlobalObject();
  }

  public Node invoke(String methodName, Dictionary parameters, Node source) throws LanguageException {
    try {
      // parameters, context, source
      Object functionArgs[] = { parameters, source };

      JSGlobalObject globalObject = this.evaluator.getGlobalObject();
      Object object = globalObject.call(methodName, functionArgs);

      if (object == null) {
        return null;
      }

      if (object instanceof Node) {
        return (Node) object;
      }

      // Wrap as node
      return this.document.createTextNode(object.toString());

      // NOTE: Convert arrays to DocumentFragment's? Is it FS?
    } catch (JSException e) {
      e.printStackTrace();
      throw new LanguageException(e.getMessage());
    }
  }

  public void destroy() {
    this.evaluator.release();
  }
}