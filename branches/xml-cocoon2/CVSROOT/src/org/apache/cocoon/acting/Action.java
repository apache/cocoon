/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.acting;

import java.util.Map;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.11 $ $Date: 2001-04-30 14:16:56 $
 */
public interface Action extends Component, ThreadSafe {
    /**
     * Controls the processing against some values of the
     * <code>Dictionary</code> objectModel and returns a
     * <code>Map</code> object with values used in subsequent
     * sitemap substitution patterns.
     *
     * NOTE: It is important that <code>Action<code> classes are
     * written in a thread safe manner.
     *
     * @param resolver    The <code>EntityResolver</code> in charge
     * @param objectModel The <code>Map</code> with object of the
     *                    calling environment which can be used
     *                    to select values this controller may need
     *                    (ie Request, Response).
     * @param source      A source <code>String</code> to the Action
     * @param parameters  The <code>Parameters</code> for this invocation
     * @return Map        The returned <code>Map</code> object with
     *                    sitemap substitution values which can be used
     *                    in subsequent elements attributes like src=
     *                    using a xpath like expression: src="mydir/{myval}/foo"
     *                    If the return value is null the processing inside
     *                    the <map:act> element of the sitemap will
     *                    be skipped.
     * @exception Exception Indicates something is totally wrong
     */
    Map act(EntityResolver resolver, Map objectModel, String source, Parameters par)
    throws Exception;
}



