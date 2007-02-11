/* Created on Oct 18, 2003 7:00:43 PM by unico */
package org.apache.cocoon.components.repository;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;

/**
 * NOP implementation of RepositoryInterceptor.
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a> 
 */
public abstract class RepositoryInterceptorBase implements RepositoryInterceptor {

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.RepositoryInterceptor#postRemoveSource(org.apache.excalibur.source.Source)
     */
    public void postRemoveSource(Source source) throws SourceException {
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.RepositoryInterceptor#postStoreSource(org.apache.excalibur.source.Source)
     */
    public void postStoreSource(Source source) throws SourceException {
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.RepositoryInterceptor#preRemoveSource(org.apache.excalibur.source.Source)
     */
    public void preRemoveSource(Source source) throws SourceException {
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.RepositoryInterceptor#preStoreSource(org.apache.excalibur.source.Source)
     */
    public void preStoreSource(Source source) throws SourceException {
    }

}
