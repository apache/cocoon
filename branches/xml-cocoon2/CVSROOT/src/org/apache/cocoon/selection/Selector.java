/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.selection;

import java.util.Dictionary;

import org.apache.avalon.Component;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.5 $ $Date: 2000-08-04 21:11:59 $
 */
public interface Selector extends Component {
    /**
     * Selectors test pattern against some objects in a <code>Dictionary</code>
     * model and signals success with the returned boolean value
     * @param expression  The expression to test.
     * @param objectModel The <code>Dictionary</code> containing object of the 
     *                    calling environment which may be used
     *                    to select values to test the expression.
     * @return boolean    Signals successfull test.
     */
    public boolean select (String expression, Dictionary objectModel);
}


