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

import java.io.InputStream;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.repository.helpers.CredentialsToken;
import org.apache.cocoon.components.repository.helpers.RepositoryTransactionHelper;
import org.apache.cocoon.components.repository.helpers.RepositoryPropertyHelper;
import org.apache.cocoon.components.repository.helpers.RepositoryVersioningHelper;
import org.apache.excalibur.source.Source;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * A repository interface intended to be used by flowscripts or corresponding wrapper components.
 */
public interface Repository {
    
    /**
     * get content as String
     * 
     * @param uri  the uri of the resource.
     * @return  the content as a String.
     * @throws ProcessingException
     */
    String getContentString(String uri) throws ProcessingException;

    /**
     * get content as Stream
     * 
     * @param uri  the uri of the resource.
     * @return  the content as a InputStream.
     * @throws ProcessingException
     */
   InputStream getContentStream(String uri) throws ProcessingException;

    /**
     * get content as DOM
     * 
     * @param uri  the uri of the resource.
     * @return  the content as a W3C Document object.
     * @throws ProcessingException
     */
    Document getContentDOM(String uri) throws ProcessingException;

    /**
     * save content
     * 
     * @param uri  the uri of the resource.
     * @param content  the to be saved content given as a String.
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean saveContent(String uri, String content) throws ProcessingException;

    /**
     * save content
     * 
     * @param uri  the uri of the resource.
     * @param node  the to be saved content given as a W3C Node object.
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean saveContent(String uri, Node node) throws ProcessingException;

    /**
     * save content
     * 
     * @param uri  the uri of the resource.
     * @param source  the to be saved content given as a Excalibur Source object.
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean saveContent(String uri, Source source) throws ProcessingException;

    /**
     * create a new resource
     * 
     * @param uri  the uri of the resource.
     * @param content  the content to initialize the resource with.
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean createResource(String uri, String content) throws ProcessingException;

    /**
     * copy a resource
     * 
     * @param uri  the uri of the resource.
     * @param dest  the destination of the copy.
     * @param recurse  if true recursively creates parent collections if not existant
     * @param overwrite  whether to overwrite the destination if it exists.
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean copy(String uri, String dest, boolean recurse, boolean overwrite) throws ProcessingException;

    /**
     * move a resource
     * 
     * @param uri  the uri of the resource.
     * @param dest  the destination of the move.
     * @param recurse  if true recursively creates parent collections if not existant
     * @param overwrite  whether to overwrite the destination if it exists.
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean move(String uri, String dest, boolean recurse, boolean overwrite) throws ProcessingException;

    /**
     * remove resource
     * 
     * @param uri  the uri of the resource.
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean remove(String uri) throws ProcessingException;

    /**
     * checks wether resource exists
     * 
     * @param uri  the uri of the document.
     * @return  a boolean indicating existance of the resource.
     * @throws ProcessingException
     */
    public boolean exists(String uri) throws ProcessingException;

    /**
     * make collection
     * 
     * @param uri  the uri of the collection.
     * @param recursive  a boolean indicating wether
     *        the operation should fail if the parent
     *        collection does not exist or wether the
     *        complete path should be created. 
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean makeCollection(String uri, boolean recursive) throws ProcessingException;

    /**
     * get a property helper
     * 
     * @return  the property helper.
     *          Returns null if the Repository does not support properties.
     */
    RepositoryPropertyHelper getPropertyHelper();

    /**
     * get a transaction helper
     * 
     * @return  a transaction helper.
     *          Returns null if the Repository does neither support transactions nor locks.
     */
    RepositoryTransactionHelper getTransactionHelper();

    /**
     * get a versioning helper
     * 
     * @return  a versioning helper.
     *          Returns null if the Repository does not support versioning.
     */
    RepositoryVersioningHelper getVersioningHelper();

    /**
     * get the credentials used against the repository
     * 
     * @return  the credentials in use.
     */
    CredentialsToken getCredentials();

    /**
     * set the credentials to be used against the repository
     * 
     * @param credentials  the credentials to use.
     */
    void setCredentials(CredentialsToken credentials);

}