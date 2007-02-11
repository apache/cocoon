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
 * @version CVS $Id: PipelineAuthenticator.java,v 1.14 2004/03/17 12:09:52 cziegeler Exp $
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
            this.getLogger().debug("BEGIN isValidAuthenticationFragment fragment="
                                   + XMLUtils.serializeNode(authenticationFragment, XMLUtils.createPropertiesForXML(false)));
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
            this.getLogger().debug("END isValidAuthenticationFragment valid=" + isValid);
        }
        return isValid;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.authentication.components.Authenticator#authenticate(org.apache.cocoon.webapps.authentication.configuration.HandlerConfiguration, org.apache.excalibur.source.SourceParameters)
     */
    public AuthenticationResult authenticate(HandlerConfiguration configuration,
                                             SourceParameters parameters)
    throws ProcessingException {
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("start authenticator using handler " + configuration.getName());
        }

        final String authenticationResourceName = configuration.getAuthenticationResource();
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
                source = SourceUtil.getSource(authenticationResourceName, null, 
                                              parameters, this.resolver);
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
                    this.getLogger().info("Authenticator: User authenticated using handler '"
                                          + configuration.getName() + "'");
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
                this.getLogger().info("Authenticator: Failed authentication using handler '"
                                      +  configuration.getName()+ "'");
            }
            // get the /authentication/data Node if available
            Node data = null;

            if (doc != null) {
                data = DOMUtil.getFirstNodeFromPath(doc,
                                                    new String[] {"authentication","data"},
                                                    false);
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
                SourceUtil.toDOM(source);
            } catch (Exception ignore) {
                this.getLogger().error("logout: " + ignore.getMessage(), ignore);
            } finally {
                this.resolver.release(source);
            }
        }
    }

}
