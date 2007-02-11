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
package org.apache.cocoon.components.store.impl;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.excalibur.store.impl.MRUMemoryStore;

/**
 * Default implementation of Cocoon's transient store. This is a <code>MRUMemoryStore</code>
 * that cannot be backed by a persistent store (this ensure it is really transient).
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id$
 */
public class DefaultTransientStore extends MRUMemoryStore {
    
    public void parameterize(Parameters params) throws ParameterException {
        if (params.getParameterAsBoolean("use-persistent-cache", false)) {
            throw new ParameterException("A transient store cannot be backed by a persistent store.");
        }
        super.parameterize(params);
    }
}
