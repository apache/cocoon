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

package org.apache.cocoon.components.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.cocoon.components.source.helpers.SourceLock;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.excalibur.source.ModifiableTraversableSource;
import org.apache.excalibur.source.MoveableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.TraversableSource;

/**
 * Data transfer object for a Source object.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: SourceDTO.java,v 1.2 2004/04/13 15:11:12 unico Exp $
 */
public class SourceDTO implements Source, ModifiableTraversableSource, 
           MoveableSource, LockableSource, InspectableSource, 
           VersionableSource {

    private String uri;
    private String scheme;
    private SourceValidity validity;
    private String mimetype;
    private boolean exists;
    private long contentlength;
    private long lastmodified;
    private ArrayList children = new ArrayList();
    private String name;
    private SourceDTO parent;
    private boolean iscollection;
    private SourceProperty[] properties;
    private boolean isversioned;
    private String revision;
    private String revisionbranch;
    private String lastrevision;

    /**
     * Create a data transfer object for a Source.
     *
     * @param source Source
     */
    public SourceDTO(Source source) {
        this(source, true);
    }

    /**
     * Create a data transfer object for a Source.
     *
     * @param source Source
     */
    public SourceDTO(Source source, boolean deep) {
        uri = source.getURI();
        scheme = source.getScheme();
        exists = source.exists();
        if (exists) {
            validity = source.getValidity();
            mimetype = source.getMimeType();
            contentlength = source.getContentLength();
            lastmodified = source.getLastModified();

            if (source instanceof TraversableSource) {
                TraversableSource traversablesource = (TraversableSource) source;
  
                iscollection = traversablesource.isCollection();

                name = traversablesource.getName();

                try {
                    if (iscollection && deep) 
                        for(Iterator i = traversablesource.getChildren().iterator(); i.hasNext(); )
                            children.add(new SourceDTO((Source)i.next(), false));
                } catch (SourceException se) {}

                try {
                    if (deep && (traversablesource.getParent()!=null))
                        parent = new SourceDTO(traversablesource.getParent(), false);
                } catch (SourceException se) {}
            }

            if (source instanceof InspectableSource) {
                InspectableSource inspectablesource = (InspectableSource) source;

                try {
                    properties = inspectablesource.getSourceProperties();
                } catch (SourceException se) {}
            }

            if (source instanceof VersionableSource) {
                VersionableSource versionablesource = (VersionableSource) source;

                try {
                    isversioned = versionablesource.isVersioned();
    
                    if (isversioned) {
                        revision = versionablesource.getSourceRevision();
                        revisionbranch = versionablesource.getSourceRevisionBranch();
                        lastrevision = versionablesource.getLatestSourceRevision();
                    }
                } catch (SourceException se) {}
            }
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
        throw new IllegalStateException("Data transfer object does not support this operation");
    }

    /**
     * Return the unique identifer for this source
     *
     * @return System identifier for the source.
     */
    public String getURI() {
        return uri;
    }

    /**
     * @see org.apache.excalibur.source.Source#getScheme()
     *
     * @return Scheme of the source.
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Return the authority of a URI. This authority is
     * typically defined by an Internet-based server or a scheme-specific
     * registry of naming authorities
     * (see <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>).
     *
     * @return Scheme of the URI.
     */
    public String getAuthority() {
        return SourceUtil.getAuthority(uri);
    }

    /**
     * Return the path of a URI. The path contains data, specific to the
     * authority (or the scheme if there is no authority component),
     * identifying the resource within the scope of that scheme and authority
     * (see <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>).
     *
     * @return Path of the URI.
     */
    public String getPath() {
        return SourceUtil.getPath(uri);
    }

    /**
     * Return the query of a URI. The query is a string of information to
     * be interpreted by the resource
     * (see <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>).
     *
     * @return Query of the URI.
     */
    public String getQuery() {
        return SourceUtil.getQuery(uri);
    }

    /**
     * Return the fragment of a URI. When a URI reference is used to perform
     * a retrieval action on the identified resource, the optional fragment
     * identifier, consists of additional reference information to be
     * interpreted by the user agent after the retrieval action has been
     * successfully completed
     * (see <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>).
     *
     * @return Fragment of the URI.
     */
    public String getFragment() {
        return SourceUtil.getFragment(uri);
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
        return validity;
    }

    /**
     * Refresh the content of this object after the underlying data
     * content has changed.
     */
    public void refresh() {
        throw new IllegalStateException("Data transfer object does not support this operation");
    }

    /**
     * The mime-type of the content described by this object.
     * If the source is not able to determine the mime-type by itself
     * this can be null.
     *
     * @return Mime type of the source.
     */
    public String getMimeType() {
        return mimetype;
    }

    /**
     * Does this source actually exist ?
     *
     * @return true if the resource exists.
     */
    public boolean exists() {
        return exists;
    }

    /**
     * Return the content length of the content or -1 if the length is
     * unknown.
     *
     * @return Content length of the source.
     */
    public long getContentLength() {
        return contentlength;
    }

    /**
     * Get the last modification date of the source or 0 if it
     * is not possible to determine the date.
     *
     * @return Last modified date of the source.
     */
    public long getLastModified() {
        return lastmodified;
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
        throw new IllegalStateException("Data transfer object does not support this operation");
    }

    /**
     * Can the data sent to an <code>OutputStream</code> returned by
     * {@link #getOutputStream()} be cancelled ?
     *
     * @param stream The ouput stream, which should be cancelled.
     * @return true if the stream can be cancelled
     */
    public boolean canCancel(OutputStream stream) {
        throw new IllegalStateException("Data transfer object does not support this operation");
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
        throw new IllegalStateException("Data transfer object does not support this operation");
    }
    
    /**
     * Delete the source.
     */
    public void delete() {
        throw new IllegalStateException("Data transfer object does not support this operation");
    }
    
    public void makeCollection() throws SourceException {
        throw new IllegalStateException("Data transfer object does not support this operation");
    }
    
    public Source getChild(String name) throws SourceException {
        throw new IllegalStateException("Data transfer object does not support this operation");
    }
    
    private Source getChildByPath(String path) throws SourceException {
        throw new IllegalStateException("Data transfer object does not support this operation");
    }

    public Collection getChildren() throws SourceException {
        return children;
    }
    
    public String getName() {
        return name;
    }
    
    public Source getParent() throws SourceException {
        return parent;

    }
    
    public boolean isCollection() {
        //System.out.println("uri="+uri+" isCollection="+iscollection);
        return iscollection;
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
        throw new IllegalStateException("Data transfer object does not support this operation");
    }

    /**
     * Copy the current source to a specified destination.
     *
     * @param source
     *
     * @throws SourceException If an exception occurs during the copy.
     */
    public void copyTo(Source source) throws SourceException {
        throw new IllegalStateException("Data transfer object does not support this operation");
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
        for (int i = 0; i<properties.length; i++)
            if (properties[i].getNamespace().equals(namespace) &&
                properties[i].getName().equals(name))
                return properties[i];
        return null;
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
        throw new IllegalStateException("Data transfer object does not support this operation");
    }

    /**
     * Returns a enumeration of the properties
     *
     * @return Enumeration of SourceProperty
     *
     * @throws SourceException If an exception occurs.
     */
    public SourceProperty[] getSourceProperties() throws SourceException {
        //System.out.println("getProperties()");
        //for(int i=0; i<properties.length; i++)
        //  System.out.println(i+". namespace="+properties[i].getNamespace()+" name="+properties[i].getName()+" value="+properties[i].getValue());
        return properties;
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
        throw new IllegalStateException("Data transfer object does not support this operation");
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
        throw new IllegalStateException("Data transfer object does not support this operation");
    }

    /**
     * Returns a enumeration of the existing locks
     *
     * @return Enumeration of SourceLock
     *
     * @throws SourceException If an exception occurs.
     */
    public SourceLock[] getSourceLocks() throws SourceException {
        throw new IllegalStateException("Data transfer object does not support this operation");
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
        return isversioned;
    }

    /**
     * Get the current revision of the source
     *
     * @return The current revision of the source
     *
     */
    public String getSourceRevision() {
        return revision;
    }

    /**
     * Not implemented.
     * 
     * @param revision The revision, which should be used.
     *
     * @throws SourceException If an exception occurs.
     */
    public void setSourceRevision(String revision) throws SourceException {
        throw new IllegalStateException("Data transfer object does not support this operation");
    }

    /**
     * Get the current branch of the revision from the source
     * 
     * @return The branch of the revision
     *
     * @throws SourceException If an exception occurs.
     */
    public String getSourceRevisionBranch() throws SourceException {
        return revisionbranch;
    }

    /**
     * Not implemented.
     * 
     * @param branch The branch, which should be used.
     *
     * @throws SourceException If an exception occurs.
     */
    public void setSourceRevisionBranch(String branch) throws SourceException {
        throw new IllegalStateException("Data transfer object does not support this operation");
    }

    /**
     * Get the latest revision
     *
     * @return Last revision of the source.
     *
     * @throws SourceException If an exception occurs.
     */
    public String getLatestSourceRevision() throws SourceException {
        return lastrevision;
    }

}

