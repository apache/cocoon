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

import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-10-19 14:42:52 $
 */
public class HelloAction implements Action {
    /**
     * A simple Action that logs if the <code>Session</code> object
     * has been created
     */
    public List act (EntityResolver resolver, Map objectModel, Parameters par) throws Exception {
        HttpServletRequest req = (HttpServletRequest) objectModel.get("request");
        if (req != null) {
            HttpSession session = req.getSession (false);
            ServletContext context = (ServletContext)objectModel.get("context");
            if (context != null) {
                if (session != null) {
                    if (session.isNew()) {
                        context.log("Session is new");
                    } else {
                        context.log("Session is old");
                    }
                } else {
                    context.log("A session object was not created");
                }
            } else {
                if (session != null) {
                    if (session.isNew()) {
                        System.out.println("Session is new");
                    } else {
                        System.out.println("Session is old");
                    }
                } else {
                    System.out.println("A session object was not created");
                }
            }
        }
        return null;
    }
}



