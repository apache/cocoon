package org.apache.cocoon.interpreter;

import org.w3c.dom.Node;
import java.util.Dictionary;

/**
 * @author <a href="mailto:rrocha@plenix.org">Ricardo Rocha</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:16 $
 */

public interface Instance {
  public Object getInstance();
  public Node invoke(String methodName, Dictionary parameters, Node source) throws LanguageException;
  public void destroy();
}