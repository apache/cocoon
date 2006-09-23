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
package org.apache.cocoon.forms.formmodel.algorithms;

import java.util.StringTokenizer;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.forms.formmodel.CalculatedFieldAlgorithm;
import org.apache.cocoon.forms.formmodel.CalculatedFieldAlgorithmBuilder;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Abstract builder for {@link org.apache.cocoon.forms.formmodel.algorithms.AbstractBaseAlgorithm}
 * subclasses.
 * 
 * <p>
 * This class parses the default triggers attribute, containing a comma separated list of widget paths
 * as defined in {@link org.apache.cocoon.forms.util.WidgetFinder}. It also calls the LifecycleHelper
 * so that algorithms gets their logger and context.
 * </p>
 * @version $Id$
 */
public abstract class AbstractBaseAlgorithmBuilder implements CalculatedFieldAlgorithmBuilder, LogEnabled, Contextualizable, Serviceable {

    private Logger logger;
    protected Context context;
    private ServiceManager manager;

    protected void setup(Element algorithmElement, AbstractBaseAlgorithm algorithm) throws Exception {
        setupComponent(algorithm);        
        setupTriggers(algorithmElement, algorithm);
    }

    protected void setupTriggers(Element algorithmElement, AbstractBaseAlgorithm algorithm) throws Exception {
        String fields = DomHelper.getAttribute(algorithmElement, "triggers", null);
        if (fields != null) setupTriggers(fields, algorithm);        
    }
    
    protected void setupTriggers(String fields, AbstractBaseAlgorithm algorithm) {
        algorithm.clearTriggers();
        StringTokenizer stok = new StringTokenizer(fields, ", ");
        while (stok.hasMoreTokens()) {
            String fname = stok.nextToken();
            algorithm.addTrigger(fname);
        }        
    }

    public void enableLogging(Logger logger) {
        this.logger = logger;
    }
    
    protected Logger getLogger() {
        return this.logger;
    }

    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }
    
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    public void setupComponent(CalculatedFieldAlgorithm algorithm) throws Exception {
        LifecycleHelper.setupComponent(algorithm, logger, context, manager, null);        
    }
}
