/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.portal.transformation;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.apache.commons.jxpath.JXPathContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Includes coplet instance data by using JXPath expressions.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Björn Lütkemeier</a>
 * @version CVS $Id: CopletTransformer.java,v 1.1 2003/05/08 11:54:00 cziegeler Exp $
 */
public class CopletTransformer 
extends AbstractSAXTransformer {

    /**
     * The namespace URI to listen for.
     */
    public static final String NAMESPACE_URI = "http://apache.org/cocoon/portal/coplet/1.0";
    
    /**
     * The XML element name to listen for.
     */
    public static final String COPLET_ELEM = "coplet";

    /**
     * The attribute containing the JXPath expression.
     */
    public static final String SELECT_ATTR = "select";

    /**
     * Parameter name.
     */
    public static final String COPLET_ID_PARAM = "copletId";
    
    /**
     * Parameter name.
     */
    public static final String PORTAL_NAME_PARAM = "portalName";
    
    /**
     * Creates new CopletTransformer.
     */
    public CopletTransformer() {
        this.defaultNamespaceURI = NAMESPACE_URI;
    }
    
    /**
     * Overridden from superclass.
     */
    public void startTransformingElement(String uri, String name, String raw, Attributes attr) 
    throws ProcessingException, IOException, SAXException {
        if (name.equals(COPLET_ELEM)) {
            String expression = attr.getValue(SELECT_ATTR);
            if (expression == null) {
                throw new ProcessingException("Attribute "+SELECT_ATTR+" must be spcified.");
            }
                
            ProfileManager profileManager = null;
            try {
                profileManager = (ProfileManager)this.manager.lookup(ProfileManager.ROLE);

                // determine coplet id
                String copletId = null;            
                Map context = (Map)objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
                if (context != null) {
                    copletId = (String)context.get(Constants.COPLET_ID_KEY);
                    if (copletId == null) {
                        throw new ProcessingException("copletId must be passed as parameter or in the object model within the parent context.");
                    }
                } else {
					try {
						copletId = this.parameters.getParameter(COPLET_ID_PARAM);
						
						// set portal name
						PortalService portalService = null;
						try {
						    portalService = (PortalService)this.manager.lookup(PortalService.ROLE);
						    portalService.setPortalName(this.parameters.getParameter(PORTAL_NAME_PARAM));
						} finally {
						    this.manager.release(portalService);
						}
					} catch (ParameterException e) {
                        throw new ProcessingException("copletId and portalName must be passed as parameter or in the object model within the parent context.");
					}
                }

                JXPathContext jxpathContext = JXPathContext.newContext(profileManager.getCopletInstanceData(copletId));
                Object object = jxpathContext.getValue(expression);
                
                if (object == null) {
                    throw new ProcessingException("Could not find value for expression "+expression);
                }
                
                String value = object.toString();
                super.characters(value.toCharArray(), 0, value.length());
            } catch (ComponentException e) {
                throw new ProcessingException("Error getting profile manager.", e);
            } finally {
                this.manager.release(profileManager);
            }
            
        } else {
            super.startTransformingElement(uri, name, raw, attr);
        }
    }

    /**
     * Overridden from superclass.
     */
    public void endTransformingElement(String uri, String name, String raw) 
    throws ProcessingException, IOException, SAXException {
        if (!name.equals(COPLET_ELEM)) {
            super.endTransformingElement(uri, name, raw);
        }
    }

}
