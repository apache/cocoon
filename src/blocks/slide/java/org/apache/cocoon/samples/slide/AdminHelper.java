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

package org.apache.cocoon.samples.slide;

import java.util.ArrayList;
import java.util.Collections;
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
import org.apache.slide.lock.Lock;
import org.apache.slide.lock.NodeLock;
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
    
    private static final SlideToken ROOT = new SlideTokenImpl(new CredentialsToken("root"));
    
    public static boolean login(NamespaceAccessToken nat,
                                String userId,
                                String password) throws Exception {
        
        String usersPath = nat.getNamespaceConfig().getUsersPath();
        String userUri = usersPath + "/" + userId;
        
        Content content = nat.getContentHelper();
        
        try {
            NodeRevisionDescriptors revisions = content.retrieve(ROOT,userUri);
            NodeRevisionDescriptor revision = content.retrieve(ROOT,revisions);
            NodeProperty property = revision.getProperty(
                "password",NodeProperty.SLIDE_NAMESPACE);
            
            return property.getValue().equals(password);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    public static void addUser(NamespaceAccessToken nat, 
                               String caller, 
                               String username, 
                               String password) throws Exception {
        
        String usersPath = nat.getNamespaceConfig().getUsersPath();
        String userUri = usersPath + "/" + username;
        
        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Structure structure = nat.getStructureHelper();
        Content content = nat.getContentHelper();
        
        try {
            
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
    
    public static void addRole(NamespaceAccessToken nat,
                                String caller,
                                String rolename) throws Exception {
        
        String rolesPath = nat.getNamespaceConfig().getRolesPath();
        String roleUri = rolesPath + "/" + rolename;
        
        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Structure structure = nat.getStructureHelper();
        Content content = nat.getContentHelper();
        
        try {
            nat.begin();
            
            ObjectNode role = new SubjectNode();
            structure.create(slideToken,role,roleUri);
            
            NodeRevisionDescriptor descriptor = new NodeRevisionDescriptor();
            descriptor.setCreationDate(new Date());
            descriptor.setLastModified(new Date());
            
            content.create(slideToken,roleUri,descriptor,null);
            
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
    
    public static void removeObject(NamespaceAccessToken nat,
                                    String caller,
                                    String objectUri) throws Exception {
        
        String usersPath = nat.getNamespaceConfig().getUsersPath();
        String callerUri = usersPath + "/" + caller;
                                        
        // user cannot delete itself
        if (callerUri.equals(objectUri)) {
            return;
        }
    
        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Macro macro = nat.getMacroHelper();
    
        try {
            nat.begin();
    
            boolean recursive = true;
            boolean overwrite = false;
            MacroParameters parameters = new MacroParameters(recursive,overwrite);
    
            macro.delete(slideToken,objectUri,parameters);
    
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
    
    public static void addMember(NamespaceAccessToken nat,
                                 String caller,
                                 String objectUri,
                                 String subjectUri) throws Exception {
        
        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Structure structure = nat.getStructureHelper();
        Content content = nat.getContentHelper();
        
        try {
            
            // check if the subject exists
            structure.retrieve(slideToken,subjectUri);
            
            NodeRevisionDescriptors descriptors = content.retrieve(slideToken,objectUri);
            NodeRevisionDescriptor descriptor = content.retrieve(slideToken,descriptors);
            NodeProperty property = descriptor.getProperty("group-member-set","DAV:");
            
            String value = null;
            if (property != null) {
                value = (String) property.getValue();
                if (value.indexOf(subjectUri) != -1) {
                    // user already a member of this group
                    return;
                }
            }
            else {
                value = "";
            }
            value = value + "<D:href xmlns:D='DAV:'>" + subjectUri + "</D:href>";
            
            descriptor.setProperty("group-member-set","DAV:",value);
            nat.begin();
            content.store(slideToken,objectUri,descriptor,null);
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
    
    public static void removeMember(NamespaceAccessToken nat,
                                    String caller,
                                    String objectUri,
                                    String subjectUri) throws Exception {
        
        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Content content = nat.getContentHelper();
        
        try {
            
            NodeRevisionDescriptors revisions = content.retrieve(slideToken,objectUri);
            NodeRevisionDescriptor revision = content.retrieve(slideToken,revisions);
            NodeProperty property = revision.getProperty("group-member-set","DAV:");
            
            if (property == null) {
                // group has no members
                return;
            }
            String value = (String) property.getValue();
            
            int index = value.indexOf(subjectUri);
            if (index == -1) {
                // subject is not a member of this group
                return;
            }
            
            // looking for the end of </D:href> after subjectUri
            int end = index + subjectUri.length();
            do {
                end++;
            } 
            while (value.charAt(end) != '>');
            
            // looking for the start of <D:href> before subjectUri
            int from = index;
            do {
                from--;
            }
            while(value.charAt(from) != '<');
            
            // snip out the user
            String before = value.substring(0,from);
            String after  = value.substring(end+1);
            value = before + after;
            
            revision.setProperty("group-member-set","DAV:",value);
            nat.begin();
            content.store(slideToken,objectUri,revision,null);
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
                                           
        String uri = getUriFromPath(nat,path);
        
        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Security security = nat.getSecurityHelper();
        
        List result = new ArrayList();
        try {
            nat.begin();
            Enumeration permissions = security.enumeratePermissions(slideToken,uri,false);
            while (permissions.hasMoreElements()) {
                result.add(permissions.nextElement());
            }
            nat.commit();
            return result;
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
    
    public static List listLocks(NamespaceAccessToken nat,
                                 String caller,
                                 String path) throws Exception {

        String uri = getUriFromPath(nat,path);
        
        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Lock lock = nat.getLockHelper();
        
        List result = new ArrayList();
        try {
            nat.begin();
            Enumeration locks = lock.enumerateLocks(slideToken,uri,false);
            while(locks.hasMoreElements()) {
                result.add(locks.nextElement());
            }
            nat.commit();
            return result;
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
    
    public static List listGroups(NamespaceAccessToken nat, String caller, String path) throws Exception {
        List result = new ArrayList();

        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Structure structure = nat.getStructureHelper();
        Content content = nat.getContentHelper();
        
        ObjectNode object = structure.retrieve(slideToken,path);
        Enumeration enum = structure.getChildren(slideToken,object);
        while (enum.hasMoreElements()) {
            String uri = ((ObjectNode) enum.nextElement()).getUri();
            NodeRevisionDescriptors revisions = content.retrieve(slideToken, uri);
            NodeRevisionDescriptor revision = content.retrieve(slideToken, revisions);
            NodeProperty property = revision.getProperty("group-member-set","DAV:");
            List members;
            if (property != null) {
                String value = (String) property.getValue();
                members = new ArrayList(10);
                int start = value.indexOf('>'), end = 0;
                while (start != -1) {
                    end = value.indexOf('<',start);
                    if (end != -1) {
                        members.add(value.substring(start+1,end));
                    }
                    end = value.indexOf('>',start+1);
                    start = value.indexOf('>',end+1);
                }
            }
            else {
                members = Collections.EMPTY_LIST;
            }
            result.add(new Group(uri,members));
        }

        return result;
    }
    
    public static List listUsers(NamespaceAccessToken nat,
                                 String caller) throws Exception {
        return listObjects(nat,caller,nat.getNamespaceConfig().getUsersPath());
    }
    
    public static List listPrivileges(NamespaceAccessToken nat,
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
            result.add(((ObjectNode) enum.nextElement()).getUri());
        }
        
        return result;
    }
    
    public static void removePermission(NamespaceAccessToken nat,
                                        String caller,
                                        String path,
                                        String subject,
                                        String action) throws Exception {

        String uri = getUriFromPath(nat,path);
        
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
                                         
        String uri = getUriFromPath(nat,path);
        
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
    
    public static void removeLock(NamespaceAccessToken nat,
                                  String caller,
                                  String uri,
                                  String lockId) throws Exception {
        
        SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
        Lock lock = nat.getLockHelper();
        
        try {
            nat.begin();
            lock.unlock(slideToken,uri,lockId);
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
    
    public static void addLock(NamespaceAccessToken nat,
                               String caller,
                               String path,
                               String subject,
                               String type,
                               String expiration,
                               String exclusive,
                               String inherit) throws Exception {

       String uri = getUriFromPath(nat,path);
       boolean isExclusive = Boolean.valueOf(exclusive).booleanValue();
       boolean isInherit = Boolean.valueOf(inherit).booleanValue();
       
       // expiration in minutes
       int intExpiration = Integer.valueOf(expiration).intValue();
       Date expire = new Date(System.currentTimeMillis() + intExpiration*1000*60);
       
       SlideToken slideToken = new SlideTokenImpl(new CredentialsToken(caller));
       Lock lock = nat.getLockHelper();
       
       try {
           nat.begin();
           lock.lock(slideToken,new NodeLock(uri,subject,type,expire,isExclusive,isInherit));
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
    
    private static String getUriFromPath(NamespaceAccessToken nat,
                                         String path) {
        String filesPath = nat.getNamespaceConfig().getFilesPath();
        String uri;
        if (path.equals("/") || path.length() == 0) {
            uri = filesPath;
        }
        else {
            uri = filesPath + "/" + path;
        }
        return uri;
    }
    
    public static class Group {
        private final String m_uri;
        private final List m_members;
        
        private Group(String uri, List members) {
            m_uri = uri;
            m_members = members;
        }
        
        public String getUri() {
            return m_uri;
        }
        
        public List getMembers() {
            return m_members;
        }
        
        public String toString() {
            return m_uri;
        }
    }
}
