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
    include  the following  acknowledgment:   "This product includes software
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

package org.apache.cocoon.components.slide.impl;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.slide.Principal;
import org.apache.cocoon.components.slide.PrincipalGroup;
import org.apache.cocoon.components.slide.PrincipalProvider;
import org.apache.cocoon.components.slide.SlideRepository;
import org.apache.slide.authenticate.CredentialsToken;
import org.apache.slide.common.NamespaceAccessToken;
import org.apache.slide.common.NamespaceConfig;
import org.apache.slide.common.ServiceAccessException;
import org.apache.slide.common.SlideException;
import org.apache.slide.common.SlideToken;
import org.apache.slide.common.SlideTokenImpl;
import org.apache.slide.content.Content;
import org.apache.slide.content.NodeProperty;
import org.apache.slide.content.NodeRevisionDescriptor;
import org.apache.slide.content.NodeRevisionDescriptors;
import org.apache.slide.content.RevisionDescriptorNotFoundException;
import org.apache.slide.macro.Macro;
import org.apache.slide.macro.MacroException;
import org.apache.slide.macro.MacroParameters;
import org.apache.slide.security.Security;
import org.apache.slide.structure.GroupNode;
import org.apache.slide.structure.LinkNode;
import org.apache.slide.structure.ObjectAlreadyExistsException;
import org.apache.slide.structure.ObjectNode;
import org.apache.slide.structure.Structure;

/**
 * Manger for principals and groups of principals
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: SlidePrincipalProvider.java,v 1.2 2003/12/08 18:08:10 unico Exp $
 */
public class SlidePrincipalProvider extends AbstractLogEnabled
  implements PrincipalProvider, Serviceable, Configurable, Initializable {

    /** The service manager instance */
    private ServiceManager manager = null;

    /** Namespace access token. */
    private NamespaceAccessToken nat;

    /** Configuration of namespace */
    private NamespaceConfig config;

    /** Structure helper. */
    private Structure structure;

    /** Content helper. */
    private Content content;

    /** Security helper. */
    private Security security;

    /** Lock helper. */
    // private Lock lock;

    /** Macro helper. */
    // private Macro macro;

    /** Slide token. */
    // private SlideToken slidetoken;

    private String namespace = null;

    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     *
     * @param manager
     *
     * @throws ServiceException
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * Pass the Configuration to the Configurable class. This method must
     * always be called after the constructor and before any other method.
     *
     * @param configuration the class configurations.
     *
     * @throws ConfigurationException
     */
    public void configure(Configuration configuration)
      throws ConfigurationException {

        this.namespace = configuration.getAttribute("namespace", null);
    }

    /**
     * Initialize the service. Initialization includes
     * allocating any resources required throughout the
     * service's lifecycle.
     *
     * @throws Exception if an error occurs
     */
    public void initialize() throws Exception {

        SlideRepository repository = null;

        try {
            repository = (SlideRepository) this.manager.lookup(SlideRepository.ROLE);
            this.nat = repository.getNamespaceToken(namespace);

            if (this.nat==null) {
                throw new ProcessingException("Repository with the namespace '"+
                                              this.namespace+
                                              "' couldn't be found");
            }

            this.config = this.nat.getNamespaceConfig();
            this.structure = nat.getStructureHelper();
            this.content = nat.getContentHelper();
            this.security = nat.getSecurityHelper();
            // this.lock = nat.getLockHelper();
            // this.macro = nat.getMacroHelper();

        } catch (ServiceException se) {
            getLogger().error("Could not lookup for service.", se);
        } finally {
            if (repository!=null) {
                this.manager.release(repository);
            }
            repository = null;
        }
    }

    /**
     * Return all users.
     *
     * @param caller The principal, which should do the operation
     * @return List of all principals
     *
     * @throws ProcessingException
     */
    public Principal[] getPrincipals(Principal caller)
      throws ProcessingException {
        try {
            SlideToken slidetoken = new SlideTokenImpl(new CredentialsToken(caller));

            String userspath = config.getUsersPath();

            ObjectNode userobjects = structure.retrieve(slidetoken,
                                                        userspath);

            Vector principals = new Vector();

            String user;
            ObjectNode userobject;

            for (Enumeration children = userobjects.enumerateChildren();
                children.hasMoreElements(); ) {
                user = (String) children.nextElement();

                userobject = structure.retrieve(slidetoken, user);

                if ( !(userobject instanceof GroupNode)) {
                    String name = userobject.getUri().substring(userspath.length()+
                                                                1);

                    // FIXME the CVS code from slide does only implement getRoles
                    Enumeration roles = this.security.getRoles(userobject);
                    String role = null;

                    if (roles.hasMoreElements()) {
                        role = (String) roles.nextElement();
                    }

                    String password = null;

                    try {
                        NodeRevisionDescriptors revisionDescriptors = content.retrieve(slidetoken,
                                                                          user);

                        // Retrieve latest revision descriptor
                        NodeRevisionDescriptor revisionDescriptor = this.content.retrieve(slidetoken,
                                                                        revisionDescriptors);

                        if (revisionDescriptor.getProperty("password", NodeProperty.SLIDE_NAMESPACE)!=
                            null) {
                            password = (String) revisionDescriptor.getProperty("password",
                                NodeProperty.SLIDE_NAMESPACE).getValue();
                        }
                    } catch (RevisionDescriptorNotFoundException rdnfe) {
                        // ignore
                    }

                    principals.add(new Principal(name, role, password));
                }
            }

            Principal[] principalArray = new Principal[principals.size()];
            int i = 0;

            for (Enumeration e = principals.elements(); e.hasMoreElements();
                i++)
                principalArray[i] = (Principal) e.nextElement();

            return principalArray;
        } catch (SlideException se) {
            throw new ProcessingException(se);
        }
    }

    /**
     * Add or modify a given principal.
     * The implementation is similar to org.apache.slide.admin.users.AddUserAction.
     *
     * @param caller The principal, which should do the operation.
     * @param principal The Principal, which should be add/modified.
     *
     * @throws ProcessingException
     */
    public void addPrincipal(Principal caller,
                             Principal principal) throws ProcessingException {
        // do the actual transaction
        try {
            try {
                nat.begin();

                SlideToken slidetoken = new SlideTokenImpl(new CredentialsToken(caller));

                String strUri = config.getUsersPath()+"/"+principal.getName();

                // create the node in the structure
                ObjectNode object;

                object = new slideroles.basic.UserRoleImpl();
                structure.create(slidetoken, object, strUri);

                // create a revision with the appropriate properties set
                NodeRevisionDescriptor revision = new NodeRevisionDescriptor(0);

                revision.setProperty(new NodeProperty("resourcetype",
                                                      "<collection/>", true));
                revision.setCreationDate(new Date());
                revision.setLastModified(new Date());
                revision.setProperty(new NodeProperty("getcontentlength",
                                                      "0", true));
                revision.setProperty(new NodeProperty("source", "", true));
                revision.setProperty(new NodeProperty("password",
                                                      principal.getPassword(),
                                                      NodeProperty.SLIDE_NAMESPACE));
                content.create(slidetoken, strUri, revision, null);

                nat.commit();

            } catch (ObjectAlreadyExistsException e) {
                // duplicate principal
                getLogger().warn("Could not create principal", e);
            } catch (ServiceAccessException e) {
                // low level service access failed
                getLogger().warn("Could not create principal", e);
                throw new ProcessingException("Could not create principal",
                                              e);
            } catch (Exception e) {
                // any other errors are unanticipated
                getLogger().warn("Could not create principal", e);
                throw new ProcessingException("Could not create principal",
                                              e);
            }
        } catch (Exception e) {
            // rollback the transaction
            getLogger().warn("Could not create principal", e);
            try {
                nat.rollback();
            } catch (Exception e2) {
                // ignore
                getLogger().error("Could roll back the operation", e2);
            }
        }
    }

    /**
     * Remove a given principal.
     *
     * @param caller The principal, which should do the operation.
     * @param principal The Principal, which should be removed.
     *
     * @throws ProcessingException
     */
    public void removePrincipal(Principal caller,
                                Principal principal)
                                  throws ProcessingException {

        if (principal.getName().length()>0) {
            // get the helpers
            Macro macro = nat.getMacroHelper();

            SlideToken slidetoken = new SlideTokenImpl(new CredentialsToken(caller));

            // get the path of the current authenticated user
            String selfUri = config.getUsersPath()+"/"+caller.getName();

            String strUri = config.getUsersPath()+"/"+principal.getName();

            // do the actual transaction
            try {
                try {
                    nat.begin();

                    // the user may not delete herself
                    if ( !selfUri.equals(strUri)) {
                        macro.delete(slidetoken, strUri,
                                     new MacroParameters(true, false));
                    }

                    nat.commit();

                } catch (MacroException e) {
                    // some aspect of the delete operation failed
                    getLogger().warn("Could not remove principal", e);
                    throw new ProcessingException("Could not remove principal",
                                                  e);
                }
            } catch (Exception e) {
                // rollback the transaction
                try {
                    nat.rollback();
                } catch (Exception e2) {
                    // ignore
                    getLogger().error("Could roll back the operation", e2);
                }
            }
        }
    }

    /**
     * Return all groups.
     *
     * @param caller The principal, which should do the operation.
     * @return List of all groups.
     *
     * @throws ProcessingException
     */
    public PrincipalGroup[] getPrincipalGroups(Principal caller)
      throws ProcessingException {
        try {
            SlideToken slidetoken = new SlideTokenImpl(new CredentialsToken(caller));

            String userspath = config.getUsersPath();

            ObjectNode userobjects = structure.retrieve(slidetoken,
                                                        userspath);

            Vector principalgroups = new Vector();

            String group;
            ObjectNode groupobject;

            for (Enumeration children = userobjects.enumerateChildren();
                children.hasMoreElements(); ) {
                group = (String) children.nextElement();

                groupobject = structure.retrieve(slidetoken, group);

                if (groupobject instanceof GroupNode) {
                    String name = groupobject.getUri().substring(userspath.length()+1);

                    principalgroups.add(new PrincipalGroup(name));
                }
            }

            PrincipalGroup[] principalgroupArray = new PrincipalGroup[principalgroups.size()];
            int i = 0;

            for (Enumeration e = principalgroups.elements();
                e.hasMoreElements(); i++)
                principalgroupArray[i] = (PrincipalGroup) e.nextElement();

            return principalgroupArray;
        } catch (SlideException se) {
            getLogger().error("Could not retrieve principal groups", se);
            throw new ProcessingException(se);
        }
    }

    /**
     * Add or modify a given group.
     *
     * @param caller The principal, which should do the operation.
     * @param group The group, which shoud be add/modified.
     *
     * @throws ProcessingException
     */
    public void addPrincipalGroup(Principal caller,
                                  PrincipalGroup group)
                                    throws ProcessingException {
        // do the actual transaction
        try {
            try {
                nat.begin();

                SlideToken slidetoken = new SlideTokenImpl(new CredentialsToken(caller));

                String strUri = config.getUsersPath()+"/"+group.getName();

                // create the node in the structure
                ObjectNode object = new GroupNode();

                structure.create(slidetoken, object, strUri);

                // create a revision with the appropriate properties set
                NodeRevisionDescriptor revision = new NodeRevisionDescriptor(0);

                revision.setProperty(new NodeProperty("resourcetype","<collection/>", true));
                revision.setCreationDate(new Date());
                revision.setLastModified(new Date());
                revision.setProperty(new NodeProperty("getcontentlength",
                                                      "0", true));
                revision.setProperty(new NodeProperty("source", "", true));
                content.create(slidetoken, strUri, revision, null);

                nat.commit();

            } catch (ObjectAlreadyExistsException e) {
                // duplicate group
                getLogger().warn("Could not create group", e);
            } catch (ServiceAccessException e) {
                // low level service access failed
                getLogger().warn("Could not create group", e);
                throw new ProcessingException("Could not create group", e);
            } catch (Exception e) {
                // any other errors are unanticipated
                getLogger().warn("Could not create group", e);
                throw new ProcessingException("Could not create group", e);
            }
        } catch (Exception e) {
            // rollback the transaction
            getLogger().warn("Could not create group", e);
            try {
                nat.rollback();
            } catch (Exception e2) {
                // ignore
                getLogger().error("Could roll back the operation", e2);
            }
        }
    }

    /**
     * Remove a given group.
     *
     * @param caller The principal, which should do the operation.
     * @param group The group, which shoud be removed.
     *
     * @throws ProcessingException
     */
    public void removePrincipalGroup(Principal caller,
                                     PrincipalGroup group)
                                       throws ProcessingException {

        if (group.getName().length()>0) {
            // get the helpers
            Macro macro = nat.getMacroHelper();

            SlideToken slidetoken = new SlideTokenImpl(new CredentialsToken(caller));

            // get the path of the current authenticated user
            String selfUri = config.getUsersPath()+"/"+caller.getName();

            String strUri = config.getUsersPath()+"/"+group.getName();

            // do the actual transaction
            try {
                try {
                    nat.begin();

                    // the user may not delete herself
                    if ( !selfUri.equals(strUri)) {
                        macro.delete(slidetoken, strUri,
                                     new MacroParameters(true, false));
                    }

                    nat.commit();

                } catch (MacroException e) {
                    // some aspect of the delete operation failed
                    getLogger().warn("Could not remove group", e);
                    throw new ProcessingException("Could not remove group",
                                                  e);
                }
            } catch (Exception e) {
                // rollback the transaction
                try {
                    nat.rollback();
                } catch (Exception e2) {
                    // ignore
                    getLogger().error("Could roll back the operation", e2);
                }
            }
        }
    }

    /**
     * Adds the specified member to the group.
     *
     * @param caller The principal, which should do the operation
     * @param group The given group.
     * @param principal The principal to add to this group.
     *
     * @throws ProcessingException
     */
    public void addMember(Principal caller, PrincipalGroup group,
                          Principal principal) throws ProcessingException {
        // do the actual transaction
        try {
            try {
                nat.begin();

                SlideToken slidetoken = new SlideTokenImpl(new CredentialsToken(caller));

                String strUri = config.getUsersPath()+"/"+group.getName()+"/"+
                                principal.getName();

                // create the node in the structure
                LinkNode link = new LinkNode();

                structure.createLink(slidetoken, link, strUri,
                                     structure.retrieve(slidetoken,
                                                        config.getUsersPath()+
                                                        "/"+
                                                        principal.getName()));

                // create a revision with the appropriate properties set
                NodeRevisionDescriptor revision = new NodeRevisionDescriptor(0);

                revision.setProperty(new NodeProperty("resourcetype",
                                                      "<collection/>", true));
                revision.setCreationDate(new Date());
                revision.setLastModified(new Date());
                revision.setProperty(new NodeProperty("getcontentlength",
                                                      "0", true));
                revision.setProperty(new NodeProperty("source", "", true));
                content.create(slidetoken, strUri, revision, null);

                nat.commit();

            } catch (ObjectAlreadyExistsException e) {
                // duplicate member
                getLogger().warn("Could not create member", e);
            } catch (ServiceAccessException e) {
                // low level service access failed
                getLogger().warn("Could not create member", e);
                throw new ProcessingException("Could not create member", e);
            } catch (Exception e) {
                // any other errors are unanticipated
                getLogger().warn("Could not create member", e);
                throw new ProcessingException("Could not create member", e);
            }
        } catch (Exception e) {
            // rollback the transaction
            getLogger().warn("Could not create member", e);
            try {
                nat.rollback();
            } catch (Exception e2) {
                // ignore
                getLogger().error("Could roll back the operation", e2);
            }
        }

    }

    /**
     * Returns true if the passed principal is a member of the group.
     *
     * @param caller The principal, which should do the operation
     * @param group The given group.
     * @param member The principal whose membership is to be checked.
     * @return True if the principal is a member of this group, false otherwise.
     *
     * @throws ProcessingException
     */
    public boolean isMember(Principal caller, PrincipalGroup group,
                            Principal member) throws ProcessingException {
        Principal[] members = members(caller, group);

        for (int i = 0; i<members.length; i++)
            if (members[i].equals(member)) {
                return true;
            }
        return false;
    }

    /**
     * Returns an enumeration of the members in the group. The returned objects are instances of Principal
     *
     * @param caller The principal, which should do the operation
     * @param group The given group.
     * @return An enumeration of the group members.
     *
     * @throws ProcessingException
     */
    public Principal[] members(Principal caller,
                               PrincipalGroup group)
                                 throws ProcessingException {
        try {
            SlideToken slidetoken = new SlideTokenImpl(new CredentialsToken(caller));

            String userspath = config.getUsersPath();

            ObjectNode groupobject = structure.retrieve(slidetoken,
                                                        userspath+"/"+
                                                        group.getName());

            if (groupobject instanceof GroupNode) {
                Vector principals = new Vector();

                String user;
                ObjectNode userobject;

                for (Enumeration children = groupobject.enumerateChildren();
                    children.hasMoreElements(); ) {
                    user = (String) children.nextElement();

                    userobject = structure.retrieve(slidetoken, user);

                    if ( !(userobject instanceof GroupNode)) {
                        String name = userobject.getUri().substring(userspath.length()+
                                                                    1);

                        // FIXME the CVS code from slide does only implement getRoles
                        /*
                        Enumeration roles = this.security.getRoles(userobject);
                        String role = null;

                        if (roles.hasMoreElements()) {
                            role = (String) roles.nextElement();
                        }
                        */

                        NodeRevisionDescriptors revisionDescriptors = content.retrieve(slidetoken,
                                                                          userobject.getUri());

                        // Retrieve latest revision descriptor
                        NodeRevisionDescriptor revisionDescriptor = this.content.retrieve(slidetoken,
                                                                        revisionDescriptors);
                        String password = null;

                        if ((revisionDescriptor.getProperty("password", NodeProperty.SLIDE_NAMESPACE)!=null) &&
                            (revisionDescriptor.getProperty("password", NodeProperty.SLIDE_NAMESPACE).getValue()!=
                             null)) {
                            password = revisionDescriptor.getProperty("password",
                                                                      NodeProperty.SLIDE_NAMESPACE).getValue().toString();
                        }

                        principals.add(new Principal(name /* , role */,
                                                     password));
                    }
                }

                Principal[] principalArray = new Principal[principals.size()];
                int i = 0;

                for (Enumeration e = principals.elements();
                    e.hasMoreElements(); i++)
                    principalArray[i] = (Principal) e.nextElement();

                return principalArray;
            } else {
                return new Principal[0];
            }

        } catch (SlideException se) {
            throw new ProcessingException(se);
        }
    }

    /**
     * Removes the specified member from the group.
     *
     * @param caller The principal, which should do the operation
     * @param group The given group.
     * @param principal The principal to remove from this group.
     *
     * @throws ProcessingException
     */
    public void removeMember(Principal caller, PrincipalGroup group,
                             Principal principal) throws ProcessingException {

        if ((group.getName().length()>0) &&
            (principal.getName().length()>0)) {
            // get the helpers
            Macro macro = nat.getMacroHelper();

            SlideToken slidetoken = new SlideTokenImpl(new CredentialsToken(caller));

            String strUri = config.getUsersPath()+"/"+group.getName()+"/"+
                            principal.getName();

            // do the actual transaction
            try {
                try {
                    nat.begin();

                    macro.delete(slidetoken, strUri,
                                 new MacroParameters(true, false));

                    nat.commit();

                } catch (MacroException e) {
                    // some aspect of the delete operation failed
                    getLogger().warn("Could not remove member", e);
                    throw new ProcessingException("Could not remove member",
                                                  e);
                }
            } catch (Exception e) {
                // rollback the transaction
                try {
                    nat.rollback();
                } catch (Exception e2) {
                    // ignore
                    getLogger().error("Could roll back the operation", e2);
                }
            }
        }
    }
}

