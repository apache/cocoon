/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.matching;

import java.util.List;
import java.util.Map;

import org.apache.avalon.Component;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2000-12-11 16:06:51 $
 */
public interface Matcher extends Component {
    /**
     * Matches the pattern against some <code>Request</code> values
     * and returns a <code>Map</code> object with replacements
     * for wildcards contained in the pattern.
     * @param pattern     The pattern to match against. Depending on the
     *                    implementation the pattern can contain wildcards
     *                    or regular expressions.
     * @param objectModel The <code>Map</code> with object of the
     *                    calling environment which can be used
     *                    to select values this matchers matches against.
     * @return Map        The returned <code>Map</code> object with
     *                    replacements for wildcards/regular-expressions
     *                    contained in the pattern.
     *                    If the return value is null there was no match.
     */
    List match (String pattern, Map objectModel);
}



