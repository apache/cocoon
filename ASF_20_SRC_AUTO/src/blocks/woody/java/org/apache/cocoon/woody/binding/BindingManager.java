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
package org.apache.cocoon.woody.binding;

import org.apache.excalibur.source.Source;

/**
 * BindingManager declares the factory method that produces actual Bindings.
 * @version CVS $Id: BindingManager.java,v 1.5 2004/03/05 13:02:26 bdelacretaz Exp $
 */
public interface BindingManager {

    /**
     * Avalon Role for this service interface.
     */
    String ROLE = BindingManager.class.getName();

    /**
     * Constant matching the namespace used for the Binding config files.
     */
    String NAMESPACE = "http://apache.org/cocoon/woody/binding/1.0";

    /**
     * Creates a binding from the XML config found at source parameter.
     */
    Binding createBinding(Source bindingFile) throws BindingException;

}
