// $Id: SessionInvalidatorAction.java,v 1.1.2.2 2001-04-17 18:18:11 dims Exp $
package org.apache.cocoon.acting;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import org.apache.avalon.configuration.Parameters;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.ConfigurationException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import org.apache.cocoon.*;
import org.apache.cocoon.util.Tokenizer;
import org.apache.cocoon.environment.Request;

import javax.servlet.http.HttpSession;

/**
 * This is the action used to invalidate an HTTP session. The action returns
 * empty map if everything is ok, null otherwise.
 *
 * @author Martin Man &lt;Martin.Man@seznam.cz&gt;
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-04-17 18:18:11 $
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
        if (req == null) 
            return null;

        /* check session validity */
        HttpSession session = req.getSession (false);
        if (session != null)
            session.invalidate ();

        return Collections.unmodifiableMap (actionMap);
    }
}

// $Id: SessionInvalidatorAction.java,v 1.1.2.2 2001-04-17 18:18:11 dims Exp $
// vim: set et ts=4 sw=4:
