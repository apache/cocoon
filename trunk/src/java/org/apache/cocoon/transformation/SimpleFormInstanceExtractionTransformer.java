/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.modules.output.OutputModule;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.dom.DocumentWrapper;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This transformer sieves an incoming stream of xml and extracts a
 * document fragment from it depending on a given tag and stores the
 * fragment using an OutputModule with a name based an attribute of
 * another enclosing tag. Default configuration fires on
 * &lt;form-instance/&gt; and uses @name of enclosing &lt;form/&gt;
 * tag. Default OutputModule is request-attr. This is usefull in
 * conjunction with the SimpleFormTransformer when setting the
 * InputModule for it to a chain of request-param and request-attr so
 * that the extracted form instance data is used only when no similar
 * request parameter exists.
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: SimpleFormInstanceExtractionTransformer.java,v 1.4 2003/12/06 21:22:07 cziegeler Exp $
 */
public class SimpleFormInstanceExtractionTransformer extends AbstractExtractionTransformer
    implements Configurable, Serviceable, Recyclable {

    protected class ElementData {
        public String uri = null;
        public String loc = null;
        public String raw = null;

        public ElementData() {
        }

        public ElementData(String uri, String loc, String raw) {
            this.uri = uri;
            this.loc = loc;
            this.raw =raw;
        }

        public boolean equals(String uri, String loc, String raw) {

            if (!this.uri.equals(uri))
                return false;
            if (!this.loc.equals(loc))
                return false;
            if (!this.raw.equals(raw))
                return false;
            return true;
        }

    }

    protected final static String OUTPUT_MODULE_SELECTOR = OutputModule.ROLE+"Selector";

    ElementData startElement = null;
    ElementData nameElement = null;
    String qname = "name";

    String instanceName = null;
    boolean nameAsRoot = true;

    String outputModuleName = "request-attr";
    Configuration outputConf = null;

    ServiceManager manager = null;
    Map objectModel = null;

    public void configure(Configuration config) throws ConfigurationException {
        this.startElement = new ElementData();
        this.startElement.uri = config.getChild("start").getAttribute("uri", "");
        this.startElement.loc = config.getChild("start").getAttribute("local-name", "form-instance");
        this.startElement.raw = config.getChild("start").getAttribute("raw-name", "form-instance");

        this.nameElement = new ElementData();
        this.nameElement.uri = config.getChild("name").getAttribute("uri", "");
        this.nameElement.loc = config.getChild("name").getAttribute("local-name", "form");
        this.nameElement.raw = config.getChild("name").getAttribute("raw-name", "form");
        this.qname = config.getChild("name").getAttribute("name-attribute", "name");

        this.nameAsRoot = config.getChild("name-as-root").getValueAsBoolean(this.nameAsRoot);

        this.outputConf = config.getChild("output");
        this.outputModuleName = this.outputConf.getAttribute("name",this.outputModuleName);
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /** Setup the transformer. */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters)
            throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, parameters);
        this.objectModel = objectModel;
    }

    public void recycle() {
        super.recycle();
        this.instanceName = null;
    }

    /**
     * Receive notification of the beginning of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     * @param a The attributes attached to the element. If there are no
     *          attributes, it shall be an empty Attributes object.
     * @return a <code>boolean</code> value to signal to start extracting
     */
    public boolean startExtracting(String uri, String loc, String raw, Attributes a) {
        if (this.nameElement.equals(uri,loc,raw)) {
            this.instanceName = a.getValue(this.qname);
        }
        boolean res = this.startElement.equals(uri,loc,raw);
        return res;
    }

    /**
     * Receive notification of the beginning of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     * @return a <code>boolean</code> value to signal to stop extracting
     */
    public boolean endExtracting(String uri, String loc, String raw) {
        boolean res = this.startElement.equals(uri,loc,raw);
        return res;
    }


    /**
     * Start root element and replace it with the instance name.
     * @see org.apache.cocoon.transformation.AbstractExtractionTransformer#startExtractingDocument(String, String, String, Attributes)
     */
    public void startExtractingDocument(String uri, String loc, String raw, Attributes a) throws SAXException {
        if (this.nameAsRoot) {
            loc = this.instanceName;
            if (uri != null && !uri.equals("")) {
                int pos = raw.indexOf(':');
                raw = raw.substring(0, pos+1) + this.instanceName;
            } else {
                raw = loc;
            }
        }
        this.currentBuilder.startElement(uri,loc,raw,a);
    }

    /**
     * End root element and replace it with the instance name.
     * @see org.apache.cocoon.transformation.AbstractExtractionTransformer#endExtractingDocument(String, String, String)
     */
    public void endExtractingDocument(String uri, String loc, String raw) throws SAXException{
        if(this.nameAsRoot){
            loc = this.instanceName;
            if (uri != null && !uri.equals("")) {
                int pos = raw.indexOf(':');
                raw = raw.substring(0, pos+1) + this.instanceName;
            } else {
                raw = loc;
            }
        }
        this.currentBuilder.endElement(uri, loc, raw);
    }


    /**
     * Receive notification of the end of the extracted Document.
     *
     * @param doc a <code>Document</code> value
     */
    public void handleExtractedDocument(Document doc) {

        ServiceSelector outputSelector = null;
        OutputModule output = null;

        try {
            if (getLogger().isDebugEnabled())
                getLogger().debug("wrote ['"+this.instanceName+"'] to "+output+" using "+outputConf);
            outputSelector = (ServiceSelector) this.manager.lookup(OUTPUT_MODULE_SELECTOR);
            if (outputSelector.isSelectable(this.outputModuleName)) {
                output = (OutputModule) outputSelector.select(this.outputModuleName);
            }
            output.setAttribute(outputConf, this.objectModel, this.instanceName, new DocumentWrapper(doc));
            output.commit(outputConf, this.objectModel);
            if (getLogger().isDebugEnabled())
                getLogger().debug("wrote ['"+this.instanceName+"'] to "+output+" using "+outputConf);

        } catch (Exception e) {
            if (getLogger().isWarnEnabled())
                getLogger().warn("Problem writing document data: "+e.getMessage());
        } finally {
            if (outputSelector != null) {
                if (output != null) {
                    outputSelector.release(output);
                    output = null;
                }
                this.manager.release(outputSelector);
            }
        }
        this.instanceName = null;
    }

}
