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

package org.apache.cocoon.components.source.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.source.InspectableSource;
import org.apache.cocoon.components.source.LockableSource;
import org.apache.cocoon.components.source.RestrictableSource;
import org.apache.cocoon.components.source.VersionableSource;
import org.apache.cocoon.components.source.helpers.GroupSourcePermission;
import org.apache.cocoon.components.source.helpers.PrincipalSourcePermission;
import org.apache.cocoon.components.source.helpers.SourceCredential;
import org.apache.cocoon.components.source.helpers.SourceLock;
import org.apache.cocoon.components.source.helpers.SourcePermission;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.excalibur.source.ModifiableTraversableSource;
import org.apache.excalibur.source.MoveableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.TimeStampValidity;
import org.apache.excalibur.xml.dom.DOMParser;
import org.apache.slide.authenticate.CredentialsToken;
import org.apache.slide.common.NamespaceAccessToken;
import org.apache.slide.common.NamespaceConfig;
import org.apache.slide.common.SlideException;
import org.apache.slide.common.SlideToken;
import org.apache.slide.common.SlideTokenImpl;
import org.apache.slide.content.Content;
import org.apache.slide.content.NodeProperty;
import org.apache.slide.content.NodeRevisionContent;
import org.apache.slide.content.NodeRevisionDescriptor;
import org.apache.slide.content.NodeRevisionDescriptors;
import org.apache.slide.content.NodeRevisionNumber;
import org.apache.slide.content.RevisionDescriptorNotFoundException;
import org.apache.slide.lock.Lock;
import org.apache.slide.lock.NodeLock;
import org.apache.slide.macro.Macro;
import org.apache.slide.security.AccessDeniedException;
import org.apache.slide.security.NodePermission;
import org.apache.slide.security.Security;
import org.apache.slide.structure.GroupNode;
import org.apache.slide.structure.ObjectNode;
import org.apache.slide.structure.ObjectNotFoundException;
import org.apache.slide.structure.Structure;
import org.apache.slide.structure.SubjectNode;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * A sources from jakarta slide repositories.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: SlideSource.java,v 1.12 2003/12/10 17:22:47 unico Exp $
 */
public class SlideSource extends AbstractLogEnabled
implements Contextualizable, Serviceable, Initializable, Source, ModifiableTraversableSource, 
           MoveableSource, RestrictableSource, LockableSource, InspectableSource, 
           VersionableSource {

    /* framework objects */
    private Context m_context;
    private ServiceManager m_manager;
    
    /* Slide access */
    private NamespaceAccessToken m_nat;
    private NamespaceConfig m_config;
    private SlideToken m_slideToken;
    
    /* Slide helpers */
    private Structure m_structure;
    private Content m_content;
    private Security m_security;
    private Lock m_lock;
    private Macro m_macro;

    /* Source specifics */
    private String m_scheme = "slide";
    private String m_path;
    private String m_scope;
    // uri = scope + path;
    private String m_uri;
    
    private ObjectNode m_node;
    private NodeRevisionNumber m_version;
    private NodeRevisionDescriptors m_descriptors;
    private NodeRevisionDescriptor m_descriptor;

    private SourceCredential m_credential;
    private SourceValidity m_validity;

    private SlideSourceOutputStream m_outputStream;

    /**
     * Create a slide source.
     *
     * @param nat Namespace access token
     * @param scheme Scheme of the source
     * @param path Path of the source.
     * @param sourcecredential Credential, which should be used.
     * @param sourcerevision Revision, which should be used.
     * @param sourcerevisionbranch Branch, which should be used.
     *
     * @throws SourceException If Exception occurs during the initialization.
     */
    public SlideSource(NamespaceAccessToken nat, 
                       String scheme, 
                       String scope,
                       String path,
                       SourceCredential sourcecredential, 
                       String version) {

        m_nat = nat;
        m_scheme = scheme;
        m_path = path;
        m_scope = scope;
        if (path.equals("/")) {
            m_uri = scope;
        }
        else if (scope.equals("/")){
            m_uri = path;
        }
        else {
            m_uri = scope + path;
        }
        m_credential = sourcecredential;
        if (version != null) {
            m_version = new NodeRevisionNumber(version);
        }

    }
    
    /**
     * Pass the Context to the component.
     * This method is called after the LogEnabled.enableLogging() (if present)
     * method and before any other method.
     *
     * @param context The context.
     */
    public void contextualize(Context context) {
        this.m_context = context;
    }

    /**
     * Pass the ServiceManager to the composer. The Serviceable implementation
     * should use the specified ServiceManager to acquire the services it needs for execution
     *
     * @param manager The ServiceManager which this Serviceable uses
     */
    public void service(ServiceManager manager) {
        m_manager = manager;
    }

    public void initialize() throws SourceException {
        
        CredentialsToken credentials = new CredentialsToken(m_credential.getPrincipal());
        m_slideToken = new SlideTokenImpl(credentials);
        
        m_config = m_nat.getNamespaceConfig();
        m_structure = m_nat.getStructureHelper();
        m_content = m_nat.getContentHelper();
        m_security = m_nat.getSecurityHelper();
        m_lock = m_nat.getLockHelper();
        m_macro = m_nat.getMacroHelper();
        
        try {
            if (m_node == null) {
                m_node = m_structure.retrieve(m_slideToken,m_uri);
            }
                
            m_descriptors = m_content.retrieve(m_slideToken,m_uri);
            if (m_version != null) {
                // get a specific version
                m_descriptor = m_content.retrieve(m_slideToken,m_descriptors,m_version);
            }
            else {
                // get the latest one
                m_descriptor = m_content.retrieve(m_slideToken,m_descriptors);
                m_version = m_descriptor.getRevisionNumber();
            }
        } 
        catch (ObjectNotFoundException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Not found.",e);
            }
            // assert m_node == null;
        }  
        catch (RevisionDescriptorNotFoundException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Could not retrieve descriptor.",e);
            }
            // assert m_descriptor == null;
        } 
        catch (AccessDeniedException e) {
            throw new SourceException("Access denied.",e);
        }
        catch (SlideException e) {
            throw new SourceException("Failure during source initialization.",e);
        }
//        catch (ObjectLockedException e) {
//            throw new SourceException("Object is locked.",e);
//        } 
//        catch (LinkedObjectNotFoundException e) {
//            throw new SourceException("Linked object not found.",e);
//        } 
//        catch (ServiceAccessException e) {
//            throw new SourceException("Low level service access exception.",e);
//        }

    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     * This is the data at the point of invocation of this method,
     * so if this is Modifiable, you might get different content
     * from two different invocations.
     *
     * @return Input stream for the source.
     *
     * @throws IOException If an IO excepetion occurs.
     * @throws SourceException If an exception occurs.
     */
    public InputStream getInputStream() throws IOException, SourceException {
        try {
            return m_content.retrieve(m_slideToken,m_descriptors,m_descriptor).streamContent();
        } catch (SlideException se) {
            throw new SourceException("Could not get source", se);
        }
    }

    /**
     * Return the unique identifer for this source
     *
     * @return System identifier for the source.
     */
    public String getURI() {
        return m_scheme + "://" + m_credential.getPrincipal() + "@" + m_nat.getName() + m_path;
    }

    /**
     * @see org.apache.excalibur.source.Source#getScheme()
     *
     * @return Scheme of the source.
     */
    public String getScheme() {
        return m_scheme;
    }

    /**
     *  Get the Validity object. This can either wrap the last modification
     *  date or the expires information or...
     *  If it is currently not possible to calculate such an information
     *  <code>null</code> is returned.
     *
     * @return Validity for the source.
     */
    public SourceValidity getValidity() {
        try {
            if (m_validity == null && m_descriptor != null) {
                m_validity = new TimeStampValidity(
                    m_descriptor.getLastModifiedAsDate().getTime());
            }
        } catch (Exception e) {
            getLogger().debug("Could not create SourceValidity", e);
            return null;
        }
        return m_validity;
    }

    /**
     * Refresh the content of this object after the underlying data
     * content has changed.
     */
    public void refresh() {
        m_validity = null;
    }

    /**
     * The mime-type of the content described by this object.
     * If the source is not able to determine the mime-type by itself
     * this can be null.
     *
     * @return Mime type of the source.
     */
    public String getMimeType() {
        if (m_descriptor != null) {
            return m_descriptor.getContentType();
        }
        return null;
    }

    /**
     * Does this source actually exist ?
     *
     * @return true if the resource exists.
     */
    public boolean exists() {
        return m_node != null;
    }

    /**
     * Return the content length of the content or -1 if the length is
     * unknown.
     *
     * @return Content length of the source.
     */
    public long getContentLength() {
        if (m_descriptor != null) {
            return m_descriptor.getContentLength();
        }
        return -1;
    }

    /**
     * Get the last modification date of the source or 0 if it
     * is not possible to determine the date.
     *
     * @return Last modified date of the source.
     */
    public long getLastModified() {
        if (m_descriptor != null) {
            return m_descriptor.getLastModifiedAsDate().getTime();
        }
        return 0;
    }
    
    // ---------------------------------------------------- ModifiableTraversableSource
    
    /**
     * Get an <code>OutputStream</code> where raw bytes can be written to.
     * The signification of these bytes is implementation-dependent and
     * is not restricted to a serialized XML document.
     *
     * @return a stream to write to
     *
     * @throws IOException
     * @throws SourceException
     */
    public OutputStream getOutputStream()
      throws IOException, SourceException {
        if (m_outputStream == null) {
            m_outputStream = new SlideSourceOutputStream();
            m_outputStream.enableLogging(getLogger());
        }
        return m_outputStream;
    }

    /**
     * Can the data sent to an <code>OutputStream</code> returned by
     * {@link #getOutputStream()} be cancelled ?
     *
     * @param stream The ouput stream, which should be cancelled.
     * @return true if the stream can be cancelled
     */
    public boolean canCancel(OutputStream stream) {
        return m_outputStream.canCancel();
    }

    /**
     * Cancel the data sent to an <code>OutputStream</code> returned by
     * {@link #getOutputStream()}.
     * <p>
     * After cancel, the stream should no more be used.
     *
     * @param stream The ouput stream, which should be cancelled.
     *
     * @throws SourceException If the ouput stream can't be cancelled.
     */
    public void cancel(OutputStream stream) throws SourceException {
        if (m_outputStream == stream) {
            try {
                m_outputStream.cancel();
            } catch (Exception e) {
                throw new SourceException("Could not cancel output stream",e);
            }
        }
    }
    
    /**
     * Delete the source.
     */
    public void delete() {
        try {
            m_nat.begin();
            m_macro.delete(m_slideToken,m_uri);
            m_nat.commit();
        } catch (Exception se) {
            getLogger().error("Could not delete source.",se);
            try {
                m_nat.rollback();
            } catch (Exception rbe) {
                getLogger().error("Rollback failed for moving source",rbe);
            }
        }
    }
    
    public void makeCollection() throws SourceException {
        SubjectNode collection = new SubjectNode();
        NodeRevisionDescriptor descriptor = new NodeRevisionDescriptor(0);

        descriptor.setResourceType("<collection/>");
        descriptor.setCreationDate(new Date());
        descriptor.setLastModified(new Date());
        descriptor.setContentLength(0);
        descriptor.setSource("");
        descriptor.setOwner(m_slideToken.getCredentialsToken().getPublicCredentials());

        try {
            m_nat.begin();
            m_structure.create(m_slideToken,collection,m_uri);
            m_content.create(m_slideToken,m_uri,descriptor,null);
            m_nat.commit();
        } catch (Exception se) {
            try {
                m_nat.rollback();
            } catch (Exception rbe) {
                getLogger().error("Rollback failed for creating collection", rbe);
            }
            throw new SourceException("Could not create collection.", se);
        }
    }
    
    public Source getChild(String name) throws SourceException {
        return getChildByPath(m_path+"/"+name);
    }
    
    private Source getChildByPath(String path) throws SourceException {
        SlideSource child = new SlideSource(m_nat,m_scheme,m_scope,path,m_credential,null);
        child.enableLogging(getLogger());
        child.contextualize(m_context);
        child.service(m_manager);
        child.initialize();
        return child;        
    }

    public Collection getChildren() throws SourceException {
        if (m_node == null || !m_node.hasChildren()) {
            return Collections.EMPTY_LIST;
        }
        List result = new ArrayList();
        final Enumeration children = m_node.enumerateChildren();
        while (children.hasMoreElements()) {
            String child = (String) children.nextElement();
            child = child.substring(m_scope.length());
            result.add(getChildByPath(child));
        }
        return result;
    }
    
    public String getName() {
        int index = m_path.lastIndexOf('/');
        if (index != -1) {
            return m_path.substring(index+1);
        }
        return m_path;
    }
    
    public Source getParent() throws SourceException {
        if (m_path.length() == 1) {
            // assert m_path.equals("/")
            return null;
        }
        int index = m_path.lastIndexOf('/');
        if (index == -1) {
            return null;
        }
        String parentPath;
        if (index == 0) {
            parentPath = "/";
        }
        else if (index == m_path.length()-1) {
            // assert m_path.endsWith("/")
            parentPath = m_path.substring(0,m_path.substring(0, m_path.length()-1).lastIndexOf('/'));
        }
        else {
            parentPath = m_path.substring(0,index);
        }
        SlideSource parent = new SlideSource(m_nat,m_scheme,m_scope,parentPath,m_credential,null);
        parent.enableLogging(getLogger());
        parent.contextualize(m_context);
        parent.service(m_manager);
        parent.initialize();
        return parent;

    }
    
    public boolean isCollection() {
        if (m_node == null) {
            return false;
        }
        if (m_descriptor == null) {
            // FIXME: is this correct?
            return true;
        }
        NodeProperty property = m_descriptor.getProperty("resourcetype");
        if (property != null && property.getValue().equals("<collection/>")) {
            return true;
        }
        return false;
    }
    
    /**
     * A helper for the getOutputStream() method
     */
    class SlideSourceOutputStream extends ByteArrayOutputStream implements LogEnabled {
        private boolean isClosed = false;
        private Logger logger = null;

        /**
         * Provide component with a logger.
         *
         * @param logger the logger
         */
        public void enableLogging(Logger logger) {
            this.logger = logger;
        }

        /**
         *
         *
         * @throws IOException
         */
        public void close() throws IOException {
            super.close();

            byte[] bytes = new byte[0]; // must be initialized

            try {
                NodeRevisionContent content = new NodeRevisionContent();
                bytes = toByteArray();
                content.setContent(bytes);

                if (m_descriptor == null) {
                    m_descriptor = new NodeRevisionDescriptor(0);
                    m_descriptor.setName(getName());
                }

                m_descriptor.setContentLength(bytes.length);
                m_descriptor.setLastModified(new Date());

                m_nat.begin();
                if (m_version == null) {
                    m_content.create(m_slideToken,m_uri,m_descriptor,null);
                }
                m_content.store(m_slideToken,m_uri,m_descriptor,content);
                try {
                    m_nat.commit();
                } catch (Exception cme) {
                    throw new CascadingIOException("Could not commit the transaction",cme);
                }

            } catch (ObjectNotFoundException e) {

                // Todo : Check to see if parent exists
                SubjectNode subject = new SubjectNode();

                try {
                    // Creating an object
                    m_structure.create(m_slideToken,subject,m_uri);
                } catch (SlideException se) {
                    throw new CascadingIOException(se);
                }

                NodeRevisionDescriptor descriptor = new NodeRevisionDescriptor(bytes.length);
                descriptor.setResourceType("");
                descriptor.setSource("");
                descriptor.setContentLanguage("en");
                descriptor.setContentLength(bytes.length);
                String contentType = null;

                try {
                    contentType = ((org.apache.cocoon.environment.Context) 
                        m_context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT)).getMimeType(m_path);
                } catch (ContextException ce) {
                    this.logger.warn("Could not get context to determine the mime type.");
                }
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                descriptor.setContentType(contentType);
                descriptor.setLastModified(new Date());
                descriptor.setOwner(m_slideToken.getCredentialsToken().getPublicCredentials());
                NodeRevisionContent content = new NodeRevisionContent();
                
                content.setContent(bytes);
                try {
                    m_content.create(m_slideToken,m_uri,descriptor,content);
                    try {
                        m_nat.commit();
                    } catch (Exception cme) {
                        throw new CascadingIOException("Could not commit the transaction",cme);

                    }
                } catch (SlideException se) {
                    try {
                        m_nat.rollback();
                    } catch (Exception rbe) {
                        this.logger.warn("Could not rollback the transaction.",rbe);
                    }
                    throw new CascadingIOException("Could not create source",se);
                }

            } catch (Exception e) {
                if (e instanceof IOException) {
                    throw (IOException) e;
                }
                throw new CascadingIOException("Could not create source", e);
            } finally {
                this.isClosed = true;
            }
        }

        /**
         * Can the data sent to an <code>OutputStream</code> returned by
         * {@link #getOutputStream()} be cancelled ?
         *
         * @return true if the stream can be cancelled
         */
        boolean canCancel() {
            return !this.isClosed;
        }

        /**
         * Cancel the data sent to an <code>OutputStream</code> returned by
         * {@link #getOutputStream()}.
         * <p>
         * After cancel, the stream should no more be used.
         *
         */
        void cancel() throws Exception {
            if (this.isClosed) {
                throw new IllegalStateException("Cannot cancel : outputstrem is already closed");
            }
            this.isClosed = true;
            super.close();
        }
    }

    // ---------------------------------------------------- MoveableSource
    
    /**
     * Move the current source to a specified destination.
     *
     * @param source
     *
     * @throws SourceException If an exception occurs during the move.
     */
    public void moveTo(Source source) throws SourceException {
        if (source instanceof SlideSource) {
            try {
                m_nat.begin();
                String destination = m_scope+((SlideSource) source).m_path;
                m_macro.move(m_slideToken,m_uri,destination);
                m_nat.commit();
            } catch (Exception se) {
                try {
                    m_nat.rollback();
                } catch (Exception rbe) {
                    getLogger().error("Rollback failed for moving source", rbe);
                }
                throw new SourceException("Could not move source.", se);
            }
        } else {
            SourceUtil.move(this,source);
        }
    }

    /**
     * Copy the current source to a specified destination.
     *
     * @param source
     *
     * @throws SourceException If an exception occurs during the copy.
     */
    public void copyTo(Source source) throws SourceException {
        if (source instanceof SlideSource) {
            try {
                m_nat.begin();
                String destination = m_scope+((SlideSource) source).m_path;
                m_macro.copy(m_slideToken,m_uri,destination);
                m_nat.commit();
            } catch (Exception se) {
                try {
                    m_nat.rollback();
                } catch (Exception rbe) {
                    
                    getLogger().error("Rollback failed for moving source",rbe);
                }
                throw new SourceException("Could not move source.",se);
            }
        } else {
            SourceUtil.copy(this,source);
        }
    }

    // ---------------------------------------------------- InspectableSource
    
    /**
     * Returns a property from a source.
     *
     * @param namespace Namespace of the property
     * @param name Name of the property
     *
     * @return Property of the source.
     *
     * @throws SourceException If an exception occurs.
     */
    public SourceProperty getSourceProperty(String namespace, String name) 
        throws SourceException {

        if (m_descriptor == null) {
            return null;
        }

        final String quote = "\"";
        NodeProperty property = m_descriptor.getProperty(name, namespace);

        if (property == null) {
            return null;
        }

        String pre = "<"+name+" xmlns="+quote+namespace+quote+" >";
        String post = "</"+name+" >";

        StringReader reader = new StringReader(pre+property.getValue().toString()+post);
        InputSource src = new InputSource(reader);

        DOMParser parser = null;
        Document doc = null;

        try {
            parser = (DOMParser) this.m_manager.lookup(DOMParser.ROLE);
            doc = parser.parseDocument(src);
        } catch (Exception e) {
            throw new SourceException("Could not parse property", e);
        } finally {
            this.m_manager.release(parser);
        }

        return new SourceProperty(doc.getDocumentElement());
    }
    
    /**
     * Sets a property for a source.
     *
     * @param sourceproperty Property of the source
     *
     * @throws SourceException If an exception occurs during this operation
     */
    public void setSourceProperty(SourceProperty sourceproperty)
      throws SourceException {
        getLogger().debug("Set source property");
        try {
            m_descriptor.setProperty(sourceproperty.getName(),
                                           sourceproperty.getNamespace(),
                                           sourceproperty.getValueAsString());

            // Last modification date
            m_descriptor.setLastModified(new Date());

            m_nat.begin();
            m_content.store(m_slideToken,m_uri,m_descriptor, null);
            m_nat.commit();
        } catch (Exception se) {
            try {
                m_nat.rollback();
            } catch (Exception rbe) {
                getLogger().error("Rollback failed for setting a source property", rbe);
            }
            throw new SourceException("Could not set source property", se);
        }
    }

    /**
     * Returns a enumeration of the properties
     *
     * @return Enumeration of SourceProperty
     *
     * @throws SourceException If an exception occurs.
     */
    public SourceProperty[] getSourceProperties() throws SourceException {

        if (m_descriptor == null) {
            return new SourceProperty[0];
        }

        List properties = new ArrayList();
        DOMParser parser = null;
        String xml = "";

        try {
            parser = (DOMParser) m_manager.lookup(DOMParser.ROLE);
            final String quote = "\"";
            Enumeration e = m_descriptor.enumerateProperties();
            while (e.hasMoreElements()) {
                NodeProperty property = (NodeProperty) e.nextElement();
                String name = property.getName();
                String namespace = property.getNamespace();
                String pre = "<"+name+" xmlns="+quote+namespace+quote+" >";
                String post = "</"+name+" >";
                xml = pre+property.getValue().toString()+post;
                
                StringReader reader = new StringReader(xml);
                Document doc = parser.parseDocument(new InputSource(reader));
                properties.add(new SourceProperty(doc.getDocumentElement()));
            }
        } catch (Exception e) {
            throw new SourceException("Could not parse property "+xml, e);
        } finally {
            m_manager.release(parser);
        }

        return (SourceProperty[]) properties.toArray(new SourceProperty[properties.size()]);
    }

    /**
     * Remove a specified source property.
     *
     * @param namespace Namespace of the property.
     * @param name Name of the property.
     *
     * @throws SourceException If an exception occurs.
     */
    public void removeSourceProperty(String namespace, String name) throws SourceException {
        try {
            if (m_descriptor != null && !namespace.equals("DAV:")) {
                m_descriptor.removeProperty(name, namespace);
                m_descriptor.setLastModified(new Date());
                m_nat.begin();
                m_content.store(m_slideToken,m_uri,m_descriptor,null);
                m_nat.commit();
            }
        } catch (Exception se) {
            try {
                m_nat.rollback();
            } catch (Exception rbe) {
                getLogger().error("Rollback failed for removing a source property", rbe);
            }
            throw new SourceException("Could not remove property", se);
        }
    }

    // ---------------------------------------------------- RestrictableSource
    
    /**
     * Get the current credential for the source
     *
     * @return Return the current used credential;
     *
     * @throws SourceException If an exception occurs.
     */
    public SourceCredential getSourceCredential() throws SourceException {
        return m_credential;
    }

    /**
     * Set the credential for the source
     *
     * @param sourcecredential The credential, which should be used.
     *
     * @throws SourceException If an exception occurs.
     */
    public void setSourceCredential(SourceCredential credential) throws SourceException {
        
        if (credential == null 
            || credential.getPrincipal() == null 
            || credential.getPrincipal().length() <= 0) {
            return;
        }
        m_credential = credential;
        m_slideToken = new SlideTokenImpl(new CredentialsToken(m_credential.getPrincipal()));
    }

    /**
     * Add a permission to this source
     *
     * @param sourcepermission Permission, which should be set
     *
     * @throws SourceException If an exception occurs during this operation
     **/
    public void addSourcePermission(SourcePermission permission) throws SourceException {

        String subject = null;

        if (permission instanceof PrincipalSourcePermission) {
            subject = m_config.getUsersPath()+"/"+
                      ((PrincipalSourcePermission) permission).getPrincipal();

            // Test if principal exists
            try {
                ObjectNode objectnode = m_structure.retrieve(m_slideToken,subject);
                
                if (!(objectnode instanceof SubjectNode)) {
                    throw new SourceException("Principal '"+
                                              ((PrincipalSourcePermission) permission).getPrincipal()+
                                              "' doesn't exists");
                }
            } catch (SlideException se) {
                throw new SourceException("Could not retrieve object for principal '"+
                                          ((PrincipalSourcePermission) permission).getPrincipal()+
                                          "'", se);
            }

        } else if (permission instanceof GroupSourcePermission) {
            subject = m_config.getUsersPath()+"/"+((GroupSourcePermission) permission).getGroup();

            // Test if group exists
            try {
                ObjectNode objectnode = m_structure.retrieve(m_slideToken,subject);

                if (!(objectnode instanceof GroupNode)) {
                    throw new SourceException("Group '"+
                                              ((GroupSourcePermission) permission).getGroup()+
                                              "' doesn't exists");
                }
            } catch (SlideException se) {
                throw new SourceException("Could not retrieve object for group '"+
                                          ((GroupSourcePermission) permission).getGroup()+
                                          "'", se);
            }

            subject = "+"+subject; // Additional '+' to expand the group
        } else {
            throw new SourceException("Does't support category of permission");
        }

        boolean negative = permission.isNegative();
        boolean inheritable = permission.isInheritable();

        if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_ALL)) {
            addPermission(subject,"/",negative,inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ)) {
            addPermission(subject, 
                          m_config.getReadObjectAction().getUri(),
                          negative, inheritable);
            addPermission(subject, 
                          m_config.getReadLocksAction().getUri(),
                          negative, inheritable);
            addPermission(subject,
                          m_config.getReadRevisionMetadataAction().getUri(),
                          negative, inheritable);
            addPermission(subject,
                          m_config.getReadRevisionContentAction().getUri(),
                          negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_SOURCE)) {
            addPermission(subject, 
                          m_config.getReadObjectAction().getUri(),
                          negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_LOCKS)) {
            addPermission(subject, 
                          m_config.getReadLocksAction().getUri(),
                          negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_PROPERTY)) {
            addPermission(subject,
                          m_config.getReadRevisionMetadataAction().getUri(),
                          negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_CONTENT)) {
            addPermission(subject,
                          m_config.getReadRevisionContentAction().getUri(),
                          negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_WRITE)) {
            addPermission(subject, 
                          m_config.getCreateObjectAction().getUri(),
                          negative, inheritable);
            addPermission(subject, 
                          m_config.getRemoveObjectAction().getUri(),
                          negative, inheritable);
            addPermission(subject, 
                          m_config.getLockObjectAction().getUri(),
                          negative, inheritable);
            addPermission(subject,
                          m_config.getCreateRevisionMetadataAction().getUri(),
                          negative, inheritable);
            addPermission(subject,
                          m_config.getModifyRevisionMetadataAction().getUri(),
                          negative, inheritable);
            addPermission(subject,
                          m_config.getRemoveRevisionMetadataAction().getUri(),
                          negative, inheritable);
            addPermission(subject,
                          m_config.getCreateRevisionContentAction().getUri(),
                          negative, inheritable);
            addPermission(subject,
                          m_config.getModifyRevisionContentAction().getUri(),
                          negative, inheritable);
            addPermission(subject,
                          m_config.getRemoveRevisionContentAction().getUri(),
                          negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_CREATE_SOURCE)) {
            addPermission(subject, 
                          m_config.getCreateObjectAction().getUri(),
                          negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_REMOVE_SOURCE)) {
            addPermission(subject, 
                          m_config.getRemoveObjectAction().getUri(),
                          negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_LOCK_SOURCE)) {
            addPermission(subject, 
                          m_config.getLockObjectAction().getUri(),
                          negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_CREATE_PROPERTY)) {
            addPermission(subject,
                          m_config.getCreateRevisionMetadataAction().getUri(),
                          negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_MODIFY_PROPERTY)) {
            addPermission(subject,
                          m_config.getModifyRevisionMetadataAction().getUri(),
                          negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_REMOVE_PROPERTY)) {
            addPermission(subject,
                          m_config.getRemoveRevisionMetadataAction().getUri(),
                          negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_CREATE_CONTENT)) {
            addPermission(subject,
                          m_config.getCreateRevisionContentAction().getUri(),
                          negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_MODIFY_CONTENT)) {
            addPermission(subject,
                          m_config.getModifyRevisionContentAction().getUri(),
                          negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_REMOVE_CONTENT)) {
            addPermission(subject,
                          m_config.getRemoveRevisionContentAction().getUri(),
                          negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_ACL)) {
            addPermission(subject,
                          m_config.getReadPermissionsAction().getUri(),
                          negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_WRITE_ACL)) {
            addPermission(subject,
                          m_config.getGrantPermissionAction().getUri(),
                          negative, inheritable);
            addPermission(subject,
                          m_config.getRevokePermissionAction().getUri(),
                          negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_GRANT_PERMISSION)) {
            addPermission(subject,
                          m_config.getGrantPermissionAction().getUri(),
                          negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_REVOKE_PERMISSION)) {
            addPermission(subject,
                          m_config.getRevokePermissionAction().getUri(),
                          negative, inheritable);
        }
    }

    /**
     * Add permission to the list of permissions.
     *
     * @param subject Subject of the permission.
     * @param action Action for the subject.
     * @param negative If the permission, should be allowed or denied.
     * @param inheritable If the permission is inheritable.
     *
     * @throws SourceException If an exception occurs.
     */
    private void addPermission(String subject, String action,
                               boolean negative,
                               boolean inheritable) throws SourceException {
        try {
            NodePermission permission = new NodePermission(
                m_uri,subject,action,inheritable,negative);
            m_nat.begin();
            m_security.grantPermission(m_slideToken, permission);
            m_descriptor.setLastModified(new Date());
            m_content.store(m_slideToken, m_uri,m_descriptor,null);
            m_nat.commit(); 
        } catch (Exception se) {
            try {
                m_nat.rollback();
            } catch (Exception rbe) {
                getLogger().error("Rollback failed for granting permission", rbe);
            }   
            throw new SourceException("Couldn't grant permission", se);
        }
    }

    /**
     * Remove a permission from this source
     *
     * @param sourcepermission Permission, which should be removed
     *
     * @throws SourceException If an exception occurs during this operation
     **/
    public void removeSourcePermission(SourcePermission permission) throws SourceException {

        String subject = null;

        if (permission instanceof PrincipalSourcePermission) {
            subject = m_config.getUsersPath()+"/"+
                     ((PrincipalSourcePermission) permission).getPrincipal();

            // Test if principal exists
            try {
                ObjectNode objectnode = m_structure.retrieve(m_slideToken,subject);

                if (!(objectnode instanceof SubjectNode)) {
                    throw new SourceException("Principal '"+
                                              ((PrincipalSourcePermission) permission).getPrincipal()+
                                              "' doesn't exists");
                }
            } catch (SlideException se) {
                throw new SourceException("Could not retrieve object for principal '"+
                                          ((PrincipalSourcePermission) permission).getPrincipal()+
                                          "'", se);
            }

        } else if (permission instanceof GroupSourcePermission) {
            subject = m_config.getUsersPath()+"/"+
                      ((GroupSourcePermission) permission).getGroup();

            // Test if group exists
            try {
                ObjectNode objectnode = m_structure.retrieve(m_slideToken,subject);

                if ( !(objectnode instanceof GroupNode)) {
                    throw new SourceException("Group '"+
                                              ((GroupSourcePermission) permission).getGroup()+
                                              "' doesn't exists");
                }
            } catch (SlideException se) {
                throw new SourceException("Could not retrieve object for group '"+
                                          ((GroupSourcePermission) permission).getGroup()+
                                          "'", se);
            }

            subject = "+"+subject; // Additional '+' to expand the group
        } else {
            throw new SourceException("Does't support category of permission");
        }

        boolean negative = permission.isNegative();
        boolean inheritable = permission.isInheritable();

        if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_ALL)) {
            removePermission(subject, "/", negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ)) {
            removePermission(subject, m_config.getReadObjectAction().getUri(),
                             negative, inheritable);
            removePermission(subject, m_config.getReadLocksAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             m_config.getReadRevisionMetadataAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             m_config.getReadRevisionContentAction().getUri(),
                             negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_SOURCE)) {
            removePermission(subject, m_config.getReadObjectAction().getUri(),
                             negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_LOCKS)) {
            removePermission(subject, m_config.getReadLocksAction().getUri(),
                             negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_PROPERTY)) {
            removePermission(subject,
                             m_config.getReadRevisionMetadataAction().getUri(),
                             negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_CONTENT)) {
            removePermission(subject,
                             m_config.getReadRevisionContentAction().getUri(),
                             negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_WRITE)) {
            removePermission(subject,
                             m_config.getCreateObjectAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             m_config.getRemoveObjectAction().getUri(),
                             negative, inheritable);
            removePermission(subject, m_config.getLockObjectAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             m_config.getCreateRevisionMetadataAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             m_config.getModifyRevisionMetadataAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             m_config.getRemoveRevisionMetadataAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             m_config.getCreateRevisionContentAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             m_config.getModifyRevisionContentAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             m_config.getRemoveRevisionContentAction().getUri(),
                             negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_CREATE_SOURCE)) {
            removePermission(subject,
                             m_config.getCreateObjectAction().getUri(),
                             negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_REMOVE_SOURCE)) {
            removePermission(subject,
                             m_config.getRemoveObjectAction().getUri(),
                             negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_LOCK_SOURCE)) {
            removePermission(subject, m_config.getLockObjectAction().getUri(),
                             negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_CREATE_PROPERTY)) {
            removePermission(subject,
                             m_config.getCreateRevisionMetadataAction().getUri(),
                             negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_MODIFY_PROPERTY)) {
            removePermission(subject,
                             m_config.getModifyRevisionMetadataAction().getUri(),
                             negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_REMOVE_PROPERTY)) {
            removePermission(subject,
                             m_config.getRemoveRevisionMetadataAction().getUri(),
                             negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_CREATE_CONTENT)) {
            removePermission(subject,
                             m_config.getCreateRevisionContentAction().getUri(),
                             negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_MODIFY_CONTENT)) {
            removePermission(subject,
                             m_config.getModifyRevisionContentAction().getUri(),
                             negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_REMOVE_CONTENT)) {
            removePermission(subject,
                             m_config.getRemoveRevisionContentAction().getUri(),
                             negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_ACL)) {
            removePermission(subject,
                             m_config.getReadPermissionsAction().getUri(),
                             negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_WRITE_ACL)) {
            removePermission(subject,
                             m_config.getGrantPermissionAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             m_config.getRevokePermissionAction().getUri(),
                             negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_GRANT_PERMISSION)) {
            removePermission(subject,
                             m_config.getGrantPermissionAction().getUri(),
                             negative, inheritable);
        } else if (permission.getPrivilege().equals(SourcePermission.PRIVILEGE_REVOKE_PERMISSION)) {
            removePermission(subject,
                             m_config.getRevokePermissionAction().getUri(),
                             negative, inheritable);
        }
    }

    /**
     * Remove a permission from the list of permissions.
     *
     * @param subject Subject of the permission.
     * @param action Action for the subject.
     * @param negative If the permission, should be allowed or denied.
     * @param inheritable If the permission is inheritable.
     *
     * @throws SourceException If an exception occurs.
     */
    private void removePermission(String subject, String action,
                                  boolean negative,
                                  boolean inheritable)
                                    throws SourceException {
        try {
            NodePermission permission = new NodePermission(m_uri, subject, action,
                                                       inheritable, negative);

            m_nat.begin();
            this.m_security.revokePermission(this.m_slideToken, permission);

            // Last modification date
            m_descriptor.setLastModified(new Date());

            m_content.store(m_slideToken, m_uri,
                          m_descriptor, null);
            m_nat.commit();
        } catch (Exception se) {
            try {
                m_nat.rollback();
            } catch (Exception rbe) {
                getLogger().error("Rollback failed for removing permission", rbe);
            }
            throw new SourceException("Couldn't remove permission", se);
        }
    }

    /**
     * Returns a list of the existing permissions.
     * Based on the implementation of org.apache.slide.webdav.util.PropertyHelper .
     *
     * @return Array of SourcePermission
     *
     * @throws SourceException If an exception occurs.
     **/
    public SourcePermission[] getSourcePermissions() throws SourceException {
        try {

            ObjectNode current = m_node;
            m_security.checkCredentials(m_slideToken, current, m_config.getReadPermissionsAction());

            String userspath = m_config.getUsersPath();

            // read
            String readObjectUri = m_config.getReadObjectAction().getUri();
            String readRevisionMetadataUri = m_config.getReadRevisionMetadataAction().getUri();
            String readRevisionContentUri = m_config.getReadRevisionContentAction().getUri();

            // write
            String createObjectUri = m_config.getCreateObjectAction().getUri();
            String removeObjectUri = m_config.getRemoveObjectAction().getUri();
            String lockObjectUri = m_config.getLockObjectAction().getUri();
            String readLocksUri = m_config.getReadLocksAction().getUri();
            String createRevisionMetadataUri = m_config.getCreateRevisionMetadataAction().getUri();
            String modifyRevisionMetadataUri = m_config.getModifyRevisionMetadataAction().getUri();
            String removeRevisionMetadataUri = m_config.getRemoveRevisionMetadataAction().getUri();
            String createRevisionContentUri = m_config.getCreateRevisionContentAction().getUri();
            String modifyRevisionContentUri = m_config.getModifyRevisionContentAction().getUri();
            String removeRevisionContentUri = m_config.getRemoveRevisionContentAction().getUri();

            // read-acl
            String readPermissionsUri = m_config.getReadPermissionsAction().getUri();

            // write-acl
            String grantPermissionUri = m_config.getGrantPermissionAction().getUri();
            String revokePermissionUri = m_config.getRevokePermissionAction().getUri();

            boolean inheritedPermissions = false;
            
            Vector permissions = new Vector();
            ArrayList sourcepermissions = new ArrayList();

            while (current!=null) {
                try {
                    // put all permissions in a list
                    permissions.clear();
                    Enumeration aclList = m_security.enumeratePermissions(m_slideToken,current);

                    while (aclList.hasMoreElements()) {
                        NodePermission permission = (NodePermission) aclList.nextElement();

                        // if we are processing inheritedPermissions (from parent and up)
                        // then the permission should be inheritable
                        if (inheritedPermissions &&
                            !permission.isInheritable()) {
                            // continue with next permission
                            continue;
                        }
                        permissions.add(permission);
                    }

                    // start combining and writing the permissions
                    while (permissions.size()>0) {
                        NodePermission permission = (NodePermission) permissions.get(0);

                        permissions.remove(0);

                        String principal = permission.getSubjectUri();
                        boolean negative = permission.isNegative();

                        String action = permission.getActionUri();

                        // read
                        boolean isReadObject = readObjectUri.startsWith(action);
                        boolean isReadLocks = readLocksUri.startsWith(action);
                        boolean isReadRevisionMetadata = readRevisionMetadataUri.startsWith(action);
                        boolean isReadRevisionContent = readRevisionContentUri.startsWith(action);

                        // write
                        boolean isCreateObject = createObjectUri.startsWith(action);
                        boolean isRemoveObject = removeObjectUri.startsWith(action);
                        boolean isLockObject = lockObjectUri.startsWith(action);
                        boolean isCreateRevisionMetadata = createRevisionMetadataUri.startsWith(action);
                        boolean isModifyRevisionMetadata = modifyRevisionMetadataUri.startsWith(action);
                        boolean isRemoveRevisionMetadata = removeRevisionMetadataUri.startsWith(action);
                        boolean isCreateRevisionContent = createRevisionContentUri.startsWith(action);
                        boolean isModifyRevisionContent = modifyRevisionContentUri.startsWith(action);
                        boolean isRemoveRevisionContent = removeRevisionContentUri.startsWith(action);

                        // read-acl
                        boolean isReadPermissions = readPermissionsUri.startsWith(action);

                        // write-acl
                        boolean isGrantPermission = grantPermissionUri.startsWith(action);
                        boolean isRevokePermission = revokePermissionUri.startsWith(action);

                        // check the other permissions to combine them
                        // (if they are for the same principal/negative)
                        for (int i = 0; i<permissions.size(); i++) {
                            NodePermission otherPermission = (NodePermission) permissions.get(i);

                            if (principal.equals(otherPermission.getSubjectUri()) &&
                                (negative==otherPermission.isNegative())) {
                                permissions.remove(i);
                                i--; // because we removed the current one

                                action = otherPermission.getActionUri();

                                // read
                                isReadObject |= readObjectUri.startsWith(action);
                                isReadLocks |= readLocksUri.startsWith(action);
                                isReadRevisionMetadata |= readRevisionMetadataUri.startsWith(action);
                                isReadRevisionContent |= readRevisionContentUri.startsWith(action);

                                // write
                                isCreateObject |= createObjectUri.startsWith(action);
                                isRemoveObject |= removeObjectUri.startsWith(action);
                                isLockObject |= lockObjectUri.startsWith(action);
                                isCreateRevisionMetadata |= createRevisionMetadataUri.startsWith(action);
                                isModifyRevisionMetadata |= modifyRevisionMetadataUri.startsWith(action);
                                isRemoveRevisionMetadata |= removeRevisionMetadataUri.startsWith(action);
                                isCreateRevisionContent |= createRevisionContentUri.startsWith(action);
                                isModifyRevisionContent |= modifyRevisionContentUri.startsWith(action);
                                isRemoveRevisionContent |= removeRevisionContentUri.startsWith(action);

                                // read-acl
                                isReadPermissions |= readPermissionsUri.startsWith(action);

                                // write-acl
                                isGrantPermission |= grantPermissionUri.startsWith(action);
                                isRevokePermission |= revokePermissionUri.startsWith(action);
                            }
                        }

                        // WebDAV privileges
                        boolean isRead = isReadObject && isReadLocks &&
                                         isReadRevisionMetadata &&
                                         isReadRevisionContent;

                        boolean isWrite = isCreateObject && isRemoveObject &&
                                          isLockObject &&
                                          isCreateRevisionMetadata &&
                                          isModifyRevisionMetadata &&
                                          isRemoveRevisionMetadata &&
                                          isCreateRevisionContent &&
                                          isModifyRevisionContent &&
                                          isRemoveRevisionContent;

                        boolean isReadAcl = isReadPermissions;

                        boolean isWriteAcl = isGrantPermission &&
                                             isRevokePermission;

                        boolean isAll = isRead && isWrite && isReadAcl &&
                                        isWriteAcl;

                        SourcePermission sourcepermission = null;

                        if (principal.equals("~")) {
                            sourcepermission = new PrincipalSourcePermission(PrincipalSourcePermission.PRINCIPAL_SELF,
                                null, inheritedPermissions, negative);
                        } else if (principal.equals("nobody")) {
                            sourcepermission = new PrincipalSourcePermission(PrincipalSourcePermission.PRINCIPAL_GUEST,
                                null, inheritedPermissions, negative);
                        } else if (principal.equals(userspath)) {
                            sourcepermission = new PrincipalSourcePermission(PrincipalSourcePermission.PRINCIPAL_ALL,
                                null, inheritedPermissions, negative);
                        } else if (principal.startsWith(userspath+"/")) {
                            sourcepermission = new PrincipalSourcePermission(principal.substring(userspath.length()+
                                1), null, inheritedPermissions, negative);
                        } else if (principal.startsWith("+"+userspath+"/")) {
                            sourcepermission = new GroupSourcePermission(principal.substring(userspath.length()+
                                2), null, inheritedPermissions, negative);
                        } else {
                            sourcepermission = new PrincipalSourcePermission(principal,
                                null, inheritedPermissions, negative);
                        }

                        if (isAll) {
                            sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_ALL);
                        } else {
                            if (isRead) {
                                sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_READ);
                            } else {
                                if (isReadObject) {
                                    sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_READ_SOURCE);
                                }
                                if (isReadLocks) {
                                    sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_READ_LOCKS);
                                }
                                if (isReadRevisionMetadata) {
                                    sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_READ_PROPERTY);
                                }
                                if (isReadRevisionContent) {
                                    sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_READ_CONTENT);
                                }
                            }
                            if (isWrite) {
                                sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_WRITE);
                            } else {
                                if (isCreateObject) {
                                    sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_CREATE_SOURCE);
                                }
                                if (isRemoveObject) {
                                    sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_REMOVE_SOURCE);
                                }
                                if (isLockObject) {
                                    sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_LOCK_SOURCE);
                                }
                                if (isCreateRevisionMetadata) {
                                    sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_CREATE_PROPERTY);
                                }
                                if (isModifyRevisionMetadata) {
                                    sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_MODIFY_PROPERTY);
                                }
                                if (isRemoveRevisionMetadata) {
                                    sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_REMOVE_PROPERTY);
                                }
                                if (isCreateRevisionContent) {
                                    sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_CREATE_CONTENT);
                                }
                                if (isModifyRevisionContent) {
                                    sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_MODIFY_CONTENT);
                                }
                                if (isRemoveRevisionContent) {
                                    sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_REMOVE_CONTENT);
                                }
                            }
                            if (isReadAcl) {
                                sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_READ_ACL);
                            }
                            if (isWriteAcl) {
                                sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_WRITE_ACL);
                            } else {
                                if (isGrantPermission) {
                                    sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_GRANT_PERMISSION);
                                }
                                if (isRevokePermission) {
                                    sourcepermission.setPrivilege(SourcePermission.PRIVILEGE_REVOKE_PERMISSION);
                                }
                            }
                        }

                        sourcepermissions.add(sourcepermission);
                    }
                } catch (SlideException se) {
                    throw new SourceException("Exception eccurs while retrieveing source permission",se);
                }

                inheritedPermissions = true;

                try {
                    current = m_structure.getParent(m_slideToken, current);
                } catch (SlideException e) {
                    break;
                }
            }

            return (SourcePermission[]) permissions.toArray(new SourcePermission[permissions.size()]);

        } catch (SlideException se) {
            throw new SourceException("Exception eccurs while retrieveing source permission",se);
        }
    }
    
    // ---------------------------------------------------- LockableSource
    
    /**
     * Add a lock to this source
     *
     * @param sourcelock Lock, which should be added
     *
     * @throws SourceException If an exception occurs during this operation
     */
    public void addSourceLocks(SourceLock sourcelock) throws SourceException {
        throw new SourceException("Operation not yet supported");
    }

    /**
     * Returns a enumeration of the existing locks
     *
     * @return Enumeration of SourceLock
     *
     * @throws SourceException If an exception occurs.
     */
    public Enumeration getSourceLocks() throws SourceException {
        try {
            Vector sourcelocks = new Vector();

            NodeLock lock;
            Enumeration locks = m_lock.enumerateLocks(m_slideToken,m_uri, false);
            while (locks.hasMoreElements()) {
                lock = (NodeLock) locks.nextElement();
                sourcelocks.addElement(new SourceLock(lock.getSubjectUri(),
                                                      lock.getTypeUri(),
                                                      lock.getExpirationDate(),
                                                      lock.isInheritable(),
                                                      lock.isExclusive()));
            }

            return sourcelocks.elements();
        } catch (SlideException se) {
            throw new SourceException("Could not retrieve locks", se);
        }
    }

    // ---------------------------------------------------- VersionableSource
    
    /**
     * If this source versioned
     *
     * @return True if the current source is versioned.
     *
     * @throws SourceException If an exception occurs.
     */
    public boolean isVersioned() throws SourceException {
        return this.m_descriptors.hasRevisions();
    }

    /**
     * Get the current revision of the source
     *
     * @return The current revision of the source
     *
     * @throws SourceException If an exception occurs.
     */
    public String getSourceRevision() {
        if (m_version != null) {
            return m_version.toString();
        }
        return null;
    }

    /**
     * Sets the wanted revision of the source
     *
     * @param sourcerevision The revision, which should be used.
     *
     * @throws SourceException If an exception occurs.
     */
    public void setSourceRevision(String sourcerevision) throws SourceException {
        // [UH] this method is wrong. different versions should be obtained
        // by creating a new source
        throw new SourceException("method not implemented");
    }

    /**
     * Get the current branch of the revision from the source
     *
     * @return The branch of the revision
     *
     * @throws SourceException If an exception occurs.
     */
    public String getSourceRevisionBranch() throws SourceException {
        return m_descriptor.getBranchName();
    }

    /**
     * Sets the wanted branch of the revision from the source
     *
     * @param sourcerevisionbranch The branch, which should be used.
     *
     * @throws SourceException If an exception occurs.
     */
    public void setSourceRevisionBranch(String sourcerevisionbranch) throws SourceException {
        // [UH] this method is wrong. different versions should be obtained
        // by creating a new source
        throw new SourceException("method not implemented");
    }

    /**
     * Get the latest revision
     *
     * @return Last revision of the source.
     *
     * @throws SourceException If an exception occurs.
     */
    public String getLatestSourceRevision() throws SourceException {
        return m_descriptors.getLatestRevision().toString();
    }

}

