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
package org.apache.cocoon.components.repository;

import java.io.IOException;
import java.util.Iterator;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.ModifiableTraversableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.TraversableSource;

/**
 * SourceRepository implementation.
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * 
 * @avalon.component
 * @avalon.service type="SourceRepository"
 * @x-avalon.lifestyle type="singleton"
 * @x-avalon.info name="source-repository"
 */
public class SourceRepositoryImpl extends AbstractLogEnabled 
implements Serviceable, ThreadSafe, SourceRepository {
    
    private SourceResolver m_resolver;
    
    
    // ---------------------------------------------------- lifecycle
    
    public SourceRepositoryImpl() {
    }
    
    /**
     * @avalon.dependency type="SourceResolver"
     */
    public void service(ServiceManager manager) throws ServiceException {
        m_resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }
    
    
    // ---------------------------------------------------- repository operations
    
    public int save(String in, String out) throws IOException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("save: " + in + "/" + out);
        }
        
        Source source = null;
        Source destination = null;
        try {
            destination = m_resolver.resolveURI(out);
            
            if (!(destination instanceof ModifiableSource)) {
                final String message = 
                    "Conflict during save(): protocol is not modifiable.";
                getLogger().warn(message);
                return STATUS_CONFLICT;
            }
            
            final boolean exists = destination.exists();
            if (!exists) {
                Source parent = ((TraversableSource) destination).getParent();
                if (!parent.exists()) {
                    final String message =
                        "Conflict during save(): parent does not exist.";
                    getLogger().warn(message);
                    return STATUS_CONFLICT;
                }
            }
            
            if (destination instanceof TraversableSource) {
                if (((TraversableSource) destination).isCollection()) {
				    final String message = 
                        "Conflict during save(): destination is a collection.";
                    getLogger().warn(message);
                    return STATUS_CONFLICT;
            	}
            }
            
            int status;
            if (exists) {
                status = STATUS_OK;
            }
            else {
                status = STATUS_CREATED;
            }
            
            source = m_resolver.resolveURI(in);
            SourceUtil.copy(source,destination);
            
            return status;
        }
        catch (IOException e) {
            getLogger().error("Unexpected exception during save().",e);
            throw e;
        }
        finally {
            if (source != null) {
                m_resolver.release(source);
            }
            if (destination != null) {
                m_resolver.release(destination);
            }
        }
        
    }
    
    public int makeCollection(String location) throws IOException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("makeCollection: " + location);
        }
        
        Source source = null;
        Source parent = null;
        try {
            source = m_resolver.resolveURI(location);
            
            if (source.exists()) {
                final String message = 
                    "makeCollection() is not allowed: the resource already exists.";
                getLogger().warn(message);
                return STATUS_NOT_ALLOWED;
            }
            
            if (!(source instanceof ModifiableTraversableSource)) {
            	final String message = 
                    "Conflict in makeCollection(): source is not modifiable traversable.";
                getLogger().warn(message);
                return STATUS_CONFLICT;
            }
            
            parent = ((TraversableSource) source).getParent();
            if (!parent.exists()) {
                final String message = "Conflict in makeCollection(): parent does not exist.";
                getLogger().warn(message);
                return STATUS_CONFLICT;
            }
            
            ((ModifiableTraversableSource) source).makeCollection();
            return STATUS_CREATED;
        }
        catch (IOException e) {
            getLogger().error("Unexpected exception during makeCollection().",e);
            throw e;
        }
        finally {
            if (source != null) {
                m_resolver.release(source);
            }
            if (parent != null) {
                m_resolver.release(parent);
            }
        }
    }
    
    /**
     * Removes a Source and all of its descendants.
     * 
     * @param location  the location of the source to remove.
     * @return  a http status code describing the exit status.
     * @throws IOException
     */
    public int remove(String location) throws IOException {
        
        Source source = null;
        try {
            source = m_resolver.resolveURI(location);
            if (!source.exists()) {
                final String message = 
                    "Trying to remove a non-existing source.";
                getLogger().warn(message);
                return STATUS_NOT_FOUND;
            }
            return remove(source);
        }
        catch (IOException e) {
            getLogger().error("Unexpected exception during remove().",e);
            throw e;
        }
        finally {
            if (source != null) {
                m_resolver.release(source);
            }
        }
        
    }
    
    private int remove(Source source) throws SourceException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("remove: " + source.getURI());
        }
        
        if (!(source instanceof ModifiableSource)) {
            final String message = 
                "Conflict in remove(): source is not modifiable";
            getLogger().warn(message);
            return STATUS_CONFLICT;         
        }
        
        if (source instanceof TraversableSource) {
            if (((TraversableSource) source).isCollection()) {
                int status = STATUS_OK;
                final Iterator iter = ((TraversableSource) source).getChildren().iterator();
                while (iter.hasNext()) {
                    Source child = null;
                    try {
                        status = remove((Source) iter.next());
                        if (status != STATUS_OK) {
                            return status;
                        }
                    }
                    finally {
                        if (child != null) {
                            m_resolver.release(child);
                        }
                    }
                }
            }
        }
        ((ModifiableSource) source).delete();
        return STATUS_OK;
    }
    
    public int move(String from, String to) throws IOException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("move: " + from + " -> " + to);
        }
        
        if (from != null && from.equals(to)) {
            final String message = 
                "move() is not allowed: " +
                "The source and destination URIs are the same";
            getLogger().warn(message);
            return STATUS_NOT_ALLOWED;
        }
        Source source = null;
        Source destination = null;
        try {
            source = m_resolver.resolveURI(from);
            destination = m_resolver.resolveURI(to);
            return move(source, destination);
        }
        catch (IOException e) {
            getLogger().error("Unexpected exception during move().",e);
            throw e;
        }
        finally {
            if (source != null) {
                m_resolver.release(source);
            }
            if (destination != null) {
                m_resolver.release(destination);
            }
        }
    }
    
    private int move(Source source, Source destination) throws IOException {
        if (!source.exists()) {
            final String message =
                "Trying to move a non-existing source.";
            getLogger().warn(message);
            return STATUS_NOT_FOUND;
        }
        if (destination.exists()) {
            remove((ModifiableSource) destination);
        }
        SourceUtil.move(source, destination);
        return STATUS_CREATED;
    }
    
    public int copy(String from, String to) throws IOException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("copy: " + from + " -> " + to);
        }
        
        if (from != null && from.equals(to)) {
            final String message = 
                "copy() is not allowed: " +
                "The source and destination URIs are the same";
            getLogger().warn(message);
            return STATUS_NOT_ALLOWED;
        }
        Source source = null;
        Source destination = null;
        try {
            source = m_resolver.resolveURI(from);
            destination = m_resolver.resolveURI(to);
            return copy(source, destination);
        }
        catch (IOException e) {
            getLogger().error("Unexpected exception during copy().",e);
            throw e;
        }
        finally {
            if (source != null) {
                m_resolver.release(source);
            }
            if (destination != null) {
                m_resolver.release(destination);
            }
        }
    }
    
    private int copy(Source source, Source destination) throws IOException {
        if (!source.exists()) {
            final String message =
                "Trying to copy a non-existing source";
            getLogger().warn(message);
            return STATUS_NOT_FOUND;
        }
        if (destination.exists()) {
            remove((ModifiableSource) destination);
        }
        SourceUtil.copy(source, destination);
        return STATUS_CREATED;
    }    
    
}
