/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon;

/**
 * Created this interface to specify the Avalon role names.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2001-02-15 20:28:31 $
 */

public interface Roles {

    String PARSER               = "org.apache.cocoon.components.parser.Parser";
    String PROCESSOR            = "org.apache.cocoon.Processor";
    String STORE                = "org.apache.cocoon.components.store.Store";
    String REPOSITORY           = "org.apache.cocoon.components.store.Repository";

    String MARKUP_LANGUAGE      = "org.apache.cocoon.components.language.markup.MarkupLanguageSelector";
    String PROGRAMMING_LANGUAGE = "org.apache.cocoon.components.language.programming.ProgrammingLanguageSelector";
    String PROGRAM_GENERATOR    = "org.apache.cocoon.components.language.generator.ProgramGenerator";
    String CLASS_LOADER         = "org.apache.cocoon.components.classloader.ClassLoaderManager";
    String POOL_CONTROLLER      = "org.apache.avalon.util.pool.PoolController";
    String IMAGE_ENCODER        = "org.apache.cocoon.components.image.ImageEncoderSelector";
    String DB_CONNECTION        = "org.apache.avalon.util.datasource.DataSourceComponentSelector";
    String URL_FACTORY          = "org.apache.cocoon.components.url.URLFactory";

}
