package org.apache.cocoon.interpreter.ecmascript;

import java.util.*;
import FESI.jslib.*;
import org.w3c.dom.*;
import org.apache.cocoon.interpreter.*;

/**
 * @author <a href="mailto:rrocha@plenix.org">Ricardo Rocha</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:17 $
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