// $Id: SessionValidatorAction.java,v 1.1.2.4 2001-04-20 20:49:46 bloritsch Exp $
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
 * This is the action used to validate HTTP session parameters.
 * The parameters are described via the external xml
 * file (its format is defined in AbstractValidatorAction).
 * <pre>
 * &lt;map:act type="session-validator"&gt;
 * 	&lt;parameter name="descriptor" value="context://descriptor.xml"&gt;
 * 	&lt;parameter name="validate" value="username,password"&gt;
 * &lt;/map:act&gt;
 * </pre>
 *
 * The list of parameters to be validated is specified as a comma separated
 * list of their names. descriptor.xml can therefore be used among many
 * various actions.
 *
 * This action returns null when validation fails, otherwise it provides
 * all validated parameters to the sitemap via {name} expression.
 *
 * @author Martin Man &lt;Martin.Man@seznam.cz&gt;
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2001-04-20 20:49:46 $
 */
public class SessionValidatorAction extends AbstractValidatorAction
{
    /**
     * Main invocation routine.
     */
    public Map act (EntityResolver resolver, Map objectModel, String src,
            Parameters parameters) throws Exception {
        Request req = (Request)
            objectModel.get (Constants.REQUEST_OBJECT);

        if (req == null)
            return null;

        /* check session validity */
        Session session = req.getSession (false);
        if (session == null)
            return null;

        try {
            Configuration conf = this.getConfiguration (
                    parameters.getParameter ("descriptor", null));
            Configuration[] desc = conf.getChildren ("parameter");
            String required = parameters.getParameter ("validate", null);
            HashMap actionMap = new HashMap ();

            /* get list of params to be validated */
            String[] rparams = Tokenizer.tokenize (required, ",", false);

            /* perform actuall validation */
            Object result;
            for (int i = 0; i < rparams.length; i ++) {
                rparams[i] = rparams[i].trim ();
                result = validateParameter (rparams[i], desc,
                        session.getAttribute (rparams[i]), false);
                if (result == null) {
                    return null;
                }
                session.setAttribute (rparams[i], result);
                actionMap.put (rparams[i], result);
            }
            getLogger().debug ("SESSIONVALIDATOR: all session params validated");
            return Collections.unmodifiableMap (actionMap);
        } catch (Exception e) {
            getLogger().debug ("exception: ", e);
        }
        return null;
    }
}

// $Id: SessionValidatorAction.java,v 1.1.2.4 2001-04-20 20:49:46 bloritsch Exp $
// vim: set et ts=4 sw=4:
