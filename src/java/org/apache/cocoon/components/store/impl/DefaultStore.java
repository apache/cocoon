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
package org.apache.cocoon.components.store.impl;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.excalibur.store.impl.MRUMemoryStore;

/**
 * Default implementation of Cocoon's store. It's a <code>MRUMemoryStore</code> whose
 * "<code>use-persistent-cache</code>" parameter defaults to <code>true</code>.
 * <p>
 * This default setting allows the store to be an in-memory front-end to the persistent store.
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: DefaultStore.java,v 1.8 2004/03/05 13:02:51 bdelacretaz Exp $
 */
public class DefaultStore extends MRUMemoryStore {
    
    public void parameterize(Parameters params) throws ParameterException {
        if (!params.isParameter("use-persistent-cache")) {
            params.setParameter("use-persistent-cache", "true");
        }
        super.parameterize(params);
    }
}
