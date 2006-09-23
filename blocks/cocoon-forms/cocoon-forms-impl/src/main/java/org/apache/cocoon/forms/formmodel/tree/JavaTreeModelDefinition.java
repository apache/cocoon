/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.forms.formmodel.tree;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.LifecycleHelper;

/**
 * A {@link org.apache.cocoon.forms.formmodel.tree.TreeModelDefinition} based on an Java class
 * implementing {@link org.apache.cocoon.forms.formmodel.tree.TreeModel}.
 *
 * @version $Id$
 */
public class JavaTreeModelDefinition extends AbstractLogEnabled
                                     implements TreeModelDefinition, Contextualizable, Serviceable {

    private Class modelClass;

    Context ctx;
    ServiceManager manager;

    public void contextualize(Context context) throws ContextException {
        this.ctx = context;
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    public void setModelClass(Class clazz) {
        this.modelClass = clazz;
    }

    public TreeModel createInstance() {
        TreeModel model;
        try {
            model = (TreeModel)modelClass.newInstance();
            LifecycleHelper.setupComponent(model, getLogger(), ctx, manager, null);
        } catch (Exception e) {
            throw new CascadingRuntimeException("Cannot instanciate class " + modelClass.getName(), e);
        }

        return model;
    }
}
