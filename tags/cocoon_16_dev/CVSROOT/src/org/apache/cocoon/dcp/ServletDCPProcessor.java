package org.apache.cocoon.dcp;

import org.w3c.dom.*;
import javax.servlet.http.*;
import org.apache.cocoon.framework.*;

/**
 * The convenience class that all DCP objects willing to access the
 * servlet request parameters should extend.
 *
 * @author <a href="mailto:rrocha@plenix.org">Ricardo Rocha</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:28 $
 */

public abstract class ServletDCPProcessor extends DefaultDCPProcessor {
  protected HttpServletRequest request;

  public void init(Configurations configurations) {
    super.init(configurations);
    this.request = (HttpServletRequest) this.parameters.get("request");
  }
}