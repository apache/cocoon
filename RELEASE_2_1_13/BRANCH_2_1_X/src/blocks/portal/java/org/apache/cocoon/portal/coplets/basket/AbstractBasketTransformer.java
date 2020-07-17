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
package org.apache.cocoon.portal.coplets.basket;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.transformation.AbstractSAXTransformer;

/**
 * This is the base class for all basket transformers
 *
 * @version $Id: BasketTransformer.java 47047 2004-09-22 12:27:27Z vgritsenko $
 */
public abstract class AbstractBasketTransformer extends AbstractSAXTransformer
                                               implements Disposable {

    /** The namespace URI to listen for. */
    public static final String NAMESPACE_URI = "http://apache.org/cocoon/portal/basket/1.0";

    /** The basket manager */
    protected BasketManager basketManager;

    /**
     * Constructor
     */
    public AbstractBasketTransformer() {
        super.defaultNamespaceURI = NAMESPACE_URI;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.basketManager = (BasketManager) this.manager.lookup(BasketManager.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            this.manager.release(this.basketManager);
        }
        super.dispose();
    }
}
