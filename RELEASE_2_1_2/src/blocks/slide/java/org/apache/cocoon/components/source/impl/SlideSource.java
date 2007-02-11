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
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.source.*;
import org.apache.cocoon.components.source.helpers.GroupSourcePermission;
import org.apache.cocoon.components.source.helpers.PrincipalSourcePermission;
import org.apache.cocoon.components.source.helpers.SourceCredential;
import org.apache.cocoon.components.source.helpers.SourceLock;
import org.apache.cocoon.components.source.helpers.SourcePermission;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.MoveableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.TimeStampValidity;
import org.apache.excalibur.xml.dom.DOMParser;
import org.apache.slide.authenticate.CredentialsToken;
import org.apache.slide.common.NamespaceAccessToken;
import org.apache.slide.common.NamespaceConfig;
import org.apache.slide.common.SlideException;
import org.apache.slide.common.SlideToken;
import org.apache.slide.common.SlideTokenImpl;
import org.apache.slide.content.*;
import org.apache.slide.lock.Lock;
import org.apache.slide.lock.NodeLock;
import org.apache.slide.macro.Macro;
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
 * @version CVS $Id: SlideSource.java,v 1.8 2003/09/24 22:34:53 cziegeler Exp $
 */
public class SlideSource extends AbstractLogEnabled
  implements Contextualizable, Composable, Source, ModifiableSource,
             ModifiableTraversableSource, MoveableSource, RestrictableSource,
             LockableSource, InspectableSource, VersionableSource {

    /** Component context */
    protected Context context;

    /** Component manager */
    private ComponentManager manager;

    /** Namespace access token. */
    protected NamespaceAccessToken nat;

    /** Configuration of namespace */
    protected NamespaceConfig config;

    /** Structure helper. */
    protected Structure structure;

    /** Content helper. */
    protected Content content;

    /** Security helper. */
    private Security security;

    /** Lock helper. */
    private Lock lock;

    /** Macro helper. */
    private Macro macro;

    private CredentialsToken credToken;

    /** Slide token. */
    protected SlideToken slideToken;

    /** Pseudo scheme */
    private String scheme = "slide";

    /** The path of the source, which means the URI without the scheme */
    protected String path;

    /** Uniform resource ifdentifier */
    private String uri;

    /** Revision number */
    protected NodeRevisionNumber revisionNumber;

    private NodeRevisionDescriptors revisionDescriptors;
    protected NodeRevisionDescriptor revisionDescriptor;

    // private String branch;

    private SourceCredential sourcecredential = new SourceCredential("guest",
                                                    "guest");
    private String sourcerevision = null;
    private String sourcerevisionbranch = null;
    private SourceValidity validity = null;

    private SlideSourceOutputStream outputstream;

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
    public SlideSource(NamespaceAccessToken nat, String scheme, String path,
                       SourceCredential sourcecredential,
                       String sourcerevision,
                       String sourcerevisionbranch) throws SourceException {

        this.sourcecredential = sourcecredential;

        this.credToken = new CredentialsToken(this.sourcecredential.getPrincipal());
        this.nat = nat;
        this.config = this.nat.getNamespaceConfig();
        this.scheme = scheme;
        this.path = path;
        this.uri = scheme+":/"+path;

        this.sourcerevision = sourcerevision;
        this.sourcerevisionbranch = sourcerevisionbranch;

        this.structure = nat.getStructureHelper();
        this.content = nat.getContentHelper();
        this.security = nat.getSecurityHelper();
        this.lock = nat.getLockHelper();
        this.macro = nat.getMacroHelper();

        this.slideToken = new SlideTokenImpl(credToken);

        try {
            this.revisionDescriptors = content.retrieve(this.slideToken,
                                                        this.config.getFilesPath()+
                                                        this.path);

            // Retrieve latest revision descriptor
            this.revisionDescriptor = content.retrieve(slideToken,
                                                       revisionDescriptors);

            this.sourcerevision = this.revisionDescriptor.getRevisionNumber().toString();
            this.sourcerevisionbranch = this.revisionDescriptor.getBranchName();

        } catch (RevisionDescriptorNotFoundException rdnfe) {

            // getLogger().warn("Could not retrieve revision descriptor", rdnfe);

            this.revisionDescriptor = null;
            this.sourcerevision = null;
            this.sourcerevisionbranch = null;
        } catch (ObjectNotFoundException onfe) {
            // getLogger().debug("Source doesn't exist", onfe);
            // ignore
        } catch (SlideException se) {
            throw new SourceException("Access denied for source '"+this.uri+
                                      "'", se);
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
        this.context = context;
    }

    /**
     * Pass the ComponentManager to the composer. The Composable implementation
     * should use the specified ComponentManager to acquire the components it needs for execution
     *
     * @param manager The ComponentManager which this Composable uses
     *
     * @throws ComponentException
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
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
            return content.retrieve(slideToken, this.revisionDescriptors,
                                    this.revisionDescriptor).streamContent();
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
        return this.uri;
    }

    /**
     * @see org.apache.excalibur.source.Source#getScheme()
     *
     * @return Scheme of the source.
     */
    public String getScheme() {
        return this.scheme;
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
            if ((this.validity==null) && (this.revisionDescriptor!=null)) {
                this.validity = new TimeStampValidity(this.revisionDescriptor.getLastModifiedAsDate().getTime());
            }
        } catch (Exception e) {
            getLogger().debug("Could not create SourceValidity", e);

            return null;
        }
        return this.validity;
    }

    /**
     * Refresh the content of this object after the underlying data
     * content has changed.
     */
    public void refresh() {

        this.validity = null;
    }

    /**
     * The mime-type of the content described by this object.
     * If the source is not able to determine the mime-type by itself
     * this can be null.
     *
     * @return Mime type of the source.
     */
    public String getMimeType() {
        if (this.revisionDescriptor!=null) {
            return this.revisionDescriptor.getContentType();
        }
        return null;
    }

    /**
     * Does this source actually exist ?
     *
     * @return true if the resource exists.
     */
    public boolean exists() {
        try {
            structure.retrieve(this.slideToken,
                               this.config.getFilesPath()+this.path);
        } catch (SlideException e) {
            return false;
        }
        return true;
    }

    /**
     * Get an <code>InputStream</code> where raw bytes can be written to.
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
        if (outputstream==null) {
            outputstream = new SlideSourceOutputStream();
            outputstream.enableLogging(getLogger());
        }
        return outputstream;
    }

    /**
     * Can the data sent to an <code>OutputStream</code> returned by
     * {@link #getOutputStream()} be cancelled ?
     *
     * @param stream The ouput stream, which should be cancelled.
     * @return true if the stream can be cancelled
     */
    public boolean canCancel(OutputStream stream) {
        return outputstream.canCancel();
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
        if (outputstream==stream) {
            try {
                outputstream.cancel();
            } catch (Exception e) {
                throw new SourceException("Could not cancel output stream",
                                          e);
            }
        }
    }

    /**
     * A helper can the getOutputStream() method
     */
    public class SlideSourceOutputStream extends ByteArrayOutputStream
      implements LogEnabled {
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
                NodeRevisionContent revisionContent = new NodeRevisionContent();

                bytes = toByteArray();
                revisionContent.setContent(bytes);

                if (revisionDescriptor==null) {
                    revisionDescriptor = new NodeRevisionDescriptor(0);

                    String resourceName = config.getFilesPath()+path;
                    int lastSlash = resourceName.lastIndexOf('/');

                    if (lastSlash!=-1) {
                        resourceName = resourceName.substring(lastSlash+1);
                    }
                    revisionDescriptor.setName(resourceName);
                }

                revisionDescriptor.setContentLength(bytes.length);

                // Last modification date
                revisionDescriptor.setLastModified(new Date());

                nat.begin();
                if (revisionNumber==null) {
                    content.create(slideToken, config.getFilesPath()+path,
                                   revisionDescriptor, null);
                }
                content.store(slideToken, config.getFilesPath()+path,
                              revisionDescriptor, revisionContent);
                try {
                    nat.commit();
                } catch (Exception cme) {
                    throw new CascadingIOException("Could not commit the transaction",
                                                   cme);
                }

            } catch (ObjectNotFoundException e) {

                // Todo : Check to see if parent exists
                SubjectNode subject = new SubjectNode();

                try {
                    // Creating an object
                    structure.create(slideToken, subject,
                                     config.getFilesPath()+path);
                } catch (SlideException se) {
                    throw new CascadingIOException(se);
                }

                NodeRevisionDescriptor revisionDescriptor = new NodeRevisionDescriptor(bytes.length);

                // Resource type
                revisionDescriptor.setResourceType("");

                // Source
                revisionDescriptor.setSource("");

                // Get content language
                revisionDescriptor.setContentLanguage("en");

                // Get content length
                revisionDescriptor.setContentLength(bytes.length);

                // Get content type
                String contentType = null;

                try {
                    contentType = ((org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT)).getMimeType(path);
                } catch (ContextException ce) {
                    this.logger.warn("Could not get context to determine the mime type.");
                }
                if (contentType==null) {
                    contentType = "application/octet-stream";
                }
                revisionDescriptor.setContentType(contentType);

                // Last modification date
                revisionDescriptor.setLastModified(new Date());

                // Owner
                revisionDescriptor.setOwner(slideToken.getCredentialsToken().getPublicCredentials());

                // Creating revisionDescriptor associated with the object
                NodeRevisionContent revisionContent = new NodeRevisionContent();

                revisionContent.setContent(bytes);

                try {
                    content.create(slideToken, config.getFilesPath()+path,
                                   revisionDescriptor, revisionContent);

                    try {
                        nat.commit();
                    } catch (Exception cme) {
                        throw new CascadingIOException("Could not commit the transaction",
                                                       cme);

                    }
                } catch (SlideException se) {

                    try {
                        nat.rollback();
                    } catch (Exception rbe) {
                        this.logger.warn("Could not rollback the transaction.",
                                         rbe);
                    }

                    throw new CascadingIOException("Could not create source",
                                                   se);
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
        public boolean canCancel() {
            return !this.isClosed;
        }

        /**
         * Cancel the data sent to an <code>OutputStream</code> returned by
         * {@link #getOutputStream()}.
         * <p>
         * After cancel, the stream should no more be used.
         *
         */
        public void cancel() throws Exception {
            if (this.isClosed) {
                throw new IllegalStateException("Cannot cancel : outputstrem is already closed");
            }

            this.isClosed = true;
            super.close();
        }
    }

    /**
     * Move the current source to a specified destination.
     *
     * @param source
     *
     * @throws SourceException If an exception occurs during
     *                         the move.
     */
    public void moveTo(Source source) throws SourceException {
        if (source instanceof SlideSource) {
            try {
                nat.begin();
                this.macro.move(slideToken,
                                this.config.getFilesPath()+this.path,
                                this.config.getFilesPath()+
                                ((SlideSource) source).path);
                nat.commit();
            } catch (Exception se) {
                try {
                    nat.rollback();
                } catch (Exception rbe) {
                    getLogger().error("Rollback failed for moving source", rbe);
                }
                throw new SourceException("Could not move source.", se);
            }
        } else {
            org.apache.excalibur.source.SourceUtil.move(this, source);
        }
    }

    /**
     * Copy the current source to a specified destination.
     *
     * @param source
     *
     * @throws SourceException If an exception occurs during
     *                         the copy.
     */
    public void copyTo(Source source) throws SourceException {
        if (source instanceof SlideSource) {
            try {
                nat.begin();
                this.macro.copy(slideToken,
                                this.config.getFilesPath()+this.path,
                                this.config.getFilesPath()+
                                ((SlideSource) source).path);
                nat.commit();
            } catch (Exception se) {
                try {
                    nat.rollback();
                } catch (Exception rbe) {
                    getLogger().error("Rollback failed for moving source", rbe);
                }
                throw new SourceException("Could not move source.", se);
            }
        } else {
            org.apache.excalibur.source.SourceUtil.copy(this, source);
        }
    }

    /**
     * Delete the source.
     *
     * @return True, if the delete operation was successful.
     */
    public void delete() {
        try {
            nat.begin();
            this.macro.delete(slideToken,
                              this.config.getFilesPath()+this.path);
            nat.commit();
        } catch (Exception se) {
            getLogger().error("Could not delete source.",se);
            try {
                nat.rollback();
            } catch (Exception rbe) {
                getLogger().error("Rollback failed for moving source", rbe);
            }
        }
    }

    /**
     * Return the content length of the content or -1 if the length is
     * unknown.
     *
     * @return Content length of the source.
     */
    public long getContentLength() {
        if (revisionDescriptor!=null) {
            return revisionDescriptor.getContentLength();
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
        if (revisionDescriptor!=null) {
            return revisionDescriptor.getLastModifiedAsDate().getTime();
        }
        return 0;
    }

    /**
     * Tests whether a resource is a collection resource.
     *
     * @return true if the descriptor represents a collection, false otherwise
     *
     * @throws SourceException If an exception occurs.
     */
    public boolean isSourceCollection() throws SourceException {

        boolean result = false;

        if (revisionDescriptor==null) {
            return true;
        }

        NodeProperty property = revisionDescriptor.getProperty("resourcetype");

        if ((property!=null) &&
            (property.getValue().equals("<collection/>"))) {
            result = true;
        }

        return result;
    }

    /**
     * Returns the count of child sources.
     *
     * @return Count of child sources.
     *
     * @throws SourceException If an exception occurs.
     */
    public int getChildSourceCount() throws SourceException {
        try {
            int i = 0;

            for (Enumeration children = structure.retrieve(this.slideToken,
                this.config.getFilesPath()+this.path).enumerateChildren();
                children.hasMoreElements(); )
                if (((String) children.nextElement()).startsWith(this.config.getFilesPath())) {
                    i++;
                }
            return i;
        } catch (SlideException se) {
            throw new SourceException("Could not get children", se);
        }
    }

    /**
     * Return the system id of a child source.
     *
     * @param index Index of the child
     *
     * @return System identifier of the child source.
     *
     * @throws SourceException If an exception occurs.
     */
    public String getChildSource(int index) throws SourceException {
        try {
            int i = 0;
            String child;

            for (Enumeration children = structure.retrieve(this.slideToken,
                this.config.getFilesPath()+this.path).enumerateChildren();
                children.hasMoreElements(); ) {
                child = (String) children.nextElement();

                if (child.startsWith(this.config.getFilesPath())) {
                    if (i==index) {
                        return scheme+":/"+
                               child.substring(this.config.getFilesPath().length());
                    }

                    i++;
                }
            }
            return null;
        } catch (SlideException se) {
            throw new SourceException("Could not get children", se);
        }
    }

    /**
     * Return the system if of the parent source. The method should return
     * null if the source hasn't a parent.
     *
     * @return System identifier of the parent source.
     */
    public String getParentSource() {
        if ((this.path==null) || (this.path.length()<=1)) {
            return null;
        }

        if (this.path.endsWith("/")) {
            return scheme+":/"+
                   this.path.substring(0, this.path.substring(0,
                       this.path.length()-1).lastIndexOf("/"));
        }

        return scheme+":/"+this.path.substring(0, this.path.lastIndexOf("/"));
    }

    /**
     * Create a collection of sources.
     *
     * @param collectionname Name of the collectiom, which
     *                       should be created.
     *
     * @throws SourceException if an exception occurs.
     */
    public void createCollection(String collectionname)
      throws SourceException {

        SubjectNode collection = new SubjectNode();
        NodeRevisionDescriptor revisionDescriptor = new NodeRevisionDescriptor(0);

        // Resource type
        revisionDescriptor.setResourceType("<collection/>");

        // Creation date
        revisionDescriptor.setCreationDate(new Date());

        // Last modification date
        revisionDescriptor.setLastModified(new Date());

        // Content length name
        revisionDescriptor.setContentLength(0);

        // Source
        revisionDescriptor.setSource("");

        // Owner
        revisionDescriptor.setOwner(slideToken.getCredentialsToken().getPublicCredentials());

        try {
            nat.begin();
            structure.create(slideToken, collection,
                             this.config.getFilesPath()+this.path+"/"+
                             collectionname);
            content.create(slideToken,
                           this.config.getFilesPath()+this.path+"/"+
                           collectionname, revisionDescriptor, null);
            nat.commit();
        } catch (Exception se) {
            try {
                nat.rollback();
            } catch (Exception rbe) {
                getLogger().error("Rollback failed for creating collection", rbe);
            }
            throw new SourceException("Could not create collection.", se);
        }
    }

    /**
     * Get the current credential for the source
     *
     * @return Return the current used credential;
     *
     * @throws SourceException If an exception occurs.
     */
    public SourceCredential getSourceCredential() throws SourceException {
        return this.sourcecredential;
    }

    /**
     * Set the credential for the source
     *
     * @param sourcecredential The credential, which should be used.
     *
     * @throws SourceException If an exception occurs.
     */
    public void setSourceCredential(SourceCredential sourcecredential)
      throws SourceException {
        if ((sourcecredential==null) ||
            (sourcecredential.getPrincipal()==null) ||
            (sourcecredential.getPrincipal().length()<=0)) {
            return;
        }

        this.sourcecredential = sourcecredential;
        this.credToken = new CredentialsToken(this.sourcecredential.getPrincipal());
        this.slideToken = new SlideTokenImpl(credToken);
    }

    /**
     * Add a permission to this source
     *
     * @param sourcepermission Permission, which should be set
     *
     * @throws SourceException If an exception occurs during this operation
     **/
    public void addSourcePermission(SourcePermission sourcepermission)
      throws SourceException {

        NamespaceConfig config = this.nat.getNamespaceConfig();

        String subject = null;

        if (sourcepermission instanceof PrincipalSourcePermission) {
            subject = config.getUsersPath()+"/"+
                      ((PrincipalSourcePermission) sourcepermission).getPrincipal();

            // Test if principal exists
            try {
                ObjectNode objectnode = structure.retrieve(this.slideToken,
                                                           subject);

                if ( !(objectnode instanceof SubjectNode)) {
                    throw new SourceException("Principal '"+
                                              ((PrincipalSourcePermission) sourcepermission).getPrincipal()+
                                              "' doesn't exists");
                }
            } catch (SlideException se) {
                throw new SourceException("Could not retrieve object for principal '"+
                                          ((PrincipalSourcePermission) sourcepermission).getPrincipal()+
                                          "'", se);
            }

        } else if (sourcepermission instanceof GroupSourcePermission) {
            subject = config.getUsersPath()+"/"+
                      ((GroupSourcePermission) sourcepermission).getGroup();

            // Test if group exists
            try {
                ObjectNode objectnode = structure.retrieve(this.slideToken,
                                                           subject);

                if ( !(objectnode instanceof GroupNode)) {
                    throw new SourceException("Group '"+
                                              ((GroupSourcePermission) sourcepermission).getGroup()+
                                              "' doesn't exists");
                }
            } catch (SlideException se) {
                throw new SourceException("Could not retrieve object for group '"+
                                          ((GroupSourcePermission) sourcepermission).getGroup()+
                                          "'", se);
            }

            subject = "+"+subject; // Additional '+' to expand the group
        } else {
            throw new SourceException("Does't support category of permission");
        }

        boolean negative = sourcepermission.isNegative();
        boolean inheritable = sourcepermission.isInheritable();

        if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_ALL)) {
            addPermission(subject, "/", negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ)) {
            addPermission(subject, config.getReadObjectAction().getUri(),
                          negative, inheritable);
            addPermission(subject, config.getReadLocksAction().getUri(),
                          negative, inheritable);
            addPermission(subject,
                          config.getReadRevisionMetadataAction().getUri(),
                          negative, inheritable);
            addPermission(subject,
                          config.getReadRevisionContentAction().getUri(),
                          negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_SOURCE)) {
            addPermission(subject, config.getReadObjectAction().getUri(),
                          negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_LOCKS)) {
            addPermission(subject, config.getReadLocksAction().getUri(),
                          negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_PROPERTY)) {
            addPermission(subject,
                          config.getReadRevisionMetadataAction().getUri(),
                          negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_CONTENT)) {
            addPermission(subject,
                          config.getReadRevisionContentAction().getUri(),
                          negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_WRITE)) {
            addPermission(subject, config.getCreateObjectAction().getUri(),
                          negative, inheritable);
            addPermission(subject, config.getRemoveObjectAction().getUri(),
                          negative, inheritable);
            addPermission(subject, config.getLockObjectAction().getUri(),
                          negative, inheritable);
            addPermission(subject,
                          config.getCreateRevisionMetadataAction().getUri(),
                          negative, inheritable);
            addPermission(subject,
                          config.getModifyRevisionMetadataAction().getUri(),
                          negative, inheritable);
            addPermission(subject,
                          config.getRemoveRevisionMetadataAction().getUri(),
                          negative, inheritable);
            addPermission(subject,
                          config.getCreateRevisionContentAction().getUri(),
                          negative, inheritable);
            addPermission(subject,
                          config.getModifyRevisionContentAction().getUri(),
                          negative, inheritable);
            addPermission(subject,
                          config.getRemoveRevisionContentAction().getUri(),
                          negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_CREATE_SOURCE)) {
            addPermission(subject, config.getCreateObjectAction().getUri(),
                          negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_REMOVE_SOURCE)) {
            addPermission(subject, config.getRemoveObjectAction().getUri(),
                          negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_LOCK_SOURCE)) {
            addPermission(subject, config.getLockObjectAction().getUri(),
                          negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_CREATE_PROPERTY)) {
            addPermission(subject,
                          config.getCreateRevisionMetadataAction().getUri(),
                          negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_MODIFY_PROPERTY)) {
            addPermission(subject,
                          config.getModifyRevisionMetadataAction().getUri(),
                          negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_REMOVE_PROPERTY)) {
            addPermission(subject,
                          config.getRemoveRevisionMetadataAction().getUri(),
                          negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_CREATE_CONTENT)) {
            addPermission(subject,
                          config.getCreateRevisionContentAction().getUri(),
                          negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_MODIFY_CONTENT)) {
            addPermission(subject,
                          config.getModifyRevisionContentAction().getUri(),
                          negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_REMOVE_CONTENT)) {
            addPermission(subject,
                          config.getRemoveRevisionContentAction().getUri(),
                          negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_ACL)) {
            addPermission(subject,
                          config.getReadPermissionsAction().getUri(),
                          negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_WRITE_ACL)) {
            addPermission(subject,
                          config.getGrantPermissionAction().getUri(),
                          negative, inheritable);
            addPermission(subject,
                          config.getRevokePermissionAction().getUri(),
                          negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_GRANT_PERMISSION)) {
            addPermission(subject,
                          config.getGrantPermissionAction().getUri(),
                          negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_REVOKE_PERMISSION)) {
            addPermission(subject,
                          config.getRevokePermissionAction().getUri(),
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
            NodePermission permission = new NodePermission(this.config.getFilesPath()+
                                            this.path, subject, action,
                                                       inheritable, negative);

            nat.begin();
            this.security.grantPermission(this.slideToken, permission);

            // Last modification date
            revisionDescriptor.setLastModified(new Date());

            content.store(slideToken, this.config.getFilesPath()+this.path,
                          revisionDescriptor, null);
            nat.commit(); 
        } catch (Exception se) {
            try {
                nat.rollback();
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
    public void removeSourcePermission(SourcePermission sourcepermission)
      throws SourceException {

        NamespaceConfig config = this.nat.getNamespaceConfig();

        String subject = null;

        if (sourcepermission instanceof PrincipalSourcePermission) {
            subject = config.getUsersPath()+"/"+
                      ((PrincipalSourcePermission) sourcepermission).getPrincipal();

            // Test if principal exists
            try {
                ObjectNode objectnode = structure.retrieve(this.slideToken,
                                                           subject);

                if ( !(objectnode instanceof SubjectNode)) {
                    throw new SourceException("Principal '"+
                                              ((PrincipalSourcePermission) sourcepermission).getPrincipal()+
                                              "' doesn't exists");
                }
            } catch (SlideException se) {
                throw new SourceException("Could not retrieve object for principal '"+
                                          ((PrincipalSourcePermission) sourcepermission).getPrincipal()+
                                          "'", se);
            }

        } else if (sourcepermission instanceof GroupSourcePermission) {
            subject = config.getUsersPath()+"/"+
                      ((GroupSourcePermission) sourcepermission).getGroup();

            // Test if group exists
            try {
                ObjectNode objectnode = structure.retrieve(this.slideToken,
                                                           subject);

                if ( !(objectnode instanceof GroupNode)) {
                    throw new SourceException("Group '"+
                                              ((GroupSourcePermission) sourcepermission).getGroup()+
                                              "' doesn't exists");
                }
            } catch (SlideException se) {
                throw new SourceException("Could not retrieve object for group '"+
                                          ((GroupSourcePermission) sourcepermission).getGroup()+
                                          "'", se);
            }

            subject = "+"+subject; // Additional '+' to expand the group
        } else {
            throw new SourceException("Does't support category of permission");
        }

        boolean negative = sourcepermission.isNegative();
        boolean inheritable = sourcepermission.isInheritable();

        if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_ALL)) {
            removePermission(subject, "/", negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ)) {
            removePermission(subject, config.getReadObjectAction().getUri(),
                             negative, inheritable);
            removePermission(subject, config.getReadLocksAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             config.getReadRevisionMetadataAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             config.getReadRevisionContentAction().getUri(),
                             negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_SOURCE)) {
            removePermission(subject, config.getReadObjectAction().getUri(),
                             negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_LOCKS)) {
            removePermission(subject, config.getReadLocksAction().getUri(),
                             negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_PROPERTY)) {
            removePermission(subject,
                             config.getReadRevisionMetadataAction().getUri(),
                             negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_CONTENT)) {
            removePermission(subject,
                             config.getReadRevisionContentAction().getUri(),
                             negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_WRITE)) {
            removePermission(subject,
                             config.getCreateObjectAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             config.getRemoveObjectAction().getUri(),
                             negative, inheritable);
            removePermission(subject, config.getLockObjectAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             config.getCreateRevisionMetadataAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             config.getModifyRevisionMetadataAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             config.getRemoveRevisionMetadataAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             config.getCreateRevisionContentAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             config.getModifyRevisionContentAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             config.getRemoveRevisionContentAction().getUri(),
                             negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_CREATE_SOURCE)) {
            removePermission(subject,
                             config.getCreateObjectAction().getUri(),
                             negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_REMOVE_SOURCE)) {
            removePermission(subject,
                             config.getRemoveObjectAction().getUri(),
                             negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_LOCK_SOURCE)) {
            removePermission(subject, config.getLockObjectAction().getUri(),
                             negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_CREATE_PROPERTY)) {
            removePermission(subject,
                             config.getCreateRevisionMetadataAction().getUri(),
                             negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_MODIFY_PROPERTY)) {
            removePermission(subject,
                             config.getModifyRevisionMetadataAction().getUri(),
                             negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_REMOVE_PROPERTY)) {
            removePermission(subject,
                             config.getRemoveRevisionMetadataAction().getUri(),
                             negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_CREATE_CONTENT)) {
            removePermission(subject,
                             config.getCreateRevisionContentAction().getUri(),
                             negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_MODIFY_CONTENT)) {
            removePermission(subject,
                             config.getModifyRevisionContentAction().getUri(),
                             negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_REMOVE_CONTENT)) {
            removePermission(subject,
                             config.getRemoveRevisionContentAction().getUri(),
                             negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_READ_ACL)) {
            removePermission(subject,
                             config.getReadPermissionsAction().getUri(),
                             negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_WRITE_ACL)) {
            removePermission(subject,
                             config.getGrantPermissionAction().getUri(),
                             negative, inheritable);
            removePermission(subject,
                             config.getRevokePermissionAction().getUri(),
                             negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_GRANT_PERMISSION)) {
            removePermission(subject,
                             config.getGrantPermissionAction().getUri(),
                             negative, inheritable);
        } else if (sourcepermission.getPrivilege().equals(SourcePermission.PRIVILEGE_REVOKE_PERMISSION)) {
            removePermission(subject,
                             config.getRevokePermissionAction().getUri(),
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
            NodePermission permission = new NodePermission(this.config.getFilesPath()+
                                            this.path, subject, action,
                                                       inheritable, negative);

            nat.begin();
            this.security.revokePermission(this.slideToken, permission);

            // Last modification date
            revisionDescriptor.setLastModified(new Date());

            content.store(slideToken, this.config.getFilesPath()+this.path,
                          revisionDescriptor, null);
            nat.commit();
        } catch (Exception se) {
            try {
                nat.rollback();
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

            NamespaceConfig config = this.nat.getNamespaceConfig();

            ObjectNode current = structure.retrieve(this.slideToken,
                                                    this.config.getFilesPath()+
                                                    this.path);

            security.checkCredentials(this.slideToken, current,
                                      config.getReadPermissionsAction());

            String userspath = config.getUsersPath();

            // read
            String readObjectUri = config.getReadObjectAction().getUri();
            String readRevisionMetadataUri = config.getReadRevisionMetadataAction().getUri();
            String readRevisionContentUri = config.getReadRevisionContentAction().getUri();

            // write
            String createObjectUri = config.getCreateObjectAction().getUri();
            String removeObjectUri = config.getRemoveObjectAction().getUri();
            String lockObjectUri = config.getLockObjectAction().getUri();
            String readLocksUri = config.getReadLocksAction().getUri();
            String createRevisionMetadataUri = config.getCreateRevisionMetadataAction().getUri();
            String modifyRevisionMetadataUri = config.getModifyRevisionMetadataAction().getUri();
            String removeRevisionMetadataUri = config.getRemoveRevisionMetadataAction().getUri();
            String createRevisionContentUri = config.getCreateRevisionContentAction().getUri();
            String modifyRevisionContentUri = config.getModifyRevisionContentAction().getUri();
            String removeRevisionContentUri = config.getRemoveRevisionContentAction().getUri();

            // read-acl
            String readPermissionsUri = config.getReadPermissionsAction().getUri();

            // write-acl
            String grantPermissionUri = config.getGrantPermissionAction().getUri();
            String revokePermissionUri = config.getRevokePermissionAction().getUri();

            boolean inheritedPermissions = false;
            Vector permissions = new Vector();

            ArrayList sourcepermissions = new ArrayList();

            while (current!=null) {
                try {
                    // put all permissions in a list
                    permissions.clear();
                    Enumeration aclList = security.enumeratePermissions(this.slideToken,
                                              current);

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
                    throw new SourceException("Exception eccurs while retrieveing source permission",
                                              se);
                }

                inheritedPermissions = true;

                try {
                    current = structure.getParent(this.slideToken, current);
                } catch (SlideException e) {
                    break;
                }
            }

            SourcePermission[] sourcepermissionArray = new SourcePermission[sourcepermissions.size()];

            return (SourcePermission[]) sourcepermissions.toArray(sourcepermissionArray);

        } catch (SlideException se) {
            throw new SourceException("Exception eccurs while retrieveing source permission",
                                      se);
        }
    }

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

            for (Enumeration locks = this.lock.enumerateLocks(this.slideToken,
                this.config.getFilesPath()+this.path, false);
                locks.hasMoreElements(); ) {
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
            revisionDescriptor.setProperty(sourceproperty.getName(),
                                           sourceproperty.getNamespace(),
                                           sourceproperty.getValueAsString());

            // Last modification date
            revisionDescriptor.setLastModified(new Date());

            nat.begin();
            content.store(slideToken, this.config.getFilesPath()+this.path,
                          revisionDescriptor, null);
            nat.commit();
        } catch (Exception se) {
            try {
                nat.rollback();
            } catch (Exception rbe) {
                getLogger().error("Rollback failed for setting a source property", rbe);
            }
            throw new SourceException("Could not set source property", se);
        }
    }

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
    public SourceProperty getSourceProperty(String namespace,
                                            String name)
                                              throws SourceException {

        if (revisionDescriptor==null) {
            return null;
        }

        final String quote = "\"";
        NodeProperty property = revisionDescriptor.getProperty(name, namespace);

        if (property==null) {
            return null;
        }

        String pre = "<"+name+" xmlns="+quote+namespace+quote+" >";
        String post = "</"+name+" >";

        StringReader reader = new StringReader(pre+property.getValue().toString()+
                                               post);
        InputSource src = new InputSource(reader);

        DOMParser parser = null;
        Document doc = null;

        try {
            parser = (DOMParser) this.manager.lookup(DOMParser.ROLE);
            doc = parser.parseDocument(src);
        } catch (Exception e) {
            throw new SourceException("Could not parse property", e);
        } finally {
            this.manager.release((Component) parser);
        }

        return new SourceProperty(doc.getDocumentElement());
    }

    /**
     * Returns a enumeration of the properties
     *
     * @return Enumeration of SourceProperty
     *
     * @throws SourceException If an exception occurs.
     */
    public SourceProperty[] getSourceProperties() throws SourceException {

        if (revisionDescriptor==null) {
            return new SourceProperty[0];
        }

        Vector sourceproperties = new Vector();

        DOMParser parser = null;
        String xml = "";

        try {
            parser = (DOMParser) this.manager.lookup(DOMParser.ROLE);
            final String quote = "\"";

            for (Enumeration e = revisionDescriptor.enumerateProperties();
                e.hasMoreElements(); ) {
                NodeProperty property = (NodeProperty) e.nextElement();
                String name = property.getName();
                String namespace = property.getNamespace();
                String pre = "<"+name+" xmlns="+quote+namespace+quote+" >";
                String post = "</"+name+" >";

                xml = pre+property.getValue().toString()+post;
                StringReader reader = new StringReader(xml);

                Document doc = parser.parseDocument(new InputSource(reader));

                SourceProperty srcProperty = new SourceProperty(doc.getDocumentElement());

                sourceproperties.addElement(srcProperty);
            }
        } catch (Exception e) {
            throw new SourceException("Could not parse property "+xml, e);
        } finally {
            this.manager.release((Component) parser);
        }

        SourceProperty[] sourcepropertiesArray = new SourceProperty[sourceproperties.size()];

        for (int i = 0; i<sourceproperties.size(); i++)
            sourcepropertiesArray[i] = (SourceProperty) sourceproperties.elementAt(i);
        return sourcepropertiesArray;
    }

    /**
     * Remove a specified source property.
     *
     * @param namespace Namespace of the property.
     * @param name Name of the property.
     *
     * @throws SourceException If an exception occurs.
     */
    public void removeSourceProperty(String namespace,
                                     String name) throws SourceException {
        try {
            if ((revisionDescriptor!=null) && ( !namespace.equals("DAV:"))) {
                revisionDescriptor.removeProperty(name, namespace);

                // Last modification date
                revisionDescriptor.setLastModified(new Date());

                content.store(slideToken,
                              this.config.getFilesPath()+this.path,
                              revisionDescriptor, null);
            }
        } catch (SlideException se) {
            throw new SourceException("Could not remove property", se);
        }
    }

    /**
     * If this source versioned
     *
     * @return True if the current source is versioned.
     *
     * @throws SourceException If an exception occurs.
     */
    public boolean isVersioned() throws SourceException {
        try {
            this.revisionDescriptors = content.retrieve(this.slideToken,
                                                        this.config.getFilesPath()+
                                                        this.path);

            return this.revisionDescriptors.hasRevisions();

        } catch (SlideException se) {
            throw new SourceException("Could not retrieve revision descriptor",
                                      se);
        }
    }

    /**
     * Get the current revision of the source
     *
     * @return The current revision of the source
     *
     * @throws SourceException If an exception occurs.
     */
    public String getSourceRevision() throws SourceException {
        return this.sourcerevision;
    }

    /**
     * Sets the wanted revision of the source
     *
     * @param sourcerevision The revision, which should be used.
     *
     * @throws SourceException If an exception occurs.
     */
    public void setSourceRevision(String sourcerevision)
      throws SourceException {
        this.sourcerevision = sourcerevision;

        try {
            this.revisionDescriptors = content.retrieve(this.slideToken,
                                                        this.config.getFilesPath()+
                                                        this.path);

            // Retrieve revision descriptor by the revision
            this.revisionDescriptor = content.retrieve(slideToken,
                                                       revisionDescriptors,
                                                       new NodeRevisionNumber(this.sourcerevision));

        } catch (SlideException se) {
            throw new SourceException("Could not retrieve revision descriptor",
                                      se);
        }
    }

    /**
     * Get the current branch of the revision from the source
     *
     * @return The branch of the revision
     *
     * @throws SourceException If an exception occurs.
     */
    public String getSourceRevisionBranch() throws SourceException {
        return this.sourcerevisionbranch;
    }

    /**
     * Sets the wanted branch of the revision from the source
     *
     * @param sourcerevisionbranch The branch, which should be used.
     *
     * @throws SourceException If an exception occurs.
     */
    public void setSourceRevisionBranch(String sourcerevisionbranch)
      throws SourceException {
        this.sourcerevisionbranch = sourcerevisionbranch;

        // FIXME Retrieve the the revsion descriptor with current branch
    }

    /**
     * Get the latest revision
     *
     * @return Last revision of the source.
     *
     * @throws SourceException If an exception occurs.
     */
    public String getLatestSourceRevision() throws SourceException {
        try {
            this.revisionDescriptors = content.retrieve(this.slideToken,
                                                        this.config.getFilesPath()+
                                                        this.path);

            return this.revisionDescriptors.getLatestRevision().toString();

        } catch (SlideException se) {
            throw new SourceException("Could not retrieve revision descriptor",
                                      se);
        }
    }
}

