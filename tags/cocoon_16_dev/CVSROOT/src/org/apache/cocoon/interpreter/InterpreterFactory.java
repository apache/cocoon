package org.apache.cocoon.interpreter;

import java.util.*;
import org.apache.cocoon.framework.*;

/**
 * @author <a href="mailto:rrocha@plenix.org">Ricardo Rocha</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:16 $
 */

public class InterpreterFactory extends Router {
    
    public Interpreter getInterpreter(String language) throws LanguageException {
        if (language == null) language = defaultType;
        return (Interpreter) objects.get(language);
    }
    
    public String getStatus() {
        return "<b>Cocoon Language Interpreters</b>" + super.getStatus();
    }    
}