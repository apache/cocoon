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
package org.apache.cocoon.portal.pluto.om.common;

import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @version $Id$
 */
public abstract class AbstractSupportSet 
    extends HashSet 
    implements java.io.Serializable, Support {

    public AbstractSupportSet() {
        // nothing to do 
    }

    // support implemenation.
    public void postLoad(Object parameter) throws Exception {
        dispatch(parameter, POST_LOAD);
    }

    public void preBuild(Object parameter) throws Exception {
        dispatch(parameter, PRE_BUILD);
    }

    public void postBuild(Object parameter) throws Exception {
        dispatch(parameter, POST_BUILD);
    }

    public void preStore(Object parameter) throws Exception {
        dispatch(parameter, PRE_STORE);
    }

    public void postStore(Object parameter) throws Exception {
        dispatch(parameter, POST_STORE);
    }

    // additional methods.
    protected void dispatch(Object parameter, int id) throws Exception {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            Support support = (Support)iterator.next();
            switch (id) {
                case POST_LOAD : support.postLoad(parameter);  break;
                case PRE_BUILD : support.preBuild(parameter);  break;
                case POST_BUILD: support.postBuild(parameter); break;
                case PRE_STORE : support.preStore(parameter);  break;
                case POST_STORE: support.postStore(parameter); break;
            }
        }
    }
}
