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
package org.apache.cocoon.webapps.authentication.generation;


import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.webapps.authentication.AuthenticationManager;
import org.apache.cocoon.webapps.authentication.context.AuthenticationContext;
import org.apache.cocoon.webapps.authentication.user.RequestState;
import org.apache.cocoon.webapps.authentication.user.UserHandler;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMUtil;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

/**
 *  This is the authentication Configuration Generator.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: ConfigurationGenerator.java,v 1.8 2004/03/17 12:09:52 cziegeler Exp $
*/
public final class ConfigurationGenerator
extends ServiceableGenerator {

    /** Request parameter */
    public static final String REQ_PARAMETER_STATE = "authstate";
    public static final String REQ_PARAMETER_ROLE  = "authrole";
    public static final String REQ_PARAMETER_ID    = "authid";
    public static final String REQ_PARAMETER_USER  = "authuser";

    private static final String SESSION_CONTEXT_ATTRIBUTE_ADMIN_ROLE = "org.apache.cocoon.webapps.generation.ConfigurationGenerator.simple-role";

    /** The XPath Processor */
    protected XPathProcessor xpathProcessor;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( this.xpathProcessor );
            this.xpathProcessor = null;
        }
        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.xpathProcessor = (XPathProcessor)this.manager.lookup(XPathProcessor.ROLE);
    }

    /**
     * Generate the configuration
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {

        AuthenticationManager authManager = null;
        RequestState state = null;
        try {
            authManager = (AuthenticationManager)this.manager.lookup(AuthenticationManager.ROLE);
            state = authManager.getState();
            
        } catch (Exception ignore) {
        }
        
        this.xmlConsumer.startDocument();
        if ( state != null ) {
            try {
                UserHandler userhandler = state.getHandler();
                
                Configuration conf = state.getModuleConfiguration("single-role-user-management");
                if (conf == null) {
                    throw new ProcessingException("Module configuration 'single-role-user-management' for authentication user management generator not found.");
                }
                UserManagementHandler handler = new UserManagementHandler(conf,
                                                                          state.getApplicationName());
                this.showConfiguration(this.xmlConsumer, this.source, handler, userhandler.getContext());
            
            } catch (ConfigurationException ex) {
                throw new ProcessingException("ConfigurationException: " + ex, ex);
            }
        }

        this.xmlConsumer.endDocument();
    }


    /**
     * Show the configuration for the admin.
     * If <code>src</code> is "admin" or null the admin configuration is shown.
     * If <code>src</code> is "user" the configuration of the current user
     * is shown.
     */
    public void showConfiguration(XMLConsumer consumer,
                                   String      src,
                                   UserManagementHandler handler,
                                   AuthenticationContext context)
    throws ProcessingException, SAXException, IOException {
        // synchronized
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN showConfiguration consumer=" + consumer + ", src="+src);
        }

        // get some important information
        Request request = ObjectModelHelper.getRequest(this.objectModel);
        Response response = ObjectModelHelper.getResponse(this.objectModel);
        Session session = request.getSession();
        
        boolean isAdmin = (src == null || src.equals("admin"));

        // now start producing xml:
        AttributesImpl attr = new AttributesImpl();
        consumer.startElement("", "configuration", "configuration", attr);

        // set the conf uri:
        // This is a bug in the servlet 2.2 API!!!
        // It does not contain the context:  String uri = HttpUtils.getRequestURL(this.request).toString();
        // So: ABSOLUTELY USELESS
        String uri = response.encodeURL(request.getRequestURI());
        consumer.startElement("", "uri", "uri", attr);
        consumer.characters(uri.toCharArray(), 0, uri.length());
        consumer.endElement("", "uri", "uri");

        if (isAdmin == true) {
            // build the menue
            consumer.startElement("", "menue", "menue", attr);

            if (handler.getNewRoleResource() != null) {
                consumer.startElement("", "addrole", "addrole", attr);
                consumer.endElement("", "addrole", "addrole");
            }
            if (handler.getDeleteRoleResource() != null) {
                consumer.startElement("", "delrole", "delrole", attr);
                consumer.endElement("", "delrole", "delrole");
            }

            consumer.endElement("", "menue", "menue");
        }


        synchronized (session) { 

            String state = request.getParameter(REQ_PARAMETER_STATE);
            if (state == null) {
                state = (isAdmin == true ? "main" : "seluser");
            }

            if (state.equals("addrole") == true) {
                String role = request.getParameter(REQ_PARAMETER_ROLE);
                if (role != null && role.trim().length() > 0) {
                    SourceParameters pars = new SourceParameters();
                    // first include all request parameters
                    Enumeration requestParameters = request.getParameterNames();
                    String current;
                    while (requestParameters.hasMoreElements() == true) {
                        current = (String)requestParameters.nextElement();
                        pars.setParameter(current, request.getParameter(current));
                    }
                    this.addRole(role, pars, handler);
                } else {
                    role = null;
                }
                session.removeAttribute(SESSION_CONTEXT_ATTRIBUTE_ADMIN_ROLE);
            }

            if (state.equals("delrole") == true) {
                try {
                    String role = request.getParameter(REQ_PARAMETER_ROLE);
                    if (role != null) {
                        // first delete user
                        Document userDF = this.getUsers(role, null, handler);
                        NodeList users   = null;
                        if (userDF != null) users = DOMUtil.selectNodeList(userDF, "users/user", this.xpathProcessor);
                        if (users != null) {
                            for(int i = 0; i < users.getLength(); i++) {
                                this.deleteUser(role, DOMUtil.getValueOf(users.item(i), "ID", this.xpathProcessor), null, handler);
                            }
                        }
                        this.deleteRole(role, null, handler);
                    }
                    session.removeAttribute(SESSION_CONTEXT_ATTRIBUTE_ADMIN_ROLE);
                } catch (javax.xml.transform.TransformerException local) {
                    throw new ProcessingException("TransformerException: " + local, local);
                }
            }


            if (state.equals("chguser") == true) {
                String role;
                String id;
                String user;

                if (isAdmin == false) {
                    Map pars = context.getContextInfo();
                    id = (String) pars.get("ID");
                    role = (String) pars.get("role");
                    user = "old";
                } else {
                    role = request.getParameter(REQ_PARAMETER_ROLE);
                    id   = request.getParameter(REQ_PARAMETER_ID);
                    user = request.getParameter(REQ_PARAMETER_USER);
                }

                boolean addingNewUserFailed = false;
                if (role != null && id != null && user != null) {
                    if (user.equals("new") == true) {
                        SourceParameters pars = new SourceParameters();
                        // first include all request parameters
                        Enumeration requestParameters = request.getParameterNames();
                        String current;
                        while (requestParameters.hasMoreElements() == true) {
                            current = (String)requestParameters.nextElement();
                            pars.setParameter(current, request.getParameter(current));
                        }
                        addingNewUserFailed = !this.addUser(role, id, pars, handler);
                        if (addingNewUserFailed == false) {
                            consumer.startElement("", "addeduser", "addeduser", attr);
                            consumer.characters(id.toCharArray(), 0, id.length());
                            consumer.endElement("", "addeduser", "addeduser");
                        }
                    } else {
                        String delete = request.getParameter("authdeluser");
                        if (delete != null && delete.equals("true") == true) {
                            this.deleteUser(role, id, null, handler);
                        } else {
                            SourceParameters pars = new SourceParameters();
                            // first include all request parameters
                            Enumeration requestParameters = request.getParameterNames();
                            String current;
                            while (requestParameters.hasMoreElements() == true) {
                                current = (String)requestParameters.nextElement();
                                pars.setParameter(current, request.getParameter(current));
                            }
                            this.changeUser(role, id, pars, handler);
                        }
                    }
                    session.removeAttribute(SESSION_CONTEXT_ATTRIBUTE_ADMIN_ROLE);
                }
                if (addingNewUserFailed == false) {
                    state = (isAdmin == true ? "adduser" : "seluser");
                } else {
                    state = "erruser";
                }
            }

            if (state.equals("seluser") == true) {
                String role;
                String id;

                if (isAdmin == false) {
                    Map pars = context.getContextInfo();
                    id = (String) pars.get("ID");
                    role = (String) pars.get("role");
                } else {
                    role = request.getParameter(REQ_PARAMETER_ROLE);
                    id   = request.getParameter(REQ_PARAMETER_ID);
                }
                if (role != null && id != null) {
                    session.setAttribute(SESSION_CONTEXT_ATTRIBUTE_ADMIN_ROLE, role);

                    // include users
                    Document userDF = this.getUsers(role, id, handler);
                    Element  users   = null;
                    try {
                        if (userDF != null) users = (Element)DOMUtil.getSingleNode(userDF, "users/user", this.xpathProcessor);
                    } catch (javax.xml.transform.TransformerException local) {
                        throw new ProcessingException("TransformerException: " + local, local);
                    }
                    consumer.startElement("", "uservalues", "uservalues", attr);
                    if (users != null && users.hasChildNodes() == true) {
                        NodeList childs = users.getChildNodes();
                        for(int i = 0; i < childs.getLength(); i++) {
                            if (childs.item(i).getNodeType() == Node.ELEMENT_NODE)
                                IncludeXMLConsumer.includeNode(childs.item(i), consumer, consumer);
                        }
                    }
                    consumer.endElement("", "uservalues", "uservalues");
                }
                consumer.startElement("", "user", "user", attr);
                consumer.characters("old".toCharArray(), 0, 3);
                consumer.endElement("", "user", "user");
                if (isAdmin == false) {
                    consumer.startElement("", "role", "role", attr);
                    consumer.characters(role.toCharArray(), 0, role.length());
                    consumer.endElement("", "role", "role");
               }
            }

            if (state.equals("erruser") == true) {
                String role;
                String id;

                if (isAdmin == false) {
                    Map pars = context.getContextInfo();
                    id = (String) pars.get("ID");
                    role = (String) pars.get("role");
                } else {
                    role = request.getParameter(REQ_PARAMETER_ROLE);
                    id   = request.getParameter(REQ_PARAMETER_ID);
                }
                if (role != null && id != null) {
                    session.setAttribute(SESSION_CONTEXT_ATTRIBUTE_ADMIN_ROLE, role);

                    // include users
                    Document userDF = this.getUsers(role, id, handler);
                    Element  users   = null;
                    try {
                        if (userDF != null) users = (Element)DOMUtil.getSingleNode(userDF, "users/user", this.xpathProcessor);
                    } catch (javax.xml.transform.TransformerException local) {
                        throw new ProcessingException("TransformerException: " + local, local);
                    }
                    consumer.startElement("", "uservalues", "uservalues", attr);
                    if (users != null && users.hasChildNodes() == true) {
                        NodeList childs = users.getChildNodes();
                        for(int i = 0; i < childs.getLength(); i++) {
                            if (childs.item(i).getNodeType() == Node.ELEMENT_NODE)
                                IncludeXMLConsumer.includeNode(childs.item(i), consumer, consumer);
                        }
                    }
                    consumer.endElement("", "uservalues", "uservalues");
                }
                consumer.startElement("", "user", "user", attr);
                consumer.characters("error".toCharArray(), 0, 5);
                consumer.endElement("", "user", "user");
                if (isAdmin == false) {
                    consumer.startElement("", "role", "role", attr);
                    consumer.characters(role.toCharArray(), 0, role.length());
                    consumer.endElement("", "role", "role");
               }
            }

            if (state.equals("adduser") == true) {
                consumer.startElement("", "user", "user", attr);
                consumer.characters("new".toCharArray(), 0, 3);
                consumer.endElement("", "user", "user");
            }

            if (state.equals("selrole") == true) {
                String role = request.getParameter(REQ_PARAMETER_ROLE);
                session.setAttribute(SESSION_CONTEXT_ATTRIBUTE_ADMIN_ROLE, role);
                // include users
                Document userDF = this.getUsers(role, null, handler);
                Node     users   = null;
                try {
                    if (userDF != null) users = DOMUtil.getSingleNode(userDF, "users", this.xpathProcessor);
                } catch (javax.xml.transform.TransformerException local) {
                    throw new ProcessingException("TransformerException: " + local, local);
                }
                IncludeXMLConsumer.includeNode(users, consumer, consumer);
            }

            if (isAdmin == true) {
                // include roles
                Document rolesDF = this.getRoles(handler);
                Node     roles   = null;
                try {
                    if (rolesDF != null) roles = DOMUtil.getSingleNode(rolesDF, "roles", this.xpathProcessor);
                } catch (javax.xml.transform.TransformerException local) {
                    throw new ProcessingException("TransformerException: " + local, local);
                }
                IncludeXMLConsumer.includeNode(roles, consumer, consumer);

                // include selected role
                String role = (String)session.getAttribute(SESSION_CONTEXT_ATTRIBUTE_ADMIN_ROLE);
                if (role != null) {
                    consumer.startElement("", "role", "role", attr);
                    consumer.characters(role.toCharArray(), 0, role.length());
                    consumer.endElement("", "role", "role");
                }
            }
        } // end synchronized(context)

        consumer.endElement("", "configuration", "configuration");
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END showConfiguration");
        }
    }

    /**
     * Get all users in a document fragment with the following children:
     * <users>
     *     <user>
     *         <ID>...</ID>
     *         <role>...</role> <!-- optional -->
     *         <data>
     *         ...
     *         </data>
     *     </user>
     *     ....
     * </users>
     * The document fragment might contain further nodes at the root!
     * If <code>role</code> is <code>null</code> all users are fetched,
     * otherwise only the users for this role.
     * If also ID is not null only the single user is fetched.
     */
    public Document getUsers(String role, String ID, UserManagementHandler handler)
    throws IOException, ProcessingException, SAXException {
        // calling method is syned
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN getUsers role="+role+", ID="+ID);
        }
        Document frag = null;

        if (handler.getLoadUsersResource() != null) {
            final String loadUsersResource = handler.getLoadUsersResource();
            final SourceParameters loadParameters = handler.getLoadUsersResourceParameters();
            SourceParameters parameters = (loadParameters == null) ? new SourceParameters()
                                                                     : (SourceParameters)loadParameters;
            if (handler.getApplicationName() != null) {
                parameters.setSingleParameterValue("application", handler.getApplicationName());
            }
            if (ID != null) {
                parameters.setSingleParameterValue("type", "user");
                parameters.setSingleParameterValue("ID", ID);
            } else {
                parameters.setSingleParameterValue("type", "users");
            }
            if (role != null) parameters.setSingleParameterValue("role", role);
            frag = this.loadResource(loadUsersResource, parameters);

        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END getUsers fragment="+(frag == null ? "null" : XMLUtils.serializeNode(frag, XMLUtils.createPropertiesForXML(false))));
        }
        return frag;
    }

    /**
     * Get all roles in a document fragment with the following children:
     * <roles>
     *     <role>...</role>
     *     ....
     * </roles>
     * The document fragment might contain further nodes at the root!
     */
    public Document getRoles(UserManagementHandler handler)
    throws IOException, ProcessingException, SAXException {
        // calling method is syned
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN getRoles");
        }
        Document frag = null;

        if (handler.getLoadRolesResource() != null) {
            final String loadRolesResource = handler.getLoadRolesResource();
            final SourceParameters loadParameters = handler.getLoadRolesResourceParameters();
            SourceParameters parameters = (loadParameters == null) ? new SourceParameters()
                                                                     : (SourceParameters)loadParameters.clone();
            if (handler.getApplicationName() != null)
                parameters.setSingleParameterValue("application", handler.getApplicationName());
            parameters.setSingleParameterValue("type", "roles");
            frag = this.loadResource(loadRolesResource, parameters);
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END getRoles fragment="+frag);
        }
        return frag;
    }

    /**
     * Add a role
     */
    private void addRole(String name, SourceParameters parameters, UserManagementHandler handler)
    throws IOException, ProcessingException, SAXException {
        // calling method is syned
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN addRole role="+name+", parameters="+parameters);
        }
        if (handler.getNewRoleResource() != null) {
            final String newRoleResource = handler.getNewRoleResource();
            final SourceParameters handlerPars = handler.getNewRoleResourceParameters();
            if (parameters == null) parameters = new SourceParameters();
            parameters.add(handlerPars);

            if (handler.getApplicationName() != null)
                parameters.setSingleParameterValue("application", handler.getApplicationName());
            parameters.setSingleParameterValue("type", "role");
            parameters.setSingleParameterValue("role", name);

            this.invokeResource(newRoleResource, parameters);
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END addRole");
        }
    }

    /**
     * Add a user.
     * @return If a user with ID already exists <code>false</code> is returned.
     */
    public boolean addUser(String role, String ID, SourceParameters parameters, UserManagementHandler handler)
    throws IOException, ProcessingException, SAXException {
        // calling method is syned
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN addUser role="+role+", ID="+ID+", parameters="+parameters);
        }
        boolean result = false;

        if (handler.getNewUserResource() != null
            && ID != null
            && ID.trim().length() > 0) {
            // first test if a user with this ID already exists
            Document user = this.getUsers(null, null, handler);
            Node node = null;
            if (user != null) {
                try {
                    node = DOMUtil.getSingleNode(user, "users/user/ID[text()='"+ID+"']", this.xpathProcessor);
                } catch (javax.xml.transform.TransformerException local) {
                    throw new ProcessingException("Transformer exception: " + local, local);
                }
            }
            if (user == null || node == null) {
                final String newUserResource = handler.getNewUserResource();
                final SourceParameters newUsersPars = handler.getNewUserResourceParameters();
                if (parameters == null) parameters = new SourceParameters();
                parameters.add(newUsersPars);

                if (handler.getApplicationName() != null)
                    parameters.setSingleParameterValue("application", handler.getApplicationName());
                parameters.setSingleParameterValue("type", "user");
                parameters.setSingleParameterValue("role", role);
                parameters.setSingleParameterValue("ID", ID);

                this.invokeResource(newUserResource, parameters);
                result = true;
            }
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END addUser success="+result);
        }
        return result;
    }

    /**
     * Delete a role
     */
    private void deleteRole(String name, SourceParameters parameters, UserManagementHandler handler)
    throws IOException, ProcessingException, SAXException {
        // calling method is syned
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN deleteRole role="+name+", parameters="+parameters);
        }
        if (handler.getDeleteRoleResource() != null) {
            final String deleteRoleResource = handler.getDeleteRoleResource();
            final SourceParameters handlerPars = handler.getDeleteRoleResourceParameters();
            if (parameters == null) parameters = new SourceParameters();
            parameters.add(handlerPars);

            if (handler.getApplicationName() != null)
                parameters.setSingleParameterValue("application", handler.getApplicationName());
            parameters.setSingleParameterValue("type", "role");
            parameters.setSingleParameterValue("role", name);

            this.invokeResource(deleteRoleResource, parameters);
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END deleteRole");
        }
    }

    /**
     * Delete a user
     */
    private void deleteUser(String role, String name, SourceParameters parameters, UserManagementHandler handler)
    throws IOException, ProcessingException, SAXException {
        // calling method is syned
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN deleteUser role="+role+", ID="+name+", parameters="+parameters);
        }
        if (handler.getDeleteUserResource() != null) {
            final String deleteUserResource = handler.getDeleteUserResource();
            final SourceParameters handlerPars = handler.getDeleteUserResourceParameters();
            if (parameters == null) parameters = new SourceParameters();
            parameters.add(handlerPars);

            if (handler.getApplicationName() != null)
                parameters.setSingleParameterValue("application", handler.getApplicationName());
            parameters.setSingleParameterValue("type", "user");
            parameters.setSingleParameterValue("role", role);
            parameters.setSingleParameterValue("ID", name);

            this.invokeResource(deleteUserResource, parameters);
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END deleteUser");
        }
    }

    /**
     * Change a user
     */
    private void changeUser(String role, String name, SourceParameters parameters, UserManagementHandler handler)
    throws IOException, ProcessingException, SAXException {
        // calling method is syned
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN changeUser role="+role+", ID="+name+", parameters="+parameters);
        }
        if (handler.getChangeUserResource() != null) {
            final String changeUserResource = handler.getChangeUserResource();
            final SourceParameters handlerPars = handler.getChangeUserResourceParameters();
            if (parameters == null) parameters = new SourceParameters();
            parameters.add(handlerPars);

            if (handler.getApplicationName() != null)
                parameters.setSingleParameterValue("application", handler.getApplicationName());
            parameters.setSingleParameterValue("type", "user");
            parameters.setSingleParameterValue("role", role);
            parameters.setSingleParameterValue("ID", name);

            this.invokeResource(changeUserResource, parameters);
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END changeUser");
        }
    }

    /**
     * Invoke resource
     */
    private void invokeResource(String resource,
                                 SourceParameters parameters)
    throws IOException, ProcessingException, SAXException {
        Source source = null;
        try {
            source = SourceUtil.getSource(resource, 
                                          null, 
                                          parameters, 
                                          this.resolver);
            SourceUtil.parse(this.manager, source, new DefaultHandler());                                          
        } catch (SourceException se) {
            throw SourceUtil.handle(se);
        } finally {
            this.resolver.release(source);
        }
    }
    
    /**
     * Load XML resource
     */
    private Document loadResource(String resource,
                                   SourceParameters parameters)
    throws IOException, ProcessingException, SAXException {
        Source source = null;
        try {
            source = SourceUtil.getSource(resource, 
                                          null, 
                                          parameters, 
                                          this.resolver);
            return SourceUtil.toDOM(source);
        } catch (SourceException se) {
            throw SourceUtil.handle(se);
        } finally {
            this.resolver.release(source);
        }
    }
}

final class UserManagementHandler {

    /** The name of the current application */
    private String applicationName;
    
    /** The load-users resource */
    private String loadUsersResource;
    private SourceParameters loadUsersResourceParameters;
    
    /** The load-roles resource */
    private String loadRolesResource;
    private SourceParameters loadRolesResourceParameters;

    /** The new-user resource */
    private String newUserResource;
    private SourceParameters newUserResourceParameters;

    /** The new-role resource */
    private String newRoleResource;
    private SourceParameters newRoleResourceParameters;

    /** The delete-role resource */
    private String deleteRoleResource;
    private SourceParameters deleteRoleResourceParameters;

    /** The delete-user resource */
    private String deleteUserResource;
    private SourceParameters deleteUserResourceParameters;

    /** The change-user resource */
    private String changeUserResource;
    private SourceParameters changeUserResourceParameters;

    /**
     * Create a new handler object.
     */
    public UserManagementHandler(Configuration   conf,
                                  String          appName)
    throws ProcessingException, SAXException, IOException, ConfigurationException {
        Configuration child;
        
        this.applicationName = appName;
        
        // get load-users resource (optional)
        child = conf.getChild("load-users", false);
        if (child != null) {
            this.loadUsersResource = child.getAttribute("uri");
            this.loadUsersResourceParameters = SourceParameters.create(child);
        }

        // get load-roles resource (optional)
        child = conf.getChild("load-roles", false);
        if (child != null) {
            this.loadRolesResource = child.getAttribute("uri");
            this.loadRolesResourceParameters = SourceParameters.create(child);
        }

        // get new user resource (optional)
        child = conf.getChild("new-user", false);
        if (child != null) {
            this.newUserResource = child.getAttribute("uri");
            this.newUserResourceParameters = SourceParameters.create(child);
        }

        // get new role resource (optional)
        child = conf.getChild("new-role", false);
        if (child != null) {
            this.newRoleResource = child.getAttribute("uri");
            this.newRoleResourceParameters = SourceParameters.create(child);
        }

        // get delete user resource (optional)
        child = conf.getChild("delete-user", false);
        if (child != null) {
            this.deleteUserResource = child.getAttribute("uri");
            this.deleteUserResourceParameters = SourceParameters.create(child);
        }

        // get delete role resource (optional)
        child = conf.getChild("delete-role", false);
        if (child != null) {
            this.deleteRoleResource = child.getAttribute("uri");
            this.deleteRoleResourceParameters = SourceParameters.create(child);
        }

        // get change user resource (optional)
        child = conf.getChild("change-user", false);
        if (child != null) {
            this.changeUserResource = child.getAttribute("uri");
            this.changeUserResourceParameters = SourceParameters.create(child);
        }
    }

    /**
     * Get the name of the current application
     */
    public String getApplicationName() { return this.applicationName; }
    
    /**
     * Get the load users resource
     */
    public String getLoadUsersResource() { return this.loadUsersResource; }
    public SourceParameters getLoadUsersResourceParameters() { return this.loadUsersResourceParameters; }

    /**
     * Get the load roles resource
     */
    public String getLoadRolesResource() { return this.loadRolesResource; }
    public SourceParameters getLoadRolesResourceParameters() { return this.loadRolesResourceParameters; }

    /**
     * Get the new user resource
     */
    public String getNewUserResource() { return this.newUserResource; }
    public SourceParameters getNewUserResourceParameters() { return this.newUserResourceParameters; }

    /**
     * Get the new role resource
     */
    public String getNewRoleResource() { return this.newRoleResource; }
    public SourceParameters getNewRoleResourceParameters() { return this.newRoleResourceParameters; }

    /** Get the delete user resource */
    public String getDeleteUserResource() { return this.deleteUserResource; }
    public SourceParameters getDeleteUserResourceParameters() { return this.deleteUserResourceParameters; }

    /** Get the delete role resource */
    public String getDeleteRoleResource() { return this.deleteRoleResource; }
    public SourceParameters getDeleteRoleResourceParameters() { return this.deleteRoleResourceParameters; }

    /** Get the change user resource */
    public String getChangeUserResource() { return this.changeUserResource; }
    public SourceParameters getChangeUserResourceParameters() { return this.changeUserResourceParameters; }
}
