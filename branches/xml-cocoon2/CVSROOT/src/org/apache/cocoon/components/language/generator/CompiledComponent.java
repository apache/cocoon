/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.generator;

import org.apache.avalon.Composer;
import org.apache.avalon.Modifiable;
import org.apache.avalon.Poolable;
import org.apache.avalon.Recyclable;

/**
 * This interface is the common base of all Compiled Components.  This
 * includes Sitemaps and XSP Pages
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2001-02-16 18:11:47 $
 */
public interface CompiledComponent extends Composer, Poolable, Recyclable, Modifiable {
}