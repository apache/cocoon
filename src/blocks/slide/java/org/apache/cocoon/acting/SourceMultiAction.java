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

package org.apache.cocoon.acting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.InspectableSource;
import org.apache.cocoon.components.source.RestrictableSource;
import org.apache.cocoon.components.source.helpers.GroupSourcePermission;
import org.apache.cocoon.components.source.helpers.PrincipalSourcePermission;
import org.apache.cocoon.components.source.helpers.SourceCredential;
import org.apache.cocoon.components.source.helpers.SourcePermission;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.servlet.multipart.Part;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.ModifiableTraversableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;

/**
 * Multiple actions for upload files, change properties and permissions.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: SourceMultiAction.java,v 1.4 2003/12/22 13:35:06 joerg Exp $
 */ 
public class SourceMultiAction extends AbstractMultiAction implements ThreadSafe {

    public final static String SOURCE_URI = "cocoon-source-uri";
    public final static String SOURCE_PROPERTY_NAMESPACE = "cocoon-source-property-namespace";
    public final static String SOURCE_PROPERTY_NAME = "cocoon-source-property-name";
    public final static String SOURCE_PROPERTY_VALUE = "cocoon-source-property-value";
    public final static String SOURCE_PERMISSION_PRINCIPAL = "cocoon-source-permission-principal";
    public final static String SOURCE_PERMISSION_PRINCIPAL_GROUP = "cocoon-source-permission-principal-group";
    public final static String SOURCE_PERMISSION_PRIVILEGE = "cocoon-source-permission-privilege";
    public final static String SOURCE_PERMISSION_INHERITABLE = "cocoon-source-permission-inheritable";
    public final static String SOURCE_PERMISSION_NEGATIVE = "cocoon-source-permission-negative";
    public final static String PRINCIPAL = "cocoon-source-principal";
    public final static String PASSWORD = "cocoon-source-password";
    public final static String UPLOAD_FILE = "cocoon-upload-file";
    public final static String SOURCE_NAME = "cocoon-source-name";

    public Map doUploadSource(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String src,
                   Parameters parameters) throws Exception {

        Request request = ObjectModelHelper.getRequest(objectModel);

        String uri = parameters.getParameter(SOURCE_URI, request.getParameter(SOURCE_URI));
        String filename = parameters.getParameter(SOURCE_NAME, request.getParameter(SOURCE_NAME));
        if (filename!=null) 
            filename = filename.trim();
        String principal = parameters.getParameter(PRINCIPAL,
                              request.getParameter(PRINCIPAL));
        String password = parameters.getParameter(PASSWORD,
                              request.getParameter(PASSWORD));

        getLogger().info("upload source called by '"+principal+"' for '"+uri+"'");

        Object uploadedFile = request.get(UPLOAD_FILE);
        
        if (( uploadedFile != null) && (uploadedFile instanceof Part)) {
            Part part = (Part) uploadedFile;
            
            try {
                if ((filename == null) || (filename.length() == 0)) {
                    filename = part.getFileName();
                }

                if ((uri == null) || (uri.length() == 0))
                    uri = filename;
                else if (uri.endsWith("/"))
                    uri = uri + filename;
                else
                    uri = uri + "/" + filename;

                Source source = resolver.resolveURI(uri);

                if (source instanceof RestrictableSource)
                    ((RestrictableSource)source).setSourceCredential(new SourceCredential(principal, password));

                if (source instanceof ModifiableSource) {
                    ModifiableSource writeablesource = (ModifiableSource)source;

                    OutputStream out = writeablesource.getOutputStream();

                    byte[] buffer = new byte[8192];
                    int length = -1;

                    InputStream in = part.getInputStream();
                    while ((length = in.read(buffer)) > -1) {
                        out.write(buffer, 0, length);
                        getLogger().debug("="+length);
                    }
                    in.close();
                    out.flush();
                    out.close();
                } else {
                    throw new ProcessingException("Source isn't writeable");
                }
            } catch (SourceException se) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Exception occurs while storing the content", se);
                throw new ProcessingException("Exception occurs while storing the content", se);
            } catch (IOException ioe) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Exception occurs while storing the content", ioe);
                throw new ProcessingException("Exception occurs while storing the content", ioe);
            }
        } else {
            getLogger().debug("Couldn't get upload file");
        }

        return EMPTY_MAP;
    }

    public Map doCreateCollection(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String src,
                   Parameters parameters) throws Exception {

        Request request = ObjectModelHelper.getRequest(objectModel);

        String uri = parameters.getParameter(SOURCE_URI, request.getParameter(SOURCE_URI));
        String principal = parameters.getParameter(PRINCIPAL,
                              request.getParameter(PRINCIPAL));

        getLogger().info("create collection called by '"+principal+"' for '"+uri+"'");

        try {
            Source source = resolver.resolveURI(uri);
            
            if (source instanceof ModifiableTraversableSource) {
                ModifiableTraversableSource modifiabletraversablesource = 
                    (ModifiableTraversableSource) source;
                modifiabletraversablesource.makeCollection();

            } else
                throw new ProcessingException("Source isn't modifiable traversable");

        } catch (SourceException se) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("Exception occurs while creation of a collection", se);
            throw new ProcessingException("Exception occurs while creation of a collection", se);
        }

        return EMPTY_MAP;
    }

    public Map doDeleteSource(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String src,
                   Parameters parameters) throws Exception {

        Request request = ObjectModelHelper.getRequest(objectModel);

        String uri = parameters.getParameter(SOURCE_URI, request.getParameter(SOURCE_URI));
        String principal = parameters.getParameter(PRINCIPAL,
                              request.getParameter(PRINCIPAL));
        String password = parameters.getParameter(PASSWORD,
                              request.getParameter(PASSWORD));

        getLogger().info("delete source called by '"+principal+"' for '"+uri+"'");

        try {
            Source source = resolver.resolveURI(uri);

            if (source instanceof RestrictableSource)
                ((RestrictableSource)source).setSourceCredential(new SourceCredential(principal, password));

            if (source instanceof ModifiableSource) {
                ModifiableSource writeablesource = (ModifiableSource)source;

                writeablesource.delete();

            } else
                throw new ProcessingException("Source isn't writeable");

        } catch (SourceException se) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("Exception occurs while modifying the source", se);
            throw new ProcessingException("Exception occurs while modifying the source", se);
        }

        return EMPTY_MAP;
    }

    public Map doAddProperty(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String src,
                   Parameters parameters) throws Exception {

        Request request = ObjectModelHelper.getRequest(objectModel);
                
        String uri = parameters.getParameter(SOURCE_URI, request.getParameter(SOURCE_URI));
        String namespace = parameters.getParameter(SOURCE_PROPERTY_NAMESPACE, 
            request.getParameter(SOURCE_PROPERTY_NAMESPACE));
        String name = parameters.getParameter(SOURCE_PROPERTY_NAME, 
            request.getParameter(SOURCE_PROPERTY_NAME));
        String value = parameters.getParameter(SOURCE_PROPERTY_VALUE, 
            request.getParameter(SOURCE_PROPERTY_VALUE));

        String principal = parameters.getParameter(PRINCIPAL,
            request.getParameter(PRINCIPAL));
        String password = parameters.getParameter(PASSWORD,
            request.getParameter(PASSWORD));

        getLogger().info("add property called by '"+principal+"' for '"+uri+"'");

        try {

            Source source = resolver.resolveURI(uri);

            if (source instanceof RestrictableSource)
                ((RestrictableSource)source).setSourceCredential(new SourceCredential(principal, password));

            if (source instanceof InspectableSource) {
                InspectableSource inspectablesource = (InspectableSource)source;

                SourceProperty property = new SourceProperty(namespace, name, value);

                inspectablesource.setSourceProperty(property);
            } else
                throw new ProcessingException("Source isn't inspectable");
        } catch (SourceException se) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("Exception occurs while modifying the source", se);
            throw new ProcessingException("Exception occurs while modifying the source", se);
        }

        return EMPTY_MAP;
    }

    public Map doDeleteProperty(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String src,
                   Parameters parameters) throws Exception {

        Request request = ObjectModelHelper.getRequest(objectModel);

        String uri = parameters.getParameter(SOURCE_URI, request.getParameter(SOURCE_URI));
        String namespace = parameters.getParameter(SOURCE_PROPERTY_NAMESPACE,
            request.getParameter(SOURCE_PROPERTY_NAMESPACE));
        String name = parameters.getParameter(SOURCE_PROPERTY_NAME,
            request.getParameter(SOURCE_PROPERTY_NAME));

        String principal = parameters.getParameter(PRINCIPAL,
            request.getParameter(PRINCIPAL));
        String password = parameters.getParameter(PASSWORD,
            request.getParameter(PASSWORD));

        getLogger().info("delete property called by '"+principal+"' for '"+uri+"'");

        try {

            Source source = resolver.resolveURI(uri);

            if (source instanceof RestrictableSource)
                ((RestrictableSource)source).setSourceCredential(new SourceCredential(principal, password));

            if (source instanceof InspectableSource) {
                InspectableSource inspectablesource = (InspectableSource)source;

                inspectablesource.removeSourceProperty(namespace, name);
            } else
                throw new ProcessingException("Source isn't inspectable");
        } catch (SourceException se) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("Exception occurs while modifying the source", se);
            throw new ProcessingException("Exception occurs while modifying the source", se);
        }


        return EMPTY_MAP;
    }

    public Map doAddPrincipalPermission(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String src,
                   Parameters parameters) throws Exception {

        Request request = ObjectModelHelper.getRequest(objectModel);

        String uri = parameters.getParameter(SOURCE_URI, request.getParameter(SOURCE_URI));
        String subject = parameters.getParameter(SOURCE_PERMISSION_PRINCIPAL,
            request.getParameter(SOURCE_PERMISSION_PRINCIPAL));
        String privilege = parameters.getParameter(SOURCE_PERMISSION_PRIVILEGE,
            request.getParameter(SOURCE_PERMISSION_PRIVILEGE));
        boolean inheritable = Boolean.valueOf(parameters.getParameter(SOURCE_PERMISSION_INHERITABLE,
            request.getParameter(SOURCE_PERMISSION_INHERITABLE))).booleanValue();
        boolean negative = Boolean.valueOf(parameters.getParameter(SOURCE_PERMISSION_NEGATIVE,
            request.getParameter(SOURCE_PERMISSION_NEGATIVE))).booleanValue();

        String principal = parameters.getParameter(PRINCIPAL,
            request.getParameter(PRINCIPAL));
        String password = parameters.getParameter(PASSWORD,
            request.getParameter(PASSWORD));

        getLogger().info("add principal permission called by '"+principal+"' for '"+uri+"'");

        try {

            Source source = resolver.resolveURI(uri);

            if (source instanceof RestrictableSource) {
                RestrictableSource restrictablesource = (RestrictableSource)source;

                restrictablesource.setSourceCredential(new SourceCredential(principal, password));

                SourcePermission permission = 
                    new PrincipalSourcePermission(subject, privilege, inheritable, negative);

                restrictablesource.addSourcePermission(permission);
            } else
                throw new ProcessingException("Source isn't restrictable");
        } catch (SourceException se) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("Exception occurs while modifying the source", se);
            throw new ProcessingException("Exception occurs while modifying the source", se);
        }

        return EMPTY_MAP;
    }

    public Map doRemovePrincipalPermission(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String src,
                   Parameters parameters) throws Exception {

        Request request = ObjectModelHelper.getRequest(objectModel);

        String uri = parameters.getParameter(SOURCE_URI, request.getParameter(SOURCE_URI));
        String subject = parameters.getParameter(SOURCE_PERMISSION_PRINCIPAL,
            request.getParameter(SOURCE_PERMISSION_PRINCIPAL));
        String privilege = parameters.getParameter(SOURCE_PERMISSION_PRIVILEGE,
            request.getParameter(SOURCE_PERMISSION_PRIVILEGE));
        boolean inheritable = Boolean.getBoolean(parameters.getParameter(SOURCE_PERMISSION_INHERITABLE,
            request.getParameter(SOURCE_PERMISSION_INHERITABLE)));
        boolean negative = Boolean.getBoolean(parameters.getParameter(SOURCE_PERMISSION_NEGATIVE,
            request.getParameter(SOURCE_PERMISSION_NEGATIVE)));

        String principal = parameters.getParameter(PRINCIPAL,
            request.getParameter(PRINCIPAL));
        String password = parameters.getParameter(PASSWORD,
            request.getParameter(PASSWORD));

        getLogger().info("remove principal permission called by '"+principal+"' for '"+uri+"'");

        try {

            Source source = resolver.resolveURI(uri);

            if (source instanceof RestrictableSource) {
                RestrictableSource restrictablesource = (RestrictableSource)source;

                restrictablesource.setSourceCredential(new SourceCredential(principal, password));

                SourcePermission permission =
                    new PrincipalSourcePermission(subject, privilege, inheritable, negative);

                restrictablesource.removeSourcePermission(permission);
            } else
                throw new ProcessingException("Source isn't restrictable");
        } catch (SourceException se) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("Exception occurs while modifying the source", se);
            throw new ProcessingException("Exception occurs while modifying the source", se);
        }

        return EMPTY_MAP;
    }

    public Map doAddPrincipalGroupPermission(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String src,
                   Parameters parameters) throws Exception {

        Request request = ObjectModelHelper.getRequest(objectModel);

        String uri = parameters.getParameter(SOURCE_URI, request.getParameter(SOURCE_URI));
        String subject = parameters.getParameter(SOURCE_PERMISSION_PRINCIPAL_GROUP,
            request.getParameter(SOURCE_PERMISSION_PRINCIPAL_GROUP));
        String privilege = parameters.getParameter(SOURCE_PERMISSION_PRIVILEGE,
            request.getParameter(SOURCE_PERMISSION_PRIVILEGE));
        boolean inheritable = Boolean.valueOf(parameters.getParameter(SOURCE_PERMISSION_INHERITABLE,
            request.getParameter(SOURCE_PERMISSION_INHERITABLE))).booleanValue();
        boolean negative = Boolean.valueOf(parameters.getParameter(SOURCE_PERMISSION_NEGATIVE,
            request.getParameter(SOURCE_PERMISSION_NEGATIVE))).booleanValue();

        String principal = parameters.getParameter(PRINCIPAL,
            request.getParameter(PRINCIPAL));
        String password = parameters.getParameter(PASSWORD,
            request.getParameter(PASSWORD));

        getLogger().info("add principal group permission called by '"+principal+"' for '"+uri+"'");

        try {

            Source source = resolver.resolveURI(uri);

            if (source instanceof RestrictableSource) {
                RestrictableSource restrictablesource = (RestrictableSource)source;

                restrictablesource.setSourceCredential(new SourceCredential(principal, password));

                SourcePermission permission =
                    new GroupSourcePermission(subject, privilege, inheritable, negative);

                restrictablesource.addSourcePermission(permission);
            } else
                throw new ProcessingException("Source isn't restrictable");
        } catch (SourceException se) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("Exception occurs while modifying the source", se);
            throw new ProcessingException("Exception occurs while modifying the source", se);
        }

        return EMPTY_MAP;
    }

    public Map doRemovePrincipalGroupPermission(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String src,
                   Parameters parameters) throws Exception {

        Request request = ObjectModelHelper.getRequest(objectModel);

        String uri = parameters.getParameter(SOURCE_URI, request.getParameter(SOURCE_URI));
        String subject = parameters.getParameter(SOURCE_PERMISSION_PRINCIPAL_GROUP,
            request.getParameter(SOURCE_PERMISSION_PRINCIPAL_GROUP));
        String privilege = parameters.getParameter(SOURCE_PERMISSION_PRIVILEGE,
            request.getParameter(SOURCE_PERMISSION_PRIVILEGE));
        boolean inheritable = Boolean.getBoolean(parameters.getParameter(SOURCE_PERMISSION_INHERITABLE,
            request.getParameter(SOURCE_PERMISSION_INHERITABLE)));
        boolean negative = Boolean.getBoolean(parameters.getParameter(SOURCE_PERMISSION_NEGATIVE,
            request.getParameter(SOURCE_PERMISSION_NEGATIVE)));

        String principal = parameters.getParameter(PRINCIPAL,
            request.getParameter(PRINCIPAL));
        String password = parameters.getParameter(PASSWORD,
            request.getParameter(PASSWORD));

        getLogger().info("remove principal group permission called by '"+principal+"' for '"+uri+"'");

        try {

            Source source = resolver.resolveURI(uri);

            if (source instanceof RestrictableSource) {
                RestrictableSource restrictablesource = (RestrictableSource)source;

                restrictablesource.setSourceCredential(new SourceCredential(principal, password));

                SourcePermission permission =
                    new GroupSourcePermission(subject, privilege, inheritable, negative);

                restrictablesource.removeSourcePermission(permission);
            } else
                throw new ProcessingException("Source isn't restrictable");
        } catch (SourceException se) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("Exception occurs while modifying the source", se);
            throw new ProcessingException("Exception occurs while modifying the source", se);
        }

        return EMPTY_MAP;
    }
}



