package org.apache.cocoon.interpreter;

import java.util.*;
import org.apache.cocoon.framework.*;

/**
 * @author <a href="mailto:rrocha@plenix.org">Ricardo Rocha</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:16 $
 */

public abstract class AbstractInterpreter extends AbstractActor implements Interpreter {
  private Hashtable modules;

  public AbstractInterpreter() {
    this.modules = new Hashtable();
  }

  public Enumeration getModules() {
    return modules.elements();
  }

  public Module createModule(String moduleName) throws LanguageException {
    Module module = (Module) this.modules.get(moduleName);

    if (module == null) {
      module = doCreateModule(moduleName);
    }

    return module;
  }

  protected abstract Module doCreateModule(String moduleName) throws LanguageException;
}