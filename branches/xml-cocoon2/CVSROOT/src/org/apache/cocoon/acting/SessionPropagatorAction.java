// $Id: SessionPropagatorAction.java,v 1.1.2.1 2001-04-24 17:33:01 dims Exp $
package org.apache.cocoon.acting;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;

import org.apache.avalon.parameters.Parameters;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.ConfigurationException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import org.apache.cocoon.*;
import org.apache.cocoon.util.Tokenizer;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;


/**
 * This is the action used to propagate parameters into session. It
 * simply propagates given expression to the session. If session does not
 * exist, action fails. Additionaly it will make all propagated values
 * available via returned Map.
 * 
 * <pre>
 * &lt;map:act type="session-propagator"&gt;
 *      &lt;paramater name="example" value="{example}"&gt;
 *      &lt;paramater name="example1" value="xxx"&gt;
 * &lt;/map:act&gt;
 * </pre>
 *
 * @author Martin Man &lt;Martin.Man@seznam.cz&gt;
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-04-24 17:33:01 $
 */
public class SessionPropagatorAction extends ComposerAction
{
    /**
     * Main invocation routine.
     */
    public Map act (EntityResolver resolver, Map objectModel, String src,
            Parameters parameters) throws Exception {
        Request req = (Request)
            objectModel.get (Constants.REQUEST_OBJECT);
        HashMap actionMap = new HashMap ();

        if (req == null) {
            getLogger ().debug ("SESSIONVALIDATOR: no request object");
            return null;
        }

        /* check session validity */
        Session session = req.getSession (false);
        if (session == null) {
            getLogger ().debug ("SESSIONVALIDATOR: no session object");
            return null;
        }

        try {
            Iterator keys = parameters.getParameterNames ();
            while (keys.hasNext ()) {
                String sessionParamName = (String) keys.next ();
                if (sessionParamName == null || 
                        "".equals (sessionParamName.trim ()))
                    return null;
                String value = parameters.getParameter (sessionParamName, null);
                getLogger().debug ("SESSIONPROPAGATOR: propagating value " 
                        + value
                        + " to session attribute " 
                        + sessionParamName);
                session.setAttribute (sessionParamName, value);
                actionMap.put (sessionParamName, value);
            }
            getLogger().debug ("SESSIONPROPAGATOR: all params propagated "
                    + "to session");
            return Collections.unmodifiableMap (actionMap);
        } catch (Exception e) {
            getLogger().debug ("exception: ", e);
        }
        return null;
    }
}

// $Id: SessionPropagatorAction.java,v 1.1.2.1 2001-04-24 17:33:01 dims Exp $
// vim: set et ts=4 sw=4:

