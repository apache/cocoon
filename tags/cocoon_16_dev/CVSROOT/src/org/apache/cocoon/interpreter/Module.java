package org.apache.cocoon.interpreter;

import org.w3c.dom.Document;
import java.util.Dictionary;

/**
 * @author <a href="mailto:rrocha@plenix.org">Ricardo Rocha</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:16 $
 */

public interface Module {
  public Instance createInstance(Document document, Dictionary parameters) throws LanguageException;
}