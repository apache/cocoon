/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

/**
 * This exception is thrown by a <code>URIMatcher</code> or by a
 * <code>URITranslator</code> when there's something wrong with the matching or
 * translation patterns.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-08-21 17:35:31 $
 */
public class PatternException extends Exception {

    /**
     * Construct a new <code>PatternException</code> instance.
     */
    public PatternException(String message) {
        super(message);
    }
}
