/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.acting;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

import org.apache.avalon.Component;
import org.apache.avalon.Parameters;

import org.apache.log.Logger;
import org.apache.log.LogKit;

import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.5 $ $Date: 2000-11-14 15:08:27 $
 */
public class HelloAction implements Action {

    private Logger log = LogKit.getLoggerFor("cocoon");

    /**
     * A simple Action that logs if the <code>Session</code> object
     * has been created
     */
    public List act (EntityResolver resolver, Map objectModel, String src, Parameters par) throws Exception {
        HttpServletRequest req = (HttpServletRequest) objectModel.get("request");
        if (req != null) {
            HttpSession session = req.getSession (false);
            ServletContext context = (ServletContext)objectModel.get("context");
            if (context != null) {
                if (session != null) {
                    if (session.isNew()) {
                        log.debug("Session is new");
                        context.log("Session is new");
                    } else {
                        log.debug("Session is new");
                        context.log("Session is old");
                    }
                } else {
                    log.debug("A session object was not created");
                    context.log("A session object was not created");
                }
            } else {
                if (session != null) {
                    if (session.isNew()) {
                        log.debug("Session is new");
                    } else {
                        log.debug("Session is old");
                    }
                } else {
                    log.debug("A session object was not created");
                }
            }
        }
        return null;
    }
}



