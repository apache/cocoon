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

import org.apache.excalibur.xml.xslt.XSLTProcessor;
import org.apache.excalibur.xml.xslt.XSLTProcessorException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.TransformerHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * A code-generation logicsheet. This class is actually a wrapper for
 * a "standard" XSLT stylesheet stored as <code>trax.Templates</code>
 * object.  Though this will change shortly: a new markup language
 * will be used for logicsheet authoring; logicsheets written in this
 * language will be transformed into an equivalent XSLT stylesheet
 * anyway... This class should probably be based on an interface...
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @version CVS $Id$
 */
public class Logicsheet extends AbstractLogEnabled
{
    /**
     * The Source Resolver object for this logicsheet.
     */
    private SourceResolver resolver;

    /**
     * The system id to resolve
     */
    private String systemId;

    /**
     * the template namespace's list
     */
    protected Map namespaceURIs = null;

    /**
     * The ServiceManager of this instance.
     */
    private ServiceManager manager;

    /**
     * An optional filter to preprocess the logicsheet source code.
     */
    private LogicsheetFilter filter;

    public Logicsheet(String systemId, ServiceManager manager,
                      SourceResolver resolver, LogicsheetFilter filter)
        throws SAXException, IOException, SourceException, ProcessingException
    {
        this.systemId = systemId;
        this.manager = manager;
        this.resolver = resolver;
        this.filter = filter;
    }

    /**
     * Return true if other logicsheet has the same system id.
     */
    public boolean equals(Object other)
    {
        if (other == this)
            return true;
        if (other == null)
            return false;
        if (!(other instanceof Logicsheet))
            return false;
        Logicsheet that = (Logicsheet)other;
        return this.systemId.equals(that.systemId);
    }

    /**
     * Return hash code value for logicsheet.
     */
    public int hashCode()
    {
        return this.systemId.hashCode();
    }

    /**
     * Return system id which uniquely identifies logicsheet.
     */
    public String getSystemId()
    {
        return this.systemId;
    }

    /**
     * This will return the list of namespaces in this logicsheet,
     * or null, if fillNamespaceURIs has not been called yet.
     */
    public Map getNamespaceURIs()
    {
        return namespaceURIs;
    }

    /**
     * Fill the list of namespaces in this logicsheet.
     */
    public void fillNamespaceURIs() throws ProcessingException
    {
        // Force the parsing of the Source which fills namespaceURIs.
        getTransformerHandler();
    }

    /**
     * Obtain the TransformerHandler object that will perform the
     * transformation associated with this logicsheet.
     *
     * @return a <code>TransformerHandler</code> value
     */
    public TransformerHandler getTransformerHandler() throws ProcessingException
    {
        XSLTProcessor xsltProcessor = null;
        Source source = null;
        try {
            xsltProcessor = (XSLTProcessor)this.manager.lookup(XSLTProcessor.ROLE);
            source = this.resolver.resolveURI( this.systemId );

            // If the Source object is not changed, the
            // getTransformerHandler() of XSLTProcessor will simply return
            // the old template object. If the Source is unchanged, the
            // namespaces are not modified either.
            if (namespaceURIs == null)
            	namespaceURIs = new HashMap();
            filter.setNamespaceMap(namespaceURIs);
            return xsltProcessor.getTransformerHandler(source, filter);

        } catch (ServiceException e) {
            throw new ProcessingException("Could not obtain XSLT processor", e);
        } catch (MalformedURLException e) {
            throw new ProcessingException("Could not resolve " + this.systemId, e);
        } catch (SourceException e) {
            throw SourceUtil.handle("Could not resolve " + this.systemId, e);
        } catch (IOException e) {
            throw new ProcessingException("Could not resolve " + this.systemId, e);
        } catch (XSLTProcessorException e) {
            throw new ProcessingException("Could not transform " + this.systemId, e);
        } finally {
            this.manager.release(xsltProcessor);
            // Release used resources
            this.resolver.release( source );
        }
    }
}
