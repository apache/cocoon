/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

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
 * @version CVS $Id: BasketContentGenerator.java,v 1.1 2004/02/23 14:52:49 cziegeler Exp $
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
