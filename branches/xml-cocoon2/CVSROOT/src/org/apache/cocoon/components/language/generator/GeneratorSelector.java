/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.generator;

import java.io.File;

import org.apache.avalon.Component;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.ComponentManagerException;
import org.apache.avalon.configuration.DefaultConfiguration;

import org.apache.cocoon.components.classloader.ClassLoaderManager;
import org.apache.cocoon.Roles;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.CocoonComponentSelector;
import org.apache.cocoon.util.ClassUtils;

/**
 * This interface is the common base of all Compiled Components.  This
 * includes Sitemaps and XSP Pages
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2001-03-21 18:40:55 $
 */
public class GeneratorSelector extends CocoonComponentSelector {
    private ClassLoaderManager classManager;

    public void compose (ComponentManager manager) throws ComponentManagerException {
        super.compose(manager);

        this.classManager = (ClassLoaderManager) manager.lookup(Roles.CLASS_LOADER);

        try {
            this.classManager.addDirectory((File) this.context.get(Constants.CONTEXT_WORK_DIR));
        } catch (Exception e) {
            throw new ComponentManagerException("Could not add repository to ClassLoaderManager", e);
        }
    }

    public Component select(Object hint) throws ComponentManagerException {
        try {
            return super.select(hint);
        } catch (Exception e) {
            // if it isn't loaded, it may already be compiled...
            this.addGenerator(hint);
            return super.select(hint);
        }
    }

    private void addGenerator(Object hint) throws ComponentManagerException {
        Class generator;
        String className = hint.toString().replace(File.separatorChar, '.');
        try {
            generator = this.classManager.loadClass(className);
        } catch (Exception e) {
            throw new ComponentManagerException("Could not add component for class: " + className, e);
        }

        this.addGenerator(hint, generator);
    }

    public void addGenerator(Object hint, Class generator) throws ComponentManagerException {
        super.addComponent(hint, generator, new DefaultConfiguration("", "GeneratorSelector"));
    }
}
