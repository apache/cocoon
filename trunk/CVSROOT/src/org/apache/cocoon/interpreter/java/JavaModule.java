package org.apache.cocoon.interpreter.java;

import java.util.*;
import org.w3c.dom.*;
import java.lang.reflect.*;
import org.apache.cocoon.framework.*;
import org.apache.cocoon.interpreter.*;

/**
 * @author <a href="mailto:rrocha@plenix.org">Ricardo Rocha</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:16 $
 */

public class JavaModule implements Module {
  private static Class[][] expectedTypes = {
    {},
    {Dictionary.class},
    {Dictionary.class, Node.class},
    {Node.class, Dictionary.class, org.openxml.x3p.ProcessContext.class},
    {Node.class},
  };

  private String className;
  private Class loadedClass;
  private Hashtable methods;

  public JavaModule(String theClassName)
    throws ClassNotFoundException,
           NoSuchMethodException
  {
    this.className = theClassName;
    this.methods = new Hashtable();

    this.loadedClass = Class.forName(this.className);

    Method[] methodList = this.loadedClass.getMethods();

    for (int i = 0; i < methodList.length; i++) {
      int modifiers = methodList[i].getModifiers();

      // Public local method
      if (methodList[i].getDeclaringClass() == this.loadedClass && Modifier.isPublic(modifiers)) {
        Class[] parameterTypes = methodList[i].getParameterTypes();


        for (int j = 0; j < expectedTypes.length; j++) {
          if (parameterTypes.length == expectedTypes[j].length) { // Same number of parameters
            int count;

            for (count = 0; count < parameterTypes.length; count++) {
              if (expectedTypes[j][count] != parameterTypes[count]) {
                break;
              }
            }
  
            if (count == expectedTypes[j].length) { // Same parameter types: bingo!
              this.methods.put(methodList[i].getName(), methodList[i]);
	          break;
            }
          }
        }
      }
    }

    if (this.methods.size() == 0) {
      throw new NoSuchMethodException("No suitable methods in class " + this.className);
    }
  }

  public Instance createInstance(Document document, Dictionary parameters) throws LanguageException {
    try {
      Instance instance = new JavaInstance(this.loadedClass.newInstance(), this.methods);
      Object object = instance.getInstance();

      if (object instanceof Configurable) {
        Configurations configurations = new Configurations();

        configurations.put("document", document);
        configurations.put("parameters", parameters);

        ((Configurable) object).init(configurations);
      }

      return instance;
    } catch (Exception e) {
      throw new LanguageException(e.getMessage());
    }
  }
}