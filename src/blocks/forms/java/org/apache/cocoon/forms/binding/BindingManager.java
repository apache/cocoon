/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.forms.binding;

import org.apache.excalibur.source.Source;

/**
 * BindingManager declares the factory method that produces actual Bindings.
 * @version CVS $Id: BindingManager.java,v 1.2 2004/06/01 10:51:28 bruno Exp $
 */
public interface BindingManager {

    /**
     * Avalon Role for this service interface.
     */
    String ROLE = BindingManager.class.getName();

    /**
     * Constant matching the namespace used for the Binding config files.
     */
    String NAMESPACE = "http://apache.org/cocoon/forms/1.0#binding";

    /**
     * Creates a binding from the XML config found at source parameter.
     * The binding will be cached.
     */
    Binding createBinding(Source bindingFile) throws BindingException;

    /**
     * Creates a binding from the XML config found at bindingURI parameter.
     * The binding will be cached.
     */
    Binding createBinding(String bindingURI) throws BindingException;
}
