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
import java.util.Map;
import java.util.Iterator;
import java.util.Collections;

/**
 * Created this class to assist the translation from easy to understand
 * role aliases and the real Avalon role names.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2001-02-08 12:24:22 $
 */

public class RoleUtils {

    private static final Map shorthand;
    private static final Map classname;

    static {
        HashMap setup = new HashMap();

        setup.put("cocoon", Roles.COCOON);
        setup.put("parser", Roles.PARSER);
        setup.put("processor", Roles.PROCESSOR);
        setup.put("store", Roles.STORE);
        setup.put("markup-languages", Roles.MARKUP_LANGUAGE);
        setup.put("programming-languages", Roles.PROGRAMMING_LANGUAGE);
        setup.put("program-generator", Roles.PROGRAM_GENERATOR);
        setup.put("classloader", Roles.CLASS_LOADER);
        setup.put("pool-controller", Roles.POOL_CONTROLLER);
        setup.put("image-encoder", Roles.IMAGE_ENCODER);
        setup.put("datasources", Roles.DB_CONNECTION);

        shorthand = Collections.unmodifiableMap(setup);

        setup = new HashMap();

        setup.put(Roles.PARSER, "org.apache.cocoon.components.parser.JaxpParser");
        setup.put(Roles.STORE, "org.apache.cocoon.components.store.MemoryStore");
        setup.put(Roles.PROGRAMMING_LANGUAGE, "org.apache.cocoon.CocoonComponentSelector");
        setup.put(Roles.MARKUP_LANGUAGE, "org.apache.cocoon.CocoonComponentSelector");
        setup.put(Roles.CLASS_LOADER, "org.apache.cocoon.components.classloader.ClassLoaderManagerImpl");
        setup.put(Roles.PROGRAM_GENERATOR, "org.apache.cocoon.components.language.generator.ProgramGeneratorImpl");
        setup.put(Roles.DB_CONNECTION, "org.apache.cocoon.CocoonComponentSelector");
        setup.put(Roles.POOL_CONTROLLER, "org.apache.cocoon.util.ComponentPoolController");

        classname = Collections.unmodifiableMap(setup);
    }

    public static String lookup(String shorthandName) {
        return (String) RoleUtils.shorthand.get(shorthandName);
    }

    public static Iterator shorthandNames() {
        return RoleUtils.shorthand.keySet().iterator();
    }

    public static String defaultClass(String role) {
        return (String) RoleUtils.classname.get(role);
    }
}
