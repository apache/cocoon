/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.auth.impl;

import org.apache.cocoon.auth.SecurityHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;

/**
 * This is a base class that can be used for own {@link SecurityHandler}s.
 *
 * @version $Id$
 */
public abstract class AbstractSecurityHandler
    implements SecurityHandler, BeanNameAware {

    /** Support for anonymous user? */
    protected boolean supportAnonUser = false;

    /** Name of the anonymous user. */
    protected String anonName = "anonymous";

    /** Password of the anonymous user. */
    protected String anonPass = "anonymous";

    /** The id for the security handler. */
    protected String id = String.valueOf(this.hashCode());

    /** By default we use the logger for this class. */
    private Log logger = LogFactory.getLog(getClass());

    public Log getLogger() {
        return this.logger;
    }

    public void setLogger(Log l) {
        this.logger = l;
    }

    public void setAnonymousName(String anonName) {
        this.anonName = anonName;
    }

    public void setAnonymousPassword(String anonPass) {
        this.anonPass = anonPass;
    }

    public void setSupportAnonymousUser(boolean supportAnonUser) {
        this.supportAnonUser = supportAnonUser;
    }

    /**
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name) {
        this.id = name;
    }

    /**
     * @see org.apache.cocoon.auth.SecurityHandler#getId()
     */
    public String getId() {
        return this.id;
    }

}
