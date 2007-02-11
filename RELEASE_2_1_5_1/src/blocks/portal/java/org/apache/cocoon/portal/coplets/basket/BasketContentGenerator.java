/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.coplets.basket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.xml.SaxBuffer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This is a generator that display the content of one item
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: BasketContentGenerator.java,v 1.2 2004/03/05 13:02:11 bdelacretaz Exp $
 */
public class BasketContentGenerator
extends ServiceableGenerator {
    
    /** This is the attribute name containing the content */
    protected String attributeName;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver,
                      Map objectModel,
                      String src,
                      Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        this.attributeName = par.getParameter("attribute-name", null);
    }

    /**
     * Get the coplet associated with this pipeline
     */
    protected CopletInstanceData getCopletInstanceData() 
    throws SAXException {
        PortalService portalService = null;
        try {

            portalService = (PortalService)this.manager.lookup(PortalService.ROLE);

            final Map context = (Map)objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
            
            String copletId = (String)context.get(Constants.COPLET_ID_KEY);

            CopletInstanceData object = portalService.getComponentManager().getProfileManager().getCopletInstanceData( copletId );
                
            if (object == null) {
                throw new SAXException("Could not find coplet instance data for " + copletId);
            }
                
            return object;
        } catch (ServiceException e) {
            throw new SAXException("Error getting portal service.", e);
        } finally {
            this.manager.release( portalService );
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.generation.Generator#generate()
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
        boolean streamed = false;
        SAXParser parser = null;
        try {
            parser = (SAXParser) this.manager.lookup(SAXParser.ROLE);
            if ( this.attributeName != null ) {
                CopletInstanceData cid = this.getCopletInstanceData();
                byte[] content = (byte[])cid.getAttribute(this.attributeName);
                if ( content == null ) {
                    this.xmlConsumer.startDocument();
                    XMLUtils.createElement(this.xmlConsumer, "p");
                    this.xmlConsumer.endDocument();
                    return;
                }
                try {
                    InputSource is = new InputSource(new ByteArrayInputStream(content));
                    SaxBuffer buffer = new SaxBuffer();
                    parser.parse(is, buffer);
                    streamed = true;
                    buffer.toSAX(this.xmlConsumer);
                } catch (Exception ignore) {
                    // ignore
                }
            }
            if ( !streamed ) {
                Source source = null;
                try {
                    source = this.resolver.resolveURI(this.source);
                    parser.parse(SourceUtil.getInputSource(source), this.xmlConsumer);
                } finally {
                    this.resolver.release(source);
                }
            }
        } catch (ServiceException se) {
            throw new ProcessingException("Unable to lookup parser.", se);
        } finally {
            this.manager.release(parser);
        }
    }
    
}
