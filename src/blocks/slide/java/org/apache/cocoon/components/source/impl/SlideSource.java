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
import org.apache.cocoon.caching.validity.EventValidity;
import org.apache.cocoon.caching.validity.NamedEvent;
import org.apache.cocoon.components.source.InspectableSource;
import org.apache.cocoon.components.source.LockableSource;
import org.apache.cocoon.components.source.VersionableSource;
import org.apache.cocoon.components.source.helpers.SourceLock;
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
 * @version CVS $Id: SlideSource.java,v 1.16 2004/01/13 11:32:54 unico Exp $
 */
public class SlideSource extends AbstractLogEnabled
implements Contextualizable, Serviceable, Initializable, Source, ModifiableTraversableSource, 
           MoveableSource, LockableSource, InspectableSource, 
           VersionableSource {

    /* framework objects */
    private Context m_context;
    private ServiceManager m_manager;
    
    /* Slide access */
    private NamespaceAccessToken m_nat;
    private SlideToken m_slideToken;
    
    /* Slide helpers */
    private Structure m_structure;
    private Content m_content;
    private Lock m_lock;
    private Macro m_macro;

    /* Source specifics */
    private String m_scheme = "slide";
    private String m_path;
    private String m_scope;
    private String m_uri;
    
    private ObjectNode m_node;
    private NodeRevisionNumber m_version;
    private NodeRevisionDescriptors m_descriptors;
    private NodeRevisionDescriptor m_descriptor;
    
    private String m_principal;
    private SourceValidity m_validity;
    private boolean m_useEventCaching;

    private SlideSourceOutputStream m_outputStream;

    /**
     * Create a slide source.
     *
     * @param nat Namespace access token
     * @param scheme Scheme of the source
     * @param path Path of the source.
     */
    public SlideSource(NamespaceAccessToken nat, 
                       String scheme, 
                       String scope,
                       String path,
                       String principal, 
                       String version,
                       boolean useEventCaching) {

        m_nat = nat;
        m_scheme = scheme;
        m_scope = scope;
        m_path = path;
        if (path.equals("/")) {
            m_uri = scope;
        }
        else if (scope.equals("/")){
            m_uri = path;
        }
        else {
            m_uri = scope + path;
        }
        m_principal = principal;
        if (version != null) {
            m_version = new NodeRevisionNumber(version);
        }
        m_useEventCaching = useEventCaching;
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
        
        CredentialsToken credentials = new CredentialsToken(m_principal);
        m_slideToken = new SlideTokenImpl(credentials);
        
        m_structure = m_nat.getStructureHelper();
        m_content = m_nat.getContentHelper();
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
        return m_scheme + "://" + m_principal + "@" + m_nat.getName() + m_path;
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
            if (m_validity == null) {
                if (m_useEventCaching) {
                    m_validity = new EventValidity(
                        new NamedEvent(m_nat.getName() + m_uri));
                }
                else if (m_descriptor != null) {
                    m_validity = new TimeStampValidity(
                        m_descriptor.getLastModifiedAsDate().getTime());
                }
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
        if (!m_useEventCaching) {
            m_validity = null;
        }
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
        SlideSource child = new SlideSource(m_nat,m_scheme,m_scope,path,m_principal,null,m_useEventCaching);
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
        SlideSource parent = new SlideSource(m_nat,m_scheme,m_scope,parentPath,m_principal,null,m_useEventCaching);
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
     * @param property Property of the source
     *
     * @throws SourceException If an exception occurs during this operation
     */
    public void setSourceProperty(SourceProperty property)
      throws SourceException {
        try {
            m_descriptor.setProperty(property.getName(),
                                     property.getNamespace(),
                                     property.getValueAsString());
            m_descriptor.setLastModified(new Date());

            m_nat.begin();
            m_content.store(m_slideToken,m_uri,m_descriptor,null);
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
    public SourceLock[] getSourceLocks() throws SourceException {
        try {
            List result = new ArrayList();

            NodeLock lock;
            Enumeration locks = m_lock.enumerateLocks(m_slideToken,m_uri, false);
            while (locks.hasMoreElements()) {
                lock = (NodeLock) locks.nextElement();
                result.add(new SourceLock(lock.getSubjectUri(),
                                          lock.getTypeUri(),
                                          lock.getExpirationDate(),
                                          lock.isInheritable(),
                                          lock.isExclusive()));
            }

            return (SourceLock[]) result.toArray(new SourceLock[result.size()]);
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
        if (m_descriptors != null) {
            return m_descriptors.hasRevisions();
        }
        return false;
    }

    /**
     * Get the current revision of the source
     *
     * @return The current revision of the source
     *
     */
    public String getSourceRevision() {
        if (m_version != null) {
            return m_version.toString();
        }
        return null;
    }

    /**
     * Not implemented.
     * 
     * @param revision The revision, which should be used.
     *
     * @throws SourceException If an exception occurs.
     */
    public void setSourceRevision(String revision) throws SourceException {
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
        if (m_descriptor != null) { 
            return m_descriptor.getBranchName();
        }
        return null;
    }

    /**
     * Not implemented.
     * 
     * @param branch The branch, which should be used.
     *
     * @throws SourceException If an exception occurs.
     */
    public void setSourceRevisionBranch(String branch) throws SourceException {
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
        if (m_descriptors != null) {
            return m_descriptors.getLatestRevision().toString();
        }
        return null;
    }

}

