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
package org.apache.cocoon.components.webdav;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpURL;
import org.apache.webdav.lib.Property;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.ResponseEntity;
import org.apache.webdav.lib.WebdavResource;

/**
 * A utility for WebDAV.
 */
public class WebDAVUtil {
    
    static private String staticURI;
    static private WebdavResource staticResource;
    
    /**
     * instantiate a WebdavResource object from a given uri 
     * 
     * @param uri  the uri of the resource.
     * @throws HttpException
     * @throws IOException
     */
    static synchronized public WebdavResource getWebdavResource(String uri)
    throws HttpException, IOException {

        if (uri == null) return null;
        if (uri.equals(staticURI)) {
            staticResource.discoverOwnLocks();
            return staticResource;            
        } 
        HttpURL sourceURL = new HttpURL(uri);
        staticURI = uri;
        staticResource = new WebdavResource(sourceURL);
        staticResource.discoverOwnLocks();
        return staticResource;
    }

    /**
     * create a new resource on the server 
     * 
     * @param uri  the uri of the resource.
     * @param content  the content to initialize the resource with.
     * @throws HttpException
     * @throws IOException
     */
    static public void createResource(final String uri, final String content)
    throws HttpException, IOException {

        final String filename = uri.substring(uri.lastIndexOf("/"));
        final String uriPrefix = uri.substring(0, uri.lastIndexOf("/") + 1);
        final HttpURL sourceURL = new HttpURL(uri);                                                                   
        final WebdavResource resource = getWebdavResource(uriPrefix);
                        
        if(!resource.putMethod(uriPrefix + filename, content)) {
            throw new HttpException("Error creating resource: " + uri
                                    + " Status: " + resource.getStatusCode()
                                    + " Message: " + resource.getStatusMessage());
        }
    }

    /**
     * copy a WebDAV resource 
     * 
     * @param from  the URI of the resource to copy
     * @param to  the URI of the destination
     * @param overwrite  if true overwrites the destination
     * @param recurse  if true recursively creates parent collections if not existant
     * @throws HttpException
     * @throws IOException
     */
    static public void copyResource(String from, String to, boolean recurse, boolean overwrite)
    throws HttpException, IOException {

        String relativeDestination = (to.substring(to.indexOf("://") + 3));
        relativeDestination = relativeDestination.substring(relativeDestination.indexOf("/"));

        // make parentCollection of target if not existant
        if (recurse) WebDAVUtil.makePath(to.substring(0, to.lastIndexOf("/")));

        // copy the resource
        WebdavResource resource = WebDAVUtil.getWebdavResource(from);
        resource.setOverwrite(overwrite);
        if (!resource.copyMethod(relativeDestination)) {
            throw new HttpException("Error copying resource: " + from
                                    + " Status: " + resource.getStatusCode()
                                    + " Message: " + resource.getStatusMessage());
        }
    }

    /**
     * move a WebDAV resource 
     * 
     * @param from  the URI of the resource to move
     * @param to  the URI of the destination
     * @param overwrite  if true overwrites the destination
     * @param recurse  if true recursively creates parent collections if not existant
     * @throws HttpException
     * @throws IOException
     */
    static public void moveResource(String from, String to, boolean recurse, boolean overwrite)
    throws HttpException, IOException {

        String relativeDestination = (to.substring(to.indexOf("://") + 3));
        relativeDestination = relativeDestination.substring(relativeDestination.indexOf("/"));

        // make parentCollection if not existant
        if (recurse) WebDAVUtil.makePath(to.substring(0, to.lastIndexOf("/")));

        // move the resource
        WebdavResource resource = WebDAVUtil.getWebdavResource(from);
        resource.setOverwrite(overwrite);
        if (!resource.moveMethod(relativeDestination)) {
            throw new HttpException("Error moving resource: " + from
                                    + " Status: " + resource.getStatusCode()
                                    + " Message: " + resource.getStatusMessage());
        }
    }

    /**
     * make the complete path of a given collection URI (including all parent collections) 
     * 
     * @param path  the URI of the collection to make
     * @throws HttpException
     * @throws IOException
     */
    static public void makePath(String path)
    throws HttpException, IOException {
        String parentPath = path;
        while (true) {

            try {
                HttpURL sourceURL = new HttpURL(parentPath+"/");
                new WebdavResource(sourceURL);

                // if code reaches here, pathUrl exists
                break;
            } catch (HttpException he) {
                parentPath = parentPath.substring(0, parentPath.lastIndexOf("/"));
            }
        }

        // the complete path to make
        if(parentPath.length() < path.length()) {
            String pathToMake = path.substring(parentPath.length()+1)+"/";
            String colToMake = null;
            while (pathToMake.indexOf("/") != -1) {
                colToMake = pathToMake.substring(0, pathToMake.indexOf("/"));
                WebDAVUtil.makeCollection(path.substring(0, path.lastIndexOf(colToMake)), colToMake);
                pathToMake = pathToMake.substring(pathToMake.indexOf("/")+1);
            }
        }
    }

    /**
     * create a collection 
     * 
     * @param parent  the uri of the parent collection
     * @param collection  the name of the collection to make
     * @throws HttpException
     * @throws IOException
     */
    static public void makeCollection(String parent, String collection)
    throws HttpException, IOException {

        WebdavResource parentResource = WebDAVUtil.getWebdavResource(parent);
        parentResource.mkcolMethod(parent + collection + "/");
    }

    /**
     * get a property 
     * 
     * @param uri  the URI to get the property from
     * @param name  the name of the property
     * @param namespace  the namespace of the property
     * @throws HttpException
     * @throws IOException
     */
    static public SourceProperty getProperty(String uri, String name, String namespace)
    throws HttpException, IOException {

        Vector propNames = new Vector(1);
        propNames.add(new PropertyName(namespace,name));
        Enumeration props= null;
        Property prop = null;
        WebdavResource resource = WebDAVUtil.getWebdavResource(uri);
        Enumeration responses = resource.propfindMethod(0, propNames);
        while (responses.hasMoreElements()) {
            ResponseEntity response = (ResponseEntity)responses.nextElement();
            props = response.getProperties();
            if (props.hasMoreElements()) {
                prop = (Property) props.nextElement();
                return new SourceProperty(prop.getElement());
            }
        }
        return null;
    }

    /**
     * get multiple properties 
     * 
     * @param uri  the URI to get the properties from
     * @param propNames  the Set containing the properties to set
     * @throws HttpException
     * @throws IOException
     */
    static public Map getProperties(String uri, Set propNames)
    throws HttpException, IOException {

        List sourceproperties = new ArrayList();
        Enumeration responses = null;
        Enumeration props = null;
        Property prop = null;
        Map propertiesMap = new HashMap();
        WebdavResource resource = WebDAVUtil.getWebdavResource(uri);
        responses = resource.propfindMethod(0, new Vector(propNames));
        while (responses.hasMoreElements()) {
            ResponseEntity response = (ResponseEntity)responses.nextElement();
            props = response.getProperties();
            while (props.hasMoreElements()) {
                prop = (Property) props.nextElement();
                SourceProperty srcProperty = new SourceProperty(prop.getElement());
                sourceproperties.add(srcProperty);
            }
        }

        for (int i = 0; i<sourceproperties.size(); i++) {
            propertiesMap.put(((SourceProperty) sourceproperties.get(i)).getNamespace()
                              + ":" + ((SourceProperty) sourceproperties.get(i)).getName(),
                              sourceproperties.get(i));
        }
        return propertiesMap;
    }

    /**
     * get all properties for given uri 
     * 
     * @param uri  the URI to get the properties from
     * @throws HttpException
     * @throws IOException
     */
    static public List getAllProperties(String uri)
    throws HttpException, IOException {

        List sourceproperties = new ArrayList();
        WebdavResource resource = WebDAVUtil.getWebdavResource(uri);
        Enumeration responses = resource.propfindMethod(0);
        Enumeration props = null;
        Property prop = null;
        while (responses.hasMoreElements()) {
            ResponseEntity response = (ResponseEntity)responses.nextElement();
            props = response.getProperties();
            while (props.hasMoreElements()) {
                prop = (Property) props.nextElement();
                SourceProperty srcProperty = new SourceProperty(prop.getElement());
                sourceproperties.add(srcProperty);
            }
        }
        return sourceproperties;
    }

    /**
     * set a property 
     * 
     * @param uri  the URI  of the resource to set the property on
     * @param name  the name of the property
     * @param namespace  the namespace of the property
     * @param value  the new value of the property
     * @throws HttpException
     * @throws IOException
     */
    static public void setProperty(String uri, String name, String namespace, String value)
    throws HttpException, IOException {

        WebdavResource resource = WebDAVUtil.getWebdavResource(uri);
        if(!resource.proppatchMethod(new PropertyName(namespace, name), value, true)) {
            throw new HttpException("Error setting property " + namespace + ":" + name + " on resource: " + uri
                                    + " Status: " + resource.getStatusCode()
                                    + " Message: " + resource.getStatusMessage());
        }
    }

    /**
     * set multiple property 
     * 
     * @param uri  the URI  of the resource to set the property on
     * @param properties  the Map containing the property values to set
     * @throws HttpException
     * @throws IOException
     */
    static public void setProperties(String uri, Map properties)
    throws HttpException, IOException {

        WebdavResource resource = WebDAVUtil.getWebdavResource(uri);
        if (!resource.proppatchMethod(new Hashtable(properties), true)) {
            throw new HttpException("Error setting properties on resource: " + uri
                                    + " Status: " + resource.getStatusCode()
                                    + " Message: " + resource.getStatusMessage());
        }
    }

}