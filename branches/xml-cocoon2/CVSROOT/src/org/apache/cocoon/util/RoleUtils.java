/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
 
package org.apache.cocoon.util;

import org.apache.cocoon.Roles;
import java.util.HashMap;

/**
 * Created this class to assist the translation from easy to understand
 * role aliases and the real Avalon role names.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-01-15 04:44:26 $
 */

public class RoleUtils {

    private static final HashMap shorthand;

    static {
        HashMap setup = new HashMap();

        setup.put("cocoon", Roles.COCOON);
        setup.put("parser", Roles.PARSER);
        setup.put("processor", Roles.PROCESSOR);
        setup.put("store", Roles.STORE);
        setup.put("markup-language", Roles.MARKUP_LANGUAGE);
        setup.put("programming-language", Roles.PROGRAMMING_LANGUAGE);
        setup.put("program-generator", Roles.PROGRAM_GENERATOR);
        setup.put("classloader", Roles.CLASS_LOADER);
        setup.put("pool-controller", Roles.POOL_CONTROLLER);
        setup.put("image-encoder", Roles.IMAGE_ENCODER);
        setup.put("datasource", Roles.DB_CONNECTION);

        shorthand = setup;
    }

    public static String lookup(String shorthandName) {
        return (String) RoleUtils.shorthand.get(shorthandName);
    }
}
