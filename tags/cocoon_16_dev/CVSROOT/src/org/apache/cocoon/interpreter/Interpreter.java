package org.apache.cocoon.interpreter;

import java.util.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements a language interpreter.
 * 
 * @author <a href="mailto:rrocha@plenix.org">Ricardo Rocha</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:16 $
 */

public interface Interpreter extends Actor {
    
  public Enumeration getModules();
  
  public Module createModule(String moduleName) throws LanguageException;
  
}