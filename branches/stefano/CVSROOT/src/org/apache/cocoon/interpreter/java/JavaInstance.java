package org.apache.cocoon.interpreter.java;

import java.util.*;
import org.w3c.dom.*;
import java.lang.reflect.*;
import org.apache.cocoon.interpreter.*;

/**
 * @author <a href="mailto:rrocha@plenix.org">Ricardo Rocha</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:16 $
 */

public class JavaInstance implements Instance {
  private Object instance;
  private Hashtable methods;

  public JavaInstance(Object theInstance, Hashtable theMethods) throws LanguageException {
    this.instance = theInstance;
    this.methods = theMethods;
  }

  public Object getInstance() {
    return this.instance;
  }

  public Node invoke(String methodName, Dictionary parameters, Node source) throws LanguageException {
    Method method = (Method) this.methods.get(methodName);

    if (method == null) {
      throw new LanguageException("No such method: " + methodName);
    }

    try {
      Class[] parameterTypes = method.getParameterTypes();
      Object[] args = new Object[parameterTypes.length];

      for (int i = 0; i < args.length; i++) {
        if (parameterTypes[i] == Node.class) {
	      args[i] = source;
	    } else if (parameterTypes[i] == Dictionary.class) {
	      args[i] = parameters;
	    } else {
	      args[i] = null;
	    }
      }

      return toNode(method.invoke(this.instance, args), source.getOwnerDocument());
    } catch (Exception e) {
      e.printStackTrace();
      throw new LanguageException(e.getClass().getName() + ": " + e.getMessage());
    }
  }

  public void destroy() { }

  private static Node toNode(Object object, Document document) {
    if (object == null || object instanceof Void) {
      return null;
    } else if (object instanceof Node) {
      return (Node) object;
    } else if (object.getClass().isArray()) {
      Object[] elements = (Object[]) object;
      DocumentFragment fragment = document.createDocumentFragment();

      for (int i = 0; i < elements.length; i++) {
        fragment.appendChild(toNode(elements[i], document));
      }

      return fragment;
    } else {
      return document.createTextNode(object.toString());
    }
  }
}