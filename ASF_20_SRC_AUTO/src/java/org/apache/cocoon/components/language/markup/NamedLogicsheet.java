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
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @version CVS $Id: NamedLogicsheet.java,v 1.4 2004/03/05 13:02:47 bdelacretaz Exp $
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

    public NamedLogicsheet(String systemId, ServiceManager manager, SourceResolver resolver)
        throws IOException, ProcessingException, SourceException, SAXException
    {
        super(systemId, manager, resolver);
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
