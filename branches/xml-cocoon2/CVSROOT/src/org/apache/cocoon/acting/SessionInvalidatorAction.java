// $Id: SessionInvalidatorAction.java,v 1.1.2.7 2001-04-25 21:01:47 dims Exp $
package org.apache.cocoon.acting;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.avalon.logger.AbstractLoggable;
import org.apache.avalon.parameters.Parameters;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.log.Logger;
import org.xml.sax.EntityResolver;

/**
 * This is the action used to invalidate an HTTP session. The action returns
 * empty map if everything is ok, null otherwise.
 *
 * @author Martin Man &lt;Martin.Man@seznam.cz&gt;
 * @version CVS $Revision: 1.1.2.7 $ $Date: 2001-04-25 21:01:47 $
 */
public class SessionInvalidatorAction extends AbstractAction
{
    /**
     * Main invocation routine.
     */
    public Map act (EntityResolver resolver, Map objectModel, String src,
            Parameters parameters) throws Exception {
        Request req = (Request)
            objectModel.get (Constants.REQUEST_OBJECT);
        HashMap actionMap = new HashMap ();

        /* check request validity */
        if (req == null) {
            getLogger ().debug ("SESSIONINVALIDATOR: no request object");
            return null;
        }

        /* check session validity */
        Session session = req.getSession (false);
        if (session != null) {
            session.invalidate ();
            getLogger ().debug ("SESSIONINVALIDATOR: session invalidated");
        } else {
            getLogger ().debug ("SESSIONINVALIDATOR: no session object");
        }

        return Collections.unmodifiableMap (actionMap);
    }
}

// $Id: SessionInvalidatorAction.java,v 1.1.2.7 2001-04-25 21:01:47 dims Exp $
// vim: set et ts=4 sw=4:
