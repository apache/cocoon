package org.apache.cocoon.interpreter.java;

import org.apache.cocoon.interpreter.*;

/**
 * @author <a href="mailto:rrocha@plenix.org">Ricardo Rocha</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:16 $
 */

public class JavaInterpreter extends AbstractInterpreter {

  protected Module doCreateModule(String moduleName) throws LanguageException {
    try {
      return new JavaModule(moduleName);
    } catch (ClassNotFoundException e) {
      throw new LanguageException("Could not find class: " + e.getMessage() + ". Make sure it's in the classpath.");
    } catch (Exception e) {
      throw new LanguageException(e.getMessage());
    }
  }
}