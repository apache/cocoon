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
 * through the InspectableSource interface.
 * </p>
 * 
 * <p>
 * Wrapped sources must implement ModifiableTraversableSource.
 * </p>
 */
public class RepositorySource extends AbstractLogEnabled 
implements Source, ModifiableTraversableSource, InspectableSource {

    // the original source prefix
    final String m_prefix;
    // the wrapped source
    final ModifiableTraversableSource m_delegate;
    private final SourceDescriptor m_descriptor;
    
    // ---------------------------------------------------- Lifecycle
    
    public RepositorySource(
        final String prefix,
        final ModifiableTraversableSource delegate, 
        final SourceDescriptor descriptor,
        final Logger logger) throws SourceException {
        
        m_prefix = prefix;
        m_delegate = delegate;
        m_descriptor = descriptor;
        enableLogging(logger);
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
     */
    public SourceValidity getValidity() {
        SourceValidity val1;
        val1 = m_delegate.getValidity();
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
            getLogger()
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
    		        getLogger()
                )
            );
    	}
        return result;
    }

    public String getName() {
        return m_delegate.getName();
    }

    public Source getParent() throws SourceException {
        return new RepositorySource(
            m_prefix,
            (ModifiableTraversableSource) m_delegate.getParent(),
        	m_descriptor, 
            getLogger()
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

}
