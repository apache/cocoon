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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.caching.validity.EventValidity;
import org.apache.cocoon.caching.validity.NamedEvent;
import org.apache.cocoon.components.source.InspectableSource;
import org.apache.cocoon.components.source.SourceDescriptor;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.excalibur.source.ModifiableTraversableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AggregatedValidity;

/**
 * Source wrapper that enhances the wrapped sources with additional capabilities.
 * 
 * <p>
 * Currently this Source optionally adds inspectability
 * through the InspectableSource interface and event caching
 * by handing out EventValidities based on the Source uri.
 * </p>
 * 
 * <p>
 * Wrapped sources must implement ModifiableTraversableSource.
 * </p>
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 */
public class RepositorySource extends AbstractLogEnabled 
implements Source, ModifiableTraversableSource, InspectableSource {

    // the original source prefix
    final String m_prefix;
    // the wrapped source
    final ModifiableTraversableSource m_delegate;
    private final SourceDescriptor m_descriptor;
    private final boolean m_useEventCaching;
    
    // ---------------------------------------------------- Lifecycle
    
    public RepositorySource(
        final String prefix,
        final ModifiableTraversableSource delegate, 
        final SourceDescriptor descriptor,
        final Logger logger,
        final boolean useEventCaching) throws SourceException {
        
        m_prefix = prefix;
        m_delegate = delegate;
        m_descriptor = descriptor;
        enableLogging(logger);
        m_useEventCaching = useEventCaching;
    }
    
    // ---------------------------------------------------- InspectableSource implementation
    
    /**
     * Get all source properties that are defined on the wrapped source. 
     * If the wrapped source is itself an InspectableSource the implementation
     * will return the aggregate set that results from combining the properties
     * returned from a delegate call to the wrapped source with the 
     * properties returned by the source descriptor.
     */
    public SourceProperty[] getSourceProperties() throws SourceException {
        
        final List properties = new ArrayList();
        if (m_delegate instanceof InspectableSource) {
            properties.addAll(Arrays.asList(((InspectableSource) m_delegate).getSourceProperties()));
        }
        if (m_descriptor != null) {
            properties.addAll(Arrays.asList(m_descriptor.getSourceProperties(m_delegate)));
        }
        return (SourceProperty[]) properties.toArray(new SourceProperty[properties.size()]);
    }
    
    /**
     * Get the source property on the wrapped source. If the wrapped source implements
     * InspectableSource the implementation will first try to get it from there.
     * If it doesn't exist on the delegate it will try to find it using the source descriptor.
     */
    public SourceProperty getSourceProperty(String uri, String name) throws SourceException {
        SourceProperty property = null;
        if (m_delegate instanceof InspectableSource) {
            property = ((InspectableSource) m_delegate).getSourceProperty(uri,name);
        }
        if (property == null && m_descriptor != null) {
            property = m_descriptor.getSourceProperty(m_delegate,uri,name);
        }
        return property;
    }
    
    /**
     * Remove the source property on the wrapped source. If the wrapped source implements
     * InspectableSource the implementation will try to remove the property on both
     * the wrapped source directly and on the source descriptor.
     */
    public void removeSourceProperty(String uri, String name) throws SourceException {
        if (m_delegate instanceof InspectableSource) {
            ((InspectableSource) m_delegate).removeSourceProperty(uri,name);
        }
        if (m_descriptor != null) {
            m_descriptor.removeSourceProperty(m_delegate,uri,name);
        }
    }
    
    /**
     * Set the source property on the wrapped source. If the wrapped source implements
     * InspectableSource set the property directly on the wrapped source. Otherwise
     * set it on the SourceDescriptor.
     */
    public void setSourceProperty(SourceProperty property) throws SourceException {
        if (m_delegate instanceof InspectableSource) {
            ((InspectableSource) m_delegate).setSourceProperty(property);
        } else if (m_descriptor != null) {
            m_descriptor.setSourceProperty(m_delegate, property);
        }
    }
    
    
    // ---------------------------------------------------- Source implementation
    
    public boolean exists() {
        return m_delegate.exists();
    }
    
    public long getContentLength() {
        return m_delegate.getContentLength();
    }
    
    public InputStream getInputStream()
        throws IOException, SourceNotFoundException {
        return m_delegate.getInputStream();
    }
    
    public long getLastModified() {
        return m_delegate.getLastModified();
    }
    
    public String getMimeType() {
        return m_delegate.getMimeType();
    }
    
    public String getScheme() {
        return m_prefix;
    }
    
    public String getURI() {
        return m_prefix + ":" + m_delegate.getURI();
    }
    
    /**
     * Return a SourceValidity object describing
     * the validity of this Source.
     * <p>
     * If the SourceDescriptor service is present, the resulting
     * validity is an aggregated validity object containing both
     * the validity describing the source itself _and_ one describing
     * the validity of the SourceProperties managed by the SourceDescriptor.
     * </p>
     * When using event caching the SourceValidity describing the Source itself
     * is an EventValidity that contains a NamedEvent which name is the wrapped 
     * Source URI.
     */
    public SourceValidity getValidity() {
        SourceValidity val1;
        if (m_useEventCaching) {
            val1 = new EventValidity(new NamedEvent(getURI()));
        }
        else {
            val1 = m_delegate.getValidity();
        }
        
        if (val1 != null && m_descriptor != null) {
            SourceValidity val2 = m_descriptor.getValidity(m_delegate);
            if (val2 != null) {
                AggregatedValidity result = new AggregatedValidity();
                result.add(val1);
                result.add(val2);
                return result;
            }
        }
        return val1;
    }
    
    public void refresh() {
        m_delegate.refresh();
    }
    
    
    // ---------------------------------------------------- ModifiableTraversableSource
    
    public Source getChild(String name) throws SourceException {
        if (!m_delegate.isCollection()) return null;
        ModifiableTraversableSource child = (ModifiableTraversableSource) m_delegate.getChild(name);
        if (child == null) return null;
        
        return new RepositorySource(
            m_prefix,
            child,
            m_descriptor,
            getLogger(),
            m_useEventCaching
        );
    }

    public Collection getChildren() throws SourceException {
        if (!m_delegate.isCollection()) return null;
    	Collection result = new ArrayList();
		Iterator iter = m_delegate.getChildren().iterator();
    	while(iter.hasNext()) {
            ModifiableTraversableSource child = (ModifiableTraversableSource) iter.next();
            
    		result.add(
                new RepositorySource(
                    m_prefix,
                    child,
                    m_descriptor,
    		        getLogger(),
                    m_useEventCaching
                )
            );
    	}
        return result;
    }

    public String getName() {
        return m_delegate.getName();
    }

    public Source getParent() throws SourceException {
        String eventName = null;
        
        return new RepositorySource(
            m_prefix,
            (ModifiableTraversableSource) m_delegate.getParent(),
        	m_descriptor, 
            getLogger(),
            m_useEventCaching
        );
    }

    public boolean isCollection() {
        return m_delegate.isCollection();
    }

    public void makeCollection() throws SourceException {
        m_delegate.makeCollection();
    }

    public boolean canCancel(OutputStream out) {
        return m_delegate.canCancel(out);
    }

    public void cancel(OutputStream out) throws IOException {
        m_delegate.cancel(out);
    }

    public void delete() throws SourceException {
        m_delegate.delete();
    }

    public OutputStream getOutputStream() throws IOException {
        return m_delegate.getOutputStream();
    }
    
        
    // ---------------------------------------------------- LockableSource implementation
    
//    public void addSourceLocks(SourceLock lock) throws SourceException {
//        if (m_delegate instanceof LockableSource) {
//            ((LockableSource) m_delegate).addSourceLocks(lock);
//        }
//    }
//    
//    public Enumeration getSourceLocks() throws SourceException {
//        if (m_delegate instanceof LockableSource) {
//            return ((LockableSource) m_delegate).getSourceLocks();
//        }
//        return null;
//    }
    
    // ---------------------------------------------------- VersionableSource implementation
    
//    public String getLatestSourceRevision() throws SourceException {
//        if (m_delegate instanceof VersionableSource) {
//            return ((VersionableSource) m_delegate).getLatestSourceRevision();
//        }
//        return null;
//    }
//    
//    public String getSourceRevision() throws SourceException {
//        if (m_delegate instanceof VersionableSource) {
//            return ((VersionableSource) m_delegate).getSourceRevision();
//        }
//        return null;
//    }
//    
//    public String getSourceRevisionBranch() throws SourceException {
//        if (m_delegate instanceof VersionableSource) {
//            return ((VersionableSource) m_delegate).getSourceRevisionBranch();
//        }
//        return null;
//    }
//    
//    public boolean isVersioned() throws SourceException {
//        if (m_delegate instanceof VersionableSource) {
//            return ((VersionableSource) m_delegate).isVersioned();
//        }
//        return false;
//    }
//    
//    public void setSourceRevision(String revision) throws SourceException {
//        if (m_delegate instanceof VersionableSource) {
//            ((VersionableSource) m_delegate).setSourceRevision(revision);
//        }
//    }
//    
//    public void setSourceRevisionBranch(String branch) throws SourceException {
//        if (m_delegate instanceof VersionableSource) {
//            ((VersionableSource) m_delegate).setSourceRevisionBranch(branch);
//        }
//    }
    
    
    // ---------------------------------------------------- RestrictableSource implementation
    
//    public void addSourcePermission(SourcePermission permission)
//        throws SourceException {
//        
//        if (m_delegate instanceof RestrictableSource) {
//            ((RestrictableSource) m_delegate).addSourcePermission(permission);
//        }
//    }
//    
//    public SourceCredential getSourceCredential() throws SourceException {
//        if (m_delegate instanceof RestrictableSource) {
//            return ((RestrictableSource) m_delegate).getSourceCredential();
//        }
//        return null;
//    }
//    
//    public SourcePermission[] getSourcePermissions() throws SourceException {
//        if (m_delegate instanceof RestrictableSource) {
//            return ((RestrictableSource) m_delegate).getSourcePermissions();
//        }
//        return null;
//    }
//    
//    public void removeSourcePermission(SourcePermission permission)
//        throws SourceException {
//        
//        if (m_delegate instanceof RestrictableSource) {
//            ((RestrictableSource) m_delegate).removeSourcePermission(permission);
//        }
//    }
//    
//    public void setSourceCredential(SourceCredential credential)
//        throws SourceException {
//        
//        if (m_delegate instanceof RestrictableSource) {
//            ((RestrictableSource) m_delegate).setSourceCredential(credential);
//        }
//    }
    

}
