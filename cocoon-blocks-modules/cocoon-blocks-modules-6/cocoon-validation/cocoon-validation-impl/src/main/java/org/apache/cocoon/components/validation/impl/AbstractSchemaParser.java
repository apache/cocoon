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
package org.apache.cocoon.components.validation.impl;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.EntityResolver;

import org.apache.cocoon.components.validation.SchemaParser;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * <p>A {@link SchemaParser} caching {@link org.apache.cocoon.components.validation.Schema}
 * instances for multiple use.</p>
 *
 * <p>A {@link org.apache.cocoon.components.validation.Schema} will be cached until its
 * {@link org.apache.excalibur.source.SourceValidity} expires.</p>
 *
 * @version $Id$
 */
public abstract class AbstractSchemaParser extends AbstractLogEnabled
                implements Serviceable, Initializable, Disposable, SchemaParser {

    /** <p>The {@link ServiceManager} configured for this instance.</p> */
    protected ServiceManager serviceManager;

    /** <p>The {@link SourceResolver} to resolve URIs into {@link org.apache.excalibur.source.Source}s.</p> */
    protected SourceResolver sourceResolver;

    /** <p>The {@link EntityResolver} resolving against catalogs of public IDs.</p> */
    protected EntityResolver entityResolver;


    /**
     * <p>Create a new {@link AbstractSchemaParser} instance.</p>
     */
    public AbstractSchemaParser() {
        super();
    }

    /**
     * <p>Contextualize this component specifying a {@link ServiceManager} instance.</p>
     */
    public void service(ServiceManager manager)
    throws ServiceException {
        this.serviceManager = manager;
    }
    
    /**
     * <p>Initialize this component instance.</p>
     * 
     * <p>A this point component resolution will happen.</p>
     */
    public void initialize()
    throws Exception {
        this.entityResolver = (EntityResolver) this.serviceManager.lookup(EntityResolver.ROLE);
        this.sourceResolver = (SourceResolver) this.serviceManager.lookup(SourceResolver.ROLE);
    }
    
    /**
     * <p>Dispose this component instance.</p>
     */
    public void dispose() {
        if (this.entityResolver != null) this.serviceManager.release(this.entityResolver);
        if (this.sourceResolver != null) this.serviceManager.release(this.sourceResolver);
    }
}
