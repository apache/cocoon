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

import org.apache.excalibur.source.SourceException;

/**
 * A stateless utility service intended to be used by flowscripts to help
 * them with persistent operations on sources.
 * 
 * <p>
 * Each operation returns a status code that is based on RFC 2518 (WebDAV).
 * This does not mean to it cannot be used outside of a WebDAV context.
 * It is reusing a standard to enable rich communication between
 * the flow layer and the service layer.
 * </p>
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a> 
 */
public interface SourceRepository {
    
    public static final String ROLE = SourceRepository.class.getName();
    
    /**
     * Status OK (<b>200</b>).
     */
    public static final int STATUS_OK = 200;
    
    /**
     * Status CREATED (<b>201</b>).
     */
    public static final int STATUS_CREATED = 201;
    
    /**
     * Status NO_CONTENT (<b>204</b>).
     */
    public static final int STATUS_NO_CONTENT = 204;
    
    /**
     * Status FORBIDDEN (<b>403</b>).
     */
    public static final int STATUS_FORBIDDEN = 403;
    
    /**
     * Status NOT_FOUND (<b>404</b>).
     */
    public static final int STATUS_NOT_FOUND = 404;

    /**
     * Status NOT_ALLOWED (<b>405</b>).
     */
    public static final int STATUS_NOT_ALLOWED = 405;
    
    /**
     * Status CONFLICT (<b>409</b>).
     */
    public static final int STATUS_CONFLICT = 409;
    
    /**
     * Status PRECONDITION_FAILED (<b>412</b>)
     */
    public static final int STATUS_PRECONDITION_FAILED = 412;
    
    
    /**
     * Saves a Source by either creating a new one or overwriting the previous one.
     * 
     * @param in  the Source location to read from.
     * @param out  the Source location to write to.
     * @return  a status code describing the exit status.
     * @throws IOException
     * @throws SourceException
     */
    public abstract int save(String in, String out) throws IOException, SourceException;
    
    /**
     * Create a Source collection.
     * 
     * @param location  the location of the source collection to create.
     * @return  a status code describing the exit status.
     * @throws IOException
     * @throws SourceException
     */
    public abstract int makeCollection(String location) throws IOException, SourceException;
    
    /**
     * Deletes a Source and all of its descendants.
     * 
     * @param location  the location of the source to delete.
     * @return  a status code describing the exit status.
     * @throws IOException
     * @throws SourceException
     */
    public abstract int remove(String location) throws IOException, SourceException;
    
    /**
     * Move a Source from one location to the other.
     * 
     * @param from       the source location.
     * @param to         the destination location.
     * @param recurse    whether to move all the source descendents also.
     * @param overwrite  whether to overwrite the destination source if it exists.
     * @return  a status code describing the exit status.
     * @throws IOException
     * @throws SourceException
     */
    public abstract int move(String from, String to, boolean recurse, boolean overwrite) 
        throws IOException, SourceException;
    
    /**
     * Copy a Souce from one location to the other.
     * 
     * @param from       the source location.
     * @param to         the destination location.
     * @param recurse    whether to move all the source descendents also.
     * @param overwrite  whether to overwrite the destination source if it exists.
     * @return  a status code describing the exit status.
     * @throws IOException
     * @throws SourceException
     */
    public abstract int copy(String from, String to, boolean recurse, boolean overwrite) 
        throws IOException, SourceException;
    
}