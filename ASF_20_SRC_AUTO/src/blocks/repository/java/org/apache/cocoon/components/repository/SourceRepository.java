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