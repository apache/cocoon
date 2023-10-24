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
package org.apache.cocoon.components.language.markup;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * An extension to <code>Logicsheet</code> that is associated with a namespace.
 * Named logicsheets are implicitly declared (and automagically applied) when
 * the markup language document's root element declares the same logichseet's
 * namespace
 *
 * @version $Id$
 */
public class NamedLogicsheet extends Logicsheet {

    /**
     * The namespace uri
     */
    protected String uri;

    /**
     * The namespace prefix
     */
    private String prefix;

    public NamedLogicsheet(String systemId, ServiceManager manager,
                           SourceResolver resolver, LogicsheetFilter filter)
        throws IOException, ProcessingException, SourceException, SAXException
    {
        super(systemId, manager, resolver, filter);
    }

    /**
     * Set the logichseet's namespace prefix
     *
     * @param prefix The namespace prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Return the logicsheet's namespace prefix
     *
     * @return The logicsheet's namespace prefix
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Set the logichseet's uri
     *
     * @param uri The logicsheet's uri
     */
    public void setURI(String uri) {
        this.uri = uri;
    }

    /**
     * Return the logicsheet's namespace prefix
     *
     * @return The logicsheet's namespace prefix
     */
    public String getURI() {
        return this.uri;
    }
}
