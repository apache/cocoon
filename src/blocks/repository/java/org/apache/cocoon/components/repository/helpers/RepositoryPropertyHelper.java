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
package org.apache.cocoon.components.repository.helpers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.w3c.dom.Node;

/**
 * A property helper interface intended to be used
 * by flowscripts or corresponding wrapper components.
 */
public interface RepositoryPropertyHelper {
    
    /**
     * get a single property
     * 
     * @param uri  the uri of the resource.
     * @param name  the name of the property.
     * @param namespace  the namespace of the property.
     * @return  the property.
     * @throws ProcessingException
     */
    SourceProperty getProperty(String uri, String name, String namespace) throws ProcessingException;

    /**
     * get multiple properties
     * 
     * @param uri  the uri of the resource.
     * @param propNames  a Set containing the property names.
     * @return  a Map containing the property values.
     * @throws ProcessingException
     */
    Map getProperties(String uri, Set propNames) throws ProcessingException;

    /**
     * get all properties
     * 
     * @param uri  the uri of the resource.
     * @return  a List containing the property values.
     * @throws ProcessingException
     */
    List getAllProperties(String uri) throws ProcessingException;

    /**
     * set a single property to a String value
     * 
     * @param uri  the uri of the resource.
     * @param name  the name of the property.
     * @param namespace  the namespace of the property.
     * @param value  the String value to set the property to.
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean setProperty(String uri, String name, String namespace, String value) throws ProcessingException;

    /**
     * set a single property to a W3C Node value
     * 
     * @param uri  the uri of the resource.
     * @param name  the name of the property.
     * @param namespace  the namespace of the property.
     * @param value  the DOM value to set the property to.
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean setProperty(String uri, String name, String namespace, Node value) throws ProcessingException;

    /**
     * set multiple properties
     * 
     * @param uri  the uri of the resource.
     * @param poperties  a Map containing the properties to set.
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean setProperties(String uri, Map properties) throws ProcessingException;

}