/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.generator;

import java.io.File;

import org.apache.avalon.component.Component;
import org.apache.avalon.component.ComponentManager;
import org.apache.avalon.component.ComponentException;
import org.apache.avalon.Disposable;
import org.apache.avalon.configuration.DefaultConfiguration;

import org.apache.cocoon.components.classloader.ClassLoaderManager;
import org.apache.cocoon.Roles;
import org.apache.cocoon.Constants;
import org.apache.excalibur.component.DefaultComponentSelector;
import org.apache.cocoon.util.ClassUtils;

/**
 * This interface is the common base of all Compiled Components.  This
 * includes Sitemaps and XSP Pages
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.15 $ $Date: 2001-04-20 20:49:50 $
 */
public class GeneratorSelector extends DefaultComponentSelector implements Disposable {
    private ClassLoaderManager classManager;

    /** The component manager */
    protected ComponentManager manager = null;

    public void compose (ComponentManager manager) throws ComponentException {
        super.compose(manager);
        this.manager = manager;

        try {
            this.classManager = (ClassLoaderManager) manager.lookup(Roles.CLASS_LOADER);
        } catch (ComponentException cme) {
            throw new ComponentException("GeneratorSelector", cme);
        }

        try {
            this.classManager.addDirectory((File) this.m_context.get(Constants.CONTEXT_WORK_DIR));
        } catch (Exception e) {
            throw new ComponentException("Could not add repository to ClassLoaderManager", e);
        }
    }

    public Component select(Object hint) throws ComponentException {
        try {
            return super.select(hint);
        } catch (Exception e) {
            // if it isn't loaded, it may already be compiled...
            this.addGenerator(hint);
            return super.select(hint);
        }
    }

    private void addGenerator(Object hint) throws ComponentException {
        Class generator;
        String className = hint.toString().replace(File.separatorChar, '.');
        try {
            generator = this.classManager.loadClass(className);
        } catch (Exception e) {
            throw new ComponentException("Could not add component for class: " + className, e);
        }

        this.addGenerator(hint, generator);
    }

    public void addGenerator(Object hint, Class generator) throws ComponentException {
        super.addComponent(hint, generator, new DefaultConfiguration("", "GeneratorSelector"));
    }

    public void dispose() {
        this.manager.release((Component)this.classManager);
        super.dispose();
    }
}
