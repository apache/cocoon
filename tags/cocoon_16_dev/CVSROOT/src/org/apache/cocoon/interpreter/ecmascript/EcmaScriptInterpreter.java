package org.apache.cocoon.interpreter.ecmascript;

import java.util.*;
import org.apache.cocoon.interpreter.*;

/**
 * @author <a href="mailto:rrocha@plenix.org">Ricardo Rocha</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:17 $
 */

public class EcmaScriptInterpreter extends AbstractInterpreter {
    private static Hashtable modules = new Hashtable();
    
    protected Module doCreateModule(String moduleName) throws LanguageException {
        try {
            EcmaScriptModule module = (EcmaScriptModule) modules.get(moduleName);
    
            if (module == null) {
                module = new EcmaScriptModule(moduleName);
                modules.put(moduleName, module);
            }
    
            return module;
        } catch (Exception e) {
            throw new LanguageException(e.getMessage());
        }
    }
}