/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.selection;

import java.util.Map;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.parameters.Parameters;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:leo.sutic">Leo Sutic</a>
 * @version CVS $Revision: 1.1.2.11 $ $Date: 2001-05-09 16:42:24 $
 */
public interface Selector extends Component {
    /**
     * Selectors test pattern against some objects in a <code>Map</code>
     * model and signals success with the returned boolean value
     * @param expression  The expression to test.
     * @param objectModel The <code>Map</code> containing object of the
     *                    calling environment which may be used
     *                    to select values to test the expression.
     * @param parameters  The sitemap parameters, as specified by &lt;parameter/&gt; tags.
     * @return boolean    Signals successfull test.
     */
    boolean select (String expression, Map objectModel, Parameters parameters);
}


