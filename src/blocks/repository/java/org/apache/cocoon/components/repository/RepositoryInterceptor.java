/* Created on Oct 18, 2003 7:00:43 PM by unico */
package org.apache.cocoon.components.repository;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;

/**
 * TODO describe class
 * 
 * Instances must be thread safe.
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a> 
 */
public interface RepositoryInterceptor {
    
    public static final String ROLE = RepositoryInterceptor.class.getName();
    
    /** called before a source is removed */
    public abstract void preRemoveSource(Source source) throws SourceException;
    
    /** called before a source was successfully removed */
    public abstract void postRemoveSource(Source source) throws SourceException;
    
    /** called before a source is stored */
    public abstract void preStoreSource(Source source) throws SourceException;
    
    /** called after a source was successfully stored */
    public abstract void postStoreSource(Source source) throws SourceException;
    
}
