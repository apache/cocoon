/* Created on Oct 18, 2003 7:00:43 PM by unico */
package org.apache.cocoon.components.repository;

import java.io.IOException;

import org.apache.excalibur.source.SourceException;

/**
 * A stateless utitlity service intended to be use by flowscripts to help
 * them with persistent operations on sources.
 * 
 * <p>
 * Each operation return a status code that is based on RFC 2518 (WebDAV).
 * This does not mean to it cannot be used outside of a WebDAV context.
 * It is reusing a standard to enable richer communication between
 * the flow layer and the service layer.
 * </p>
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a> 
 */
public interface SourceRepository {
    
    public static final String ROLE = SourceRepository.class.getName();
    
    /**
     * Status OK (<code>200</code>).
     */
    public static final int STATUS_OK = 200;
    
    /**
     * Status CREATED (<code>201</code>).
     */
    public static final int STATUS_CREATED = 201;

    /**
     * Status NOT_FOUND (<code>404</code>).
     */
    public static final int STATUS_NOT_FOUND = 404;

    /**
     * Status NOT_ALLOWED (<code>495</code>).
     */
    public static final int STATUS_NOT_ALLOWED = 405;
    
    /**
     * Status CONFLICT (<code>409</code>).
     */
    public static final int STATUS_CONFLICT = 409;
    
    
    /**
     * Saves a Source by either creating a new one or overwriting the previous one.
     * 
     * @param in  the Source location to read from.
     * @param out  the Source location to write to.
     * @return  a status code describing the exit status.
     * @throws IOException
     */
    public abstract int save(String in, String out) throws IOException;
    
    /**
     * Create a Source collection.
     * 
     * @param location  the location of the source collection to create.
     * @return  a status code describing the exit status.
     * @throws IOException
     */
    public abstract int makeCollection(String location) throws IOException;
    
    /**
     * Deletes a Source and all of its descendants.
     * 
     * @param location  the location of the source to delete.
     * @return  a status code describing the exit status.
     * @throws IOException
     */
    public abstract int remove(String location) throws SourceException, IOException;
    
    /**
     * Move a Source from one location to the other.
     * 
     * @param from  the source location.
     * @param to    the destination location.
     * @return  a status code describing the exit status.
     * @throws IOException
     */
    public abstract int move(String from, String to) throws IOException;
    
    /**
     * Copy a Souce from one location to the other.
     * 
     * @param from  the source location.
     * @param to    the destination location.
     * @return  a status code describing the exit status.
     * @throws IOException
     */
    public abstract int copy(String from, String to) throws IOException;
    
}