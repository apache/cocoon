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
package org.apache.cocoon.webapps.authentication.components;

import java.io.IOException;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.webapps.authentication.configuration.HandlerConfiguration;
import org.apache.cocoon.webapps.authentication.user.UserHandler;
import org.apache.cocoon.webapps.session.MediaManager;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMUtil;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Verify if a user can be authenticated.
 * This is a helper class that could be made pluggable if required.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: PipelineAuthenticator.java,v 1.10 2004/01/27 08:26:25 cziegeler Exp $
*/
public class PipelineAuthenticator 
    extends AbstractLogEnabled
    implements Serviceable, ThreadSafe, Disposable, Authenticator {
    
    /** The service manager */
    protected ServiceManager manager;
    
    /** The source resolver */
    protected SourceResolver resolver;
    
    /**
     * Check the fragment if it is valid
     */
    private boolean isValidAuthenticationFragment(Document authenticationFragment) 
    throws ProcessingException {
        // calling method is synced
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN isValidAuthenticationFragment fragment=" + XMLUtils.serializeNodeToXML(authenticationFragment));
        }
        boolean isValid = false;

        // authenticationFragment must only have exactly one child with
        // the name authentication
        if (authenticationFragment.hasChildNodes() == true
            && authenticationFragment.getChildNodes().getLength() == 1) {
            Node child = authenticationFragment.getFirstChild();

            if (child.getNodeType() == Node.ELEMENT_NODE
                && child.getNodeName().equals("authentication") == true) {

                // now authentication must have one child ID
                if (child.hasChildNodes() == true) {
                    NodeList children = child.getChildNodes();
                    boolean  found = false;
                    int      i = 0;
                    int      l = children.getLength();

                    while (found == false && i < l) {
                        child = children.item(i);
                        if (child.getNodeType() == Node.ELEMENT_NODE
                            && child.getNodeName().equals("ID") == true) {
                            found = true;
                        } else {
                            i++;
                        }
                    }

                    // now the last check: ID must have a TEXT child
                    if (found == true) {
                        child.normalize(); // join text nodes
                        if (child.hasChildNodes() == true &&
                            child.getChildNodes().getLength() == 1 &&
                            child.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE) {
                            String value = child.getChildNodes().item(0).getNodeValue().trim();
                            if (value.length() > 0) isValid = true;
                        }
                    }
                }

            }
        }
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END isValidAuthenticationFragment valid="+isValid);
        }
        return isValid;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.authentication.components.Authenticator#authenticate(org.apache.cocoon.webapps.authentication.configuration.HandlerConfiguration, org.apache.excalibur.source.SourceParameters)
     */
    public AuthenticationResult authenticate( HandlerConfiguration configuration,
                                              SourceParameters      parameters)
    throws ProcessingException {
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("start authenticator using handler " + configuration.getName());
        }

        final String   authenticationResourceName = configuration.getAuthenticationResource();
        final SourceParameters authenticationParameters = configuration.getAuthenticationResourceParameters();
        if (parameters != null) {
            parameters.add(authenticationParameters);
        } else {
            parameters = authenticationParameters;
        }

        Document doc = null;
        String exceptionMsg = null;
        
        // invoke the source
        try {
            Source source = null;
            try {
                source = SourceUtil.getSource(authenticationResourceName, 
                                                                                  null, 
                                                                                  parameters, 
                                                                                  this.resolver);
                
                doc = SourceUtil.toDOM(source);
            } catch (SAXException se) {
                throw new ProcessingException(se);
            } catch (SourceException se) {
                throw SourceUtil.handle(se);
            } catch (IOException e) {
                throw new ProcessingException(e);
			} finally {
                this.resolver.release(source);
            }

        } catch (ProcessingException local) {
            this.getLogger().error("authenticator: " + local.getMessage(), local);
            exceptionMsg = local.getMessage();
        }

        // test if authentication was successful
        boolean isValid = false;
        AuthenticationResult result = null;
        if (doc != null) {
            isValid = this.isValidAuthenticationFragment( doc );

            if ( isValid ) {
                if (this.getLogger().isInfoEnabled() ) {
                    this.getLogger().info("Authenticator: User authenticated using handler '" + configuration.getName()+"'");
                }
                
                MediaManager mediaManager = null;
                String mediaType;
                try {
                    mediaManager = (MediaManager)this.manager.lookup( MediaManager.ROLE );
                    mediaType = mediaManager.getMediaType();
                } catch (ServiceException se) {
                    throw new ProcessingException("Unable to lookup media manager.", se);
                } finally {
                    this.manager.release( mediaManager );
                }
                synchronized (configuration) {
                    // add special nodes to the authentication block:
                    // useragent, type and media
                    Element specialElement;
                    Text    specialValue;
                    Element authNode;

                    authNode = (Element)doc.getFirstChild();

                    specialElement = doc.createElementNS(null, "type");
                    specialValue = doc.createTextNode("cocoon.authentication");
                    specialElement.appendChild(specialValue);
                    authNode.appendChild(specialElement);

                    specialElement = doc.createElementNS(null, "media");
                    specialValue = doc.createTextNode(mediaType);
                    specialElement.appendChild(specialValue);
                    authNode.appendChild(specialElement);

                    result = new AuthenticationResult(true, doc);

                } // end sync
            }
        }
        
        if ( !isValid ) {
            if (this.getLogger().isInfoEnabled() ) {
                this.getLogger().info("Authenticator: Failed authentication using handler '" +  configuration.getName()+"'");
            }
            // get the /authentication/data Node if available
            Node data = null;

            if (doc != null) {
                data = DOMUtil.getFirstNodeFromPath(doc, new String[] {"authentication","data"}, false);
            }
            doc = DOMUtil.createDocument();

            // now create the following xml:
            // <root>
            //   <failed/>
            //   if data is available data is included, otherwise:
            //   <data>No information</data>
            //   If exception message contains info, it is included into failed
            // </root>
            final Element root = doc.createElementNS(null, "root");
            doc.appendChild(root);
            Element element = doc.createElementNS(null, "failed");
            root.appendChild(element);

            if (exceptionMsg != null) {
                Text text = doc.createTextNode(exceptionMsg);
                element.appendChild(text);
            }

            if (data == null) {
                element = doc.createElementNS(null, "data");
                root.appendChild(element);
                Text text = doc.createTextNode("No information available");
                element.appendChild(text);
            } else {
                root.appendChild(doc.importNode(data, true));
            }
            
            result = new AuthenticationResult(false, doc);
        }
            
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("end authenticator");
        }

        return result;
    }
    
    
	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
	 */
	public void service(ServiceManager manager) throws ServiceException {
		this.manager = manager;
        this.resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
	}

	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.activity.Disposable#dispose()
	 */
	public void dispose() {
		if ( this.manager != null ){
            this.manager.release( this.resolver );
            this.manager = null;
            this.resolver = null;
		}
	}

    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.authentication.components.Authenticator#logout(org.apache.cocoon.webapps.authentication.user.UserHandler)
     */
    public void logout(UserHandler handler) {
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("logout using handler " + handler.getHandlerName());
        }
        
        final HandlerConfiguration configuration = handler.getHandlerConfiguration();
        final String logoutResourceName = configuration.getLogoutResource();
        if (logoutResourceName != null) {
            final SourceParameters parameters = configuration.getAuthenticationResourceParameters();
        
            // invoke the source
            Source source = null;
            try {
                // This allows arbitrary business logic to be called. Whatever is returned
                // is ignored.
                source = SourceUtil.getSource(logoutResourceName, null, parameters, this.resolver);
                Document doc = SourceUtil.toDOM(source);
            } catch (Exception ignore) {
                this.getLogger().error("logout: " + ignore.getMessage(), ignore);
            } finally {
                this.resolver.release(source);
            }
        }
    }

}
