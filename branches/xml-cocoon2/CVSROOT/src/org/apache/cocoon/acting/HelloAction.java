/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.acting;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

import org.apache.avalon.Component;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;
import org.apache.avalon.Parameters;

import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

import org.apache.cocoon.Constants;

/**
 * A simple Action that tracks if a <code>Session</code> object
 * has been created or not.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.9 $ $Date: 2001-02-09 22:16:54 $
 */
public class HelloAction extends ComposerAction {

    /**
     * A simple Action that logs if the <code>Session</code> object
     * has been created
     */
    public Map act (EntityResolver resolver, Map objectModel, String src, Parameters par) throws Exception {
        HttpServletRequest req = (HttpServletRequest) objectModel.get(Constants.REQUEST_OBJECT);
        if (req != null) {
            HttpSession session = req.getSession (false);

            if (session != null) {
                if (session.isNew()) {
                    getLogger().debug("Session is new");
                } else {
                    getLogger().debug("Session is old");
                }
            } else {
                getLogger().debug("A session object was not created");
            }
        }

        return null;
    }
}



