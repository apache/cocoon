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

package org.apache.cocoon.samples.slide;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.apache.slide.authenticate.CredentialsToken;
import org.apache.slide.common.NamespaceAccessToken;
import org.apache.slide.common.SlideToken;
import org.apache.slide.common.SlideTokenImpl;
import org.apache.slide.content.Content;
import org.apache.slide.content.NodeProperty;
import org.apache.slide.content.NodeRevisionDescriptor;
import org.apache.slide.content.NodeRevisionDescriptors;
import org.apache.slide.macro.Macro;
import org.apache.slide.macro.MacroParameters;
import org.apache.slide.security.NodePermission;
import org.apache.slide.security.Security;
import org.apache.slide.structure.ObjectNode;
import org.apache.slide.structure.ObjectNotFoundException;
import org.apache.slide.structure.Structure;
import org.apache.slide.structure.SubjectNode;

/**
 * Helper class for the slide samples administration application.
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a> 
 */
public class AdminHelper {
    
    public static void addUser(NamespaceAccessToken nat, 
                               String caller, 
                               String username, 
                               String password, 
                               String rolename) throws Exception {
        
        String usersPath = nat.getNamespaceConfig().getUsersPath();
        String userUri = usersPath + "/" + username;
        String rolesPath = nat.getNamespaceConfig().getRolesPath();
        String roleUri = rolesPath + "/" + rolename;
        
        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Structure structure = nat.getStructureHelper();
        Content content = nat.getContentHelper();
        
        try {
            
            // make sure the role exists
            ObjectNode role = structure.retrieve(slideToken,roleUri);
            nat.begin();
            
            ObjectNode user = new SubjectNode();
            structure.create(slideToken,user,userUri);
            
            // create the user descriptor
            NodeRevisionDescriptor descriptor = new NodeRevisionDescriptor();
            descriptor.setCreationDate(new Date());
            descriptor.setLastModified(new Date());
            descriptor.setProperty(new NodeProperty(
                "password",password,NodeProperty.SLIDE_NAMESPACE));
            content.create(slideToken,userUri,descriptor,null);
            
            if (rolename != null && !rolename.equals("")) {
                // modify the role descriptor
                NodeRevisionDescriptors descriptors = content.retrieve(slideToken,roleUri);
                descriptor = content.retrieve(slideToken,descriptors);
                NodeProperty property = descriptor.getProperty("group-member-set","DAV:");
                String value;
                if (property != null) {
                    value = (String) property.getValue();
                }
                else {
                    value = "";
                }
                value += "<D:href xmlns:D=\"DAV:\">" + userUri + "</D:href>";
                descriptor.setProperty("group-member-set","DAV:",value);
                content.store(slideToken,roleUri,descriptor,null);
            }
            
            nat.commit();
        }
        catch (Exception e) {
            try {
                nat.rollback();
            }
            catch (Exception f) {
                e.printStackTrace();
            }
            throw e;
        }
        
    }
    
    public static void removeUser(NamespaceAccessToken nat,
                                  String caller,
                                  String username) throws Exception {
        
        String usersPath = nat.getNamespaceConfig().getUsersPath();
        String userUri = usersPath + "/" + username;
        String callerUri = usersPath + "/" + caller;
        
        // user cannot delete itself
        if (callerUri.equals(userUri)) {
            return;
        }
        
        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Macro macro = nat.getMacroHelper();
        
        try {
            nat.begin();
            
            boolean recursive = true;
            boolean overwrite = false;
            MacroParameters parameters = new MacroParameters(recursive,overwrite);
            
            macro.delete(slideToken,userUri,parameters);
            
            nat.commit();
        }
        catch (Exception e) {
            try {
                nat.rollback();
            }
            catch (Exception f) {
                f.printStackTrace();
            }
            throw e;
        }
    }
    
    public static void addGroup(NamespaceAccessToken nat,
                                String caller,
                                String groupname) throws Exception {
        
        String groupsPath = nat.getNamespaceConfig().getGroupsPath();
        String groupUri = groupsPath + "/" + groupname;
        
        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Structure structure = nat.getStructureHelper();
        Content content = nat.getContentHelper();
        
        try {
            nat.begin();
            
            ObjectNode group = new SubjectNode();
            structure.create(slideToken,group,groupUri);
            
            NodeRevisionDescriptor descriptor = new NodeRevisionDescriptor();
            descriptor.setCreationDate(new Date());
            descriptor.setLastModified(new Date());
            
            content.create(slideToken,groupUri,descriptor,null);
            
            nat.commit();
        }
        catch (Exception e) {
            try {
                nat.rollback();
            }
            catch (Exception f) {
                f.printStackTrace();
            }
            throw e;
        }
        
    }
    
    public static void removeGroup(NamespaceAccessToken nat,
                                   String caller,
                                   String groupname) throws Exception {
        
        String groupsPath = nat.getNamespaceConfig().getGroupsPath();
        String groupUri = groupsPath + "/" + groupname;
        
        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Macro macro = nat.getMacroHelper();
        
        try {
            nat.begin();
            
            boolean recursive = true;
            boolean overwrite = false;
            MacroParameters parameters = new MacroParameters(recursive,overwrite);
            
            macro.delete(slideToken,groupUri,parameters);
            
            nat.commit();
        }
        catch (Exception e) {
            try {
                nat.rollback();
            }
            catch (Exception f) {
                f.printStackTrace();
            }
            throw e;
        }
    }
    
    public static void addGroupMember(NamespaceAccessToken nat,
                                      String caller,
                                      String groupname,
                                      String username) throws Exception {
        
        String groupsPath = nat.getNamespaceConfig().getGroupsPath();
        String groupUri = groupsPath + "/" + groupname;
        String usersPath = nat.getNamespaceConfig().getUsersPath();
        String userUri = usersPath + "/" + username;
        
        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Structure structure = nat.getStructureHelper();
        Content content = nat.getContentHelper();
        
        try {
            
            // check if the user exists
            structure.retrieve(slideToken,userUri);
            
            NodeRevisionDescriptors descriptors = content.retrieve(slideToken,groupUri);
            NodeRevisionDescriptor descriptor = content.retrieve(slideToken,descriptors);
            NodeProperty property = descriptor.getProperty("group-member-set","DAV:");
            
            String value = null;
            if (property != null) {
                value = (String) property.getValue();
                if (value.indexOf(userUri) != -1) {
                    // user already a member of this group
                    return;
                }
            }
            else {
                value = "";
            }
            value = value + "<D:href xmlns:D='DAV:'>" + userUri + "</D:href>";
            
            descriptor.setProperty("group-member-set","DAV:",value);
            nat.begin();
            content.store(slideToken,groupUri,descriptor,null);
            nat.commit();
        }
        catch (ObjectNotFoundException e) {
            // no such user or group
        }
        catch (Exception e) {
            try {
                nat.rollback();
            }
            catch (Exception f) {
                f.printStackTrace();
            }
            throw e;
        }
    }
    
    public static void removeGroupMember(NamespaceAccessToken nat,
                                         String caller,
                                         String groupname,
                                         String username) throws Exception {
        
        String groupsPath = nat.getNamespaceConfig().getGroupsPath();
        String groupUri = groupsPath + "/" + groupname;
        String usersPath = nat.getNamespaceConfig().getUsersPath();
        String userUri = usersPath + "/" + username;
        
        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Structure structure = nat.getStructureHelper();
        Content content = nat.getContentHelper();
        
        try {
            
            NodeRevisionDescriptors descriptors = content.retrieve(slideToken,groupUri);
            NodeRevisionDescriptor descriptor = content.retrieve(slideToken,descriptors);
            NodeProperty property = descriptor.getProperty("group-member-set","DAV:");
            
            if (property == null) {
                // group has no members
                return;
            }
            String value = (String) property.getValue();
            
            int index = value.indexOf(userUri);
            if (index == -1) {
                // user is not a member of this group
                return;
            }
            
            // looking for the end of </D:href> after userUri
            int end = index + userUri.length();
            do {
                end++;
            } 
            while (value.charAt(end) != '>');
            
            // looking for the start of <D:href> before userUri
            int from = index;
            do {
                from--;
            }
            while(value.charAt(from) != '<');
            
            // snip out the user
            String before = value.substring(0,from);
            String after  = value.substring(end+1);
            value = before + after;
            
            descriptor.setProperty("group-member-set","DAV:",value);
            nat.begin();
            content.store(slideToken,groupUri,descriptor,null);
            nat.commit();
        }
        catch (ObjectNotFoundException e) {
            // no such user or group
        }
        catch (Exception e) {
            try {
                nat.rollback();
            }
            catch (Exception f) {
                f.printStackTrace();
            }
            throw e;
        }
    }
    
    public static List listPermissions(NamespaceAccessToken nat,
                                       String caller,
                                       String path) throws Exception {
        String filesPath = nat.getNamespaceConfig().getFilesPath();
        String uri = filesPath + "/" + path;
        
        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Security security = nat.getSecurityHelper();
        
        List result = new ArrayList();
        Enumeration permissions = security.enumeratePermissions(slideToken,uri,false);
        while (permissions.hasMoreElements()) {
            result.add(permissions.nextElement());
        }
        return result;
    }
    
    public static List listUsers(NamespaceAccessToken nat,
                                      String caller) throws Exception {
        return listObjects(nat,caller,nat.getNamespaceConfig().getUsersPath());
    }
    
    public static List listRoles(NamespaceAccessToken nat,
                                 String caller) throws Exception {
        return listObjects(nat,caller,nat.getNamespaceConfig().getRolesPath());
    }
    
    public static List listGroups(NamespaceAccessToken nat,
                                  String caller) throws Exception {
        return listObjects(nat,caller,nat.getNamespaceConfig().getGroupsPath());
    }
    
    public static List listActions(NamespaceAccessToken nat,
                                   String caller) throws Exception {
        return listObjects(nat,caller,nat.getNamespaceConfig().getActionsPath());
    }
    
    private static List listObjects(NamespaceAccessToken nat,
                                    String caller,
                                    String path) throws Exception {
        
        List result = new ArrayList();
        
        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Structure structure = nat.getStructureHelper();
        
        ObjectNode object = structure.retrieve(slideToken,path);
        Enumeration enum = structure.getChildren(slideToken,object);
        while (enum.hasMoreElements()) {
            result.add(enum.nextElement());
        }
        
        return result;
    }
    
    public static void removePermission(NamespaceAccessToken nat,
                                        String caller,
                                        String path,
                                        String subject,
                                        String action) throws Exception {

        String filesPath = nat.getNamespaceConfig().getFilesPath();
        String uri;
        if (path.equals("/")) {
            uri = filesPath;
        }
        else {
            uri = filesPath + "/" + path;
        }
        
        
        System.out.println("uri: " + uri);
        System.out.println("subject: " + subject);
        System.out.println("action: " + action);
        System.out.println("caller: " + caller);
        
        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Security security = nat.getSecurityHelper();
        
        try {
            NodePermission permission = new NodePermission(uri,subject,action);
            nat.begin();
            security.revokePermission(slideToken,permission);
            nat.commit();
        }
        catch (Exception e) {
            try {
                nat.rollback();
            } 
            catch (Exception f) {
                f.printStackTrace();
            }
            throw e;
        }
        
    }
    
    public static void addPermission(NamespaceAccessToken nat,
                                     String caller,
                                     String path,
                                     String subject,
                                     String action,
                                     String inheritable,
                                     String negative) throws Exception {
        String filesPath = nat.getNamespaceConfig().getFilesPath();
        String uri;
        if (path.equals("/")) {
            uri = filesPath;
        }
        else {
            uri = filesPath + "/" + path;
        }
        
        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Security security = nat.getSecurityHelper();
        
        boolean isInheritable  = Boolean.valueOf(inheritable).booleanValue();
        boolean isNegative     = Boolean.valueOf(negative).booleanValue();
        
        try {
            NodePermission permission = new NodePermission(uri,subject,action,isInheritable,isNegative);
            
            nat.begin();
            if (isNegative) {
                security.denyPermission(slideToken,permission);
            }
            else {
                security.grantPermission(slideToken,permission);
            }
            nat.commit();
        } catch (Exception e) {
            try {
                nat.rollback();
            } 
            catch (Exception f) {
                f.printStackTrace();
            }
            throw e;
        }
    }
}
