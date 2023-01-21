/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.auth.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.auth.AuthenticationException;
import org.apache.cocoon.auth.User;
import org.apache.cocoon.components.source.util.SourceUtil;
import org.apache.cocoon.util.NetUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Verify if a user can be authenticated.
 *
 * @version $Id$
*/
public class PipelineSecurityHandler
    extends AbstractSecurityHandler {

    /** The source resolver. */
    protected SourceResolver resolver;

    /** The authentication resource. */
    protected String authenticationResource;

    /** The logout resource. */
    protected String logoutResource;

    public void setAuthenticationResource(String authenticationResource) {
        this.authenticationResource = authenticationResource;
    }

    public void setLogoutResource(String logoutResource) {
        this.logoutResource = logoutResource;
    }

    public void setSourceResolver(SourceResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Check if this is a valid document.
     * A valid document has "authentication" as the root node and
     * at least one child element "ID".
     * @param doc The document read by the pipeline.
     * @return The value of the ID element or null if the document is not valid
     */
    protected String isValidAuthenticationDocument(final Document doc) {
        String validId = null;

        final Element child = doc.getDocumentElement();

        if ( child.getNodeName().equals("authentication") ) {

            // now authentication must have one child ID
            if (child.hasChildNodes()) {
                final NodeList children = child.getChildNodes();
                int     i = 0;
                Node    current = null;

                while (current == null && i < children.getLength()) {
                    final Node node = children.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE
                        && node.getNodeName().equals("ID")) {
                        current = node;
                    } else {
                        i++;
                    }
                }

                // now the last check: ID must have a TEXT child
                if ( current != null ) {
                    current.normalize(); // join text nodes
                    if (current.hasChildNodes() &&
                        current.getChildNodes().getLength() == 1 &&
                        current.getFirstChild().getNodeType() == Node.TEXT_NODE) {

                        final String value = current.getFirstChild().getNodeValue().trim();
                        if (value.length() > 0) {
                            validId = value;
                        }
                    }
                }
            }

        }
        return validId;
    }

    /**
     * @see org.apache.cocoon.auth.SecurityHandler#login(java.util.Map)
     */
    public User login(final Map loginContext)
    throws AuthenticationException {
        String authenticationResourceName = this.authenticationResource;

        // append parameters
        final StringBuffer b = new StringBuffer(authenticationResourceName);
        boolean hasParams = (authenticationResourceName.indexOf('?') != -1);
        final Iterator i = loginContext.entrySet().iterator();
        while ( i.hasNext() ) {
            final Map.Entry current = (Map.Entry)i.next();
            final String key = current.getKey().toString();
            final String value = current.getValue().toString();
            if ( hasParams ) {
                b.append('&');
            } else {
                b.append('?');
                hasParams = true;
            }
            try {
                b.append(key).append('=').append(NetUtils.encode(value, "utf-8"));
            } catch (UnsupportedEncodingException ignore) {
                // this can never happen
            }
        }
        authenticationResourceName = b.toString();

        User user = null;
        Document doc = null;

        // invoke the source
        Source source = null;
        try {
            source = SourceUtil.getSource(authenticationResourceName, null,
                                          null, this.resolver);
            doc = org.apache.cocoon.components.source.SourceUtil.toDOM(source);
        } catch (IOException e) {
            throw new AuthenticationException(e);
        } catch (ProcessingException e) {
            throw new AuthenticationException(e);
        } catch (SAXException e) {
            throw new AuthenticationException(e);
        } finally {
            this.resolver.release(source);
        }

        // test if authentication was successful
        String validId = null;
        if (doc != null) {
            validId = this.isValidAuthenticationDocument( doc );

            if ( validId != null ) {
                user = new PipelineSHUser( doc, validId );
            }
        }
        // TODO - What do we do, if authentication fails?

        return user;
    }

    /**
     * @see org.apache.cocoon.auth.SecurityHandler#logout(java.util.Map, org.apache.cocoon.auth.User)
     */
    public void logout(final Map logoutContext, final User user) {
        final String logoutResourceName = this.logoutResource;
        if (logoutResourceName != null) {
            // invoke the source
            Source source = null;
            try {
                // This allows arbitrary business logic to be called. Whatever is returned
                // is ignored.
                source = SourceUtil.getSource(logoutResourceName, null, null, this.resolver);
                org.apache.cocoon.components.source.SourceUtil.toDOM(source);
            } catch (Exception ignore) {
                this.getLogger().warn("Exception during logout of user: " + user.getId(),
                        ignore);
            } finally {
                this.resolver.release(source);
            }
        }
    }

    /**
     * The internal user class.
     */
    public static class PipelineSHUser extends StandardUser {

        /** The document delivered by the pipeline. */
        protected final Document userInfo;

        /**
         * Create a new user object.
         * @param info The pipeline document.
         * @param id   The unique id of the user.
         */
        public PipelineSHUser(final Document info, final String id) {
            super(id);
            this.userInfo = info;
            this.calculateContextInfo();
        }

        /**
         * Return the pipeline document.
         * @return The document.
         */
        public Document getUserInfo() {
            return this.userInfo;
        }

        /**
         * Internal method that calculates the context information. All
         * key-value pairs contained in the document are added as
         * attributes to the user object.
         */
        protected void calculateContextInfo() {
            final Properties parameters = new Properties();

            // add all elements from inside the handler data
            this.addParametersFromAuthenticationXML("data",
                                                    parameters);

            // add all top level elements from authentication
            this.addParametersFromAuthenticationXML(null,
                                                    parameters);

            final Iterator i = parameters.entrySet().iterator();
            while ( i.hasNext() ) {
                final Map.Entry current = (Map.Entry)i.next();
                final String key = current.getKey().toString();
                final String value = current.getValue().toString();
                this.setAttribute(key, value);
            }
        }

        /**
         * Convert the authentication XML of a handler to parameters.
         * The XML is flat and consists of elements which all have exactly one text node:
         * &lt;parone&gt;value_one&lt;parone&gt;
         * &lt;partwo&gt;value_two&lt;partwo&gt;
         * A parameter can occur more than once with different values.
         * @param childElementName The name of the element to search in.
         * @param parameters The found key-value pair is added to this parameters object.
         */
        private void addParametersFromAuthenticationXML(final String     childElementName,
                                                        final Properties parameters) {
            Element root = this.userInfo.getDocumentElement();
            if ( childElementName != null ) {
                NodeList l = root.getElementsByTagName(childElementName);
                if ( l.getLength() > 0 ) {
                    root = (Element)l.item(0);
                } else {
                    root = null;
                }
            }
            if (root != null) {
                NodeList   childs = root.getChildNodes();
                if (childs != null) {
                    Node current;
                    for(int i = 0; i < childs.getLength(); i++) {
                        current = childs.item(i);

                        // only element nodes
                        if (current.getNodeType() == Node.ELEMENT_NODE) {
                            current.normalize();
                            NodeList valueChilds = current.getChildNodes();
                            String   key;
                            StringBuffer   valueBuffer;
                            String         value;

                            key = current.getNodeName();
                            valueBuffer = new StringBuffer();
                            for(int m = 0; m < valueChilds.getLength(); m++) {
                                current = valueChilds.item(m); // attention: current is reused here!
                                if (current.getNodeType() == Node.TEXT_NODE) { // only text nodes
                                    if (valueBuffer.length() > 0) {
                                        valueBuffer.append(' ');
                                    }
                                    valueBuffer.append(current.getNodeValue());
                                }
                            }
                            value = valueBuffer.toString().trim();
                            if (key != null && value != null && value.length() > 0) {
                                parameters.setProperty(key, value);
                            }
                        }
                    }
                }
            }
        }
    }
}
